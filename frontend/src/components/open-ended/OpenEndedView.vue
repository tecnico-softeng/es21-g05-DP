<template>
  <ul>
    <li v-if="studentAnswered() !== ''">
      <v-card-text>
        <span><b>Student's Answer </b></span>
        <p>
          <span>{{ studentAnswered() }}</span>
        </p>
      </v-card-text>
    </li>
    <li>
      <v-card-text>
        <span><b>Criteria </b></span>
        <p>
          <span>{{ QuestionDetails.criteria }}</span>
        </p>
      </v-card-text>
    </li>
    <li>
      <v-card-text>
        <span><b>Regex </b></span>
        <p>
          <span>{{ QuestionDetails.regexQuestion }}</span>
        </p>
      </v-card-text>
    </li>
  </ul>
</template>
<script lang="ts">
import { Component, Vue, PropSync, Prop } from 'vue-property-decorator';
import OpenEndedQuestionDetails from '@/models/management/questions/OpenEndedQuestionDetails';
import OpenEndedAnswerDetails from '@/models/management/questions/OpenEndedAnswerDetails';

@Component
export default class OpenEndedQuestionView extends Vue {
  @PropSync('questionDetails', { type: OpenEndedQuestionDetails })
  readonly QuestionDetails!: OpenEndedQuestionDetails;
  @Prop() readonly answerDetails?: OpenEndedAnswerDetails;

  studentAnswered() {
    return this.answerDetails && this.answerDetails?.openAnswer != null
      ? this.answerDetails.openAnswer
      : '';
  }
}
</script>
