package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.webservice

import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportQuizWebServiceIT extends SpockTest{
    @LocalServerPort
    private int port

    def teacher
    def openEndedQuestion
    def quiz
    def openEndedQuestionDetails
    def user
    def question
    def questionDetails

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course and course execution'
        createExternalCourseAndExecution()
    }

    def "export an Open Ended Quiz"() {
        given: 'a teacher'
        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        externalCourseExecution.addUser(teacher)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)
        question.setTitle(QUESTION_1_TITLE)

        questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria("Criteria")
        questionDetails.setRegexQuestion("^[0-9]*\$")
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz answered by the user'
        quiz = new Quiz()

        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        def quizQuestion = new QuizQuestion()

        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(teacher)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new OpenEndedAnswer(questionAnswer, OPEN_ENDED_ANSWER_1);
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)
        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the export quiz web service is invoked"
        def map = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null
    }

    @Unroll
    def "valid arguments: orderA=#orderA | orderB=#orderB | orderC=#orderC | answerType=#answerType"() {
        given: 'a teacher'
        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        userRepository.save(teacher)
        externalCourseExecution.addUser(teacher)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)
        question.setTitle(QUESTION_1_TITLE)

        questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(answerType)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        def options = new ArrayList<Option>()
        def optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(orderA)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)
        options.add(optionA)

        def optionB = new Option()
        optionB.setContent(OPTION_2_CONTENT)
        optionB.setCorrect(false)
        optionB.setSequence(1)
        optionB.setOrder(orderB)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)
        options.add(optionB)

        def optionC = new Option()
        optionC.setContent(OPTION_3_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(2)
        optionC.setOrder(orderC)
        optionC.setQuestionDetails(questionDetails)
        optionRepository.save(optionC)
        options.add(optionC)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        def quizQuestion = new QuizQuestion()

        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(teacher)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options);
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the export quiz web service is invoked"
        def map = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null

        cleanup:
        optionRepository.deleteAll()

        where:
        orderA | orderB | orderC | answerType      
        -1     |  -1    | -1     | MultipleChoiceQuestion.AnswerType.MULTIPLE 
        1      |   0    |  0     | MultipleChoiceQuestion.AnswerType.ORDER
        1      |  -1    | -1     | MultipleChoiceQuestion.AnswerType.SINGLE
    }

    def "student can not export a quiz"() {
        given: 'a student'
        def student = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        externalCourseExecution.addUser(student)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)
        question.setTitle(QUESTION_1_TITLE)

        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'one option'
        def options = new ArrayList<Option>()
        def option = new Option()
        option.setContent(OPTION_1_CONTENT)
        option.setCorrect(true)
        option.setSequence(0)
        option.setOrder(-1)
        option.setQuestionDetails(questionDetails)
        optionRepository.save(option)
        options.add(option)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        def quizQuestion = new QuizQuestion()

        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(student)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options);
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the export quiz web service is invoked"
        def map = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "the request returns 403"
        assert response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        optionRepository.deleteAll()
    }

    def 'teacher exports a quiz - invalid access'() {
        given: 'a different course'
        def externalCourse2 = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(externalCourse2)
        def externalCourseExecution2 = new CourseExecution(externalCourse2, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TODAY)
        courseExecutionRepository.save(externalCourseExecution2)

        and: 'a teacher'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution2)
        userRepository.save(user)
        externalCourseExecution2.addUser(user)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a multiple choice question'
        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)
        question.setTitle(QUESTION_1_TITLE)

        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'one option'
        def options = new ArrayList<Option>()
        def option = new Option()
        option.setContent(OPTION_1_CONTENT)
        option.setCorrect(true)
        option.setSequence(0)
        option.setOrder(-1)
        option.setQuestionDetails(questionDetails)
        optionRepository.save(option)
        options.add(option)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        def quizQuestion = new QuizQuestion()

        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options);
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        and: "prepare request response"
        restClient.handler.failure = {resp, reader -> [response:resp, reader:reader]}
        restClient.handler.success = {resp, reader -> [response:resp, reader:reader]}

        when: "the export quiz web service is invoked"
        def map = restClient.get(
                path: '/quizzes/'+ quiz.getId() + '/export',
                requestContentType: 'application/zip'
        )

        def response = map['response']

        then: "the request returns 403"
        assert response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        optionRepository.deleteAll()
    }

    def cleanup() {
        userRepository.deleteAll()
        questionDetailsRepository.deleteAll()
        questionRepository.deleteAll()
        courseRepository.deleteAll()
        quizAnswerRepository.deleteAll()
        quizQuestionRepository.deleteAll()
        quizRepository.deleteAll()
    }
}
