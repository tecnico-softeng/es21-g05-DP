package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenEndedQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;


public class OpenEndedQuestionDto extends QuestionDetailsDto {

    private String criteria;

    private String regexQuestion;


    public OpenEndedQuestionDto() {
    }

    public OpenEndedQuestionDto(OpenEndedQuestion question) {
        criteria = question.getCriteria();
        regexQuestion = question.getRegexQuestion();
    }

    public String getCriteria() {return criteria;}

    public String getRegexQuestion() {return regexQuestion;}

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public void setRegexQuestion(String regexQuestion) {
        this.regexQuestion = regexQuestion;
    }

    @Override
    public String toString() {
        return "OpenEndedQuestionDto{" +
                "criteria=" + criteria +
                "regex=" + regexQuestion +
                '}';
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new OpenEndedQuestion(question, this);
    }

    @Override
    public void update(OpenEndedQuestion question) {
        question.update(this);
    }
}
