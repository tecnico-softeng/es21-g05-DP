package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateMultipleChoiceQuestionTypeTest extends SpockTest {
    def question
    def optionA
    def optionB
    def optionC
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

        given: 'a new question'
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(3)
        question.setNumberOfCorrect(2)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        questionDetails.setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'three options'
        optionA = new Option()
        optionA.setContent(OPTION_1_CONTENT)
        optionA.setCorrect(true)
        optionA.setSequence(0)
        optionA.setQuestionDetails(questionDetails)
        optionRepository.save(optionA)

        optionB = new Option()
        optionB.setContent(OPTION_1_CONTENT)
        optionB.setCorrect(true)
        optionB.setSequence(1)
        optionB.setQuestionDetails(questionDetails)
        optionRepository.save(optionB)

        optionC = new Option()
        optionC.setContent(OPTION_1_CONTENT)
        optionC.setCorrect(false)
        optionC.setSequence(2)
        optionC.setQuestionDetails(questionDetails)
        optionRepository.save(optionC)
    }

    def "update question from MULTIPLE to SINGLE correct answers type"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: "a multiple choice question with single correct answers"
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.SINGLE.name())

        def optionDto = new OptionDto(optionA)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionB)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionC)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        and: 'a count to load options to memory due to in memory database flaw'
        optionRepository.count()

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        def result = questionRepository.findAll().get(0)
        result.getQuestionDetails().getAnswerType() == MultipleChoiceQuestion.AnswerType.SINGLE
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 3
        def resOptionCorrect= result.getQuestionDetails().getOptions().stream().filter({ option -> option.isCorrect()}).findAny().orElse(null)
        resOptionCorrect.isCorrect()
        def resIncorrectOptions = result.getQuestionDetails().getOptions().stream().filter({ option -> !option.isCorrect()}).findAll()
        resIncorrectOptions.size() == 2
    }

    def "update question from MULTIPLE to SINGLE with two correct answers"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: "a multiple choice question with SINGLE correct answer"
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.SINGLE.name())

        def optionDto = new OptionDto(optionA)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionB)
        options.add(optionDto)
        optionDto = new OptionDto(optionC)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ONE_CORRECT_OPTION_NEEDED
    }

    def "update question from MULTIPLE to ORDER type with two correct ordered answers and one incorrect"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: "an ordered multiple choice question with single correct answers"
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.ORDER.name())

        and: "ordered correct options"
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionA)
        optionDto.setOrder(1)
        options.add(optionDto)
        optionDto = new OptionDto(optionB)
        optionDto.setOrder(2)
        options.add(optionDto)

        and: "non ordered false option"
        optionDto = new OptionDto(optionC)
        optionDto.setOrder(0)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        and: 'a count to load options to memory due to in memory database flaw'
        optionRepository.count()

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed to have ordered answers"
        def result = questionRepository.findAll().get(0)
        result.getQuestionDetails().getAnswerType() == MultipleChoiceQuestion.AnswerType.ORDER

        and: 'options are correctly ordered'
        result.getQuestionDetails().getOptions().size() == 3
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getOrder() == 1}).findAny().orElse(null)
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getOrder() == 2}).findAny().orElse(null)
        resOptionTwo.isCorrect()
        def resOptionThree = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getOrder() == 0}).findAny().orElse(null)
        !resOptionThree.isCorrect()
    }

    def "update question from MULTIPLE to ORDER type with one correct answer"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: "an ordered multiple choice question with a single correct answer"
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.ORDER.name())

        and: "a single correct answer"
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionB)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.MORE_THAN_ONE_CORRECT_OPTION_NEEDED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
