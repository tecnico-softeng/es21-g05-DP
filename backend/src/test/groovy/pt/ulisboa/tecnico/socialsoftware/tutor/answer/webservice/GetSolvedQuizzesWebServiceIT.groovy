package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice


import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetSolvedQuizzesWebServiceIT extends SpockTest {

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

    def 'student concludes a quiz with a multiple choice question with multiple answer type and sees the results'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)

        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)

        and: 'two options'
        def options = new ArrayList<Option>()
        def optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setOrder(-1)
        optionOK.setQuestionDetails(questionDetails)
        options.add(optionOK)
        optionRepository.save(optionOK)

        def optionOKK = new Option()
        optionOKK.setContent(OPTION_1_CONTENT)
        optionOKK.setCorrect(true)
        optionOKK.setSequence(1)
        optionOKK.setOrder(-1)
        optionOKK.setQuestionDetails(questionDetails)
        options.add(optionOKK)
        optionRepository.save(optionOKK)

        def optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(2)
        optionKO.setOrder(-1)
        optionKO.setQuestionDetails(questionDetails)
        options.add(optionKO)
        optionRepository.save(optionKO)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options)
        questionAnswer.setAnswerDetails(answerDetails)

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        when: 'the conclude quiz web service is invoked'
        response = restClient.get(
                path: '/answers/' + externalCourseExecution.getId() + '/quizzes/solved',
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        def solvedQuizDtos = response.data
        solvedQuizDtos.size() == 1
        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.statementQuiz
        statementQuizDto.questions.size() == 1
        solvedQuizDto.statementQuiz.answers.size() == 1
        def answer = solvedQuizDto.statementQuiz.answers.get(0)
        answer.sequence == 0
        answer.answerDetails.optionsIds.get(0) == options.get(0).getId()
        solvedQuizDto.correctAnswers.size() == 1
        def correct = solvedQuizDto.correctAnswers.get(0)
        correct.sequence == 0
        !correct.correctAnswerDetails.isOrdered
        def correctOptions = correct.correctAnswerDetails.correctOptionsIds
        correctOptions.size() == 2
        correctOptions.get(0) == options.get(0).getId()
        correctOptions.get(1) == options.get(1).getId()

        cleanup:
        optionRepository.delete(optionRepository.findById(optionOK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionOKK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionKO.getId()).get())
    }

    def 'student concludes a quiz with a multiple choice question with order answer type and sees the results'() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)

        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.ORDER)

        and: 'three options'
        def options = new ArrayList<Option>()
        def optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setOrder(1)
        optionOK.setQuestionDetails(questionDetails)
        options.add(optionOK)
        optionRepository.save(optionOK)

        def optionOKK = new Option()
        optionOKK.setContent(OPTION_1_CONTENT)
        optionOKK.setCorrect(true)
        optionOKK.setSequence(1)
        optionOKK.setOrder(2)
        optionOKK.setQuestionDetails(questionDetails)
        options.add(optionOKK)
        optionRepository.save(optionOKK)

        def optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(2)
        optionKO.setOrder(0)
        optionKO.setQuestionDetails(questionDetails)
        options.add(optionKO)
        optionRepository.save(optionKO)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options)
        questionAnswer.setAnswerDetails(answerDetails)

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        when: 'the conclude quiz web service is invoked'
        response = restClient.get(
                path: '/answers/' + externalCourseExecution.getId() + '/quizzes/solved',
                requestContentType: 'application/json'
        )

        then: "there is a not null return response"
        response != null
        response.status == HttpStatus.SC_OK

        def solvedQuizDtos = response.data
        solvedQuizDtos.size() == 1
        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.statementQuiz
        statementQuizDto.questions.size() == 1
        solvedQuizDto.statementQuiz.answers.size() == 1
        def answer = solvedQuizDto.statementQuiz.answers.get(0)
        answer.sequence == 0
        answer.answerDetails.optionsIds.get(0) == options.get(0).getId()
        solvedQuizDto.correctAnswers.size() == 1
        def correct = solvedQuizDto.correctAnswers.get(0)
        correct.sequence == 0
        !correct.correctAnswerDetails.isOrdered
        def correctOptions = correct.correctAnswerDetails.correctOptionsIds
        correctOptions.size() == 2
        correctOptions.get(0) == options.get(0).getId()
        correctOptions.get(1) == options.get(1).getId()

        cleanup:
        optionRepository.delete(optionRepository.findById(optionOK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionOKK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionKO.getId()).get())
    }

    def 'teacher can not see solved quizzes'() {
        given: 'a logged in teacher'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)
        question.setContent(QUESTION_1_CONTENT)

        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)

        and: 'two options'
        def options = new ArrayList<Option>()
        def optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setOrder(-1)
        optionOK.setQuestionDetails(questionDetails)
        options.add(optionOK)
        optionRepository.save(optionOK)

        def optionOKK = new Option()
        optionOKK.setContent(OPTION_1_CONTENT)
        optionOKK.setCorrect(true)
        optionOKK.setSequence(1)
        optionOKK.setOrder(-1)
        optionOKK.setQuestionDetails(questionDetails)
        options.add(optionOKK)
        optionRepository.save(optionOKK)

        def optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(2)
        optionKO.setOrder(-1)
        optionKO.setQuestionDetails(questionDetails)
        options.add(optionKO)
        optionRepository.save(optionKO)

        and: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options)
        questionAnswer.setAnswerDetails(answerDetails)

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        when: 'the conclude quiz web service is invoked'
        response = restClient.get(
                path: '/answers/' + externalCourseExecution.getId() + '/quizzes/solved',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        optionRepository.delete(optionRepository.findById(optionOK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionOKK.getId()).get())
        optionRepository.delete(optionRepository.findById(optionKO.getId()).get())
    }

    def "returns solved quiz with open ended answer"() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria("Criteria")
        questionDetails.setRegexQuestion("^[0-9]*\$")
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)


        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)


        and: 'a quiz question'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)


        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)


        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new OpenEndedAnswer(questionAnswer, "Answer");
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)


        when: 'the solved quizzes web service is invoked'
        response = restClient.get(
                path: '/answers/' + externalCourseExecution.getId() + '/quizzes/solved',
                requestContentType: 'application/json'
        )

        then: "there is not a null response"
        response != null
        response.status == HttpStatus.SC_OK

        def solvedQuizDto = response.data

        solvedQuizDto.statementQuiz.answers.size() == 1
        def answer = solvedQuizDto.statementQuiz.answers.get(0)
        answer.sequence.get(0) == 0
        answer.answerDetails.openAnswer.get(0) == "Answer"
        solvedQuizDto.correctAnswers.size() == 1
        def correct = solvedQuizDto.correctAnswers.get(0)
        correct.sequence.get(0) == 0
        correct.correctAnswerDetails.criteria.get(0) == "Criteria"
        correct.correctAnswerDetails.regex.get(0) == "^[0-9]*\$"

    }

    def "attempt to get solved quiz with invalid access"() {
        given: 'a logged in student'
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))

        def externalCourse2 = new Course(COURSE_2_NAME, Course.Type.TECNICO)
        courseRepository.save(externalCourse2)
        def externalCourseExecution2 = new CourseExecution(externalCourse2, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.TECNICO, LOCAL_DATE_TODAY)
        courseExecutionRepository.save(externalCourseExecution2)

        user.addCourse(externalCourseExecution2)
        userRepository.save(user)
        externalCourseExecution2.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'an open ended question'
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria("Criteria")
        questionDetails.setRegexQuestion("^[0-9]*\$")
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)


        and: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)


        and: 'a quiz question'
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)


        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)


        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new OpenEndedAnswer(questionAnswer, "Answer");
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)


        when: 'the solved quizzes web service is invoked'
        response = restClient.get(
                path: '/answers/' + externalCourseExecution.getId() + '/quizzes/solved',
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
