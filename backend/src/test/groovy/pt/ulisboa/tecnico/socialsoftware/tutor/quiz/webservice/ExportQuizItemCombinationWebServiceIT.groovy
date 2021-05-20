package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.webservice

import groovyx.net.http.RESTClient
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportQuizItemCombinationWebServiceIT extends SpockTest{
    @LocalServerPort
    private int port
    def teacher
    def quiz
    def item1L
    def item2L
    def item1R
    def item2R
    def user
    def question
    def questionDetails
    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course and course execution'
        createExternalCourseAndExecution()
    }

    def 'export item combination quiz'() {
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

        questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "4 items"
        item1L = new CombinationItem()
        item1L.setContent(OPTION_1_CONTENT)
        item1L.setSequence(1)
        item1L.setGroup(CombinationItem.Group.LEFT)
        item1L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1L)


        item2L = new CombinationItem()
        item2L.setContent(OPTION_2_CONTENT)
        item2L.setSequence(2)
        item2L.setGroup(CombinationItem.Group.LEFT)
        item2L.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2L)

        item1R = new CombinationItem()
        item1R.setContent(OPTION_1_CONTENT)
        item1R.setSequence(3)
        item1R.setGroup(CombinationItem.Group.RIGHT)
        item1R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item1R)

        item2R = new CombinationItem()
        item2R.setContent(OPTION_2_CONTENT)
        item2R.setSequence(4)
        item2R.setGroup(CombinationItem.Group.RIGHT)
        item2R.setQuestionDetails(questionDetails)
        combinationItemRepository.save(item2R)

        connectItems(item1L, item1R)
        connectItems(item2L, item2R)

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

        and: 'an empty answer'
        List<CombinationAnswer> itemsAnswered = new ArrayList<>()

        def answerDetails = new ItemCombinationAnswer(questionAnswer, itemsAnswered)
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
                path: '/quizzes/' + quiz.getId() + '/export',
                requestContentType: 'application/zip'
        )
        def response = map['response']

        then: "check the response status"
        assert response.status == HttpStatus.SC_OK
        assert map['reader'] != null

        cleanup:
        combinationItemRepository.deleteAll()

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