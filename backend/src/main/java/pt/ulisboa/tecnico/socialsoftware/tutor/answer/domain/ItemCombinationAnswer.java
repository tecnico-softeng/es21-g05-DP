package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_ITEM_MISMATCH;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)

public class ItemCombinationAnswer extends AnswerDetails {
    @OneToMany(mappedBy = "itemCombinationAnswer",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CombinationAnswer> itemsConnections = new ArrayList<>();

    public ItemCombinationAnswer() {
        super();
    }

    public ItemCombinationAnswer(QuestionAnswer questionAnswer) {
        super(questionAnswer);
    }

    public ItemCombinationAnswer(QuestionAnswer questionAnswer, List<CombinationAnswer> itemsConnections) {
        super(questionAnswer);
        this.setItemsConnections(itemsConnections);
    }

    public List<CombinationAnswer> getItemsConnections() {
        return itemsConnections;
    }

    public void setItemsConnections(List<CombinationAnswer> connections) {
        this.itemsConnections = connections;
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new ItemCombinationAnswerDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto(this);
    }

    @Override
    public boolean isAnswered() {
        return itemsConnections != null && !itemsConnections.isEmpty();
    }

    @Override
    public String getAnswerRepresentation() {
        List<String> answers = new ArrayList<>();
        for(CombinationAnswer answer: this.getItemsConnections()) {
            answers.add(answer.answerRepresentation());
        }
        return String.join(" | ", answers);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }

    @Override
    public void remove() {
        if (itemsConnections != null) {
            itemsConnections.forEach(CombinationAnswer::remove);
            itemsConnections.clear();
        }
    }

    public void setItemsConnections(ItemCombinationQuestion question,
            ItemCombinationStatementAnswerDetailsDto itemCombinationStatementAnswerDetailsDto) {
        itemsConnections.clear();
        if (!itemCombinationStatementAnswerDetailsDto.emptyAnswer()) {
            for (ItemStatementAnswerDto item : itemCombinationStatementAnswerDetailsDto.getAnsweredItems()) {

                List<CombinationItem> connectedItems = getConnectedItemsBySequence(question, item);

                List<CombinationItem> answers = getConnectedItemsById(question, item, connectedItems);

                CombinationItem left = question.getItems().stream()
                        .filter(c -> c.getSequence().equals(item.getLeftItem())).findAny()
                        .orElseThrow(() -> new TutorException(QUESTION_ITEM_MISMATCH, item.getItemId()));

                CombinationAnswer combAnswer = new CombinationAnswer(answers, this, left.getId());
                getItemsConnections().add(combAnswer);
                left.addItemAnswer(combAnswer);
            }
        }
    }

    private List<CombinationItem> getConnectedItemsById(ItemCombinationQuestion question, ItemStatementAnswerDto item,
            List<CombinationItem> connectedItems) {
        List<CombinationItem> answers = new ArrayList<>();
        for (CombinationItem conn : connectedItems) {
            CombinationItem combinationItem = question.getItems().stream().filter(c1 -> c1.getId().equals(conn.getId()))
                    .findAny().orElseThrow(() -> new TutorException(QUESTION_ITEM_MISMATCH, item.getItemId()));
            answers.add(combinationItem);
        }
        return answers;
    }

    private List<CombinationItem> getConnectedItemsBySequence(ItemCombinationQuestion question,
            ItemStatementAnswerDto item) {
        List<CombinationItem> conns = new ArrayList<>();
        for (Integer c : item.getConnections()) {
            CombinationItem i = question.getItems().stream().filter(c1 -> c1.getSequence() == c).findAny()
                    .orElseThrow(() -> new TutorException(QUESTION_ITEM_MISMATCH, item.getItemId()));
            conns.add(i);
        }
        return conns;
    }

    @Override
    public boolean isCorrect() {
        List<List<Integer>> correctConns = new ArrayList<>();
        List<List<Integer>> answerConns = new ArrayList<>();
        if (!this.getItemsConnections().isEmpty()) {
            setResponses(correctConns, answerConns);
            List<Boolean> results = new ArrayList<>();
            checkResponses(correctConns, answerConns, results);
            return results.stream().allMatch(r -> r);
        }
        return false;
    }

    private void checkResponses(List<List<Integer>> correctConns, List<List<Integer>> answerConns, List<Boolean> results) {

        correctConns.forEach(correctConn -> {
            answerConns.forEach(answerConn -> {
                results.add(CollectionUtils.isEqualCollection(correctConn,answerConn));
            });
        });
    }

    private void setResponses(List<List<Integer>> correctConns, List<List<Integer>> answerConns) {
        ItemCombinationQuestion question = ((ItemCombinationQuestion) getQuestionAnswer().getQuestion()
                .getQuestionDetails());
        question.getItems().forEach(item -> correctConns.add(item.getCorrectConnections()));
        this.getItemsConnections().forEach(answer -> answerConns
                .add(answer.getConns().stream().map(CombinationItem::getSequence).collect(Collectors.toList())));
    }
}
