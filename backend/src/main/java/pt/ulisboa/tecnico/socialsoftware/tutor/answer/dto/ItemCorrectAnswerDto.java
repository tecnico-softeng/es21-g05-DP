package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ItemCorrectAnswerDto implements Serializable {
    private Integer sequence;
    private List<Integer> correctConnections = new ArrayList<>();

    public ItemCorrectAnswerDto(CombinationItem item) {
        this.sequence = item.getSequence();
        item.getConnections().forEach(comb -> this.correctConnections.add(comb.getSequence()));
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public List<Integer> getCorrectConnections() {
        return correctConnections;
    }

    public void setCorrectConnections(List<Integer> connections) {
        this.correctConnections = connections;
    }
}