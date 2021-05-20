package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.CombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto;

@Entity
@Table(name = "combination_item")
public class CombinationItem implements DomainEntity {

    public enum Group {
        LEFT, RIGHT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sequence")
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "itemGroup")
    private Group group;

    @ManyToMany
    @JoinTable(name = "connections", joinColumns = @JoinColumn(name = "connections_id"), inverseJoinColumns = @JoinColumn(name = "connection_of_id"))
    private List<CombinationItem> connections = new ArrayList<>();

    @ManyToMany(mappedBy = "connections")
    private List<CombinationItem> connectionOf = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemCombinationQuestion questionDetails;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "conns")
    private final Set<CombinationAnswer> itemAnswers = new HashSet<>();

    public CombinationItem() {
    }

    public CombinationItem(CombinationItemDto combinationItemDto) {
        this.sequence = combinationItemDto.getSequence();
        this.content = combinationItemDto.getContent();
        this.group = Group.valueOf(combinationItemDto.getGroup());
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

    public List<CombinationItem> getConnections() {
        return connections;
    }

    public void setConnections(List<CombinationItem> connections) {
        this.connections = connections;
    }

    public List<CombinationItem> getConnectionOf() {
        return connectionOf;
    }

    public void setConnectionOf(List<CombinationItem> connectionOf) {
        this.connectionOf = connectionOf;
    }

    public void addConnection(CombinationItem connection) {
        this.connections.add(connection);
    }

    public void addConnectionOf(CombinationItem connection) {
        this.connectionOf.add(connection);
    }

    public void removeConnection(CombinationItem connection) {
        this.connections.remove(connection);
    }

    public void removeConnectionOf(CombinationItem connection) {
        this.connectionOf.remove(connection);
    }

    public String getContent() {
        return "null";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ItemCombinationQuestion getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(ItemCombinationQuestion question) {
        this.questionDetails = question;
        question.addItem(this);
    }

    public Set<CombinationAnswer> getItemAnswers() {
        return itemAnswers;
    }

    public void addItemAnswer(CombinationAnswer itemAnswer) {
        itemAnswers.add(itemAnswer);
    }

    public List<Integer> getCorrectConnections() {
        List<Integer> rightConnections = new ArrayList<>();
        this.connections.forEach(item -> rightConnections.add(item.sequence));
        return rightConnections;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void remove() {
        this.questionDetails = null;
        this.connections.clear();
        this.group = null;
        this.itemAnswers.clear();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCombinationItem(this);
    }

    @Override
    public String toString() {
        return "CombinationItem{" + "id=" + id + ", sequence=" + sequence + ", content='" + content + '\'' + ", Group="
                + group + ", Connections=" + getCorrectConnections() + ", item answers=" + itemAnswers + '}';
    }

}
