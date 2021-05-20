import ItemCorrectAnswer from '@/models/management/questions/ItemCorrectAnswer';
import { QuestionTypes } from '@/services/QuestionHelpers';
import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';

export default class ItemCombinationStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public correctItems!: ItemCorrectAnswer[];

  constructor(jsonObj?: ItemCombinationStatementCorrectAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.correctItems = jsonObj.correctItems || [];
    }
  }
}
