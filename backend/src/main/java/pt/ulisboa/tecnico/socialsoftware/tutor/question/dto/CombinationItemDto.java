package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombinationItemDto implements Serializable {
    private Integer id;
    private Integer sequence;
    private List<CombinationItemDto> connections = new ArrayList<>();
    private List<CombinationItemDto> connectionOf =new ArrayList<>();
    private String content;
    private String group;

    public CombinationItemDto() {
    }

    public CombinationItemDto(CombinationItem combinationItem) {
        this.id = combinationItem.getId();
        this.sequence = combinationItem.getSequence();
        this.content = combinationItem.getContent();
        this.group = combinationItem.getGroup().name();
    }

    public void connectDtos(CombinationItem item) {
        this.connections = item.getConnections().stream().map(CombinationItemDto::new).collect(Collectors.toList());
        this.connectionOf = item.getConnectionOf().stream().map(CombinationItemDto::new).collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<CombinationItemDto> getConnections() {
        return connections;
    }

    public void setConnections(List<CombinationItemDto> connections) {
        this.connections = connections;
    }

    public List<CombinationItemDto> getConnectionOf() {
        return connectionOf;
    }

    public void setConnectionOf(List<CombinationItemDto> connectionOf) {
        this.connectionOf = connectionOf;
    }

    public void addConnection(CombinationItemDto connection) {
        this.connections.add(connection);
    }

    public void addConnectionOf(CombinationItemDto connection) {
        this.connectionOf.add(connection);
    }

    public void removeConnection(CombinationItemDto connection) { this.connections.remove(connection); }

    public void removeConnectionOf(CombinationItemDto connection) { this.connectionOf.remove(connection); }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "CombinationItemDto{" +
                "id=" + id +
                ", sequence=" + id +
                ", content='" + content + '\'' +
                ", Group=" + group +
                '}';
    }

}
