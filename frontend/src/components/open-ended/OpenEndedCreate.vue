<template>
  <div class="criteria-create">
    <v-container fluid>
      <v-textarea
        v-model="sQuestionDetails.criteria"
        auto-grow
        label="Criteria"
        clearable
        clear-icon="mdi-close-circle"
        :rules="[(v) => !!v || 'Criteria is required']"
        data-cy="questionCriteriaInput"
      ></v-textarea>
    </v-container>
    <v-container fluid>
      <v-textarea
        v-model="sQuestionDetails.regexQuestion"
        auto-grow
        label="Regex"
        clearable
        clear-icon="mdi-close-circle"
        :rules="[(v) => !!v || 'Regex is required']"
        data-cy="questionRegexInput"
      ></v-textarea>
      <v-tooltip right>
        <template v-slot:activator="{ on, attrs }">
          <span v-bind="attrs" v-on="on">What is regex?</span>
        </template>
        <span
          >Regex (short for regular expression) is a string of text that helps
          describe sets of characters using syntactic rules. For more details
          please refer to regex documentation.
        </span>
      </v-tooltip>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Vue, PropSync } from 'vue-property-decorator';
import OpenEndedQuestionDetails from '@/models/management/questions/OpenEndedQuestionDetails';

@Component
export default class OpenEndedQuestionEdit extends Vue {
  @PropSync('questionDetails', { type: OpenEndedQuestionDetails })
  sQuestionDetails!: OpenEndedQuestionDetails;
}
</script>
