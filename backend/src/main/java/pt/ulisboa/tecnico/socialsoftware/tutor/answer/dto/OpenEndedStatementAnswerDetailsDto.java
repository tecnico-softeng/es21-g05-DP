package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenEndedAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;


import javax.persistence.Transient;

public class OpenEndedStatementAnswerDetailsDto extends StatementAnswerDetailsDto{
    private String openAnswer;

    public OpenEndedStatementAnswerDetailsDto(){
    }

    public OpenEndedStatementAnswerDetailsDto(OpenEndedAnswer questionAnswer){
        if(questionAnswer.getOpenAnswer() != null && !questionAnswer.getOpenAnswer().isBlank()) {
            this.openAnswer = questionAnswer.getOpenAnswer();
        }

    }

    public void setOpenAnswer(String openAnswer){
        this.openAnswer = openAnswer;
    }

    public String getOpenAnswer(){
        return this.openAnswer;
    }

    @Transient
    private OpenEndedAnswer createdOpenEndedAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdOpenEndedAnswer = new OpenEndedAnswer(questionAnswer, this.openAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdOpenEndedAnswer;
    }

    public void update() {
        createdOpenEndedAnswer.setOpenAnswer(this.openAnswer);
    }

    @Override
    public boolean emptyAnswer() {
        return (this.openAnswer == null);
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new OpenEndedAnswerItem(username, quizId,statementAnswerDto, this);
    }
}
