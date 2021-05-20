package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeFillInOption;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;

import java.io.Serializable;

public class StatementOptionDto implements Serializable {
    private Integer optionId;
    private String content;
    private Integer order;

    public StatementOptionDto(Option option) {
        this.optionId = option.getId();
        this.content = option.getContent();
        this.order = option.getOrder();
    }

    public StatementOptionDto(CodeFillInOption option) {
        this.optionId = option.getId();
        this.content = option.getContent();
    }

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    private String orderToString() {
        return order == null ? "-" : order.toString();
    }

    @Override
    public String toString() {
        return "StatementOptionDto{" +
                "optionId=" + optionId +
                ", content='" + content + '\'' +
                ", order=" + orderToString() +
                '}';
    }
}