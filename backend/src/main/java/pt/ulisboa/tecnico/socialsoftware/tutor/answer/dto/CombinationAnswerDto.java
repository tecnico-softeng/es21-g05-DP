package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombinationAnswerDto implements Serializable {
    Integer id;
    Integer itemId;
    private List<Integer> conns = new ArrayList<>();

    public CombinationAnswerDto(CombinationAnswer combinationAnswer) {
        this.id = combinationAnswer.getId();
        this.itemId = combinationAnswer.getItemId();
        List<CombinationItem> items = combinationAnswer.getConns();
        if(combinationAnswer.getConns()!= null) {
            this.conns =items.stream().map(CombinationItem::getSequence).collect(Collectors.toList());
        }
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public List<Integer> getConns() {
        return conns;
    }

    public void setConns(List<Integer> conns) {
        this.conns = conns;
    }
}