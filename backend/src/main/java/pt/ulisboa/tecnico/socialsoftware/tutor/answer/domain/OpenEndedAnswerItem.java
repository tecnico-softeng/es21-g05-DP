package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ENDED_QUESTION)
public class OpenEndedAnswerItem extends QuestionAnswerItem {

    private String openAnswer;

    public OpenEndedAnswerItem() {
    }

    public OpenEndedAnswerItem(String username, int quizId, StatementAnswerDto answer, OpenEndedStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.openAnswer = detailsDto.getOpenAnswer();
    }

    public String getOpenAnswer() {
        return this.openAnswer;
    }

    public void setOpenAnswer(String openAnswer) {
        this.openAnswer = openAnswer;
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.openAnswer;
    }
}