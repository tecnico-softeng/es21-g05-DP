<template>
  <div>
    <ul>
      <span>
        <v-layout wrap>
          <v-flex>
            <v-row
              class="mt-6"
              v-for="(lItem, index) in questionDetails.items"
              :key="index"
              data-cy="questionItemsLeft"
            >
              <v-col cols="4">
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
              <v-col cols="2">
                <v-chip
                  v-for="con in lItem.connections"
                  :key="con"
                  :value="con.sequence"
                  class="ma-2"
                  color="teal"
                  text-color="white"
                >
                  <v-avatar left>
                    <v-icon>mdi-checkbox-marked-circle</v-icon>
                  </v-avatar>
                  {{ con }}
                </v-chip>
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
      </span>
    </ul>
    <span>
      <v-flex>
        <v-row
          class="mt-6"
          v-for="(i, index) in answerDetailsSynced.answeredItems"
          :key="index"
        >
          <v-col>
            <v-text-field
              v-model="selected[index]"
              :label="`Item ${index + 1} student answer:`"
              :class="
                isCorrect(index + 1) ? 'correct-response' : 'wrong-response'
              "
              readonly
              filled
              dense
            >
            </v-text-field>
          </v-col>
        </v-row>
      </v-flex>
    </span>
  </div>
</template>

<script lang="ts">
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import { Component, Model, Prop, PropSync, Vue } from 'vue-property-decorator';
import { Group } from '@/models/management/questions/Item';

@Component
export default class ItemCombinationAnswer extends Vue {
  @Model('questionOrder', Number) questionOrder: number | undefined;
  @Prop(ItemCombinationStatementQuestionDetails)
  readonly questionDetails!: ItemCombinationStatementQuestionDetails;
  @PropSync('answerDetails', ItemCombinationStatementAnswerDetails)
  answerDetailsSynced!: ItemCombinationStatementAnswerDetails;
  @Prop(ItemCombinationStatementCorrectAnswerDetails)
  readonly correctAnswerDetails!: ItemCombinationStatementCorrectAnswerDetails;
  Group = Group;
  selected: number[][] = [];

  created() {
    for (const itemAnswered of this.answerDetailsSynced.answeredItems) {
      this.selected.push(itemAnswered.connections);
    }
  }

  isCorrect(index: number): boolean {
    return this.answerDetailsSynced.isCorrectByIndex(
      index,
      this.correctAnswerDetails
    );
  }
}
</script>

<style lang="scss">
.correct-response {
  background-color: green;
}
.wrong-response {
  background-color: red;
}
</style>
