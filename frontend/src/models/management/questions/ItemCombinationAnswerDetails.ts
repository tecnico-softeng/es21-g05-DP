import { QuestionTypes } from '@/services/QuestionHelpers';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import CombinationAnswer from '@/models/management/questions/CombinationAnswer';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';

export default class ItemCombinationAnswerDetails extends AnswerDetails {
  connections: CombinationAnswer[] = [];

  constructor(jsonObj?: ItemCombinationAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.connections = jsonObj.connections.map(
        (item: CombinationAnswer) => new CombinationAnswer(item)
      );
    }
  }

  isCorrect(questionDetails: ItemCombinationQuestionDetails): boolean {
    for (const c of this.connections) {
      const leftItem = c.itemId;
      const questionItem = questionDetails.items.find(item => item.id === leftItem);
      const rightItems = questionItem?.connections.map(item => item.sequence);
      if (c.conns.slice().sort().join(',') !== rightItems?.slice().sort().join(','))
        return false;
    }
    return this.connections.length === questionDetails.items.filter(i => i.group === 'LEFT').length;
  }

  answerRepresentation(question: ItemCombinationQuestionDetails): string {
    let str = '';
    for (const answer of this.connections) {
      str += answer.answerRepresentation() + ' | ';
    }
    return str;
  }
}
