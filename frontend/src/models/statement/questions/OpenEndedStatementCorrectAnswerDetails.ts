import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenEndedStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public regex: string | null = null;
  public criteria: string | null = null;

  constructor(jsonObj?: OpenEndedStatementCorrectAnswerDetails) {
    super(QuestionTypes.OpenEnded);
    if (jsonObj) {
      this.regex = jsonObj.regex;
      this.criteria = jsonObj.criteria;
    }
  }

  getRegex(): string | null {
    return this.regex;
  }

  getCriteria(): string | null {
    return this.criteria;
  }
}
