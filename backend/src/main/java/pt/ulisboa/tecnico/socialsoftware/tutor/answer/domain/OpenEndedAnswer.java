package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.regex.Pattern;


@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ENDED_QUESTION)
public class OpenEndedAnswer extends AnswerDetails{

    @Column(columnDefinition = "TEXT")
    private String openAnswer;

    public OpenEndedAnswer() { super();}

    public OpenEndedAnswer(QuestionAnswer questionAnswer) {
        super(questionAnswer);
    }

    public OpenEndedAnswer(QuestionAnswer questionAnswer, String openAnswer){
        super(questionAnswer);
        this.setOpenAnswer(openAnswer);
    }

    public String getOpenAnswer(){
        return this.openAnswer;
    }

    public void setOpenAnswer(String openAnswer) {
        this.openAnswer = openAnswer;
    }

    @Override
    public boolean isCorrect() {
        var questionDetails = getQuestionAnswer().getQuizQuestion().getQuestion().getQuestionDetails();
        String regex = questionDetails.getCorrectAnswerRepresentation();
        if(regex == null || this.openAnswer == null) return false;
        var pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(this.openAnswer);
        return matcher.find();
    }

    @Override
    public void remove() {
        if(this.openAnswer != null && !this.openAnswer.isBlank())
            this.openAnswer = null;
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() { return new OpenEndedAnswerDto(this); }

    @Override
    public String getAnswerRepresentation() {
        return this.openAnswer;
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new OpenEndedStatementAnswerDetailsDto(this);
    }

    @Override
    public boolean isAnswered() { return this.openAnswer != null && !this.openAnswer.isBlank(); }

    @Override
    public void accept(Visitor visitor) { visitor.visitAnswerDetails(this); }
}
