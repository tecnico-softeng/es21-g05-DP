import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenEndedQuestionDetails extends QuestionDetails {
  criteria: string = '';
  regexQuestion: string = '';

  constructor(jsonObj?: OpenEndedQuestionDetails) {
    super(QuestionTypes.OpenEnded);
    if (jsonObj) {
      this.criteria = jsonObj.criteria || this.criteria;
      this.regexQuestion = jsonObj.regexQuestion || this.regexQuestion;
    }
  }

  getRegex(): string {
    return this.regexQuestion;
  }
  setAsNew(): void {}
}
