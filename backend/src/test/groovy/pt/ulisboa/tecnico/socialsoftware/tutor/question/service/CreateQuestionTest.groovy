package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeFillInQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeOrderQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*
import spock.lang.Unroll

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage

@DataJpaTest
class CreateQuestionTest extends SpockTest {
    def setup() {
        createExternalCourseAndExecution()
    }

    def "create a multiple choice question with no image and one option"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.SINGLE.name())

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetails().getOptions().size() == 1
        result.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(result)
        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()
    }

    def "create a multiple choice question with image and two options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'an image'
        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)
        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.SINGLE.name())

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == IMAGE_1_URL
        result.getImage().getWidth() == 20
        result.getQuestionDetails().getOptions().size() == 2

        and: 'the question has a single answer type when is created'
        result.getQuestionDetails().getAnswerType() == MultipleChoiceQuestion.AnswerType.SINGLE
    }

    def "create two multiple choice questions"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.SINGLE.name())

        when: 'are created two questions'
        questionService.createQuestion(externalCourse.getId(), questionDto)
        questionDto.setKey(null)
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the two questions are created with the correct numbers"
        questionRepository.count() == 2L
        def resultOne = questionRepository.findAll().get(0)
        def resultTwo = questionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3
    }

    def "create a multiple choice question with multiple correct answers"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        and: "a multiple choice question with multiple correct answers"
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.MULTIPLE.name())

        and: "two correct options"
        def optionDtoA = new OptionDto()
        def optionDtoB = new OptionDto()
        optionDtoA.setContent(OPTION_1_CONTENT)
        optionDtoA.setCorrect(true)
        optionDtoB.setContent(OPTION_2_CONTENT)
        optionDtoB.setCorrect(true)

        and: "an incorrect option"
        def optionDtoC = new OptionDto()
        optionDtoC.setContent(OPTION_3_CONTENT)
        optionDtoC.setCorrect(false)

        def options = new ArrayList<OptionDto>()
        options.add(optionDtoA)
        options.add(optionDtoB)
        options.add(optionDtoC)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetails().getOptions().size() == 3
        result.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(result)
        result.getQuestionDetails().getAnswerType() == MultipleChoiceQuestion.AnswerType.MULTIPLE

        and: "the correspondent options are settled"
        def resOptionA = result.getQuestionDetails().getOptions().get(0)
        resOptionA.getContent() == OPTION_1_CONTENT
        resOptionA.isCorrect()

        def resOptionB = result.getQuestionDetails().getOptions().get(1)
        resOptionB.getContent() == OPTION_2_CONTENT
        resOptionB.isCorrect()

        def resOptionC = result.getQuestionDetails().getOptions().get(2)
        resOptionC.getContent() == OPTION_3_CONTENT
        resOptionC.isCorrect() == false
    }

    def "create an order multiple choice question type"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        and: "a multiple choice question with ordered answers"
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setAnswerType(MultipleChoiceQuestion.AnswerType.ORDER.name())

        and: "two correct options"
        def optionDtoA = new OptionDto()
        def optionDtoB = new OptionDto()
        optionDtoA.setContent(OPTION_1_CONTENT)
        optionDtoA.setCorrect(true)
        optionDtoB.setContent(OPTION_2_CONTENT)
        optionDtoB.setCorrect(true)

        and: "order the correct options"
        optionDtoA.setOrder(1)
        optionDtoB.setOrder(2)

        and: "an incorrect option with no order"
        def optionDtoC = new OptionDto()
        optionDtoC.setContent(OPTION_3_CONTENT)
        optionDtoC.setCorrect(false)
        optionDtoC.setOrder(0)

        def options = new ArrayList<OptionDto>()
        options.add(optionDtoA)
        options.add(optionDtoB)
        options.add(optionDtoC)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getQuestionDetails().getOptions().size() == 3
        result.getQuestionDetails().getAnswerType() == MultipleChoiceQuestion.AnswerType.ORDER

        and: "the correspondent ordered options are settled"
        def resOptionA = result.getQuestionDetails().getOptions().get(0)
        resOptionA.getContent() == OPTION_1_CONTENT
        resOptionA.isCorrect()
        resOptionA.getOrder() == 1

        def resOptionB = result.getQuestionDetails().getOptions().get(1)
        resOptionB.getContent() == OPTION_2_CONTENT
        resOptionB.isCorrect()
        resOptionB.getOrder() == 2

        def resOptionC = result.getQuestionDetails().getOptions().get(2)
        resOptionC.getContent() == OPTION_3_CONTENT
        resOptionC.isCorrect() == false
        resOptionC.getOrder() == 0
    }

    def "create a code fill in question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeFillInQuestionDto()
        codeQuestionDto.setCode(CODE_QUESTION_1_CODE)
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        OptionDto optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        fillInSpotDto.getOptions().add(optionDto)
        fillInSpotDto.setSequence(1)

        codeQuestionDto.getFillInSpots().add(fillInSpotDto)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def rawResult = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct data is sent back"
        rawResult instanceof QuestionDto
        def result = (QuestionDto) rawResult
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.toString()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetailsDto().getFillInSpots().size() == 1
        result.getQuestionDetailsDto().getFillInSpots().get(0).getOptions().size() == 1

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getKey() == 1
        repoResult.getStatus() == Question.Status.AVAILABLE
        repoResult.getTitle() == QUESTION_1_TITLE
        repoResult.getContent() == QUESTION_1_CONTENT
        repoResult.getImage() == null
        repoResult.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(repoResult)

        def repoCode = (CodeFillInQuestion) repoResult.getQuestionDetails()
        repoCode.getFillInSpots().size() == 1
        repoCode.getCode() == CODE_QUESTION_1_CODE
        repoCode.getLanguage() == CODE_QUESTION_1_LANGUAGE
        def resOption = repoCode.getFillInSpots().get(0).getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

    }

    def "cannot create a code fill in question without fill in spots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_ONE_OPTION_NEEDED
    }

    def "cannot create a code fill in question with fill in spots without options"() {
        given: "a questionDto with 1 fill in spot without options"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        questionDto.getQuestionDetailsDto().getFillInSpots().add(fillInSpotDto)


        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_OPTION
    }

    def "cannot create a code fill in question with fillin spots without correct options"() {
        given: "a questionDto with 1 fill in spot without options"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        OptionDto optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        questionDto.getQuestionDetailsDto().getFillInSpots().add(fillInSpotDto)


        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_OPTION
    }


    def "create a code order question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        CodeOrderSlotDto slotDto3 = new CodeOrderSlotDto()
        slotDto3.content = OPTION_1_CONTENT;
        slotDto3.order = 3;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)
        codeQuestionDto.getCodeOrderSlots().add(slotDto3)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def rawResult = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct data is sent back"
        rawResult instanceof QuestionDto
        def result = (QuestionDto) rawResult
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.toString()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetailsDto().getCodeOrderSlots().size() == 3
        result.getQuestionDetailsDto().getCodeOrderSlots().get(0).getContent() == OPTION_1_CONTENT

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getKey() == 1
        repoResult.getStatus() == Question.Status.AVAILABLE
        repoResult.getTitle() == QUESTION_1_TITLE
        repoResult.getContent() == QUESTION_1_CONTENT
        repoResult.getImage() == null
        repoResult.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(repoResult)

        def repoCode = (CodeOrderQuestion) repoResult.getQuestionDetails()
        repoCode.getCodeOrderSlots().size() == 3
        repoCode.getLanguage() == CODE_QUESTION_1_LANGUAGE
        def resOption = repoCode.getCodeOrderSlots().get(0)
        resOption.getContent() == OPTION_1_CONTENT
    }

    def "cannot create a code order question without CodeOrderSlots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }

    def "cannot create a code order question without 3 CodeOrderSlots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)

        questionDto.setQuestionDetailsDto(codeQuestionDto)
        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }

    def "cannot create a code order question without 3 CodeOrderSlots with order"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        CodeOrderSlotDto slotDto3 = new CodeOrderSlotDto()
        slotDto3.content = OPTION_1_CONTENT;
        slotDto3.order = null;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)
        codeQuestionDto.getCodeOrderSlots().add(slotDto3)

        questionDto.setQuestionDetailsDto(codeQuestionDto)
        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }

    def "create a item combination question with 2 options in each group"(){
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "the left group of items"
        def items = new ArrayList<CombinationItemDto>()
        def item1LDto = new CombinationItemDto()
        item1LDto.setContent(OPTION_1_CONTENT)
        item1LDto.setSequence(1)
        item1LDto.setGroup("LEFT")
        items.add(item1LDto)
        def item2LDto = new CombinationItemDto()
        item2LDto.setContent(OPTION_2_CONTENT)
        item2LDto.setSequence(2)
        item2LDto.setGroup("LEFT")
        items.add(item2LDto)

        and: "the right group of options"
        def item1RDto = new CombinationItemDto()
        item1RDto.setContent(OPTION_1_CONTENT)
        item1RDto.setSequence(3)
        item1RDto.setGroup("RIGHT")
        items.add(item1RDto)
        def item2RDto = new CombinationItemDto()
        item2RDto.setContent(OPTION_2_CONTENT)
        item2RDto.setSequence(4)
        item2RDto.setGroup("RIGHT")
        items.add(item2RDto)

        connectItemDtos(item1LDto, item2RDto)
        connectItemDtos(item2LDto, item1RDto)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the item combination question is in the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        def allItems= result.getQuestionDetails().getItems()
        countGroupItems(allItems, "LEFT") == 2
        countGroupItems(allItems, "RIGHT") == 2
        externalCourse.getQuestions().contains(result)

        and: "the correspondent connections are connected"
        def resItemL1 = result.getQuestionDetails().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item2RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT

        def resItemL2 = result.getQuestionDetails().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().get(0).getSequence() == item1RDto.getSequence()
        resItemL2.getGroup() == CombinationItem.Group.LEFT

        def resItemR1 = result.getQuestionDetails().getItems().get(2)
        resItemR1.getContent() == OPTION_1_CONTENT
        resItemR1.getGroup() == CombinationItem.Group.RIGHT

        def resItemR2 = result.getQuestionDetails().getItems().get(3)
        resItemR2.getContent() == OPTION_2_CONTENT
        resItemR2.getGroup() == CombinationItem.Group.RIGHT
    }


    def "cannot create item combination question without 2 items in each group"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "the left group of options"
        def items = new ArrayList<CombinationItemDto>()
        def item1LDto = new CombinationItemDto()
        item1LDto.setContent(OPTION_1_CONTENT)
        item1LDto.setSequence(1)
        item1LDto.setGroup("LEFT")
        items.add(item1LDto)

        and: "the right group of options"
        def item1RDto = new CombinationItemDto()
        item1RDto.setContent(OPTION_1_CONTENT)
        item1RDto.setSequence(2)
        item1RDto.setGroup("RIGHT")
        items.add(item1RDto)

        connectItemDtos(item1LDto,item1RDto)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_TWO_OPTIONS_PER_GROUP
    }

    def "create a many-to-many item combination question"(){
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "the left group of options"
        def items = new ArrayList<CombinationItemDto>()
        def item1LDto = new CombinationItemDto()
        item1LDto.setContent(OPTION_1_CONTENT)
        item1LDto.setSequence(1)
        item1LDto.setGroup("LEFT")
        items.add(item1LDto)
        def item2LDto = new CombinationItemDto()
        item2LDto.setContent(OPTION_2_CONTENT)
        item2LDto.setSequence(2)
        item2LDto.setGroup("LEFT")
        items.add(item2LDto)

        and: "the right group of options"
        def item1RDto = new CombinationItemDto()
        item1RDto.setContent(OPTION_1_CONTENT)
        item1RDto.setSequence(3)
        item1RDto.setGroup("RIGHT")
        items.add(item1RDto)
        def item2RDto = new CombinationItemDto()
        item2RDto.setContent(OPTION_2_CONTENT)
        item2RDto.setSequence(4)
        item2RDto.setGroup("RIGHT")
        items.add(item2RDto)
        def item3RDto = new CombinationItemDto()
        item3RDto.setContent(OPTION_3_CONTENT)
        item3RDto.setSequence(5)
        item3RDto.setGroup("RIGHT")
        items.add(item3RDto)

        connectItemDtos(item1LDto, item1RDto)
        connectItemDtos(item2LDto, item3RDto)
        connectItemDtos(item1LDto, item2RDto)

        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the item combination question is in the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        def allItems= result.getQuestionDetails().getItems()
        countGroupItems(allItems, "LEFT") == 2
        countGroupItems(allItems, "RIGHT") == 3
        externalCourse.getQuestions().contains(result)

        and: "the correspondent connections are connected"
        def resItemL1 = result.getQuestionDetails().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item1RDto.getSequence()
        resItemL1.getConnections().get(1).getSequence() == item2RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT

        def resItemL2 = result.getQuestionDetails().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().get(0).getSequence() == item3RDto.getSequence()
        resItemL2.getGroup() == CombinationItem.Group.LEFT

        def resItemR1 = result.getQuestionDetails().getItems().get(2)
        resItemR1.getContent() == OPTION_1_CONTENT
        resItemR1.getGroup() == CombinationItem.Group.RIGHT

        def resItemR2 = result.getQuestionDetails().getItems().get(3)
        resItemR2.getContent() == OPTION_2_CONTENT
        resItemR2.getGroup() == CombinationItem.Group.RIGHT

        def resItemR3 = result.getQuestionDetails().getItems().get(4)
        resItemR3.getContent() == OPTION_3_CONTENT
        resItemR3.getGroup() == CombinationItem.Group.RIGHT
    }

    def "create a one-to-none item combination question"(){
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "the left group of items"
        def items = new ArrayList<CombinationItemDto>()
        def item1LDto = new CombinationItemDto()
        item1LDto.setContent(OPTION_1_CONTENT)
        item1LDto.setSequence(1)
        item1LDto.setGroup("LEFT")
        items.add(item1LDto)
        def item2LDto = new CombinationItemDto()
        item2LDto.setContent(OPTION_2_CONTENT)
        item2LDto.setSequence(2)
        item2LDto.setGroup("LEFT")
        items.add(item2LDto)

        and: "the right group of options"
        def item1RDto = new CombinationItemDto()
        item1RDto.setContent(OPTION_1_CONTENT)
        item1RDto.setGroup("RIGHT")
        item1RDto.setSequence(3)
        items.add(item1RDto)
        def item2RDto = new CombinationItemDto()
        item2RDto.setContent(OPTION_2_CONTENT)
        item2RDto.setGroup("RIGHT")
        item2RDto.setSequence(4)
        items.add(item2RDto)

        connectItemDtos(item1LDto,item1RDto)

        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the item combination question is in the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        def allItems= result.getQuestionDetails().getItems()
        countGroupItems(allItems, "LEFT") == 2
        countGroupItems(allItems, "RIGHT") == 2
        externalCourse.getQuestions().contains(result)

        and: "the correspondent connections are connected"
        def resItemL1 = result.getQuestionDetails().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item1RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT

        def resItemL2 = result.getQuestionDetails().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().size() == 0
        resItemL2.getGroup() == CombinationItem.Group.LEFT

        def resItemR1 = result.getQuestionDetails().getItems().get(2)
        resItemR1.getContent() == OPTION_1_CONTENT
        resItemR1.getGroup() == CombinationItem.Group.RIGHT

        def resItemR2 = result.getQuestionDetails().getItems().get(3)
        resItemR2.getContent() == OPTION_2_CONTENT
        resItemR2.getGroup() == CombinationItem.Group.RIGHT
    }

     def "create an open ended question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        OpenEndedQuestionDto openEndedQuestion = new OpenEndedQuestionDto();
        openEndedQuestion.setCriteria(QUESTION_2_CONTENT)
        openEndedQuestion.setRegexQuestion(QUESTION_4_REGEX)
        questionDto.setQuestionDetailsDto(openEndedQuestion)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(result)

        def resDetails = (OpenEndedQuestion) result.getQuestionDetails()
        resDetails.getCriteria() == QUESTION_2_CONTENT
        resDetails.getRegexQuestion() == QUESTION_4_REGEX
    }

    static Long countGroupItems(List<CombinationItem> items, String group) {
        return items.stream().filter(op-> op.getGroup().name().equals(group)).count()
    }

    @Unroll
    def "fail to create any question for invalid/non-existent course (#nonExistentId)"(Integer nonExistentId) {
        given: "any multiple choice question dto"
        def questionDto = new QuestionDto()
        when:
        questionService.createQuestion(nonExistentId, questionDto)
        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.COURSE_NOT_FOUND
        where:
        nonExistentId << [-1, 0, 200]
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
