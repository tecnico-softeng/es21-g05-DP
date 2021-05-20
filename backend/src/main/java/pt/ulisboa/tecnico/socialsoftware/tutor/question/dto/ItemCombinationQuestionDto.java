package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationQuestionDto extends QuestionDetailsDto {
    private List<CombinationItemDto> items = new ArrayList<>();

    public ItemCombinationQuestionDto() {
    }

    public ItemCombinationQuestionDto(ItemCombinationQuestion question) {
        this.items = question.getItems().stream().map(CombinationItemDto::new).collect(Collectors.toList());
        connectItems(question.getItems());
    }

    public void connectItems(List<CombinationItem> items) {
        int index = 0;
        for (CombinationItemDto item : this.items) {
            item.connectDtos(items.get(index++));
        }
    }
    public List<CombinationItemDto> getItems() {
        return this.items;
    }

    public void setItems(List<CombinationItemDto> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ItemCombinationQuestionDto{" +
                "items='" + this.items +
                '}';
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new ItemCombinationQuestion(question,this);
    }

    @Override
    public void update(ItemCombinationQuestion question) {
        question.update(this);
    }
}
