<template>
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
  },
  watch: {
  },
  mounted() {
    const params = [
      `orden=POR_LIBROS`,
      `numero_pagina=1`,
      `desc=true`,
      `por_pagina=30`,
      `favoritos_autores=false`
    ].join("&");
    axios.get(`/librarian/autores?${params}`)
            .then(({ data }) => {
              data.results.forEach(item => {
                if(item.nombre !== 'AA. VV.') {
                  this.chartData.labels.push(item.nombre)
                  this.chartData.datasets[0].data.push(item.libros)
                }
              });
            })
            .catch(error => {
              this.$nextTick(() => {
                throw error;
              });
            })
            ;  
  }
};
</script>