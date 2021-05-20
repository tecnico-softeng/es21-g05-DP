package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswerItem
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.CombinationItemRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetQuizAnswersItemCombinationWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response
    def user
    def quizQuestion
    def quizAnswer
    def questionAnswer
    def quiz
    def questionDetails
    def question
    def item1L
    def item2L
    def item1R
    def item2R

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'an external course and course execution'
        createExternalCourseAndExecution()
    }

    def 'teacher sees an item combination quiz result'() {
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
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)

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

        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()

        def answer1L = new ItemStatementAnswerDto()
        answer1L.setItemId(item1L.getId())
        answer1L.setLeftItem(item1L.getSequence())
        answer1L.setConnections([3] as List)
        itemsAnswered.add(answer1L)

        def answer2L = new ItemStatementAnswerDto()
        answer2L.setItemId(item1L.getId())
        answer2L.setLeftItem(item1L.getSequence())
        answer2L.setConnections([4] as List)
        itemsAnswered.add(answer2L)

        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)
        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
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

        cleanup:
        combinationItemRepository.deleteAll()

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