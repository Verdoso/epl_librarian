<template>
  <section>
    <p class="control" v-if="this.$store.state.calibreIntegration">
      <b-switch v-model="calibre" @input="loadAsyncData()">Mi biblioteca calibre</b-switch>
    </p>
    <Doughnut :chart-options="chartOptions" :chart-data="chartData" :chart-id="chartId" :dataset-id-key="datasetIdKey"
      :plugins="plugins" :css-classes="cssClasses" :styles="styles" :width="width" :height="height" />
  </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import { Doughnut } from 'vue-chartjs/legacy'
import Chart from 'chart.js/auto';
import ColorHash from 'color-hash'
import { EventBus } from '../event-bus';

const colorHash = new ColorHash()


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
      default: 800
    },
    height: {
      type: Number,
      default: 500
    },
    cssClasses: {
      default: 'idiomas_doughnut',
      type: String
    },
    styles: {
      type: Object,
      default: () => { }
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
            , backgroundColor: []
          }
        ]
      }
      , chartOptions: {
        responsive: false
        , aspectRatio: 5
        , maintainAspectRatio: false
        , plugins: {
          legend: {
            position: 'right'
            , labels: {
              generateLabels: ((chart) => {
                const data = chart.data;
                if (data.labels.length && data.datasets.length) {
                  const { labels: { pointStyle } } = chart.legend.options;

                  return data.labels.map((label, i) => {
                    const meta = chart.getDatasetMeta(0);
                    const style = meta.controller.getStyle(i);

                    var total = chart.config.data.datasets[0].data.reduce(function (previousValue, currentValue, currentIndex, array) {
                      return previousValue + currentValue;
                    });

                    var value = chart.config.data.datasets[0].data[i];
                    var percentage = parseFloat((value / total * 100).toFixed(2));

                    return {
                      text: label + ': ' + Intl.NumberFormat('ca').format(value) + ' (' + percentage + ' %)',
                      fillStyle: style.backgroundColor,
                      strokeStyle: style.borderColor,
                      lineWidth: style.borderWidth,
                      pointStyle: pointStyle,
                      hidden: !chart.getDataVisibility(i),

                      // Extra data used for toggling the correct item
                      index: i
                    };
                  });
                }
                return [];
              })
            }
          }
          , tooltip: {
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
                var total = dataset.data.reduce(function (previousValue, currentValue, currentIndex, array) {
                  return previousValue + currentValue;
                });
                var currentValue = context.parsed;
                var percentage = parseFloat((currentValue / total * 100).toFixed(2));
                return label + ' (' + percentage + ' %)';
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
          this.chartData.labels.splice(0, this.chartData.labels.length);
          this.chartData.datasets[0].data.splice(0, this.chartData.datasets[0].data.length);
          this.chartData.datasets[0].backgroundColor.splice(0, this.chartData.datasets[0].backgroundColor.length);
          data.results.forEach(item => {
            this.chartData.labels.push(item.nombre)
            this.chartData.datasets[0].data.push(item.libros)
            this.chartData.datasets[0].backgroundColor.push(colorHash.hex(item.nombre))
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
      if (this.calibre) {
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