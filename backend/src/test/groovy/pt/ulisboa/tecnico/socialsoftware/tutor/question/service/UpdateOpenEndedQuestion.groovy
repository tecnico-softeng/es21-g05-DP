package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateOpenEndedQuestion extends SpockTest {
    def question
    def user

    def setup() {
        createExternalCourseAndExecution()
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: 'an image'
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        given: "create a question"
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setContent(QUESTION_2_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setImage(image)
        def questionDetails = new OpenEndedQuestion()
        questionDetails.setCriteria(QUESTION_1_CONTENT)
        questionDetails.setRegexQuestion(QUESTION_4_REGEX)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

    }

    def "update a question"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto();
        openEndedQuestion.setCriteria(QUESTION_2_CONTENT)
        openEndedQuestion.setRegexQuestion(QUESTION_4_REGEX)
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getImage() != null
        and: 'criteria is changed'
        def resCriteria = (OpenEndedQuestion) result.getQuestionDetails()
        resCriteria.getCriteria() == QUESTION_2_CONTENT
        def resRegex = (OpenEndedQuestion) result.getQuestionDetails()
        resRegex.getRegexQuestion() == QUESTION_4_REGEX
    }

    def "update question with missing data"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto();
        openEndedQuestion.setCriteria('     ')
        openEndedQuestion.setRegexQuestion(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_CRITERIA
    }

    def "update question with missing regex"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto();
        openEndedQuestion.setCriteria(QUESTION_2_CONTENT)
        openEndedQuestion.setRegexQuestion('     ')
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_REGEX
    }

    def "update question with invalid regex"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto();
        openEndedQuestion.setCriteria(QUESTION_2_CONTENT)
        openEndedQuestion.setRegexQuestion('[')
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_REGEX
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
