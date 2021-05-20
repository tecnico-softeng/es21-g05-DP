package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice


import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswerItem;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetQuizAnswersWebServiceIT extends SpockTest {

    @LocalServerPort
    private int port

    def response
    def user
    def quizQuestion
    def quizAnswer
    def questionAnswer
    def date
    def quiz
    def questionDetails
    def question

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course and course execution'
        createExternalCourseAndExecution()
    }

    def 'valid arguments: orderA=#orderA | orderB=#orderB | orderC=#orderC | answerType=#answerType'() {
        given: 'a logged in teacher'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        
        and: 'a multiple choice question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(answerType)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        def optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(orderA)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        def optionB = new Option()
        optionB.setContent(OPTION_2_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setOrder(orderB)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        def optionC = new Option()
        optionC.setContent(OPTION_3_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(2)
        optionC.setOrder(orderC)
        optionC.setQuestionDetails(questionDetails)
        optionRepository.save(optionC)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'a quiz question and respective answer'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)
        
        and: 'a wrong answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setTimeTaken(100)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswerRepository.save(questionAnswer)

        and: 'an answer with three options selected'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        def options = new ArrayList<Integer>()
        options.add(optionA.getId())
        options.add(optionB.getId())
        options.add(optionC.getId())

        multipleChoiceAnswerDto.setOptionsIds(options)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        def quizAnswerItem = new QuizAnswerItem(statementQuizDto)
        quizAnswerItemRepository.save(quizAnswerItem)

        quiz.setConclusionDate(DateHandler.now())

        when: 'the get quiz answer web service is invoked'
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        List<QuizAnswerDto> answers = response.data.quizAnswers
        answers.size() == 1
        QuizAnswerDto answer = answers.get(0)
        answer.id == quizAnswer.getId()
        def questionAnswerResult = answer.questionAnswers[0]

        and: 'the question is correct'
        def questionResult = questionAnswerResult.question
        questionResult.id == question.getId()

        and: 'the options chosen by the student match'
        def answerResult = questionAnswerResult.answerDetails
        def optionsResult = answerResult.options
        optionsResult.size() == 3
        def optionAResult = optionsResult.get(0)
        optionAResult.id == optionA.getId()
        optionAResult.correct
        optionAResult.order == orderA

        def optionBResult = optionsResult.get(1)
        optionBResult.id == optionB.getId()
        optionBResult.correct
        optionBResult.order == orderB

        def optionCResult = optionsResult.get(2)
        optionCResult.id == optionC.getId()
        !optionCResult.correct
        optionCResult.order == orderC

        cleanup:
        optionRepository.deleteAll()

        where:
        orderA | orderB | orderC | answerType      
        -1     |  -1    | -1     | MultipleChoiceQuestion.AnswerType.MULTIPLE 
        1      |   2    |  0     | MultipleChoiceQuestion.AnswerType.ORDER
        1      |  -1    | -1     | MultipleChoiceQuestion.AnswerType.SINGLE
    }

    def 'student can not get quiz answers'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a multiple choice question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'an option'
        def optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setOrder(-1)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'a quiz question and respective answer'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)
        
        and: 'a correct answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setTimeTaken(100)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswerRepository.save(questionAnswer)

        and: 'an answer with three options selected'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        def options = new ArrayList<Integer>()
        options.add(optionA.getId())

        multipleChoiceAnswerDto.setOptionsIds(options)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        def quizAnswerItem = new QuizAnswerItem(statementQuizDto)
        quizAnswerItemRepository.save(quizAnswerItem)

        quiz.setConclusionDate(DateHandler.now())

        when: 'the get quiz answer web service is invoked'
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        optionRepository.delete(optionA)
    }

    def 'teacher sees an open ended quiz result'() {
        given: 'a logged in teacher'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        and: 'an open ended question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        questionDetails = new OpenEndedQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'respective answer'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a wrong answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setTimeTaken(100)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswerRepository.save(questionAnswer)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def openEndedAnswerDto = new OpenEndedStatementAnswerDetailsDto()


        openEndedAnswerDto.setOpenAnswer("Answer")
        statementAnswerDto.setAnswerDetails(openEndedAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        def quizAnswerItem = new QuizAnswerItem(statementQuizDto)
        quizAnswerItemRepository.save(quizAnswerItem)

        quiz.setConclusionDate(DateHandler.now())

        when: 'the get quiz answer web service is invoked'
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        List<QuizAnswerDto> answers = response.data.quizAnswers
        answers.size() == 1
        QuizAnswerDto answer = answers.get(0)
        answer.id == quizAnswer.getId()
        def questionAnswerResult = answer.questionAnswers[0]

        and: 'the question is correct'
        def questionResult = questionAnswerResult.question
        questionResult.id == question.getId()

        and: 'the answer matches'
        def answerResult = questionAnswerResult.answerDetails
        answerResult.openAnswer == "Answer"

    }
    
    def 'teacher sees a quiz - invalid access'() {
        given: 'a different course'
        def externalCourse2 = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(externalCourse2)
        def externalCourseExecution2 = new CourseExecution(externalCourse2, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TODAY)
        courseExecutionRepository.save(externalCourseExecution2)

        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution2)
        userRepository.save(user)
        externalCourseExecution2.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        and: 'an open ended question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        questionDetails = new OpenEndedQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: 'respective answer'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)

        and: 'a wrong answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setTimeTaken(100)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswerRepository.save(questionAnswer)

        and: 'an answer '
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def openEndedAnswerDto = new OpenEndedStatementAnswerDetailsDto()


        openEndedAnswerDto.setOpenAnswer("Answer")
        statementAnswerDto.setAnswerDetails(openEndedAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        def quizAnswerItem = new QuizAnswerItem(statementQuizDto)
        quizAnswerItemRepository.save(quizAnswerItem)

        quiz.setConclusionDate(DateHandler.now())

        when: 'the get quiz answer web service is invoked'
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
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