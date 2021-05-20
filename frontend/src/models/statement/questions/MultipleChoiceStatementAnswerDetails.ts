import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';

export default class MultipleChoiceStatementAnswerDetails extends StatementAnswerDetails {
  public optionsIds: number[] = [];

  constructor(jsonObj?: MultipleChoiceStatementAnswerDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.optionsIds = jsonObj.optionsIds.slice();
    }
  }

  isQuestionAnswered(): boolean {
    return this.optionsIds.length > 0;
  }

  isAnswerCorrect(
    correctAnswerDetails: MultipleChoiceStatementCorrectAnswerDetails
  ): boolean {
    if (!!correctAnswerDetails) {
      const size = correctAnswerDetails.correctOptionsIds.length;
      if (!correctAnswerDetails.isOrdered) {
        return (
          this.optionsIds.length == size &&
          this.optionsIds.every(
            (optId) =>
              correctAnswerDetails.correctOptionsIds.indexOf(optId) != -1
          )
        );
      } else {
        return (
          this.optionsIds.length ==
            correctAnswerDetails.correctOptionsIds.length &&
          this.optionsIds.every(
            (optId) =>
              optId ==
              correctAnswerDetails.correctOptionsIds[
                this.optionsIds.indexOf(optId)
              ]
          )
        );
      }
    }
    return false;
  }
}
