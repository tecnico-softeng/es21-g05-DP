import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import OpenEndedQuestionDetails from '@/models/management/questions/OpenEndedQuestionDetails';

export default class OpenEndedAnswerType extends AnswerDetails {
  public openAnswer: string | null = null;

  constructor(jsonObj?: OpenEndedAnswerType) {
    super(QuestionTypes.OpenEnded);
    if (jsonObj) {
      this.openAnswer = jsonObj.openAnswer;
    }
  }

  isCorrect(question: OpenEndedQuestionDetails): boolean {
    const regex = question.getRegex();
    if (regex != null && this.openAnswer != null) {
      const regexp = new RegExp(regex, 'i');
      return regexp.test(this.openAnswer);
    }
    return false;
  }

  answerRepresentation(): string {
    if (this.openAnswer != null) return this.openAnswer;
    return '';
  }
}
