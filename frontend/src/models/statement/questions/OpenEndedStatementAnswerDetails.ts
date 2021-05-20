import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenEndedStatementCorrectAnswerDetails from '@/models/statement/questions/OpenEndedStatementCorrectAnswerDetails';

export default class OpenEndedStatementAnswerDetails extends StatementAnswerDetails {
  public openAnswer: string | null = null;

  constructor(jsonObj?: OpenEndedStatementAnswerDetails) {
    super(QuestionTypes.OpenEnded);
    if (jsonObj) {
      this.openAnswer = jsonObj.openAnswer;
    }
  }

  isQuestionAnswered(): boolean {
    return this.openAnswer != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: OpenEndedStatementCorrectAnswerDetails
  ): boolean {
    const regex = correctAnswerDetails.getRegex();
    if (regex != null && this.openAnswer != null) {
      const regexp = new RegExp(regex, 'i');
      return regexp.test(this.openAnswer);
    }
    return false;
  }
}
