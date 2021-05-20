package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedCorrectAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenEndedStatementQuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenEndedQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;


import javax.persistence.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CRITERIA;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_REGEX;


@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ENDED_QUESTION)
public class OpenEndedQuestion extends QuestionDetails {

    @Column(columnDefinition = "TEXT")
    private String criteria;

    @Column(columnDefinition = "TEXT")
    private String regexQuestion;

    public OpenEndedQuestion() {
        super();
    }

    public OpenEndedQuestion(Question question, OpenEndedQuestionDto openEndedQuestionDto) {
        super(question);
        update(openEndedQuestionDto);
    }

    public String getCriteria(){return "";}

    public String getRegexQuestion(){return regexQuestion;}

    public void setCriteria(String criteria) {
        if(criteria == null || criteria.isBlank())
            throw new TutorException(INVALID_CRITERIA);
        this.criteria = criteria;
    }

    public void setRegexQuestion(String regexQuestion) {
        if(regexQuestion == null || regexQuestion.isBlank())
            throw new TutorException(INVALID_REGEX);
        validateRegex(regexQuestion);
        this.regexQuestion = regexQuestion;
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new OpenEndedCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new OpenEndedStatementQuestionDetailsDto();
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new OpenEndedStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new OpenEndedAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new OpenEndedQuestionDto(this);
    }

    @Override
    public void delete() {
        this.criteria = null;
        this.regexQuestion = null;
        super.delete();
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return this.regexQuestion;
    }

    public void validateRegex (String regex) {

        try{
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception){
            throw new TutorException(INVALID_REGEX);
        }
    }

    public void update(OpenEndedQuestionDto questionDetails) {
        if(questionDetails.getCriteria() == null || questionDetails.getCriteria().isBlank())
            throw new TutorException(INVALID_CRITERIA);

        if(questionDetails.getRegexQuestion() == null || questionDetails.getRegexQuestion().isBlank())
            throw new TutorException(INVALID_REGEX);

        validateRegex(questionDetails.getRegexQuestion());

        this.criteria = questionDetails.getCriteria();
        this.regexQuestion = questionDetails.getRegexQuestion();
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }


    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return null;
    }
}
