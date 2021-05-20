package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.MORE_THAN_ONE_CORRECT_OPTION_NEEDED;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ONE_CORRECT_OPTION_NEEDED;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceCorrectAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementQuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {

    public enum AnswerType {
        SINGLE, MULTIPLE, ORDER
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> options = new ArrayList<>();

    @Column(name = "answer_type")
    @Enumerated(EnumType.STRING)
    private AnswerType answerType = AnswerType.SINGLE;

    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        this.update(questionDto);
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> optionDtos) {
        checkNumberOfCorrectAnswers(optionDtos);
        for (Option option: this.options) {
            option.remove();
        }
        this.options.clear();

        for (Option option: this.options) {
            option.remove();
        }
        this.options.clear();

        int index = 0;
        for (OptionDto optionDto : optionDtos) {
            optionDto.setSequence(index++);
            new Option(optionDto).setQuestionDetails(this);
        }
    }

    private void checkNumberOfCorrectAnswers(List<OptionDto> options) {
        long numberOfCorrectAnswers = options.stream().filter(OptionDto::isCorrect).count();
        switch (this.answerType) {
            case SINGLE:
                if (numberOfCorrectAnswers != 1) {
                    throw new TutorException(ONE_CORRECT_OPTION_NEEDED);
                }
                break;

            case MULTIPLE:
            case ORDER:
                if (numberOfCorrectAnswers <= 1){
                    throw new TutorException(MORE_THAN_ONE_CORRECT_OPTION_NEEDED);
                }
                break;
        }
    }

    public void addOption(Option option) {

    }

    public List<Integer> getCorrectOptionsIds() {
        return this.getOptions().stream()
                .filter(Option::isCorrect)
                .map(Option::getId)
                .collect(Collectors.toList());
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setAnswerType(AnswerType.valueOf(questionDetails.getAnswerType()));
        setOptions(questionDetails.getOptions());
    }

    public AnswerType getAnswerType() { return answerType; }

    public void setAnswerType(AnswerType answerType) { this.answerType = answerType; }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        StringBuilder builder = new StringBuilder();
        for (Integer sequence : this.getCorrectAnswer()) {
            builder.append(convertSequenceToLetter(sequence));
        }
        return builder.toString();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public List<Integer> getCorrectAnswer() {
        return this.getOptions()
                .stream()
                .filter(Option::isCorrect)
                .map(Option::getSequence)
                .collect(Collectors.toList());
    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.options) {
            option.remove();
        }
        this.answerType = null;
        this.options.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "options=" + options +
                "answerType=" + answerType +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.options
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}
