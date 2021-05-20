package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


public class StatementItemDto implements Serializable {
    private Integer itemId;
    private String content;
    private Integer sequence;
    private String group;
    private List<Integer> connections = new ArrayList<>();

    public StatementItemDto(CombinationItem item) {
        this.itemId= item.getId();
        this.content = item.getContent();
        this.sequence= item.getSequence();
        this.group = item.getGroup().toString();
        if(item.getConnections()!= null){
            item.getConnections().forEach(conn -> this.connections.add(conn.getSequence()));
        }
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<Integer> getConnections() {
        return connections;
    }

    public void setConnections(List<Integer> connections) {
        this.connections = connections;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StatementItemDto{" +
                "itemId=" + itemId +
                ", content='" + content + '\'' +
                ", sequence=" + sequence +
                ", group=" + group +
                ", connections=" + connections +
                '}';
    }
}