package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class ImportExportItemCombinationQuestionsTest extends SpockTest{

    def questionId
    def item1LDto
    def item2LDto
    def item1RDto
    def item2RDto

    def setup(){
        given: "an external course execution"
        createExternalCourseAndExecution()

        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "the left group of items"
        def items = new ArrayList<CombinationItemDto>()
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
        connectItemDtos(item2LDto, item1RDto)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export and import item combination questions to xml'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def items = questionResult.getQuestionDetailsDto().getItems()
        countGroupItems(items,"LEFT") == 2
        countGroupItems(items, "RIGHT") == 2

        def resItemL1 = questionResult.getQuestionDetailsDto().getItems().get(0)
        resItemL1.getContent() == OPTION_1_CONTENT
        resItemL1.getConnections().get(0).getSequence() == item2RDto.getSequence()
        resItemL1.getGroup() == CombinationItem.Group.LEFT.name()

        def resItemL2 = questionResult.getQuestionDetailsDto().getItems().get(1)
        resItemL2.getContent() == OPTION_2_CONTENT
        resItemL2.getConnections().get(0).getSequence() == item1RDto.getSequence()
        resItemL2.getGroup() == CombinationItem.Group.LEFT.name()

        def resItemR1 = questionResult.getQuestionDetailsDto().getItems().get(2)
        resItemR1.getContent() == OPTION_1_CONTENT
        resItemR1.getGroup() == CombinationItem.Group.RIGHT.name()

        def resItemR2 = questionResult.getQuestionDetailsDto().getItems().get(3)
        resItemR2.getContent() == OPTION_2_CONTENT
        resItemR2.getGroup() == CombinationItem.Group.RIGHT.name()
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

}
