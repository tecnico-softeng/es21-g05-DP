package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

import javax.persistence.Transient;

public class MultipleChoiceStatementAnswerDetailsDto extends StatementAnswerDetailsDto {
    private List<Integer> optionsIds = new ArrayList<>();

    public MultipleChoiceStatementAnswerDetailsDto() {
    }

    public MultipleChoiceStatementAnswerDetailsDto(MultipleChoiceAnswer questionAnswer) {
        questionAnswer.getOptions().forEach(opt -> this.optionsIds.add(opt.getId()));
    }

    public List<Integer> getOptionsIds() {
        return optionsIds;
    }

    public void setOptionsIds(List<Integer> optionsIds) {
        this.optionsIds = optionsIds;
    }

    @Transient
    private MultipleChoiceAnswer createdMultipleChoiceAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdMultipleChoiceAnswer = new MultipleChoiceAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdMultipleChoiceAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return optionsIds.isEmpty();
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new MultipleChoiceAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(MultipleChoiceQuestion question) {
        createdMultipleChoiceAnswer.setOptions(question, this);
    }

    @Override
    public String toString() {
        return "MultipleChoiceStatementAnswerDto{" +
                "optionsIds=" + optionsIds +
                '}';
    }
}
