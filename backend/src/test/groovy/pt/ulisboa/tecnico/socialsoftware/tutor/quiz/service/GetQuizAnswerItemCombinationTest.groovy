package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CombinationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswersDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler


@DataJpaTest
class GetQuizAnswerItemCombinationTest extends SpockTest {
    def user
    def quizQuestion
    def item1L
    def item2L
    def item1R
    def item2R
    def quizAnswer
    def questionAnswer
    def date
    def quiz
    def questionDetails
    def question

    def setup() {
        given: 'an external course and execution'
        createExternalCourseAndExecution()

        and: 'a student'
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: 'a multiple choice question'
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
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'get quiz answer of item combination question'() {
        given: 'an answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)

        List<CombinationAnswer> itemsAnswered = new ArrayList<>()
        and: 'connections'
        def item1LAnswer = new CombinationAnswer()
        item1LAnswer.setConns([item1R] as List)
        itemsAnswered.add(item1LAnswer)


        def item2LAnswer = new CombinationAnswer()

        item2LAnswer.setConns([] as List)
        itemsAnswered.add(item2LAnswer)

        def answerDetails = new ItemCombinationAnswer(questionAnswer, itemsAnswered)
        questionAnswer.setAnswerDetails(answerDetails);
        item1LAnswer.setItemCombinationAnswer(answerDetails)
        item2LAnswer.setItemCombinationAnswer(answerDetails)

        when:
        QuizAnswersDto quizAnswers = quizService.getQuizAnswers(quiz.getId())

        then: 'the quizzes answers are correctly returned'
        List<QuizAnswerDto> answers = quizAnswers.getQuizAnswers()
        answers.size() == 1

        QuizAnswerDto answer = answers.get(0)
        answer.getId() == quizAnswer.getId()
        QuestionAnswerDto questionAnswerResult = answer.getQuestionAnswers().get(0)
        and: 'the question is correct'
        QuestionDto questionResult = questionAnswerResult.getQuestion()
        questionResult.getId() == question.getId()

        ItemCombinationAnswerDto answerResult =(ItemCombinationAnswerDto) questionAnswerResult.getAnswerDetails()
        List<CombinationAnswerDto> itemsResult = answerResult.getConnections()
        itemsResult.size() == 2

    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}