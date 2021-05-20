<template>
  <div class="multiple-choice-options">
    <v-row>
      <v-col cols="6">
        <v-select
          v-model="sQuestionDetails.answerType"
          :rules="[(v) => !!v || 'Answer type is required']"
          label="Answer Type"
          @change="orderOptions()"
          required
          :items="answerTypesOptions"
          data-cy="answerTypeInput"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="1"> Correct </v-col>
    </v-row>
    <v-row
      v-for="(option, index) in sQuestionDetails.options"
      :key="index"
      data-cy="questionOptionsInput"
    >
      <div>
        <v-col cols="1">
          <v-switch
            v-model="option.correct"
            @click.native="orderOptions()"
            inset
            :data-cy="`Switch${index + 1}`"
          />
        </v-col>
      </div>
      <v-col cols="10">
        <v-textarea
          v-model="option.content"
          :label="`Option ${index + 1}`"
          :data-cy="`Option${index + 1}`"
          rows="1"
          auto-grow
        ></v-textarea>
      </v-col>
      <div v-if="sQuestionDetails.answerType === AnswerTypes.Ordered">
        <v-col cols="2">
          <v-btn
            v-if="option.correct"
            icon
            color="blue darken-1"
            :max-height="19"
            :max-width="35"
            @click="changeOptionOrder(index, 'up')"
            :disabled="option.order === 1"
          >
            <v-icon>fa-chevron-up</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="2">
          <v-btn
            v-if="option.correct"
            icon
            color="blue darken-1"
            :max-height="19"
            :max-width="35"
            @click="changeOptionOrder(index, 'down')"
            :disabled="checkLastTrue(index)"
          >
            <v-icon>fa-chevron-down</v-icon>
          </v-btn>
        </v-col>
      </div>
      <v-col v-if="sQuestionDetails.options.length > 2">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              :data-cy="`Delete${index + 1}`"
              small
              class="ma-1 action-button"
              v-on="on"
              @click="removeOption(index)"
              color="red"
              >close</v-icon
            >
          </template>
          <span>Remove Option</span>
        </v-tooltip>
      </v-col>
    </v-row>

    <v-row>
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addOption"
        data-cy="addOptionMultipleChoice"
        ><span class="white--text">Add option</span></v-btn
      >
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, PropSync, Vue } from 'vue-property-decorator';
import MultipleChoiceQuestionDetails, {
  AnswerTypes,
} from '@/models/management/questions/MultipleChoiceQuestionDetails';
import Option from '@/models/management/Option';

@Component
export default class MultipleChoiceCreate extends Vue {
  @PropSync('questionDetails', { type: MultipleChoiceQuestionDetails })
  sQuestionDetails!: MultipleChoiceQuestionDetails;
  AnswerTypes = AnswerTypes;

  get answerTypesOptions() {
    return Object.values(AnswerTypes).map((at) => ({
      text: at.charAt(0) + at.substr(1).toLowerCase(),
      value: at,
    }));
  }

  addOption() {
    this.sQuestionDetails.options.push(new Option());
  }

  removeOption(index: number) {
    this.sQuestionDetails.options.splice(index, 1);
  }

  checkLastTrue(index: number): boolean {
    const option = this.sQuestionDetails.options.find((opt) => !opt.correct);
    if (!option) {
      return this.sQuestionDetails.options.length === index + 1;
    }
    return this.sQuestionDetails.options.indexOf(option) === index + 1;
  }

  changeOptionOrder(index: number, move: string) {
    const optOrder = index + 1;
    if (move == 'up') {
      this.moveOption(index, index - 1, optOrder, optOrder - 1);
    } else {
      this.moveOption(index, index + 1, optOrder, optOrder + 1);
    }
  }

  orderOptions() {
    if (this.sQuestionDetails.answerType === AnswerTypes.Ordered) {
      const correctOptions: Option[] = this.sQuestionDetails.options.filter(
        (opt: Option) => opt.correct
      );
      let index = 1;
      correctOptions.forEach((o) => (o.order = index++));
      const incorrectOptions: Option[] = this.sQuestionDetails.options.filter(
        (opt: Option) => !opt.correct
      );
      incorrectOptions.forEach((o) => (o.order = 0));
      this.sQuestionDetails.options = correctOptions.concat(incorrectOptions);
    } else {
      this.sQuestionDetails.options.forEach((opt: Option) => (opt.order = -1));
    }
  }

  private moveOption(
    oldIndex: number,
    newIndex: number,
    currentOptOrder: number,
    newOptOrder: number
  ) {
    this.sQuestionDetails.options[newIndex].order = currentOptOrder;
    this.sQuestionDetails.options[oldIndex].order = newOptOrder;
    this.sQuestionDetails.options.splice(
      newIndex,
      0,
      this.sQuestionDetails.options.splice(oldIndex, 1)[0]
    );
  }
}
</script>
