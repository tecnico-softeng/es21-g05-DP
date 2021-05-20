package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<CombinationItem> items = new ArrayList<>();

    public ItemCombinationQuestion() {
        super();
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new ItemCombinationCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new ItemCombinationStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new ItemCombinationAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new ItemCombinationQuestionDto(this);
    }

    public ItemCombinationQuestion(Question question, ItemCombinationQuestionDto questionDto) {
        super(question);
        update(questionDto);
    }

    public List<CombinationItem> getItems() {
        return this.items;
    }

    private void checkRules(List<CombinationItemDto> items) {
        List<CombinationItemDto> leftItems = getItemsByGroup(items, CombinationItem.Group.LEFT.name());
        List<CombinationItemDto> rightItems = getItemsByGroup(items, CombinationItem.Group.RIGHT.name());

        if (leftItems.size() < 2 || rightItems.size() < 2) {
            throw new TutorException(AT_LEAST_TWO_OPTIONS_PER_GROUP);
        }
        checkSameGroupConnections(leftItems);
    }

    private List<CombinationItemDto> getItemsByGroup(List<CombinationItemDto> items, String group) {
        return items.stream().filter(i -> i.getGroup().equals(group)).collect(Collectors.toList());
    }

    private void checkSameGroupConnections(List<CombinationItemDto> items) {
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (items.get(i).getConnections().contains(items.get(j)))
                    throw new TutorException(SAME_GROUP_CONNECTION);
            }
        }
    }

    public void setItems(List<CombinationItemDto> items) {
        checkRules(items);
        this.items.forEach(CombinationItem::remove);
        this.items.clear();
        for (CombinationItemDto itemDto : items) {
            new CombinationItem(itemDto).setQuestionDetails(this);
        }
        connectItems(items);
    }
    
    public void connectItems(List<CombinationItemDto> itemsDto) {
        int index = 0;
        for (CombinationItemDto itemDto : itemsDto) {
            CombinationItem comb = this.items.get(index++);
            connect(itemDto, comb);
        }
    }

    private void connect(CombinationItemDto itemDto, CombinationItem comb) {
        for (CombinationItemDto i : itemDto.getConnections()) {
            CombinationItem ci = this.items.stream().filter(item -> item.getSequence() == i.getSequence()).findAny().get();
            comb.addConnection(ci);
        }
    }

    public void addItem(CombinationItem item) {
       this.items.add(item); 
    } 

    @Override
    public void delete() {
        super.delete();
        this.items.forEach(CombinationItem::remove);
        this.items.clear();
    }

    public void update(ItemCombinationQuestionDto questionDetails) {
        setItems(questionDetails.getItems());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        StringBuilder correctAnswer = new StringBuilder();
        for(CombinationItem item : this.items) {
            correctAnswer.append( "Item " + item.getSequence() + "connected to:" + item.getCorrectConnections().toString() + " | ");
        }
        return correctAnswer.toString();
    }

    @Override
    public String getAnswerRepresentation(List<Integer> answeredIds) {
        List<String> result = new ArrayList<>();
        List<CombinationItem> questionItems = getItems().stream().sorted(Comparator.comparing(CombinationItem::getSequence)).collect(Collectors.toList());
        for(CombinationItem item: questionItems) {
            var conn = item.getConnections().stream().filter(x -> answeredIds.contains(x.getSequence())).findAny();
            if(conn.isPresent()) {
                String answer = "Item " + item.getSequence() + "connected to:" + conn.toString();
                result.add(answer);
            } else {
                result.add("-");
            }
        }
        return String.join(" | ", result);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitCombinationItems(Visitor visitor) {
        for (var item : this.getItems()) {
            item.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return "ItemCombinationQuestion{" +
                "items=" + items +
                '}';
    }

}
