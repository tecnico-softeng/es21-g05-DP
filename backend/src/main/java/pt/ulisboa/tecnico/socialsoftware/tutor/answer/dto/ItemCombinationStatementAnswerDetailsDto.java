package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationStatementAnswerDetailsDto extends StatementAnswerDetailsDto {
    private List<ItemStatementAnswerDto> answeredItems = new ArrayList<>();

    public ItemCombinationStatementAnswerDetailsDto() {
    }

    public ItemCombinationStatementAnswerDetailsDto(ItemCombinationAnswer questionAnswer) {
        if (questionAnswer.getItemsConnections() != null) {
            this.answeredItems = questionAnswer.getItemsConnections().stream().map(ItemStatementAnswerDto::new)
                    .collect(Collectors.toList());
        }
    }

    public List<ItemStatementAnswerDto> getAnsweredItems() {
        return answeredItems;
    }

    public void setAnsweredItems(List<ItemStatementAnswerDto> answeredItems) {
        this.answeredItems = answeredItems;
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementAnswerDetailsDto{" + "answeredItems=" + answeredItems + '}';
    }

    @Transient
    private ItemCombinationAnswer itemCombinationAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        itemCombinationAnswer = new ItemCombinationAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return itemCombinationAnswer;
    }

    @Override
    public void update(ItemCombinationQuestion itemCombinationQuestion) {
        itemCombinationAnswer.setItemsConnections(itemCombinationQuestion, this);
    }

    @Override
    public boolean emptyAnswer() {
        return answeredItems == null || answeredItems.isEmpty();
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId,
            StatementAnswerDto statementAnswerDto) {
        return new ItemCombinationAnswerItem(username, quizId, statementAnswerDto, this);
    }

}