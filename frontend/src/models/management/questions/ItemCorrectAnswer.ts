export default class ItemCorrectAnswer {
  sequence: number | null = null;
  correctConnections: number[] = [];

  constructor(jsonObj?: ItemCorrectAnswer) {
    if (jsonObj) {
      this.sequence = jsonObj.sequence;
      this.correctConnections = jsonObj.correctConnections;
    }
  }
}
