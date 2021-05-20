<!--
Used on:
  - QuestionComponent.vue
  - ResultComponent.vue
-->
<template>
  <ul data-cy="optionList" class="option-list">
    <v-alert color="#1976d2" type="info" v-if="!isReadonly">
      <span v-if="questionDetails.answerType === AnswerTypes.Single">
        Select the correct option
      </span>
      <span v-else-if="questionDetails.answerType === AnswerTypes.Multiple">
        Select all the correct options
      </span>
      <span v-else> Select and order all the correct options </span>
    </v-alert>
    <li
      v-for="(n, index) in questionDetails.options.length"
      :key="index"
      v-bind:class="['option', optionClass(index)]"
      @click="
        !isReadonly && selectOption(questionDetails.options[index].optionId)
      "
    >
      <span
        v-if="
          isReadonly &&
          correctAnswerDetails.correctOptionsIds.indexOf(
            questionDetails.options[index].optionId
          ) !== -1
        "
        class="fas fa-check option-letter"
      />
      <span
        v-else-if="
          isReadonly &&
          answerDetails.optionsIds.indexOf(
            questionDetails.options[index].optionId
          ) !== -1
        "
        class="fas fa-times option-letter"
      />
      <span v-else class="option-letter">{{
        String.fromCharCode(65 + index)
      }}</span>
      <span
        class="option-content"
        v-html="convertMarkDown(questionDetails.options[index].content)"
      />
    </li>
    <v-row
      class="mt-6"
      v-if="questionDetails.answerType === AnswerTypes.Ordered"
    >
      <v-simple-table :class="isReadonly ? 'double-table' : 'single-table'">
        <template v-slot:default
          ><thead>
            <tr>
              <th class="text-center text-sm-h6 font-weight-black">
                Selected order
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="optionId in answerDetails.optionsIds" :key="optionId">
              <td>{{ representation(optionId) }}</td>
            </tr>
          </tbody>
        </template>
      </v-simple-table>
      <v-simple-table v-if="isReadonly" class="double-table">
        <template v-slot:default
          ><thead>
            <tr>
              <th class="text-center text-sm-h6 font-weight-black">
                Correct order
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="correctOptionId in correctAnswerDetails.correctOptionsIds"
              :key="correctOptionId"
            >
              <td>{{ representation(correctOptionId) }}</td>
            </tr>
          </tbody>
        </template>
      </v-simple-table>
    </v-row>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit } from 'vue-property-decorator';
import MultipleChoiceStatementQuestionDetails from '@/models/statement/questions/MultipleChoiceStatementQuestionDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import MultipleChoiceStatementAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementAnswerDetails';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';
import { AnswerTypes } from '@/models/management/questions/MultipleChoiceQuestionDetails';

@Component
export default class MultipleChoiceAnswer extends Vue {
  @Prop(MultipleChoiceStatementQuestionDetails)
  readonly questionDetails!: MultipleChoiceStatementQuestionDetails;
  @Prop(MultipleChoiceStatementAnswerDetails)
  answerDetails!: MultipleChoiceStatementAnswerDetails;
  @Prop(MultipleChoiceStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?: MultipleChoiceStatementCorrectAnswerDetails;
  AnswerTypes = AnswerTypes;

  get isReadonly() {
    return !!this.correctAnswerDetails;
  }

  representation(optionId: number) {
    const questionOptionIndex = this.questionDetails.options.findIndex(
      (opt) => opt.optionId === optionId
    );
    return String.fromCharCode(65 + questionOptionIndex);
  }

  optionClass(index: number) {
    const optionClassId = this.questionDetails.options[index].optionId;
    if (this.isReadonly) {
      if (
        !!this.correctAnswerDetails &&
        this.correctAnswerDetails.correctOptionsIds.indexOf(optionClassId) !==
          -1 &&
        this.answerDetails.optionsIds.indexOf(optionClassId) !== -1
      ) {
        return 'selected-correct';
      } else if (
        !!this.correctAnswerDetails &&
        this.correctAnswerDetails.correctOptionsIds.indexOf(optionClassId) !==
          -1
      ) {
        return 'correct';
      } else if (this.answerDetails.optionsIds.indexOf(optionClassId) !== -1) {
        return 'wrong';
      } else {
        return '';
      }
    } else {
      return this.answerDetails.optionsIds.indexOf(optionClassId) !== -1
        ? 'selected'
        : '';
    }
  }

  @Emit('question-answer-update')
  selectOption(optionId: number) {
    const answerType = this.questionDetails.answerType;
    const sameOpt = this.answerDetails.optionsIds.indexOf(optionId) != -1;
    if (answerType === AnswerTypes.Single) {
      this.answerDetails.optionsIds.splice(0, 1);
      if (!sameOpt) {
        this.answerDetails.optionsIds.push(optionId);
      }
    } else {
      if (sameOpt) {
        this.answerDetails.optionsIds.splice(
          this.answerDetails.optionsIds.indexOf(optionId),
          1
        );
      } else {
        this.answerDetails.optionsIds.push(optionId);
      }
    }
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>

<style lang="scss" scoped>
.single-table {
  width: 100%;
}
.double-table {
  width: 50%;
}

.unanswered {
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.correct-question {
  .selected-correct {
    .option-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #299455 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.incorrect-question {
  .wrong {
    .option-content {
      background-color: #cf2323;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #cf2323 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }

  .selected-correct {
    .option-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #299455 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}
</style>
