package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDto;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class CombinationAnswerItem{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ElementCollection
    private Set<Integer> conns = new HashSet<>();

    @Column(name = "left_item")
    private Integer leftItem;

    public CombinationAnswerItem() {}

    public CombinationAnswerItem(ItemStatementAnswerDto itemStatementAnswerDto) {
        itemId = itemStatementAnswerDto.getItemId();
        leftItem = itemStatementAnswerDto.getLeftItem();
        List<Integer> connections= itemStatementAnswerDto.getConnections();
        conns = new HashSet<>(connections);
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getLeftItem() {
        return leftItem;
    }

    public void setLeftItem(Integer leftItem) {
        this.leftItem = leftItem;
    }

    public Set<Integer> getConns() {
        return conns;
    }

    public void setConns(Set<Integer> conns) {
        this.conns = conns;
    }
}