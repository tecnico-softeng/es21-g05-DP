package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;

public class MultipleChoiceAnswerDto extends AnswerDetailsDto {
    private List<OptionDto> options = new ArrayList<>();

    public MultipleChoiceAnswerDto() {
    }

    public MultipleChoiceAnswerDto(MultipleChoiceAnswer answer) {
        if (answer.getOptions() != null)
            this.options = answer.getOptions().stream().map(OptionDto::new).collect(Collectors.toList());
    }

    public List<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {
        this.options = options;
    }
}
