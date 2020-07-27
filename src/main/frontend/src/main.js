import 'buefy/dist/buefy.css'
import 'bootstrap/dist/css/bootstrap.css';
import '@mdi/font/css/materialdesignicons.css'
import 'metismenu/dist/metisMenu.css';
import 'font-awesome/css/font-awesome.css';

import './less/sb-admin-2.less';
import './less/librarian.less';

import 'jquery'
import 'jquery-ui-dist/jquery-ui'
import 'bootstrap/dist/js/bootstrap'
import 'metismenu/dist/metisMenu'
import './sb-admin-2'

import Vue from 'vue'
import Buefy from 'buefy'
import Vuex from 'vuex'
import axios from "axios";

Vue.use(Buefy)
Vue.use(Vuex)

import sumarioBiblioteca from './vue/sumario-biblioteca.vue';
import navigationOption from './vue/navigation-option.vue';
import biblioteca from './vue/biblioteca.vue';
import autores from './vue/autores.vue';
import generos from './vue/generos.vue';
import idiomas from './vue/idiomas.vue';

Vue.component('sumario-biblioteca', sumarioBiblioteca)
Vue.component('biblioteca', biblioteca)
Vue.component('autores', autores)
Vue.component('generos', generos)
Vue.component('idiomas', idiomas)
Vue.component('navigation-option', navigationOption)

const store = new Vuex.Store({
  state: {
  currentTab: 'biblioteca',
  lastUpdate: null,
  autorfilter: null,
  generofilter: null,
  idiomafilter: null,
  buildVersion: null
  },
  mutations: {
    markUpdate (state) {
      state.lastUpdate = new Date()
    },
    changeVersion (state,newBuildVersion) {
      state.buildVersion = newBuildVersion
    },
    changeTab (state,newTab) {
      state.currentTab = newTab
    },
    changeAutorFilter (state,newFilter) {
      state.autorfilter = newFilter
    },
    changeGeneroFilter (state,newFilter) {
      state.generofilter = newFilter
    },
    changeIdiomaFilter (state,newFilter) {
      state.idiomafilter = newFilter
    }
  }
})

var app = new Vue({
  el: '#app',
  store,
  data() {
    return {
      currentTab: this.$store.state.currentTab,
      autorfilter: this.$store.state.autorfilter,
      generofilter: this.$store.state.generofilter,
      idiomafilter: this.$store.state.idiomafilter,
      buildVersion: this.$store.state.buildVersion
    }
  },
  methods: {
    salir(){
      console.log('Saliendo...')
          axios
          .get('/librarian/exit')
          .then(response => {
            console.log('Aplicación notificada, saliendo')
             this.$buefy.notification.open({
                 type: 'is-info'
                 , duration: 5000
                 , message:'La aplicación ha sido notificada, ya puede cerrar esta ventana.'
                 , hasIcon: true
             })
          })
         .catch(e => {
             this.$buefy.notification.open({
                 type: 'is-danger'
                 , duration: 5000
                 , message:'Error notificando aplicación: ' + e
                 , hasIcon: true
             })
             console.error(e)
         });
    }
  },
  mounted() {
    document.getElementById("loader").style.display = "none";
    document.getElementById("app").style.display = "block";
  }
})