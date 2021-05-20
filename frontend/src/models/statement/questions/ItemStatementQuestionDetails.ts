export default class ItemStatementQuestionDetails {
  itemId!: number;
  content!: string;
  group!: string;
  sequence: number | null = null;
  connections: number[] | number[] = [];

  constructor(jsonObj?: ItemStatementQuestionDetails) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId;
      this.content = jsonObj.content;
      this.sequence = jsonObj.sequence || this.sequence;
      this.group = jsonObj.group;
      this.connections = jsonObj.connections || this.connections;
    }
  }
}
