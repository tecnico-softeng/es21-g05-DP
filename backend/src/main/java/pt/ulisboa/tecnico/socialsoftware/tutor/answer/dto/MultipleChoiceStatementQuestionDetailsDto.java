package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<StatementOptionDto> options;
    private String answerType;

    public MultipleChoiceStatementQuestionDetailsDto(MultipleChoiceQuestion question) {
        this.options = question.getOptions().stream()
                .map(StatementOptionDto::new)
                .collect(Collectors.toList());
        this.answerType = question.getAnswerType().name();
    }

    public List<StatementOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<StatementOptionDto> options) {
        this.options = options;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    @Override
    public String toString() {
        return "MultipleChoiceStatementQuestionDetailsDto{" +
                "options=" + options +
                "answerType=" + answerType +
                '}';
    }
}