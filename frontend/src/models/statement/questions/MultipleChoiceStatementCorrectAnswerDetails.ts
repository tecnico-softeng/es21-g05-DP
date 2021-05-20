import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class MultipleChoiceStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public correctOptionsIds: number[] = [];
  public isOrdered: boolean = false;

  constructor(jsonObj?: MultipleChoiceStatementCorrectAnswerDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.correctOptionsIds = jsonObj.correctOptionsIds.slice();
      this.isOrdered = jsonObj.isOrdered;
    }
  }
}
