package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CombinationItem;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class CombinationAnswer {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    private ItemCombinationAnswer itemCombinationAnswer;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<CombinationItem> conns = new ArrayList<>();

    @Column(name = "item_id")
    private Integer itemId;

    public CombinationAnswer() {
    }

    public CombinationAnswer(List<CombinationItem> connects, ItemCombinationAnswer itemCombinationAnswer, Integer itemId) {
        setConns(connects);
        setItemCombinationAnswer(itemCombinationAnswer);
        setItemId(itemId);
    }

    public Integer getId() {
        return id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public ItemCombinationAnswer getItemCombinationAnswer() {
        return itemCombinationAnswer;
    }

    public void setItemCombinationAnswer(ItemCombinationAnswer itemCombinationAnswer) {
        this.itemCombinationAnswer = itemCombinationAnswer;
    }

    public List<CombinationItem> getConns() {
        return conns;
    }

    public void setConns(List<CombinationItem> conns) {
        this.conns = conns;
    }

    public void remove() {
        this.conns.forEach(CombinationItem::remove);
        this.conns= null;
    }

    public String answerRepresentation() {
        return "Item with id: " + this.getItemId() + " -> " + this.conns;
    }
}
