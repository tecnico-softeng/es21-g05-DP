package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

public class MultipleChoiceCorrectAnswerDto extends CorrectAnswerDetailsDto {
    private List<Integer> correctOptionsIds = new ArrayList<>();
    private Boolean isOrdered;

    public MultipleChoiceCorrectAnswerDto(MultipleChoiceQuestion question) {
        this.correctOptionsIds = question.getCorrectOptionsIds();
        this.isOrdered = question.getAnswerType() == MultipleChoiceQuestion.AnswerType.ORDER;
    }
    
    public List<Integer> getCorrectOptionsIds() {
        return correctOptionsIds;
    }
    
    public void setCorrectOptionsIds(List<Integer> correctOptionsIds) {
        this.correctOptionsIds = correctOptionsIds;
    }

    public Boolean getIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(Boolean isOrdered) {
        this.isOrdered = isOrdered;
    }
    
    @Override
    public String toString() {
        return "MultipleChoiceCorrectAnswerDto{" +
                "correctOptionsIds=" + correctOptionsIds +
                '}';
    }
}