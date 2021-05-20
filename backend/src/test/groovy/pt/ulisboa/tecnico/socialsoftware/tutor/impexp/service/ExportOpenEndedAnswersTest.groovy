package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@DataJpaTest
class ExportOpenEndedAnswersTest extends SpockTest {

    def quizAnswer
    def questionAnswer

    def setup() {
        createExternalCourseAndExecution()

        Question question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        def questionDetails = new OpenEndedQuestion()
        question.setQuestionDetails(questionDetails)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setCourseExecution(externalCourseExecution)

        quiz.setCreationDate(DateHandler.now())
        quiz.setAvailableDate(DateHandler.now())
        quiz.setConclusionDate(DateHandler.now())
        quiz.setType(Quiz.QuizType.EXAM.toString())
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setSequence(0)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        User user = userService.createUserWithAuth(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, AuthUser.Type.EXTERNAL).getUser()

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswer.setAnswerDate(LOCAL_DATE_TODAY)
        quizAnswer.setCompleted(true)
        quizAnswerRepository.save(quizAnswer)

        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion, 1, 0)
        def answer = new OpenEndedAnswer(questionAnswer)
        answer.setOpenAnswer("answer")
        questionAnswer.setAnswerDetails(answer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answer)
    }

    def 'export open ended answers'() {
        when: 'exporting a quiz'
        def answersXml = answerService.exportAnswers()
        print(answersXml)

        then:
        answersXml != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
