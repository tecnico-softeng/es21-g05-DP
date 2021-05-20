package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswerItem extends QuestionAnswerItem {

    @ElementCollection
    private List<Integer> optionsIds = new ArrayList<>();

    public MultipleChoiceAnswerItem() {
    }

    public MultipleChoiceAnswerItem(String username, int quizId, StatementAnswerDto answer, MultipleChoiceStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.optionsIds = detailsDto.getOptionsIds();
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.getOptionsIds().isEmpty() ? "-" : questionDetails.getAnswerRepresentation(optionsIds) ;
    }

    public List<Integer> getOptionsIds() {
        return optionsIds;
    }

    public void setOptionsIds(List<Integer> optionsIds) {
        this.optionsIds = optionsIds;
    }
}
