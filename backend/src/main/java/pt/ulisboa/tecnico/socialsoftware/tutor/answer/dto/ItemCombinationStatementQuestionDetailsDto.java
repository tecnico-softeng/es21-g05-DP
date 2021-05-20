package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<StatementItemDto> items;

    public ItemCombinationStatementQuestionDetailsDto(ItemCombinationQuestion question) {
        if (question.getItems() != null) {
        this.items = question.getItems().stream().map(StatementItemDto::new).collect(Collectors.toList());
        }
    }

    public List<StatementItemDto> getItems() {
        return items;
    }

    public void setItems(List<StatementItemDto> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementQuestionDetailsDto{" +
                "items=" + items +
                '}';
    }
}