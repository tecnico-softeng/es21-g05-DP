export default class StatementOption {
  optionId!: number;
  content!: string;
  order!: number;

  constructor(jsonObj?: StatementOption) {
    if (jsonObj) {
      this.optionId = jsonObj.optionId;
      this.content = jsonObj.content;
      this.order = jsonObj.order;
    }
  }
}
