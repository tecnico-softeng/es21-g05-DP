package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.CombinationItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ItemCombinationAnswerDto extends AnswerDetailsDto {
    private List<CombinationAnswerDto> connections = new ArrayList<>();


    public ItemCombinationAnswerDto() {
    }

    public ItemCombinationAnswerDto(ItemCombinationAnswer answer) {
        if(answer.getItemsConnections() != null) {
            this.connections = answer.getItemsConnections().stream().map(CombinationAnswerDto::new).collect(Collectors.toList());
        }
    }

    public List<CombinationAnswerDto> getConnections() {
        return connections;
    }

    public void setConnections(List<CombinationAnswerDto> conns) {
        this.connections = conns;
    }
}