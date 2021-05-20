package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateItemCombinationQuestion extends SpockTest {
    def question
    def items
    def questionDto
    def questionDetails
    def item1LDto
    def item2LDto
    def item1RDto
    def item2RDto
    def item3RDto

    def setup() {
        given: "create a question"
        createExternalCourseAndExecution()
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: "a question"
        questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        ItemCombinationQuestionDto itemCombinationQuestion = new ItemCombinationQuestionDto();
        questionDto.setQuestionDetailsDto(itemCombinationQuestion)

        and: "the left group of items"
        items = new ArrayList<CombinationItemDto>()
        item1LDto = new CombinationItemDto()
        item1LDto.setContent(OPTION_1_CONTENT)
        item1LDto.setSequence(1)
        item1LDto.setGroup("LEFT")
        items.add(item1LDto)
        item2LDto = new CombinationItemDto()
        item2LDto.setContent(OPTION_2_CONTENT)
        item2LDto.setSequence(2)
        item2LDto.setGroup("LEFT")
        items.add(item2LDto)

        and: "the right group of options"
        item1RDto = new CombinationItemDto()
        item1RDto.setContent(OPTION_1_CONTENT)
        item1RDto.setSequence(3)
        item1RDto.setGroup("RIGHT")
        items.add(item1RDto)
        item2RDto = new CombinationItemDto()
        item2RDto.setContent(OPTION_2_CONTENT)
        item2RDto.setSequence(4)
        item2RDto.setGroup("RIGHT")
        items.add(item2RDto)

        connectItemDtos(item1LDto, item2RDto)
        connectItemDtos(item2LDto,item1RDto)

        questionDto.getQuestionDetailsDto().setItems(items)
    }

    def "update a question"() {
        given: "Added an item and a combination"
        item3RDto = new CombinationItemDto()
        item3RDto.setContent(OPTION_3_CONTENT)
        item3RDto.setSequence(5)
        item3RDto.setGroup("RIGHT")
        items.add(item3RDto)
        connectItemDtos(item2LDto,item3RDto)

        questionDto.getQuestionDetailsDto().setItems(items)

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

        and: 'combinations are changed'
        def resItemL1 = result.getQuestionDetails().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item2RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT

        def resItemL2 = result.getQuestionDetails().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().get(0).getSequence() == item1RDto.getSequence()
        resItemL2.getConnections().get(1).getSequence() == item3RDto.getSequence()
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

    def "update a combination"() {
        given: "Change one combination"
        item2LDto.getConnections().remove(item1RDto)
        item1RDto.getConnections().remove(item2LDto)
        connectItemDtos(item2LDto,item2RDto)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the combination is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'is not changed'
        result.getStatus() == Question.Status.AVAILABLE

        and: 'combinations are changed'
        def resItemL1 = result.getQuestionDetails().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item2RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT

        def resItemL2 = result.getQuestionDetails().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().get(0).getSequence() == item2RDto.getSequence()
        resItemL2.getGroup() == CombinationItem.Group.LEFT

        def resItemR1 = result.getQuestionDetails().getItems().get(2)
        resItemR1.getContent() == OPTION_1_CONTENT
        resItemR1.getGroup() == CombinationItem.Group.RIGHT

        def resItemR2 = result.getQuestionDetails().getItems().get(3)
        resItemR2.getContent() == OPTION_2_CONTENT
        resItemR2.getGroup() == CombinationItem.Group.RIGHT
    }

    def "cannot remove second item"() {
        given: "removed an item and a combination"
        items.remove(item2RDto)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_TWO_OPTIONS_PER_GROUP
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}