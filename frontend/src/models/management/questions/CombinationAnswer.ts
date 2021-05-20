export default class CombinationAnswer {
  itemId: number | null = null;
  conns: number[] = [];

  constructor(jsonObj?: CombinationAnswer) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId;
      this.conns = jsonObj.conns;
    }
  }

  answerRepresentation() {
    return 'Item with id:' + this.itemId + ' -> ' + this.conns.toString();
  }
}
