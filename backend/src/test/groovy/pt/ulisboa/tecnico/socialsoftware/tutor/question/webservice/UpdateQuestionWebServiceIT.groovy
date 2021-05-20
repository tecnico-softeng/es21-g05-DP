package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def teacher
    def response
    def question
    ObjectWriter ow = new ObjectMapper().writer()
    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)
        createExternalCourseAndExecution()

        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        externalCourseExecution.addUser(teacher)
    }

    def 'update an Open Ended question'() {
        given: 'a demo login'
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)
        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)
        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a new questionDto'
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto newOpenEndedQuestion = new OpenEndedQuestionDto()
        newOpenEndedQuestion.setCriteria(QUESTION_3_CONTENT)
        newOpenEndedQuestion.setRegexQuestion(QUESTION_5_CONTENT)
        newQuestionDto.setQuestionDetailsDto(newOpenEndedQuestion)

        when: 'the update question webservice is invoked'
        String json = ow.writeValueAsString(newQuestionDto)
        response = restClient.put(
                path: '/questions/'+ question.getId(),
                body: json,
                requestContentType: 'application/json'
        )

        then: 'check the response status'
        response != null
        response.status == HttpStatus.SC_OK
        and: 'if it responds with the updated question'
        def questionResp = response.data
        questionResp.id != null
        questionResp.title == newQuestionDto.getTitle()
        questionResp.content == newQuestionDto.getContent()
        def questionDetailsResp = questionResp.questionDetailsDto
        questionDetailsResp.criteria == newQuestionDto.getQuestionDetailsDto().getCriteria()
        questionDetailsResp.regexQuestion == newQuestionDto.getQuestionDetailsDto().getRegexQuestion()

    }

    def 'update question with no access to course'(){
        given: 'a new teacher without same course'
        def teacher2 = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher2.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        userRepository.save(teacher2)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)
        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a new questionDto'
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto newOpenEndedQuestion = new OpenEndedQuestionDto()
        newOpenEndedQuestion.setCriteria(QUESTION_3_CONTENT)
        newOpenEndedQuestion.setRegexQuestion(QUESTION_5_CONTENT)
        newQuestionDto.setQuestionDetailsDto(newOpenEndedQuestion)

        when: 'the update question webservice is invoked'
        String json = ow.writeValueAsString(newQuestionDto)
        response = restClient.put(
                path: '/questions/'+ question.getId(),
                body: json,
                requestContentType: 'application/json'
        )

        then: 'check the response status that it is forbidden'
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'if it the question was not updated'
        def  questionResult = questionRepository.findAll().get(0)
        questionResult.getId() == question.getId()
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        def resCriteria = (OpenEndedQuestion) questionResult.getQuestionDetails()
        resCriteria.getCriteria() == QUESTION_1_CONTENT
        def resRegex = (OpenEndedQuestion) questionResult.getQuestionDetails()
        resRegex.getRegexQuestion() == QUESTION_4_REGEX
    }

    @Unroll
    def 'demo teacher updates a created single type multipleChoiceQuestion to other types'() {

        given: 'a teacher login'
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: 'a single type multipleChoiceQuestion'
        def question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        def optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        def optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)

        when: 'the update question web service is invoked'
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: ow.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: 'the request returns 200'
        response.status == HttpStatus.SC_OK

        and: 'the question is created correctly'
        def questionDtoResult = response.data
        questionDtoResult.id != null
        questionDtoResult.status == Question.Status.AVAILABLE.name()
        questionDtoResult.title == questionTitle
        questionDtoResult.content == questionContent
        def resultQuestionDetails = questionDtoResult.questionDetailsDto
        resultQuestionDetails.options.size() == numberOfOptions
        resultQuestionDetails.answerType == answerType

        and: 'the question is in the database'
        questionRepository.findAll().size() == 1

        where:
        questionDto                                  || questionTitle    | questionContent    | numberOfOptions | answerType
        createMultipleAnswerMultipleChoiceQuestion() || QUESTION_2_TITLE | QUESTION_2_CONTENT | 3               | MultipleChoiceQuestion.AnswerType.MULTIPLE.name()
        createOrderedAnswerMultipleChoiceQuestion()  || QUESTION_3_TITLE | QUESTION_3_CONTENT | 3               | MultipleChoiceQuestion.AnswerType.ORDER.name()
    }

    @Unroll
    def 'demo #demoRole is not allowed to update questions'() {
        given: 'a multipleChoiceQuestion with a single correct answer'
        def question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        def optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        def optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)

        and: 'the new questionDto'
        def questionDto = createSingleAnswerMultipleChoiceQuestion()

        and:'demo role'
        loginAsADemo(demoRole)

        when: 'the update question web service is invoked'
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: ow.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        where:
        demoRole          | _
        ROLE_DEMO_ADMIN   | _
        ROLE_DEMO_STUDENT | _

    }

    def "update an item combination question as demo teacher"() {

        given: 'a teacher login'
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: 'an item combination question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)

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

        connectItems(item1L,item1R)
        connectItems(item2L,item2R)

        def questionDto = createItemCombinationQuestion()

        when: "the web service is invoked"
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: ow.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: 'the request returns 200'
        response.status == HttpStatus.SC_OK

        and: "question is updated"
        def result = response.data
        result.id != null
        result.title == questionDto.getTitle()
        result.content == questionDto.getContent()

        result.questionDetailsDto.items[0].content == OPTION_1_CONTENT
        result.questionDetailsDto.items[0].connections[0].sequence == 4
        result.questionDetailsDto.items[0].group == "LEFT"

        result.questionDetailsDto.items[1].content == OPTION_2_CONTENT
        result.questionDetailsDto.items[1].connections[0].sequence == 3
        result.questionDetailsDto.items[1].group == "LEFT"

        result.questionDetailsDto.items[2].content == OPTION_1_CONTENT
        result.questionDetailsDto.items[2].group == "RIGHT"

        result.questionDetailsDto.items[3].content == OPTION_2_CONTENT
        result.questionDetailsDto.items[3].group == "RIGHT"

        and: 'the question is in the database'
        questionRepository.findAll().size() == 1
    }


    def cleanup(){
        userRepository.deleteAll()
        questionRepository.deleteAll()
        courseRepository.deleteAll()
    }
}
