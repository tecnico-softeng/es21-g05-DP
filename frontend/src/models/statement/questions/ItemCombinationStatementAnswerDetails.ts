import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';
import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from './ItemCombinationStatementCorrectAnswerDetails';

export default class ItemCombinationStatementAnswerDetails extends StatementAnswerDetails {
  public answeredItems!: ItemStatementAnswerDetails[];

  constructor(jsonObj?: ItemCombinationStatementAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.answeredItems = jsonObj.answeredItems || [];
    }
  }

  isQuestionAnswered(): boolean {
    return this.answeredItems != null && this.answeredItems.length > 0;
  }
  isAnswerCorrect(
    correctAnswerDetails: ItemCombinationStatementCorrectAnswerDetails
  ): boolean {
    for (const item of this.answeredItems) {
      if (item.leftItem) {
        const correct = correctAnswerDetails.correctItems[item.leftItem - 1];
        const match = item.connections.every(
          (value, index) => value === correct.correctConnections[index]
        );
        if (!match) return false;
      }
    }
    return this.answeredItems.length === correctAnswerDetails.correctItems.length;
  }

  isCorrectByIndex(
    index: number,
    correctAnswerDetails: ItemCombinationStatementCorrectAnswerDetails
  ): boolean {
    const answer = this.answeredItems[index - 1];
    if (answer && answer.leftItem) {
      const correct = correctAnswerDetails.correctItems[answer.leftItem - 1];
      return answer.connections.every(
        (value, i) => value === correct.correctConnections[i]
      );
    }
    return false;
  }
}
