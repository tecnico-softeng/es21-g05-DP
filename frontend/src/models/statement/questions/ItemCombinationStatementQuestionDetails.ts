import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemStatementQuestionDetails from '@/models/statement/questions/ItemStatementQuestionDetails';
import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';

export default class ItemCombinationStatementQuestionDetails extends StatementQuestionDetails {
  items: ItemStatementQuestionDetails[] = [];

  constructor(jsonObj?: ItemCombinationStatementQuestionDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.items = jsonObj.items
        ? jsonObj.items.map(
            (item: ItemStatementQuestionDetails) =>
              new ItemStatementQuestionDetails(item)
          )
        : this.items;
    }
  }
}
