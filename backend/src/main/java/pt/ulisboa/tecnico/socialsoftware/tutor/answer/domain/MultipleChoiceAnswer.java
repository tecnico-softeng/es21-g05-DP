package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_OPTION_MISMATCH;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswer extends AnswerDetails {

    @ManyToMany
    @JoinTable(name = "options_id")
    private List<Option> options = new ArrayList<>();

    public MultipleChoiceAnswer() {
        super();
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer) {
        super(questionAnswer);
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer, List<Option> options) {
        super(questionAnswer);
        this.setOptions(options);
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
        options.forEach(opt -> opt.addQuestionAnswer(this));
    }

    public void setOptions(MultipleChoiceQuestion question,
            MultipleChoiceStatementAnswerDetailsDto multipleChoiceStatementAnswerDetailsDto) {

        if (!multipleChoiceStatementAnswerDetailsDto.emptyAnswer()) {
            List<Option> opts = question.getOptions().stream().filter(
                    option1 -> multipleChoiceStatementAnswerDetailsDto.getOptionsIds().contains(option1.getId()))
                    .collect(Collectors.toList());

            if (opts.isEmpty()) {
                throw new TutorException(QUESTION_OPTION_MISMATCH,
                        multipleChoiceStatementAnswerDetailsDto.getOptionsIds().get(0));
            }

            if (!this.getOptions().isEmpty()) {
                this.getOptions().forEach(opt -> opt.getQuestionAnswers().remove(this));
            }

            this.setOptions(opts);
        }
    }

    @Override
    public boolean isCorrect() {
        if (!options.isEmpty()) {

            MultipleChoiceQuestion question = (MultipleChoiceQuestion) getQuestionAnswer().getQuestion().getQuestionDetails();
            long size = question.getOptions().stream().filter(Option::isCorrect).count();
            boolean correct = isAnswerOrdered()
                    ? options.stream().allMatch(opt -> opt.getOrder() == options.indexOf(opt) + 1)
                    : options.stream().allMatch(Option::isCorrect);
            return correct && size == options.size();
        }
        return false;
    }

    private boolean isAnswerOrdered() {
        return options.get(0).getOrder() != -1;
    }

    public void remove() {
        options.forEach(opt -> opt.getQuestionAnswers().remove(this));
        options.clear();
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return !this.getOptions().isEmpty();
    }

    @Override
    public String getAnswerRepresentation() {
        return this.getOptions().isEmpty() ? "-" : convertAllOptionsSequencesToLetter(getOptions());
    }

    private String convertAllOptionsSequencesToLetter(List<Option> options) {
        StringBuilder builder = new StringBuilder();
        for (Option opt : options) {
            builder.append(MultipleChoiceQuestion.convertSequenceToLetter(opt.getSequence()));
        }
        return builder.toString();
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}
