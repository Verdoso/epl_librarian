<template>
  <section>
    <p class="control" v-if="this.$store.state.calibreIntegration">
      <b-switch v-model="calibre" @input="loadAsyncData()">Mi biblioteca calibre</b-switch>
    </p>
    <Doughnut
      :chart-options="chartOptions"
      :chart-data="chartData"
      :chart-id="chartId"
      :dataset-id-key="datasetIdKey"
      :plugins="plugins"
      :css-classes="cssClasses"
      :styles="styles"
      :width="width"
      :height="height"
    />
  </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import { Doughnut } from 'vue-chartjs/legacy'
import Chart from 'chart.js/auto';
import colors from 'nice-color-palettes'


export default {
  components: { Doughnut },
  props: {
    chartId: {
      type: String,
      default: 'idiomas-chart'
    },
    datasetIdKey: {
      type: String,
      default: 'label'
    },
    width: {
      type: Number,
      default: 400
    },
    height: {
      type: Number,
      default: 150
    },
    cssClasses: {
      default: '',
      type: String
    },
    styles: {
      type: Object,
      default: () => {}
    },
    plugins: {
      type: Array,
      default: () => []
    }
  }
  , data() {
    return {
      calibre: false,
      chartData: {
        labels: [],
        datasets: [
          {
            data: []
            , backgroundColor: colors[8].concat(colors[10]).concat(colors[11]).concat(colors[4])
          }
        ]
      }
      , chartOptions: {
        responsive: true
        , maintainAspectRatio: false
        , plugins: {
          tooltip: {
              enabled: true,
              callbacks: {
                label: ((context) => {
                  let label = context.label || '';
  
                  if (label) {
                      label += ': ';
                  }
                  if (context.parsed !== null) {
                     label += Intl.NumberFormat('ca').format(context.parsed);
                  }

                  var dataset = context.dataset;
                  var total = dataset.data.reduce(function(previousValue, currentValue, currentIndex, array) {
                    return previousValue + currentValue;
                  });
                  var currentValue = context.parsed;
                  var percentage = parseFloat((currentValue/total*100).toFixed(2));
                  return label + ' (' + percentage + '%)';
                })
              }
            }
          }
      }
    };
  },
  methods: {
    loadAsyncData() {
      const params = [
        `orden=POR_LIBROS`,
        `numero_pagina=1`,
        `desc=true`,
        `por_pagina=20`,
        `favoritos_idiomas=false`
      ].join("&");
      let url = this.calibre ? 'top_idiomas_propios' : '/librarian/idiomas';
      axios.get(`${url}?${params}`)
              .then(({ data }) => {
                this.chartData.labels.splice(0,this.chartData.labels.length);
                this.chartData.datasets[0].data.splice(0,this.chartData.datasets[0].data.length);
                data.results.forEach(item => {
                  this.chartData.labels.push(item.nombre)
                  this.chartData.datasets[0].data.push(item.libros)
                });
              })
              .catch(error => {
                this.$nextTick(() => {
                  throw error;
                });
              });
    }  
  },
  watch: {
  },
  mounted() {
    this.loadAsyncData();
  }
};
</script>