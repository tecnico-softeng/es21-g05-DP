package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ItemCombinationCorrectAnswerDto extends CorrectAnswerDetailsDto{
    private List<ItemCorrectAnswerDto> correctItems = new ArrayList<>();

    public ItemCombinationCorrectAnswerDto(ItemCombinationQuestion question) {
        this.correctItems = question.getItems().stream().map(ItemCorrectAnswerDto::new).collect(Collectors.toList());
    }

    public List<ItemCorrectAnswerDto> getCorrectItems() {
        return correctItems;
    }

    public void setCorrectItems(List<ItemCorrectAnswerDto> correctItems) {
        this.correctItems = correctItems;
    }
}