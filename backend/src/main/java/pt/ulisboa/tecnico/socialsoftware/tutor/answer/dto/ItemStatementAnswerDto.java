package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemStatementAnswerDto implements Serializable {
    private Integer leftItem;
    private Integer itemId;
    private List<Integer> connections = new ArrayList<>();

    public ItemStatementAnswerDto() {
    }

    public ItemStatementAnswerDto(CombinationAnswer item) {
        if(item.getConns()!= null) {
            List<CombinationItem> items = item.getConns();
            this.connections = items.stream().map(CombinationItem::getSequence).collect(Collectors.toList());
        }
        setLeftItem(item.getItemId());
    }

    public Integer getLeftItem() {
        return leftItem;
    }

    public void setLeftItem(Integer leftItem) {
        this.leftItem = leftItem;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public List<Integer> getConnections() {
        return connections;
    }

    public void setConnections(List<Integer> connections) {
        this.connections = connections;
    }

    public void addConnection(Integer conn) {
        this.connections.add(conn);
    }

    public String toString() {
        return "ItemStatementAnswerDetailsDto{" +
                "leftItem=" + leftItem +
                ", connections=" + connections +
                '}';
    }
}