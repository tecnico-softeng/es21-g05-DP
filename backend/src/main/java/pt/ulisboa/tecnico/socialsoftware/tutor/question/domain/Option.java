package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_OPTION;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_SEQUENCE_FOR_OPTION;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;

@Entity
@Table(name = "options")
public class Option implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "correct_order")
    private Integer order;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean correct;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private MultipleChoiceQuestion questionDetails;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "options", fetch = FetchType.LAZY)
    private final Set<MultipleChoiceAnswer> questionAnswers = new HashSet<>();

    public Option() {
    }

    public Option(OptionDto option) {
        setSequence(option.getSequence());
        setContent(option.getContent());
        setCorrect(option.isCorrect());
        setOrder(option.getOrder());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOption(this);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        if (sequence == null || sequence < 0)
            throw new TutorException(INVALID_SEQUENCE_FOR_OPTION);

        this.sequence = sequence;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_OPTION);

        this.content = content;
    }

    public MultipleChoiceQuestion getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(MultipleChoiceQuestion question) {
        this.questionDetails = question;
        question.addOption(this);
    }

    public Set<MultipleChoiceAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void addQuestionAnswer(MultipleChoiceAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", correct=" + correct +
                ", content='" + content + '\'' +
                ", order=" + order +
                ", question=" + questionDetails.getId() +
                ", questionAnswers=" + questionAnswers +
                '}';
    }

    public void remove() {
        this.questionDetails = null;
        this.questionAnswers.clear();
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
