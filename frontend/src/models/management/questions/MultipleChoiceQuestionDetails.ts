import Option from '@/models/management/Option';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export enum AnswerTypes {
  Single = 'SINGLE',
  Multiple = 'MULTIPLE',
  Ordered = 'ORDER',
}

export default class MultipleChoiceQuestionDetails extends QuestionDetails {
  options: Option[] = [new Option(), new Option(), new Option(), new Option()];
  answerType: string = AnswerTypes.Single;

  constructor(jsonObj?: MultipleChoiceQuestionDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.answerType = jsonObj.answerType;
      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );
    }
  }

  setAsNew(): void {
    this.options.forEach((option) => {
      option.id = null;
    });
  }
}
