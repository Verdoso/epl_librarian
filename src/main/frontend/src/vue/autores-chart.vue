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
import ColorHash from 'color-hash'
import { EventBus } from '../event-bus';

const colorHash = new ColorHash()

export default {
  components: { Bar },
  props: {
    chartId: {
      type: String,
      default: 'autores-chart'
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
            , backgroundColor: []
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
        `favoritos_autores=false`
      ].join("&");
      let url = this.calibre ? 'top_autores_propios' : '/librarian/autores';
      axios.get(`${url}?${params}`)
              .then(({ data }) => {
                this.chartData.labels.splice(0,this.chartData.labels.length);
                this.chartData.datasets[0].data.splice(0,this.chartData.datasets[0].data.length);
                this.chartData.datasets[0].backgroundColor.splice(0,this.chartData.datasets[0].backgroundColor.length);
                data.results.forEach(item => {
                  if(item.nombre !== 'AA. VV.') {
                    this.chartData.labels.push(item.nombre)
                    this.chartData.datasets[0].data.push(item.libros)
                    this.chartData.datasets[0].backgroundColor.push(colorHash.hex(item.nombre))
                  }
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
    EventBus.$on('updatedCalibre', () => {
      if(this.calibre){
        this.loadAsyncData();
      }
    });    
    EventBus.$on('updatedData', (payload) => {
      if (payload === 'Correct') {
        this.loadAsyncData();
      }
    });
  }
};
</script>