<template>
  <div>
    <v-container fluid>
      <v-textarea
        v-model="answerDetails.openAnswer"
        auto-grow
        label="Answer"
        clearable
        clear-icon="mdi-close-circle"
        counter
        data-cy="openAnswer"
      >
      </v-textarea>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import OpenEndedStatementAnswerDetails from '@/models/statement/questions/OpenEndedStatementAnswerDetails';

@Component
export default class OpenEndedAnswer extends Vue {
  @Prop(OpenEndedStatementAnswerDetails)
  answerDetails!: OpenEndedStatementAnswerDetails;
}
</script>
