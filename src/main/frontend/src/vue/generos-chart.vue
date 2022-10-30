<template>
  <section>
    <p class="control" v-if="this.$store.state.calibreIntegration">
      <b-switch v-model="calibre" @input="loadAsyncData()">Mi biblioteca calibre</b-switch>
    </p>
    <Bar
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
import { Bar } from 'vue-chartjs/legacy'
import Chart from 'chart.js/auto';
import colors from 'nice-color-palettes'

export default {
  components: { Bar },
  props: {
    chartId: {
      type: String,
      default: 'generos-chart'
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
            label: 'Libros'
            , data: []
            , backgroundColor: colors[8].concat(colors[10]).concat(colors[1]).concat(colors[7]).concat(colors[2]).concat(colors[3]).concat(colors[5]).concat(colors[9])
          }
        ]
      }
      , chartOptions: {
        responsive: true
         , scales: {
         y: {
            display: true
            , ticks: {
              callback: (value, index, values) => {
                return Intl.NumberFormat('ca').format(value);
              },
            },
         }
        }
        , plugins: {
           legend: {
              display: false
           }
          , tooltip: {
              enabled: true,
              callbacks: {
                label: ((context) => {
                  let label = context.dataset.label || '';
  
                  if (label) {
                      label += ': ';
                  }
                  if (context.parsed.y !== null) {
                     label += Intl.NumberFormat('ca').format(context.parsed.y);
                  }
                  return label;
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
        `por_pagina=30`,
        `favoritos_generos=false`
      ].join("&");
      let url = this.calibre ? 'top_generos_propios' : '/librarian/generos';
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