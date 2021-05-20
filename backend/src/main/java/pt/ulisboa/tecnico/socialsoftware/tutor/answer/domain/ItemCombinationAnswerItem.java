package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationAnswerItem extends QuestionAnswerItem {
    @OneToMany(mappedBy = "itemId")
    private List<CombinationAnswerItem> connectionsPicked;

    public ItemCombinationAnswerItem() {}

    public ItemCombinationAnswerItem(String username, int quizId, StatementAnswerDto answer, ItemCombinationStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.connectionsPicked = detailsDto.getAnsweredItems()
                .stream().map(CombinationAnswerItem::new).collect(Collectors.toList());
    }

    public List<CombinationAnswerItem> getConnectionsPicked() {
        return connectionsPicked;
    }

    public void setConnectionsPicked(List<CombinationAnswerItem> connectionsPicked) {
        this.connectionsPicked = connectionsPicked;
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return questionDetails.getAnswerRepresentation(connectionsPicked.stream().map(CombinationAnswerItem::getItemId).collect(Collectors.toList()));
    }

}