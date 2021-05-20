<template>
  <ul>
    <li v-for="option in questionDetails.options" :key="option.id">
      <span
        v-if="
          option.correct && questionDetails.answerType === AnswerTypes.Ordered
        "
        v-html="
          convertMarkDown(
            studentAnswered(option.id) +
              `**[★${option.order}]** ` +
              option.content
          )
        "
        v-bind:class="[option.correct ? 'font-weight-bold' : '']"
      />
      <span
        v-else-if="option.correct"
        v-html="
          convertMarkDown(
            studentAnswered(option.id) + '**[★]** ' + option.content
          )
        "
        v-bind:class="[option.correct ? 'font-weight-bold' : '']"
      />
      <span
        v-else
        v-html="convertMarkDown(studentAnswered(option.id) + option.content)"
      />
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import MultipleChoiceQuestionDetails, {
  AnswerTypes,
} from '@/models/management/questions/MultipleChoiceQuestionDetails';
import MultipleChoiceAnswerDetails from '@/models/management/questions/MultipleChoiceAnswerDetails';

@Component
export default class MultipleChoiceView extends Vue {
  @Prop() readonly questionDetails!: MultipleChoiceQuestionDetails;
  @Prop() readonly answerDetails?: MultipleChoiceAnswerDetails;
  AnswerTypes = AnswerTypes;

  studentAnswered(optionId: number) {
    let optionIndex: number;
    return this.answerDetails &&
      (optionIndex = this.answerDetails?.options.findIndex(
        (opt) => opt.id === optionId
      )) !== -1
      ? this.questionDetails.answerType === AnswerTypes.Ordered
        ? `**[S-${optionIndex + 1}]** `
        : '**[S]** '
      : '';
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
