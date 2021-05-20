package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto

@DataJpaTest
class RemoveItemCombinationQuestionTest extends SpockTest {
    def question
    def item1L
    def item2L
    def item1R
    def item2R

    def setup(){
        given: 'a new item combination test'
        createExternalCourseAndExecution()
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: '4 items'
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

    }

    def "remove a item Combination question"() {
        when:
        questionService.removeQuestion(question.getId())

        then: "the question is removed"
        questionRepository.count() == 0L
        combinationItemRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }

    def "remove a item combination question that has topics"() {
        given: 'a question with topics'
        def topicDto = new TopicDto()
        topicDto.setName("name1")
        def topicOne = new Topic(externalCourse, topicDto)
        topicDto.setName("name2")
        def topicTwo = new Topic(externalCourse, topicDto)
        question.getTopics().add(topicOne)
        topicOne.getQuestions().add(question)
        question.getTopics().add(topicTwo)
        topicTwo.getQuestions().add(question)
        topicRepository.save(topicOne)
        topicRepository.save(topicTwo)

        when:
        questionService.removeQuestion(question.getId())

        then:
        questionRepository.count() == 0L
        combinationItemRepository.count() == 0L
        topicRepository.count() == 2L
        topicOne.getQuestions().size() == 0
        topicTwo.getQuestions().size() == 0
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
