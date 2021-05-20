<template>
  <v-layout wrap>
    <v-flex>
      <v-row
        class="mt-6"
        v-for="(lItem, index) in questionDetails.items"
        :key="index"
        data-cy="questionItemsLeft"
      >
        <v-col cols="5">
          <v-text-field
            v-if="lItem.group === Group.Left"
            v-model="lItem.content"
            :label="`Item ${index + 1}`"
            :data-cy="`Item ${index + 1}`"
            rows="1"
            readonly
            filled
            rounded
            dense
          ></v-text-field>
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
        class="mt-6"
        v-for="(rItem, index) in questionDetails.items"
        :key="index"
        data-cy="questionItemsRight"
      >
        <v-text-field
          v-if="rItem.group === Group.Right"
          v-model="rItem.content"
          :label="`Item ${index + 1}`"
          :data-cy="`Item ${index + 1}`"
          rows="1"
          readonly
          filled
          rounded
          dense
        ></v-text-field>
      </v-row>
    </v-col>
  </v-layout>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';
import { Group } from '@/models/management/questions/Item';
import ItemStatementQuestionDetails from '@/models/statement/questions/ItemStatementQuestionDetails';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';

@Component
export default class ItemCombinationAnswer extends Vue {
  @Prop(ItemCombinationStatementQuestionDetails)
  readonly questionDetails!: ItemCombinationStatementQuestionDetails;
  @Prop(ItemCombinationStatementAnswerDetails)
  answerDetails!: ItemCombinationStatementAnswerDetails;
  @Prop(ItemCombinationStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?: ItemCombinationStatementCorrectAnswerDetails;
  Group = Group;
  rightItems: number[] = [];
  selected: number[][] = [];

  created() {
    let conns: number[];
    for (const i of this.questionDetails.items) {
      conns = [];
      this.synchronizeAllInfo(i, conns);
    }
  }

  private synchronizeAllInfo(i: ItemStatementQuestionDetails, conns: number[]) {
    for (const conn of i.connections) {
      if (conn) conns.push(conn);
    }
    if (i.group === Group.Right && i.sequence) this.rightItems.push(i.sequence);
  }

  updateConnections(event: number[], lItem: any) {
    let item: ItemStatementAnswerDetails = new ItemStatementAnswerDetails();
    item.leftItem = lItem.sequence;
    item.itemId = lItem.itemId;
    item.connections = event;

    let index = this.answerDetails.answeredItems.findIndex(
      (i) => i.leftItem == lItem.sequence
    );
    if (index != -1) {
      this.answerDetails.answeredItems[index].connections = event;
    } else {
      this.answerDetails.answeredItems.push(item);
    }
  }
}
</script>
