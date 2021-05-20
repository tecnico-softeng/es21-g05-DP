import QuestionDetails from '@/models/management/questions/QuestionDetails';
import Item from '@/models/management/questions/Item';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationQuestionDetails extends QuestionDetails {
  items: Item[] = [];

  constructor(jsonObj?: ItemCombinationQuestionDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.items = jsonObj.items.map((item: Item) => new Item(item));
    }
  }

  setAsNew(): void {
    this.items.forEach((item) => {
      item.id = null;
    });
  }
}
