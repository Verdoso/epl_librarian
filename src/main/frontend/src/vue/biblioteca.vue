<template>
  <section>
    <div id="fecha_base">
    <b-field label="Considerar novedades libros posteriores a" grouped>
        <b-datepicker
            placeholder="Selecciona una fecha para filtrar"
            icon="calendar-today"
            :first-day-of-week="1"
            :day-names="['D', 'L', 'M', 'X', 'J', 'V', 'S']"
            :month-names="['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembere']"
            trap-focus
            v-model="fechaBase"
            @input="cambioFechaNovedades()"
            >
            <button class="button is-primary"
                @click="fechaBaseHoy()">
                <b-icon icon="calendar-today"></b-icon>
                <span>Hoy</span>
            </button>
        </b-datepicker>
        <p class="control">
            <b-button class="button is-primary" @click="guardarFechaBase()">guardar</b-button>
            <b-switch v-model="soloNovedades" @input="cambioNovedades()">Solo novedades</b-switch>
        </p>
    </b-field>
    </div>
    <b-table
      ref="table"
      :data="data"
      :loading="loading"
      :total="total"
      :per-page="perPage"
      :default-sort-direction="defaultSortOrder"
      :default-sort="[sortField, sortOrder]"
      detailed
      detail-key="id"
      paginated
      backend-pagination
      backend-sorting
      backend-filtering
      pagination-position="top"
      aria-next-label="Next page"
      aria-previous-label="Previous page"
      aria-page-label="Page"
      aria-current-label="Current page"
      @filters-change="onFilterChange"
      @page-change="onPageChange"
      @sort="onSort"
    >
      <template slot-scope="props">
        <b-table-column
          field="POR_TITULO"
          label="Titulo"
          searchable
          sortable
          width="20%"
        >{{ props.row.titulo }}</b-table-column>

        <b-table-column
          field="POR_COLECCION"
          label="Coleccion"
          sortable
          searchable
          width="20%"
        >{{ props.row.coleccionCompleta }}</b-table-column>

        <b-table-column field="POR_AUTOR" label="Autor" sortable searchable width="25%">
          <template
              slot="searchable">
              <b-input
                  v-model="currentAutorFilter"
                  placeholder="autor..."
                  icon-right="close-circle"
                  icon-right-clickable
                  @icon-right-click="clearAutorFilter"
                  @input="changedAutorFilter"
                  />
          </template>
          <b-tooltip
            :label="props.row.autor | truncate(120)"
            type="is-light"
            position="is-bottom"
            v-if="(props.row.autor.length > 45)"
          >
        {{ props.row.autor | truncate(45) }}</b-tooltip>
          <span v-if="(props.row.autor.length <= 45)">{{ props.row.autor }}</span>
        </b-table-column>

        <b-table-column field="POR_IDIOMA" label="Idiomas" sortable searchable width="10%">
          <template
              slot="searchable">
              <b-input
                  v-model="currentIdiomaFilter"
                  placeholder="idioma..."
                  icon-right="close-circle"
                  icon-right-clickable
                  @icon-right-click="clearIdiomaFilter"
                  @input="changedIdiomaFilter"
                  />
          </template>
        {{ props.row.idioma}}</b-table-column>

        <b-table-column field="POR_GENERO" label="Generos" searchable width="25%">
          <template
              slot="searchable">
              <b-input
                  v-model="currentGeneroFilter"
                  placeholder="genero..."
                  icon-right="close-circle"
                  icon-right-clickable
                  @icon-right-click="clearGeneroFilter"
                  @input="changedGeneroFilter"
                  />
          </template>
          <b-tooltip
            :label="props.row.generos"
            type="is-light"
            position="is-bottom"
            v-if="(props.row.generos.length > 45)"
          >{{ props.row.generos | truncate(45) }}</b-tooltip>
          <span v-if="(props.row.generos.length <= 45)">{{ props.row.generos }}</span>
        </b-table-column>

        <b-table-column
          field="POR_PUBLICADO"
          label="Publicado"
          sortable
          width="10%"
        >{{ props.row.publicado.substring(2) }}</b-table-column>

      </template>
      <template slot="detail" slot-scope="props">
        <article class="media">
          <div class="media-content">
            <div class="content">
                <strong>{{ props.row.titulo }}</strong>
                - <small>v.{{ props.row.revision }} - {{ props.row.paginas }} pàginas ( {{props.row.autor}} )</small>
                <hr />
                <br /> {{props.row.sinopsis}}
            </div>
            <div>
                  <b-button
                    v-if="props.row.magnetId"
                    tag="a"
                    :href="props.row | toMagnet"
                    type="is-link"
                    pack="fa"
                    icon-left="magnet"
                  >
                    Descarga
                  </b-button>
            </div>
          </div>
        </article>
      </template>
    </b-table>
  </section>
</template>

<script>
import Vue from 'vue'
import axios from "axios";
import Vuex from 'vuex'

Vue.use(Vuex)

export default {
  props : ['autorfilter','generofilter','idiomafilter'],
  data() {
    return {
      data: [],
      total: 0,
      loading: false,
      currentAutorFilter: null,
      currentGeneroFilter: null,
      currentIdiomaFilter: null,
      sortField: "POR_TITULO",
      sortOrder: "asc",
      defaultSortOrder: "asc",
      page: 1,
      filters: null,
      perPage: 15,
      soloNovedades: false,
      fechaBase: null
    };
  },
  methods: {
    /*
     * Load async data
     */
    loadAsyncData() {
      const params = [
        `orden=${this.sortField}`,
        `numero_pagina=${this.page}`,
        `desc=${this.sortOrder == "desc" ? "true" : "false"}`,
        `filtro_titulo=${this.filterOnCriteria('POR_TITULO')}`,
        `filtro_coleccion=${this.filterOnCriteria("POR_COLECCION")}`,
        `filtro_autor=${this.filterOnValue(this.autorfilter)}`,
        `filtro_genero=${this.filterOnValue(this.generofilter)}`,
        `filtro_idioma=${this.filterOnValue(this.idiomafilter)}`,
        `filtro_fecha=${this.porFechaBase()}`,
        `por_pagina=${this.perPage}`
      ].join("&");

      this.loading = true;
      axios
        .get(`/librarian/libros?${params}`)
        .then(({ data }) => {
          // api.themoviedb.org manage max 1000 pages
          this.data = [];
          this.total = data.total;
          data.results.forEach(item => {
            this.data.push(item);
          });
          this.loading = false;
        })
        .catch(error => {
          this.data = [];
          this.total = 0;
          this.loading = false;
          throw error;
        });
    },
    /*
     * Handle page-change event
     */
    filterOnValue(value) {
      if (value == null) {
        return ''
      } else {
        return value
      }
    },
    /*
     * Handle page-change event
     */
    porFechaBase() {
      if (this.soloNovedades && this.fechaBase) {
        return this.fechaBase.getTime()
      } else {
        return ''
      }
    },
    /*
     * Handle page-change event
     */
    filterOnCriteria(criteria) {
      if (this.filters == null) {
        return "";
      } else if (this.filters[criteria] == null) {
        return "";
      } else {
        return this.filters[criteria];
      }
    },
    /*
     * Handle page-change event
     */
    onPageChange(page) {
      this.page = page;
      this.loadAsyncData();
    },
    onFilterChange(filters) {
      this.filters = filters;
      this.loadAsyncData();
    },
    changedAutorFilter(e) {
      this.$store.commit('changeAutorFilter',this.currentAutorFilter)
    },
    changedGeneroFilter(e) {
      this.$store.commit('changeGeneroFilter',this.currentGeneroFilter)
    },
    changedIdiomaFilter(e) {
      this.$store.commit('changeIdiomaFilter',this.currentIdiomaFilter)
    },
    clearAutorFilter(e) {
      this.$store.commit('changeAutorFilter','')
    },
    clearGeneroFilter(e) {
      this.$store.commit('changeGeneroFilter','')
    },
    clearIdiomaFilter(e) {
      this.$store.commit('changeIdiomaFilter','')
    },
    fechaBaseHoy() {
      this.fechaBase = new Date()
    },
    cambioFechaNovedades() {
      if(this.soloNovedades) {
        this.loadAsyncData();
      }
    },
    cambioNovedades() {
      this.loadAsyncData();
    },
    guardarFechaBase() {
      if(this.fechaBase) {
        var formData = new FormData();
        formData.append('fechaBase',this.fechaBase.getTime())
        axios
          .post('/librarian/preferences/fecha_base', formData, {headers: { 'Content-Type': 'multipart/form-data' }})
          .then((response) => {
            this.$buefy.notification.open({
              type: 'is-info'
              , duration: 5000
              , message:`Fecha base ${new Intl.DateTimeFormat('es').format(this.fechaBase)} almacenada en las preferencias`
              , hasIcon: true
            });
            if(this.soloNovedades) {
              this.loadAsyncData();
            }
          })
          .catch(error => {
            this.$buefy.notification.open({
              type: 'is-danger'
              , duration: 5000
              , message:'Error almacenando fecha base: ' + e
              , hasIcon: true
            })
            throw error;
          });
      }
    },
    /*
     * Handle sort event
     */
    onSort(field, order) {
      this.sortField = field;
      this.sortOrder = order;
      this.loadAsyncData();
    }
  },
  watch: {
    autorfilter: function() {
      if(this.currentAutorFilter===this.autorfilter) {
        //console.log('Nothing to do')
      } else {
        this.currentAutorFilter = this.autorfilter
      }
      this.loadAsyncData();
    },
    generofilter: function() {
      if(this.currentGeneroFilter===this.generofilter) {
        //console.log('Nothing to do')
      } else {
        this.currentGeneroFilter = this.generofilter
      }
      this.loadAsyncData();
    },
    idiomafilter: function() {
      if(this.currentIdiomaFilter===this.idiomafilter) {
        //console.log('Nothing to do')
      } else {
        this.currentIdiomaFilter = this.idiomafilter
      }
      this.loadAsyncData();
    }
  },
  filters: {
    /**
     * Filter to truncate string, accepts a length parameter
     */
    truncate(value, length) {
      return value.length > length ? value.substr(0, length) + "..." : value;
    },
    toMagnet(book) {
      return `magnet:?xt=urn:btih:${book.magnetId}&dn=EPL_${book.id}_${encodeURIComponent(book.titulo)}`
              + '&tr=http://tracker.tfile.me/announce'
              + '&tr=udp://tracker.opentrackr.org:1337/announce'
              + '&tr=udp://tracker.openbittorrent.com:80'
              + '&tr=udp://tracker.publicbt.com:80'
              + '&tr=udp://open.demonii.com:1337/announce'
    }
  },
  mounted() {
    axios
      .get('/librarian/preferences/fecha_base')
      .then(({ data }) => {
        if(data) {
          this.fechaBase = new Date(data);
        }
      })
      .catch(error => {
        throw error;
      });
    this.loadAsyncData();
  }
};
</script>

<style>
  #fecha_base label {
    padding-right: .5em;
  }
</style>