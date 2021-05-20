package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto

@DataJpaTest
class ImportExportOpenEndedQuizzesTest extends SpockTest {
    def quiz
    def creationDate
    def availableDate
    def conclusionDate

    def setup() {
        createExternalCourseAndExecution()

        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenEndedQuestionDto())
        questionDto.getQuestionDetailsDto().setCriteria("Criteria")
        questionDto.getQuestionDetailsDto().setRegexQuestion("Regex")

        questionDto = questionService.createQuestion(externalCourse.getId(), questionDto)


        def quizDto = new QuizDto()
        quizDto.setKey(1)
        quizDto.setScramble(false)
        quizDto.setQrCodeOnly(true)
        quizDto.setOneWay(false)
        quizDto.setTitle(QUIZ_TITLE)
        creationDate = DateHandler.now()
        availableDate = DateHandler.now()
        conclusionDate = DateHandler.now().plusDays(2)
        quizDto.setCreationDate(DateHandler.toISOString(creationDate))
        quizDto.setAvailableDate(DateHandler.toISOString(availableDate))
        quizDto.setConclusionDate(DateHandler.toISOString(conclusionDate))
        quizDto.setType(Quiz.QuizType.EXAM.toString())
        quiz = quizService.createQuiz(externalCourseExecution.getId(), quizDto)

        quizService.addQuestionToQuiz(questionDto.getId(), quiz.getId())
    }

    def 'export and import open ended quiz'() {
        given: 'a xml with a quiz'
        def quizzesXml = quizService.exportQuizzesToXml()
        and: 'delete quiz and quizQuestion'
        print quizzesXml
        quizService.removeQuiz(quiz.getId())

        when:
        quizService.importQuizzesFromXml(quizzesXml)

        then:
        quizzesXml != null
        quizRepository.findAll().size() == 1
        def quizResult = quizRepository.findAll().get(0)
        quizResult.getKey() == 1
        !quizResult.getScramble()
        quizResult.isQrCodeOnly()
        !quizResult.isOneWay()
        quizResult.getTitle() == QUIZ_TITLE
        quizResult.getCreationDate() == creationDate
        quizResult.getAvailableDate() == availableDate
        quizResult.getConclusionDate() == conclusionDate
        quizResult.getType() == Quiz.QuizType.EXAM
        quizResult.getQuizQuestionsNumber() == 1
        def quizQuestionResult =  quizResult.getQuizQuestions().stream().findAny().orElse(null)
        quizQuestionResult.getSequence() == 0
        quizQuestionResult.getQuiz() == quizResult
        quizQuestionResult.getQuestion().getKey() == 1
        quizQuestionResult.getQuestion().getQuestionDetails().getCriteria() == "Criteria"
        quizQuestionResult.getQuestion().getQuestionDetails().getRegexQuestion() == "Regex"
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}