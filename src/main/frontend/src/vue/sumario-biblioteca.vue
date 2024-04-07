<template>
    <section class="summary">
        <table class="brief_summary" v-if="this.sumario">
            <caption>
            Sumario <small>- {{ Intl.DateTimeFormat('es', {timeStyle: "short",dateStyle: "medium"}).format(this.fechaActualizacion) }}</small>
            </caption>
            <tr>
            <th>Libros</th>
            <td align="right">
                {{ Intl.NumberFormat('ca').format(this.sumario.libros) }}
            </td>
            </tr>
            <tr>
            <th>Autores</th>
            <td align="right">
                {{ Intl.NumberFormat('ca').format(this.sumario.autores) }}
            </td>
            </tr>
            <tr>
            <th>Idiomas</th>
            <td align="right">
                {{ Intl.NumberFormat('ca').format(this.sumario.idiomas) }}
            </td>
            </tr>
            <tr>
            <th>Generos</th>
            <td align="right">
                {{ Intl.NumberFormat('ca').format(this.sumario.generos) }}
            </td>
            </tr>
        </table>
        <span></span>
        <div class="brief_summary">
            <b-button
                :class="this.$store.state.calibreIntegration ? 'button is-link' : 'button is-light'"
                :loading="actualizando"
                @click="actualizar()"
                >
                <b-icon
                    pack="fa"
                    :icon="this.$store.state.calibreIntegration ? 'check' : 'times'"
                ></b-icon>
                <span>Integración con Calibre</span>
                {{  }}
            </b-button>
        </div>
    </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import Vuex from "vuex";
import { EventBus } from '../event-bus';

Vue.use(Vuex);

export default {
        data () {
            return {
                sumario: null,
                fechaActualizacion: null,
                actualizando: false
            }
        },
        mounted() {
          axios
          .get('/librarian/sumario')
          .then(response => {
            this.sumario = response.data;
            this.fechaActualizacion = new Date(this.sumario.fechaActualizacion);
            this.$store.commit("changeVersion", this.sumario.buildVersion);
            this.$store.commit("changeLatestVersion", this.sumario.latestVersion);
            this.$store.commit("changeCalibreIntegration", this.sumario.integracionCalibreHabilitada);
          })
         .catch(e => {
             this.$buefy.notification.open({
                 type: 'is-danger'
                 , duration: 5000
                 , message:'Error mostrando sumario: ' + e
                 , hasIcon: true
             })
             console.error(e)
         });
        },
        methods: {
          actualizar() {
            if(this.$store.state.calibreIntegration) {
              this.actualizando = true;
              axios.get('/librarian/updateCalibre')
                      .then(({ data }) => {
                        this.actualizando = false;
                        EventBus.$emit('updatedCalibre', 'Correct');
                        this.$buefy.notification.open({
                           type: 'is-info'
                           , duration: 3000
                           , message:'Sincronización con Calibre finalizada correctamente.'
                           , hasIcon: true
                        });
                      })
                      .catch(error => {
                        this.actualizando = false;
                        EventBus.$emit('updatedCalibre', 'Error');
                        this.$buefy.notification.open({
                           type: 'is-error'
                           , duration: 3000
                           , message:'Error sincronizando con Calibre.'
                           , hasIcon: true
                        });
                        this.$nextTick(() => {
                          throw error;
                        });
                      });
            }
          }
        },
    }
</script>

<style scoped>
</style>