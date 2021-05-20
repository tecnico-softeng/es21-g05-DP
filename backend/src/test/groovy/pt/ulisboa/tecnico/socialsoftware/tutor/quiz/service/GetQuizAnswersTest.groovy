package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.*;
import spock.lang.Unroll

@DataJpaTest
class GetQuizAnswersTest extends SpockTest {
    def user
    def quizQuestion
    def optionA
    def optionB
    def optionC
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

        and: 'a multiple choice question'
        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        options = new ArrayList<Option>()
        optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        optionB = new Option()
        optionB.setContent(OPTION_2_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        optionC = new Option()
        optionC.setContent(OPTION_3_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(2)
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
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    @Unroll
    def 'valid arguments: orderA=#orderA | orderB=#orderB | orderC=#orderC | answerType=#answerType'() {
        given: 'three not ordered options'
        questionDetails.setAnswerType(answerType)
        optionA.setOrder(orderA)
        optionB.setOrder(orderB)
        optionC.setOrder(orderC)

        and: 'a wrong answer'
        options.add(optionA)
        options.add(optionB)
        options.add(optionC)
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options);
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

        and: 'the options chosen by the student match'
        MultipleChoiceAnswerDto answerResult = questionAnswerResult.getAnswerDetails()
        List<OptionDto> optionsResult = answerResult.getOptions()
        optionsResult.size() == 3
        def optionAResult = optionsResult.get(0)
        optionAResult.getId() == optionA.getId()
        optionAResult.isCorrect()
        optionAResult.getOrder() == orderA

        def optionBResult = optionsResult.get(1)
        optionBResult.getId() == optionB.getId()
        optionBResult.isCorrect()
        optionBResult.getOrder() == orderB

        def optionCResult = optionsResult.get(2)
        optionCResult.getId() == optionC.getId()
        !optionCResult.isCorrect()
        optionCResult.getOrder() == orderC

        where:
        orderA | orderB | orderC | answerType      
        -1     |  -1    | -1     | MultipleChoiceQuestion.AnswerType.MULTIPLE 
        1      |   2    |  0     | MultipleChoiceQuestion.AnswerType.ORDER
        1      |  -1    | -1     | MultipleChoiceQuestion.AnswerType.SINGLE
    }

    def 'can not get quiz answer results from a nonexistent quiz'() {
        given: 'three not ordered options'
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        optionA.setOrder(-1)
        optionB.setOrder(-1)
        optionC.setOrder(-1)

        and: 'a wrong answer'
        options.add(optionA)
        options.add(optionB)
        options.add(optionC)
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, options);
        questionAnswer.setAnswerDetails(answerDetails);

        when:
        QuizAnswersDto quizAnswers = quizService.getQuizAnswers(-1)

        then: 'an error is thrown'
        def error = thrown(TutorException)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
