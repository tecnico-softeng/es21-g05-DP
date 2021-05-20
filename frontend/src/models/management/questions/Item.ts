export enum Group {
  Left = 'LEFT',
  Right = 'RIGHT',
}

export default class Item {
  id: number | null = null;
  sequence!: number | null;
  group!: string;
  content: string = '';
  connections: Item[] = [];
  connectionOf: Item[] = [];

  constructor(jsonObj?: Item) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.sequence = jsonObj.sequence;
      this.group = jsonObj.group;
      this.content = jsonObj.content;
      this.connections = jsonObj.connections;
      this.connectionOf = jsonObj.connectionOf;
    }
  }
}
