<template>
  <v-container>
    <v-row>
      <v-col>
        <v-text-field value="Left Items" filled rounded dense readonly>
        </v-text-field>
      </v-col>
      <v-col>
        <v-text-field value="Combinations" filled rounded dense readonly>
        </v-text-field>
      </v-col>
      <v-col>
        <v-text-field value="Right Items" filled rounded dense readonly>
        </v-text-field>
      </v-col>
    </v-row>
    <v-layout wrap>
      <v-flex>
        <v-row v-for="item in questionDetails.items" :key="item.sequence">
          <v-col cols="6">
            <v-text-field
              v-if="item.group === 'LEFT'"
              :value="`${item.sequence}. ${item.content}`"
              rounded
              dense
              readonly
            >
            </v-text-field>
          </v-col>
          <v-col cols="5" v-if="item.group === 'LEFT'">
            <v-chip
              v-for="con in item.connections"
              :key="con.sequence"
              :value="con.sequence"
            >
              {{ con.sequence }}
            </v-chip>
          </v-col>
        </v-row>
      </v-flex>
      <v-col cols="4">
        <v-row v-for="i in questionDetails.items" :key="i.sequence">
          <v-text-field
            v-if="i.group === 'RIGHT'"
            :value="`${i.sequence}. ${i.content}`"
            rounded
            dense
            readonly
          >
          </v-text-field>
        </v-row>
      </v-col>
    </v-layout>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';

@Component
export default class ItemCombinationView extends Vue {
  @Prop() readonly questionDetails!: ItemCombinationQuestionDetails;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
