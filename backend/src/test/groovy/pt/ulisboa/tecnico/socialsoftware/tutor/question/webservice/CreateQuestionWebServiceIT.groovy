package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import spock.lang.Unroll
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DemoUtils

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response
    def courseId

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)
    }

    @Unroll
    def "demo teacher creates every multiple choice question: question=#question || questionTitle=#questionTitle | questionContent=#questionContent | numberOfOptions=#numberOfOptions || answerType=#answerType"() {
        given: 'a demo login'
        demoTeacherLogin()

        and: "the courseId that the demo teacher has access"
        courseId = courseRepository.findByNameType(DemoUtils.COURSE_NAME, Course.Type.TECNICO.name()).get().getId()

        when: 'the create question web service is invoked'
        response = restClient.post(
                path: '/questions/courses/' + courseId,
                body: JsonOutput.toJson(question),
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        and: "the response has the correct question"
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

        cleanup:
        optionRepository.deleteAll()
        questionRepository.deleteAll()

        where:
        question                                     || questionTitle    | questionContent    | numberOfOptions | answerType
        createSingleAnswerMultipleChoiceQuestion()   || QUESTION_1_TITLE | QUESTION_1_CONTENT | 2               | MultipleChoiceQuestion.AnswerType.SINGLE.name()
        createMultipleAnswerMultipleChoiceQuestion() || QUESTION_2_TITLE | QUESTION_2_CONTENT | 3               | MultipleChoiceQuestion.AnswerType.MULTIPLE.name()
        createOrderedAnswerMultipleChoiceQuestion()  || QUESTION_3_TITLE | QUESTION_3_CONTENT | 3               | MultipleChoiceQuestion.AnswerType.ORDER.name()
    }

    def "create an Open Ended Question"(){
        given: "a demo teacher"
        demoTeacherLogin()

        and: "the courseId that the demo teacher has access"
        courseId = courseRepository.findByNameType(DemoUtils.COURSE_NAME, Course.Type.TECNICO.name()).get().getId()

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setKey(1)


        and: "an OpenEndedQuestionDto"
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto()
        openEndedQuestion.setCriteria(QUESTION_2_CONTENT)
        openEndedQuestion.setRegexQuestion(QUESTION_4_REGEX)
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: "the web service is invoked"
        response = restClient.post(
                path: '/questions/courses/' + courseId,
                body: json.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "if it responds with the correct question "
        def question = response.data
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        def resQuestionDetails = question.questionDetailsDto
        resQuestionDetails.criteria == questionDto.getQuestionDetailsDto().getCriteria()
        resQuestionDetails.regexQuestion == questionDto.getQuestionDetailsDto().getRegexQuestion()

        and: 'the question is in the database'
        def questionInRepo = questionRepository.findByKey(1).get()
        questionInRepo != null

        cleanup:
        questionRepository.delete(questionRepository.findById(questionInRepo.getId()).get())
    }

    def "create a item combination question as demo teacher"() {
        given: "a demo teacher"
        demoTeacherLogin()

        and: "the courseId that the demo teacher has access"
        courseId = courseRepository.findByNameType(DemoUtils.COURSE_NAME, Course.Type.TECNICO.name()).get().getId()

        and: "a questionDto"
        def questionDto = createItemCombinationQuestion()

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: "the web service is invoked"
        response = restClient.post(
                path: '/questions/courses/' + courseId,
                body: json.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

         then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "if it responds with the correct question "
        def question = response.data
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()

        question.questionDetailsDto.items[0].content == OPTION_1_CONTENT
        question.questionDetailsDto.items[0].connections[0].sequence == 4
        question.questionDetailsDto.items[0].group == "LEFT"

        question.questionDetailsDto.items[1].content == OPTION_2_CONTENT
        question.questionDetailsDto.items[1].connections[0].sequence == 3
        question.questionDetailsDto.items[1].group == "LEFT"

        question.questionDetailsDto.items[2].content == OPTION_1_CONTENT
        question.questionDetailsDto.items[2].group == "RIGHT"

        question.questionDetailsDto.items[3].content == OPTION_2_CONTENT
        question.questionDetailsDto.items[3].group == "RIGHT"
        
        and: 'the question is in the database'
        def questionInRepo = questionRepository.findByKey(1).get()
        questionInRepo != null

        cleanup:
        questionRepository.delete(questionRepository.findById(questionInRepo.getId()).get())
    }


    @Unroll
    def "demo #demoRole is not allowed to create questions"() {
        given: "a demo role"
        loginAsADemo(demoRole)

        and: "the courseId created by the demo login"
        courseId = courseRepository.findByNameType(DemoUtils.COURSE_NAME, Course.Type.TECNICO.name()).get().getId()

        and: 'a question'
        def questionDto = createSingleAnswerMultipleChoiceQuestion()

        when: 'the create question web service is invoked'
        response = restClient.post(
                path: '/questions/courses/' + courseId,
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: 'the question repository is empty'
        questionRepository.findAll().size() == 0

        where:
        demoRole          | _
        ROLE_DEMO_ADMIN   | _
        ROLE_DEMO_STUDENT | _
    }

    def "demo teacher can only create questions in a course he has access"() {
        given: "a demo teacher"
        demoTeacherLogin()

        and: "a course where the demo teacher does not have access"
        createExternalCourseAndExecution()

        and: "the courseId that the demo teacher has not access"
        courseId = courseRepository.findByNameType(COURSE_1_NAME, Course.Type.TECNICO.name()).get().getId()

        and: "a multiple choice question"
        def multipleChoiceQuestionDto = createSingleAnswerMultipleChoiceQuestion()

        when: 'the create question web service is invoked'
        response = restClient.post(
                path: '/questions/courses/' + courseId,
                body: JsonOutput.toJson(multipleChoiceQuestionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: 'the question repository is empty'
        questionRepository.findAll().size() == 0

        cleanup:
        courseRepository.deleteById(courseId)
    }
    def cleanup() {
        userRepository.deleteAll()
        courseRepository.deleteAll()
    }
}
