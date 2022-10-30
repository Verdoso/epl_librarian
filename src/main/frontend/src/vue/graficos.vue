<template>
  <section>
    <b-tabs v-model="activeTab" position="is-centered" expanded size="is-medium" type="is-boxed">
        <b-tab-item label="Autores" icon="address-card-o" icon-pack="fa">
          <autores-chart />
        </b-tab-item>
        <b-tab-item label="Generos" icon="sitemap" icon-pack="fa">
          <generos-chart />
        </b-tab-item>
        <b-tab-item label="Idiomas" icon="flag" icon-pack="fa">
          <idiomas-chart />
        </b-tab-item>
    </b-tabs>    
  </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import generosChart from './generos-chart.vue';
import autoresChart from './autores-chart.vue';
import idiomasChart from './idiomas-chart.vue';

Vue.component('generos-chart', generosChart)
Vue.component('autores-chart', autoresChart)
Vue.component('idiomas-chart', idiomasChart)

export default {
  data() {
    return {
      activeTab: undefined
    };
  },
  methods: {
  },
  watch: {
  },
  mounted() {
  }
};
</script>