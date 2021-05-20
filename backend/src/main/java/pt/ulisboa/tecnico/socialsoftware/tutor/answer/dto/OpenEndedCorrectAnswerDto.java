package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion;

public class OpenEndedCorrectAnswerDto extends CorrectAnswerDetailsDto {
    private String regex;
    private String criteria;

    public OpenEndedCorrectAnswerDto(OpenEndedQuestion question) {
        this.regex = question.getRegexQuestion();
        this.criteria = question.getCriteria();
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getCriteria() { return this.criteria; }

    public void setCriteria(String criteria) { this.criteria = criteria; }

    @Override
    public String toString() {
        return "OpenEndedCorrectAnswerDto{" +
                "regex=" + regex + ", criteria=" + criteria +
                '}';
    }
}