package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

import java.lang.reflect.Array
import java.util.stream.Collectors

@DataJpaTest
class ConcludeItemCombinationQuizTest extends SpockTest {
    def user
    def quizQuestion
    def item1L
    def item2L
    def item1R
    def item2R
    def quizAnswer
    def date
    def quiz

    def setup() {
        createExternalCourseAndExecution()
        given: "a user"
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: "a quiz"
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        and: "a question"
        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)

        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        and: "4 items"
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

        and: "a date"
        date = DateHandler.now()

        and: "a answer"
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'conclude quiz with one-to-one answer, before conclusionDate'() {
        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        and: 'an quiz statement'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        and: 'a answer statement'
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()
        and: 'connections'
        def item1LAnswerDto = new ItemStatementAnswerDto()
        item1LAnswerDto.setItemId(item1L.getId())
        item1LAnswerDto.setLeftItem(1)
        item1LAnswerDto.setConnections([3].asList())
        itemsAnswered.add(item1LAnswerDto)
        def item2LAnswerDto = new ItemStatementAnswerDto()
        item2LAnswerDto.setItemId(item2L.getId())
        item2LAnswerDto.setLeftItem(2)
        item2LAnswerDto.setConnections([4].asList())
        itemsAnswered.add(item2LAnswerDto)

        and: 'a item combination answer'
        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)


        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)

        Set<CombinationAnswer> setAnswers = ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()

        for(CombinationAnswer answer: setAnswers) {
            answer.getConns().size()==1
        }
        setAnswers.size() == 2

        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(0)
                .getCorrectConnections().get(0) == item1L.getConnections().get(0).getSequence()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(1)
                .getCorrectConnections().get(0) == item2L.getConnections().get(0).getSequence()

        var l1 = item1L.getItemAnswers().toArray();
        (((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()).contains(l1[0])
        var l2 = item2L.getItemAnswers().toArray();
        (((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()).contains(l2[0])

    }

    def 'conclude quiz with one-to-none answer, before conclusionDate'() {
        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        and: 'an quiz statement'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        and: 'a answer statement'
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()
        and: 'connections'
        def item1LAnswerDto = new ItemStatementAnswerDto()
        item1LAnswerDto.setItemId(item1L.getId())
        item1LAnswerDto.setLeftItem(1)
        item1LAnswerDto.setConnections([3].asList())
        itemsAnswered.add(item1LAnswerDto)
        def item2LAnswerDto = new ItemStatementAnswerDto()
        item2LAnswerDto.setItemId(item2L.getId())
        item2LAnswerDto.setLeftItem(2)
        itemsAnswered.add(item2LAnswerDto)

        and: 'a item combination answer'
        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)


        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)

        Set<CombinationAnswer> setAnswers = ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()
        List<List<CombinationItem>> conns = new ArrayList<>();
        for(CombinationAnswer answer: setAnswers) {
            conns.add(answer.getConns());
        }
        conns.get(0).size()==1 || conns.get(0).size()==0
        conns.get(1).size()==0 || conns.get(1).size()==1

        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(0)
                .getCorrectConnections().get(0) == item1L.getConnections().get(0).getSequence()

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(1)
                .getCorrectConnections().get(0) == item2L.getConnections().get(0).getSequence()

        var l1 = item1L.getItemAnswers().toArray();
        (((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()).contains(l1[0])
    }


    def 'conclude quiz with one-to-many answer, before conclusionDate'() {
        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        and: 'an quiz statement'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        and: 'a answer statement'
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()
        and: 'connections'
        def item1LAnswerDto = new ItemStatementAnswerDto()
        item1LAnswerDto.setItemId(item1L.getId())
        item1LAnswerDto.setLeftItem(1)
        item1LAnswerDto.setConnections([3,4].asList())
        itemsAnswered.add(item1LAnswerDto)
        def item2LAnswerDto = new ItemStatementAnswerDto()
        item2LAnswerDto.setItemId(item2L.getId())
        item2LAnswerDto.setLeftItem(2)
        item2LAnswerDto.setConnections([4].asList())
        itemsAnswered.add(item2LAnswerDto)

        and: 'a item combination answer'
        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)
        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)

        Set<CombinationAnswer> setAnswers = ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()
        List<List<CombinationItem>> conns = new ArrayList<>();
        for(CombinationAnswer answer: setAnswers) {
            conns.add(answer.getConns());
        }
        conns.get(0).size()==2 || conns.get(0).size()==1
        conns.get(1).size()==1 || conns.get(1).size()==2

        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(0)
                .getCorrectConnections().get(0) == item1L.getConnections().get(0).getSequence()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems().get(1)
                .getCorrectConnections().get(0) == item2L.getConnections().get(0).getSequence()

        var l1 = item1L.getItemAnswers().toArray();
        (((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()).contains(l1[0])
        var l2 = item2L.getItemAnswers().toArray();
        (((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getItemsConnections()).contains(l2[0])
    }

    def 'conclude item combination completed quiz'() {
        given:  'a completed quiz'
        quizAnswer.completed = true
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        and: 'a answer statement'
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        List<ItemStatementAnswerDto> itemsAnswered = new ArrayList<>()
        and: 'connections'
        def item1LAnswerDto = new ItemStatementAnswerDto()
        item1LAnswerDto.setItemId(item1L.getId())
        item1LAnswerDto.setLeftItem(1)
        item1LAnswerDto.setConnections([3].asList())
        itemsAnswered.add(item1LAnswerDto)
        def item2LAnswerDto = new ItemStatementAnswerDto()
        item2LAnswerDto.setItemId(item2L.getId())
        item2LAnswerDto.setLeftItem(2)
        itemsAnswered.add(item2LAnswerDto)

        and: 'a item combination answer'
        itemCombinationAnswerDto.setAnsweredItems(itemsAnswered)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'nothing occurs'
        quizAnswer.getAnswerDate() == null
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getAnswerDetails() == null
        and: 'the return value is OK'
        correctAnswers.size() == 0

    }

    def cleanup() {
        combinationItemRepository.deleteAll()
        questionAnswerRepository.deleteAll()
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
