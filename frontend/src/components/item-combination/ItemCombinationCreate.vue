<template>
  <div class="item-combination-items">
    <v-col align="end">
      <v-btn
        elevation="20"
        x-small
        class="ma-auto"
        color="blue darken-1"
        @click="clearAll"
        data-cy="clearAll"
        ><span class="white--text">Clear all</span></v-btn
      >
    </v-col>
    <v-layout wrap>
      <v-flex>
        <v-row
          v-for="(lItem, index) in sQuestionDetails.items"
          :key="index"
          data-cy="questionItemsInputLeft"
        >
          <v-col cols="5">
            <v-textarea
              v-if="lItem.group === Group.Left"
              v-model="lItem.content"
              :label="`Item ${index + 1}`"
              :data-cy="`Item ${index + 1}`"
              rows="1"
              auto-grow
            ></v-textarea>
          </v-col>
          <v-col v-if="lItem.group === Group.Left" cols="4">
            <v-select
              multiple
              :value="selected[lItem.sequence]"
              :items="rightItems"
              :label="`Connections ${index + 1}`"
              @input="updateConnections($event, lItem)"
              :data-cy="`Connections ${index + 1}`"
              outlined
              dense
            ></v-select>
          </v-col>
        </v-row>
      </v-flex>
      <v-col cols="4">
        <v-row
          v-for="(rItem, index) in sQuestionDetails.items"
          :key="index"
          data-cy="questionItemsInputRight"
        >
          <v-textarea
            v-if="rItem.group === Group.Right"
            v-model="rItem.content"
            @change="updateText($event, rItem)"
            :label="`Item ${index + 1}`"
            :data-cy="`Item ${index + 1}`"
            rows="1"
            auto-grow
          ></v-textarea>
        </v-row>
      </v-col>
    </v-layout>
    <v-row>
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addLeftItem"
        data-cy="addLeftCombinationItem"
        ><span class="white--text">Add Left Item</span></v-btn
      >
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addRightItem"
        data-cy="addRightCombinationItem"
        ><span class="white--text">Add Right Item</span></v-btn
      >
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, PropSync, Vue } from 'vue-property-decorator';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import Item, { Group } from '@/models/management/questions/Item';

@Component
export default class ItemCombinationCreate extends Vue {
  @PropSync('questionDetails', { type: ItemCombinationQuestionDetails })
  sQuestionDetails!: ItemCombinationQuestionDetails;
  rightItems: number[] = [];
  selected: number[][] = [];
  Group = Group;

  created() {
    let conns: number[];
    for (const i of this.sQuestionDetails.items) {
      conns = [];
      this.synchronizeAllInfo(i, conns);
    }
  }

  private synchronizeAllInfo(i: Item, conns: number[]) {
    for (const conn of i.connections) {
      if (conn.sequence) conns.push(conn.sequence);
    }
    if (i.sequence) this.selected[i.sequence] = conns;
    if (i.group === Group.Right && i.sequence) this.rightItems.push(i.sequence);
  }

  addLeftItem() {
    const item: Item = new Item();
    item.group = Group.Left;
    item.sequence = this.sQuestionDetails.items.length + 1;
    this.sQuestionDetails.items.push(item);
  }

  addRightItem() {
    const item: Item = new Item();
    item.group = Group.Right;
    item.sequence = this.sQuestionDetails.items.length + 1;
    this.rightItems.push(item.sequence);
    this.sQuestionDetails.items.push(item);
  }

  updateText(event: any, rItem: any) {
    const index = this.rightItems.findIndex((seq) => seq === rItem.sequence);
    this.rightItems[index] = rItem.sequence;
  }

  updateConnections(event: number[], lItem: any) {
    let conns: Item[] = [];
    for (const e of event) {
      const comb = this.sQuestionDetails.items.find((i) => i.sequence === e);
      if (comb) conns.push(comb);
    }
    lItem.connections = conns;
  }

  clearAll() {
    this.sQuestionDetails.items.forEach((i) => (i.connections = []));
    this.sQuestionDetails.items = [];
    this.rightItems = [];
    this.selected.forEach((i) => (i = []));
  }
}
</script>
