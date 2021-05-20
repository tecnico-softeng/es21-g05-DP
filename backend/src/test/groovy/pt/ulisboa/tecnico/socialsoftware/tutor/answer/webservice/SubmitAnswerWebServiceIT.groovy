package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitAnswerWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response
    def questionDetails
    Quiz quiz
    Question question
    User user
    QuizAnswer quizAnswer
    QuizQuestion quizQuestion

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course and course execution'
        createExternalCourseAndExecution()
    }

    def 'student submits an answer for an open ended question'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)


        and: 'a quiz question'
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        def date = DateHandler.now()

        and: 'a quiz answer'
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(LOCAL_DATE_LATER)


        def statementAnswerDto = new StatementAnswerDto()
        def openEndedAnswerDto = new OpenEndedStatementAnswerDetailsDto()
        openEndedAnswerDto.setOpenAnswer(OPEN_ENDED_ANSWER_1)
        statementAnswerDto.setAnswerDetails(openEndedAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: 'the submit answer web service is invoked'
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: 'there is a not null return response'
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the answer submited is in the repository'
        def questionAnswerResult = questionAnswerItemRepository.findAll()
        questionAnswerResult.size() == 1
        def questionAnswer = questionAnswerResult.get(0)
        questionAnswer.getQuizId() == quiz.getId()
        questionAnswer.getTimeTaken() == statementAnswerDto.getTimeTaken()
        questionAnswer.getUsername() == user.getUsername()

        cleanup:
        questionAnswerItemRepository.deleteAll()
    }
    def "answer quiz with invalid access"() {
        given: 'a different course'

        def externalCourse2 = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(externalCourse2)
        def externalCourseExecution2 = new CourseExecution(externalCourse2, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TODAY)
        courseExecutionRepository.save(externalCourseExecution2)


        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution2)
        userRepository.save(user)
        externalCourseExecution2.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz question'
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        def date = DateHandler.now()

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(LOCAL_DATE_LATER)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def openEndedAnswerDto = new OpenEndedStatementAnswerDetailsDto()


        openEndedAnswerDto.setOpenAnswer(OPEN_ENDED_ANSWER_1)
        statementAnswerDto.setAnswerDetails(openEndedAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementQuizDto.getAnswers().add(statementAnswerDto)

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: 'the submit answer web service is invoked'
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

    }
    def 'student submits an answer a multiple choice question with multiple options'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz question'
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        and: 'two options'
        Option optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(-1)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        Option optionB = new Option()
        optionB.setContent(OPTION_1_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setOrder(-1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        def date = DateHandler.now()

        and: 'a quiz answer'
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(LOCAL_DATE_LATER)

        and: 'an answer with two options selected'
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        def options = new ArrayList<Integer>()
        options.add(optionA.getId())
        options.add(optionB.getId())

        multipleChoiceAnswerDto.setOptionsIds(options)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: 'the submit answer web service is invoked'
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: 'there is a not null return response'
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the answer submited is in the repository'
        def questionAnswerResult = questionAnswerItemRepository.findAll()
        questionAnswerResult.size() == 1
        def questionAnswer = questionAnswerResult.get(0)
        questionAnswer.getQuizId() == quiz.getId()
        questionAnswer.getTimeTaken() == statementAnswerDto.getTimeTaken()
        questionAnswer.getUsername() == user.getUsername()

        cleanup:
        optionRepository.delete(optionRepository.findById(optionA.getId()).get())
        optionRepository.delete(optionRepository.findById(optionB.getId()).get())
        questionAnswerItemRepository.deleteAll()
    }

    def 'student submits an answer a multiple choice question with multiple ordered options'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz question'
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        and: 'two ordered options and a false option'
        Option optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(1)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        Option optionB = new Option()
        optionB.setContent(OPTION_1_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setOrder(2)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        Option optionC = new Option()
        optionC.setContent(OPTION_1_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(1)
        optionC.setOrder(0)
        optionC.setQuestionDetails(questionDetails)
        optionRepository.save(optionC)

        def date = DateHandler.now()

        and: 'a quiz answer'
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(LOCAL_DATE_LATER)

        and: 'an answer with two options selected'
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        def options = new ArrayList<Integer>()
        options.add(optionA.getId())
        options.add(optionB.getId())

        multipleChoiceAnswerDto.setOptionsIds(options)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: 'the submit answer web service is invoked'
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: 'there is a not null return response'
        response != null
        response.status == HttpStatus.SC_OK

        and: 'the answer submited is in the repository'
        def questionAnswerResult = questionAnswerItemRepository.findAll()
        questionAnswerResult.size() == 1
        def questionAnswer = questionAnswerResult.get(0)
        questionAnswer.getQuizId() == quiz.getId()
        questionAnswer.getTimeTaken() == statementAnswerDto.getTimeTaken()
        questionAnswer.getUsername() == user.getUsername()

        cleanup:
        optionRepository.delete(optionRepository.findById(optionA.getId()).get())
        optionRepository.delete(optionRepository.findById(optionB.getId()).get())
        optionRepository.delete(optionRepository.findById(optionC.getId()).get())
        questionAnswerItemRepository.deleteAll()
    }

    def 'teacher can not submit an answer'() {
        given: 'a logged in teacher'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz question'
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        and: 'two options'
        Option optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(-1)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        Option optionB = new Option()
        optionB.setContent(OPTION_1_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setOrder(-1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        def date = DateHandler.now()

        and: 'a quiz answer'
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(LOCAL_DATE_LATER)

        and: 'an answer with two options selected'
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        def options = new ArrayList<Integer>()
        options.add(optionA.getId())
        options.add(optionB.getId())

        multipleChoiceAnswerDto.setOptionsIds(options)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())

        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();

        when: 'the submit answer web service is invoked'
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        optionRepository.delete(optionRepository.findById(optionA.getId()).get())
        optionRepository.delete(optionRepository.findById(optionB.getId()).get())
    }
    def cleanup() {
        quizAnswerRepository.delete(quizAnswerRepository.findById(quizAnswer.getId()).get())
        quizQuestionRepository.delete(quizQuestionRepository.findById(quizQuestion.getId()).get())
        quizRepository.delete(quizRepository.findById(quiz.getId()).get())
        questionDetailsRepository.delete(questionDetailsRepository.findById(questionDetails.getId()).get())
        userRepository.delete(userRepository.findById(user.getId()).get())
        courseRepository.delete(courseRepository.findById(externalCourse.getId()).get())
    }
}