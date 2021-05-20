package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitAnswerItemCombinationWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    def response
    def user
    def item1L
    def item2L
    def item1R
    def item2R
    def quiz
    def quizQuestion
    def quizAnswer
    def questionDetails
    def question

    def setup() {
        given: 'a rest client'
        restClient = new RESTClient("http://localhost:" + port)

        and: 'a course'
        createExternalCourseAndExecution()
    }

    def "submit item combination answer"() {
        given: "a user"
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        externalCourseExecution.addUser(user)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: "a quiz"
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: "a question"
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setCourse(externalCourse)

        questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "a quiz question"
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

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

        def date = DateHandler.now()

        and: "a answer"
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        quiz.setConclusionDate(LOCAL_DATE_LATER)


        and: 'a answer statement'
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()

        and: 'connections'
        def item1LAnswerDto = new ItemStatementAnswerDto()
        item1LAnswerDto.setItemId(item1L.getId())
        item1LAnswerDto.setLeftItem(1)
        item1LAnswerDto.setConnections([3].asList())
        itemsAnswered.add(item1LAnswerDto)
        def item2LAnswerDto = new ItemStatementAnswerDto()
        item2LAnswerDto.setItemId(item2L.getId())
        item2LAnswerDto.setLeftItem(2)
        item2LAnswerDto.setConnections([4].asList())
        itemsAnswered.add(item2LAnswerDto)

        and: 'a item combination answer'
        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())


        def json = new ObjectMapper().writer().withDefaultPrettyPrinter();
        when: "the web service is invoked"
        response = restClient.post(
                path: '/answers/' + quiz.getId() + '/submit',
                body: json.writeValueAsString(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
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
        combinationItemRepository.deleteAll()
        questionAnswerItemRepository.deleteAll()
        quizAnswerRepository.delete(quizAnswerRepository.findById(quizAnswer.getId()).get())
        quizQuestionRepository.delete(quizQuestionRepository.findById(quizQuestion.getId()).get())
        quizRepository.delete(quizRepository.findById(quiz.getId()).get())
        questionDetailsRepository.delete(questionDetailsRepository.findById(questionDetails.getId()).get())
        userRepository.delete(userRepository.findById(user.getId()).get())
        courseRepository.delete(courseRepository.findById(externalCourse.getId()).get())
    }
}