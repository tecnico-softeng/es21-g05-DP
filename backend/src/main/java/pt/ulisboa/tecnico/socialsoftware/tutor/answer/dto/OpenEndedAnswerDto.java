package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswer;


public class OpenEndedAnswerDto extends AnswerDetailsDto {
    private String openAnswer;

    public OpenEndedAnswerDto() {
    }

    public OpenEndedAnswerDto(OpenEndedAnswer answer) {
        if (answer.getOpenAnswer() != null)
            this.openAnswer = answer.getOpenAnswer();
    }

    public String getOpenAnswer() {
        return this.openAnswer;
    }

    public void setOpenAnswer(String openAnswer) {
        this.openAnswer = openAnswer;
    }
}
