package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.*;
import spock.lang.Unroll


@DataJpaTest
class GetQuizAnswersOpenEndedTest extends SpockTest {
    def user
    def quizQuestion
    def quizAnswer
    def questionAnswer
    def date
    def quiz
    def questionDetails
    def question
    def options

    def setup() {
        given: 'an external course and execution'
        createExternalCourseAndExecution()

        and: 'a student'
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

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

    @Unroll
    def 'get quiz answer of open ended quiz'() {

        given: 'an answer'
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new OpenEndedAnswer(questionAnswer);
        questionAnswer.setAnswerDetails(answerDetails);

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

        and: 'the students answer match'
        OpenEndedAnswerDto answerResult = questionAnswerResult.getAnswerDetails()
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
