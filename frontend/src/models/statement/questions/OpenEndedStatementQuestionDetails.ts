import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenEndedStatementQuestionDetails extends StatementQuestionDetails {
  constructor(jsonObj?: OpenEndedStatementQuestionDetails) {
    super(QuestionTypes.OpenEnded);
  }
}
