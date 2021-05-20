export default class ItemStatementAnswerDetails {
  leftItem: number | null = null;
  itemId: number | null = null;
  connections: number[] = [];

  constructor(jsonObj?: ItemStatementAnswerDetails) {
    if (jsonObj) {
      this.leftItem = jsonObj.leftItem;
      this.itemId = jsonObj.itemId;
      this.connections = jsonObj.connections || [];
    }
  }
}
