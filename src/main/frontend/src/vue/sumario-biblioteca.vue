<template>
    <section>
        <table class="brief_summary" v-if="this.sumario">
            <caption>
            Sumario <small>- {{ Intl.DateTimeFormat('es', {timeStyle: "short",dateStyle: "medium"}).format(this.fechaActualizacion) }}</small>
            </caption>
            <tr>
            <th>Libros</th>
            <td align="right">
                {{ Intl.NumberFormat('es').format(this.sumario.libros) }}
            </td>
            </tr>
            <tr>
            <th>Autores</th>
            <td align="right">
                {{ Intl.NumberFormat('es').format(this.sumario.autores) }}
            </td>
            </tr>
            <tr>
            <th>Idiomas</th>
            <td align="right">
                {{ Intl.NumberFormat('es').format(this.sumario.idiomas) }}
            </td>
            </tr>
            <tr>
            <th>Generos</th>
            <td align="right">
                {{ Intl.NumberFormat('es').format(this.sumario.generos) }}
            </td>
            </tr>
        </table>
        <span></span>
    </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import Vuex from "vuex";

Vue.use(Vuex);

export default {
        data () {
            return {
                sumario: null,
                fechaActualizacion: null
            }
        },
        mounted() {
          axios
          .get('/librarian/sumario')
          .then(response => {
            this.sumario = response.data;
            this.fechaActualizacion = new Date(this.sumario.fechaActualizacion);
            this.$store.commit("changeVersion", this.sumario.buildVersion);
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
    }
</script>

<style scoped>
</style>