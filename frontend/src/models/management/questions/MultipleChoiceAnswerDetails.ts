import Option from '@/models/management/Option';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';

export default class MultipleChoiceAnswerType extends AnswerDetails {
  options!: Option[];

  constructor(jsonObj?: MultipleChoiceAnswerType) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.options = jsonObj.options.map((opt: Option) => new Option(opt));
    }
  }

  isCorrect(questionDetails: MultipleChoiceQuestionDetails): boolean {
    if (this.options.length > 0) {
      const size = questionDetails.options.filter((opt) => opt.correct).length;
      const correct = this.isAnswerOrdered()
        ? this.options.every(
            (opt) => opt.order == this.options.indexOf(opt) + 1
          )
        : this.options.every((opt) => opt.correct);
      return correct && size == this.options.length;
    }
    return false;
  }

  answerRepresentation(): string {
    return (
      this.options
        .map((x) => '' + (convertToLetter(x.sequence) || 0))
        .join(' | ') || '-'
    );
  }

  private isAnswerOrdered(): boolean {
    return this.options[0].order != -1;
  }
}
