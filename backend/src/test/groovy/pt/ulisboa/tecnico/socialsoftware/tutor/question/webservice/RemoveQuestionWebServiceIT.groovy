package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    User teacher
    def response
    Question question

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course'
        createExternalCourseAndExecution()

        and: 'a logged in teacher associated with the external course'
        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        externalCourseExecution.addUser(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: 'a question'
        question = new Question()
        question.setCourse(externalCourse)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
    }

    def "teacher removes a multiple choice question"() {
        given: 'a multiple choice question with multiple answers'
        def questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        def optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        def optionB = new Option()
        optionB.setContent(OPTION_1_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        def optionC = new Option()
        optionC.setContent(OPTION_1_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(2)
        optionC.setQuestionDetails(questionDetails)
        optionRepository.save(optionC)

        when: 'the remove question web service is invoked'
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the question is not in the database'
        questionRepository.findAll().size() == 0
        optionRepository.findAll().size() == 0
        questionDetailsRepository.findAll().size() == 0
    }

    def "teacher removes a item combination question"() {
        given: 'a item combination question'
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: '4 items'
        def item1L = new CombinationItem()
        item1L.setContent(OPTION_1_CONTENT)
        item1L.setSequence(1)
        item1L.setGroup(CombinationItem.Group.LEFT)
        item1L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1L)

        def item2L = new CombinationItem()
        item2L.setContent(OPTION_2_CONTENT)
        item2L.setSequence(2)
        item2L.setGroup(CombinationItem.Group.LEFT)
        item2L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2L)

        def item1R = new CombinationItem()
        item1R.setContent(OPTION_1_CONTENT)
        item1R.setSequence(3)
        item1R.setGroup(CombinationItem.Group.RIGHT)
        item1R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1R)

        def item2R = new CombinationItem()
        item2R.setContent(OPTION_2_CONTENT)
        item2R.setSequence(4)
        item2R.setGroup(CombinationItem.Group.RIGHT)
        item2R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2R)

        connectItems(item1L, item1R)
        connectItems(item2L, item2R)

        when: 'the remove question web service is invoked'
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the question is not in the database'
        questionRepository.count() == 0L
        combinationItemRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }

    @Unroll
    def "demo #demoRole is not allowed to remove questions"() {
        given: 'a multiple choice question with a single answer'
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'two options'
        def optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        def optionB = new Option()
        optionB.setContent(OPTION_2_CONTENT)
        optionB.setCorrect(false)
        optionB.setSequence(1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        and: "a demo role"
        loginAsADemo(demoRole)

        when: 'the remove question web service is invoked'
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: 'the question repository still stores the question'
        questionRepository.findById(question.getId()).get() != null

        cleanup:
        questionRepository.delete(question)
        questionDetailsRepository.delete(questionDetails)
        optionRepository.delete(optionA)
        optionRepository.delete(optionB)

        where:
        demoRole          | _
        ROLE_DEMO_ADMIN   | _
        ROLE_DEMO_STUDENT | _
    }

    
    def "demo teacher can only remove questions in a course he has access"() {
        given: 'a new teacher without same course'
        def teacher2 = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher2.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        userRepository.save(teacher2)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a item combination question'
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: '4 items'
        def item1L = new CombinationItem()
        item1L.setContent(OPTION_1_CONTENT)
        item1L.setSequence(1)
        item1L.setGroup(CombinationItem.Group.LEFT)
        item1L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1L)

        def item2L = new CombinationItem()
        item2L.setContent(OPTION_2_CONTENT)
        item2L.setSequence(2)
        item2L.setGroup(CombinationItem.Group.LEFT)
        item2L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2L)

        def item1R = new CombinationItem()
        item1R.setContent(OPTION_1_CONTENT)
        item1R.setSequence(3)
        item1R.setGroup(CombinationItem.Group.RIGHT)
        item1R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1R)

        def item2R = new CombinationItem()
        item2R.setContent(OPTION_2_CONTENT)
        item2R.setSequence(4)
        item2R.setGroup(CombinationItem.Group.RIGHT)
        item2R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2R)

        connectItems(item1L, item1R)
        connectItems(item2L, item2R)

        when: 'the remove question web service is invoked'
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: 'the question repository still stores the question'
        questionRepository.findById(question.getId()).get() != null

        cleanup:
        questionRepository.delete(question)
        questionDetailsRepository.delete(questionDetails)
        combinationItemRepository.delete(item1L)
        combinationItemRepository.delete(item2L)
        combinationItemRepository.delete(item1R)
        combinationItemRepository.delete(item2R)

    }

    def "teacher removes an open ended question"() {
        given: 'an open ended question'

        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)


        when: 'the remove question web service is invoked'
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the question is not in the database'
        questionRepository.findAll().size() == 0
        questionDetailsRepository.findAll().size() == 0
    }


    def cleanup(){
        userRepository.deleteAll()
        courseRepository.deleteAll()
    }
}
