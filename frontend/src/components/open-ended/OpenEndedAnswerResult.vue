<template>
  <div id="display_result" class="d-flex flex-column mb-6">
    <div :class="{ correct: isCorrect() }">
      <v-card class="pa-2" outlined tile>
        <v-card-subtitle class="font-weight-bold text-left"
          >Answer</v-card-subtitle
        >
        <v-card-text>{{ answerDetails.openAnswer }}</v-card-text>
      </v-card>
      <v-card class="pa-2" outlined tile>
        <v-card-subtitle class="font-weight-bold text-left"
          >Correct answer</v-card-subtitle
        >
        <v-card-text>{{ correctAnswerDetails.criteria }}</v-card-text>
      </v-card>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import OpenEndedStatementAnswerDetails from '@/models/statement/questions/OpenEndedStatementAnswerDetails';
import OpenEndedStatementCorrectAnswerDetails from '@/models/statement/questions/OpenEndedStatementCorrectAnswerDetails';

@Component
export default class OpenEndedAnswerResult extends Vue {
  @Prop(OpenEndedStatementAnswerDetails)
  readonly answerDetails!: OpenEndedStatementAnswerDetails;
  @Prop(OpenEndedStatementCorrectAnswerDetails)
  readonly correctAnswerDetails!: OpenEndedStatementCorrectAnswerDetails;

  isCorrect() {
    return this.answerDetails.isAnswerCorrect(this.correctAnswerDetails);
  }
}
</script>

<style lang="scss">
#display_result > div {
  border: 1px solid;

  &.correct {
    border-color: #299455;
  }

  &:not(.correct) {
    border-color: #cf2323;
  }
}
</style>
