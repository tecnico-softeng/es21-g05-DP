package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    User teacher
    Question question

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        createExternalCourseAndExecution()

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        externalCourseExecution.addUser(teacher)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)
    }

    def "export an Open Ended Question"(){

        given: "an Open Ended Question"
        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)

        and: "the question is in the repository"
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the web service is invoked"
        def map = restClient.get(
                path: '/questions/courses/' + externalCourse.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null
    }

    def "export a MultipleChoiceQuestion"(){

        given: "a single type MultipleChoiceQuestion"
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

        and: "the question is in the repository"

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the web service is invoked"
        def map = restClient.get(
                path: '/questions/courses/' + externalCourse.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null
    }

    def "export an Item Combination Question"(){

        given: "an Item Combination Question"
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)

        and: "the question is in the repository"
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the web service is invoked"
        def map = restClient.get(
                path: '/questions/courses/' + externalCourse.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null
    }

    def "teacher can only export questions in a course he has access"() {
        given: "a demo teacher"
        demoTeacherLogin()

        and: "an Open Ended Question"
        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)

        and: "the question is in the repository"
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the web service is invoked"
        def map = restClient.get(
                path: '/questions/courses/' + externalCourse.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_FORBIDDEN
    }

    @Unroll
    def "demo #demoRole is not allowed to export questions"() {
        given: "a demo role"
        loginAsADemo(demoRole)

        and: "an Item Combination Question"
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)

        and: "the question is in the repository"
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the web service is invoked"
        def map = restClient.get(
                path: '/questions/courses/' + externalCourse.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_FORBIDDEN

        where:
        demoRole          | _
        ROLE_DEMO_ADMIN   | _
        ROLE_DEMO_STUDENT | _
    }

    def cleanup() {
        optionRepository.deleteAll()
        userRepository.deleteAll()
        questionRepository.deleteAll()
        courseRepository.deleteAll()
    }
}
