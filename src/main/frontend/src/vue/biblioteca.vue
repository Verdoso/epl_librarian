<template>
  <section>
    <div id="fecha_base">
      <b-field label="Son novedad posteriores a" grouped>
        <b-datepicker
          placeholder="Selecciona una fecha para filtrar"
          icon="calendar-today"
          :first-day-of-week="1"
          :day-names="['D', 'L', 'M', 'X', 'J', 'V', 'S']"
          :month-names="['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']"
          trap-focus
          v-model="fechaBase"
          @input="cambioFechaNovedades()"
        >
          <button class="button is-primary" @click="fechaBaseHoy()">
            <b-icon icon="calendar-today"></b-icon>
            <span>hoy</span>
          </button>
        </b-datepicker>
        <p class="control">
          <b-button class="button is-primary" @click="guardarFechaBase()">guardar</b-button>
          <b-switch v-model="soloNovedades" @input="cambioNovedades()">Solo novedades</b-switch>
          <b-switch
            v-model="soloAutoresFavoritos"
            @input="cambioAutoresFavoritos()"
          >Autores favoritos</b-switch>
          <b-switch
            v-model="soloIdiomasFavoritos"
            @input="cambioIdiomasFavoritos()"
          >Idiomas favoritos</b-switch>
          <b-switch
            v-model="soloGenerosFavoritos"
            @input="cambioGenerosFavoritos()"
          >Géneros favoritos</b-switch>
          <b-switch
            v-model="soloNoEnPropiedad"
            @input="cambioSoloNoEnPropiedad()"
            v-if="integracioncalibre"
          >Ocultar los que tengo/descartados</b-switch>
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
      <b-table-column
        field="POR_TITULO"
        label="Titulo"
        searchable
        sortable
        width="20%"
        v-slot="props"
      >{{ props.row.titulo }}</b-table-column>

      <b-table-column
        field="POR_COLECCION"
        label="Coleccion"
        sortable
        searchable
        width="20%"
        v-slot="props"
      >{{ props.row.coleccionCompleta }}</b-table-column>

      <b-table-column field="POR_AUTOR" label="Autor" sortable searchable width="25%">
        <template #searchable="props">
          <b-input
            v-model="props.filters[props.column.field]"
            placeholder="autor..."
            icon-right="close-circle"
            icon-right-clickable
            @icon-right-click="clearAutorFilter(props)"
          />
        </template>
        <template  v-slot="props">
          <b-tooltip
            :label="props.row.autor | truncate(120)"
            type="is-light"
            position="is-bottom"
            v-if="(props.row.autor.length > 45)"
          >{{ props.row.autor | truncate(45) }}</b-tooltip>
          <span v-if="(props.row.autor.length <= 45)">{{ props.row.autor }}</span>
        </template>
      </b-table-column>

      <b-table-column field="POR_IDIOMA" label="Idiomas" sortable searchable width="10%">
        <template #searchable="props">
          <b-input
            v-model="props.filters[props.column.field]"
            placeholder="idioma..."
            icon-right="close-circle"
            icon-right-clickable
            @icon-right-click="clearIdiomaFilter(props)"
          />
        </template>
        <template  v-slot="props">
          {{ props.row.idioma}}
        </template>
      </b-table-column>

      <b-table-column field="POR_GENERO" label="Generos" searchable width="25%">
        <template #searchable="props">
          <b-input
            v-model="props.filters[props.column.field]"
            placeholder="genero..."
            icon-right="close-circle"
            icon-right-clickable
            @icon-right-click="clearGeneroFilter(props)"
          />
        </template>
        <template  v-slot="props">
          <b-tooltip
            :label="props.row.generos"
            type="is-light"
            position="is-bottom"
            v-if="(props.row.generos.length > 45)"
          >{{ props.row.generos | truncate(45) }}</b-tooltip>
          <span v-if="(props.row.generos.length <= 45)">{{ props.row.generos }}</span>
        </template>
      </b-table-column>

      <b-table-column
        field="POR_PUBLICADO"
        label="Publicado"
        sortable
        width="10%"
        v-slot="props"
      >{{ props.row.publicado.substring(2) }}</b-table-column>

      <b-table-column
        field="POR_CALIBRE"
        label="Calibre"
        :visible="integracioncalibre"
        sortable
        width="5%"
        v-slot="props"
      >
        <b-icon
          pack="fa"
          :type="props.row.inCalibre ? 'is-success' : 'is-danger'"
          :icon="props.row.inCalibre ? 'check' : 'times'"
        ></b-icon>
      </b-table-column>
      <b-table-column
        field="descartado"
        label="Descarte"
        :visible="integracioncalibre"
        width="5%"
        v-slot="props"
      >
        <b-tooltip
          :type="!props.row.descartado ? 'is-danger' : 'is-success'"
          :label="props.row.descartado ? 'Mostrar' : 'Descartar'"
          >
          <b-button
            size="is-small"
            outlined
            rounded
            icon-pack="fa"
            :type="props.row.descartado ? 'is-danger' : 'is-success'"
            :icon-right="props.row.descartado ? 'eye-slash' : 'eye'"
            @click="descartar(props.row)"
            />
        </b-tooltip>
      </b-table-column>
      <template #detail="props">
        <article class="media">
          <figure class="media-left">
              <p class="image">
                  <img :src="props.row | toPortada" style="height: 350px; width: auto;">
              </p>
          </figure>
          <div class="media-content">
            <div class="content">
              <strong>{{ props.row.titulo }}</strong>
              -
              <small>v.{{ props.row.revision }} - {{ props.row.paginas }} pàginas ( {{props.row.autor}} )</small>
              <hr />
              <br />
              {{props.row.sinopsis}}
            </div>
            <div>
              <b-field grouped>
                <p class="control">
                  <b-button
                    tag="a"
                    :href="props.row | toEplURL"
                    type="is-info"
                    icon-pack="fa"
                    icon-left="book"
                    target="_blank"
                  >En ePubLibre</b-button>
                </p>
                <span v-if="props.row.magnetId">
                  <p
                    v-for="(magnet_id, index) in props.row.magnet_ids"
                    :key="magnet_id"
                    class="control"
                  >
                    <b-button
                      tag="a"
                      :href="magnet_id | toMagnet(props.row)"
                      type="is-link"
                      icon-pack="fa"
                      icon-left="magnet"
                    >Descarga {{ props.row.magnet_ids.length > 1 ? index + 1 : ''}}</b-button>
                  </p>
                </span>
              </b-field>
            </div>
          </div>
        </article>
      </template>
    </b-table>
  </section>
</template>

<script>
import Vue from "vue";
import axios from "axios";
import Vuex from "vuex";
import Bottleneck from "bottleneck";

Vue.use(Vuex);

const limiter = new Bottleneck({
  maxConcurrent: 1,
  highWater: 1,
  strategy: Bottleneck.strategy.LEAK

});

export default {
  props: [
    "autorfilter",
    "generofilter",
    "idiomafilter",
    "lastupdate",
    "integracioncalibre"
  ],
  data() {
    return {
      data: [],
      total: 0,
      loading: false,
      sortField: "POR_TITULO",
      sortOrder: "asc",
      defaultSortOrder: "asc",
      page: 1,
      filters: null,
      perPage: 15,
      soloNovedades: true,
      soloAutoresFavoritos: false,
      soloIdiomasFavoritos: true,
      soloGenerosFavoritos: false,
      soloNoEnPropiedad: false,
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
        `filtro_titulo=${this.filterOnCriteria("POR_TITULO")}`,
        `filtro_coleccion=${this.filterOnCriteria("POR_COLECCION")}`,
        `filtro_autor=${this.filterOnCriteria("POR_AUTOR")}`,
        `filtro_genero=${this.filterOnCriteria("POR_GENERO")}`,
        `filtro_idioma=${this.filterOnCriteria("POR_IDIOMA")}`,
        `favoritos_autores=${this.soloAutoresFavoritos}`,
        `favoritos_idiomas=${this.soloIdiomasFavoritos}`,
        `favoritos_generos=${this.soloGenerosFavoritos}`,
        `filtro_fecha=${this.porFechaBase()}`,
        `solo_no_en_propiedad=${this.soloNoEnPropiedad}`,
        `por_pagina=${this.perPage}`
      ].join("&");

      this.loading = true;
      console.log("Lanzando peticion")
      limiter.schedule(() => axios.get(`/librarian/libros?${params}`))
        .then(({ data }) => {
          this.data = [];
          this.total = data.total;
          data.results.forEach(item => {
            this.data.push(item);
          });
          this.$nextTick(() => {
            this.loading = false;
          });
        })
        .catch(error => {
          this.$nextTick(() => {
            this.data = [];
            this.total = 0;
            throw error;
            this.loading = false;
          });
        });
    },
    /*
     * Handle page-change event
     */
    filterOnValue(value) {
      if (value == null) {
        return "";
      } else {
        return value;
      }
    },
    /*
     * Handle page-change event
     */
    porFechaBase() {
      if (this.soloNovedades && this.fechaBase) {
        return this.fechaBase.getTime();
      } else {
        return "";
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
    clearAutorFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeAutorFilter", '');
    },
    clearGeneroFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeGeneroFilter", "");
    },
    clearIdiomaFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeIdiomaFilter", "");
    },
    fechaBaseHoy() {
      this.fechaBase = new Date();
    },
    cambioFechaNovedades() {
      if (this.soloNovedades) {
        this.loadAsyncData();
      }
    },
    cambioNovedades() {
      this.loadAsyncData();
    },
    cambioAutoresFavoritos() {
      this.loadAsyncData();
    },
    cambioIdiomasFavoritos() {
      this.loadAsyncData();
    },
    cambioGenerosFavoritos() {
      this.loadAsyncData();
    },
    cambioSoloNoEnPropiedad() {
      this.loadAsyncData();
    },
    descartar(libro) {
      var formData = new FormData();
      formData.append("id", libro.id);
      formData.append("descartado", !libro.descartado);
      axios
        .post("/librarian/preferences/descarte", formData, {
          headers: { "Content-Type": "multipart/form-data" }
        })
        .then(response => {
          libro.descartado = !libro.descartado;
          this.$buefy.notification.open({
            type: "is-info",
            duration: 2000,
            message: `${libro.titulo} ${libro.descartado? 'descartado' : 'mostrado'}`,
            hasIcon: true
          });
          if (libro.descartado && this.soloNoEnPropiedad) {
            this.loadAsyncData();
          }
        })
        .catch(error => {
          this.$buefy.notification.open({
            type: "is-danger",
            duration: 2000,
            message: "Error descartando libro: " + error,
            hasIcon: true
          });
          throw error;
        });
    },
    guardarFechaBase() {
      if (this.fechaBase) {
        var formData = new FormData();
        formData.append("fechaBase", this.fechaBase.getTime());
        axios
          .post("/librarian/preferences/fecha_base", formData, {
            headers: { "Content-Type": "multipart/form-data" }
          })
          .then(response => {
            this.$buefy.notification.open({
              type: "is-info",
              duration: 5000,
              message: `Fecha base ${new Intl.DateTimeFormat("es").format(
                this.fechaBase
              )} almacenada en las preferencias`,
              hasIcon: true
            });
            if (this.soloNovedades) {
              this.loadAsyncData();
            }
          })
          .catch(error => {
            this.$buefy.notification.open({
              type: "is-danger",
              duration: 5000,
              message: "Error almacenando fecha base: " + error,
              hasIcon: true
            });
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
    lastupdate: function() {
      if (
        this.soloAutoresFavoritos ||
        this.soloIdiomasFavoritos ||
        this.soloGenerosFavoritos
      ) {
        this.loadAsyncData();
      }
    },
    autorfilter: function() {
      if (this.$refs.table.filters['POR_AUTOR'] !== this.autorfilter) {
        this.$nextTick(() => {
          this.$set(this.$refs.table.filters,'POR_AUTOR',this.autorfilter);
        });
      }
    },
    generofilter: function() {
      if (this.$refs.table.filters['POR_GENERO'] !== this.generofilter) {
        this.$nextTick(() => {
          this.$set(this.$refs.table.filters,'POR_GENERO',this.generofilter);
        });
      }
    },
    idiomafilter: function() {
      if (this.$refs.table.filters['POR_IDIOMA'] !== this.idiomafilter) {
        this.$nextTick(() => {
          this.$set(this.$refs.table.filters,'POR_IDIOMA',this.idiomafilter);
        });
      }
    }
  },
  filters: {
    /**
     * Filter to truncate string, accepts a length parameter
     */
    truncate(value, length) {
      return value.length > length ? value.substr(0, length) + "..." : value;
    },
    toPortada(book) {
      if(book.portada.startsWith('http')) {
        return book.portada;
      } else {
        return `https://i.imgur.com/${book.portada}.jpg`;
      }
    },
    toEplURL(book) {
      return `https://www.epublibre.org/libro/detalle/${book.id}`;
    },
    toMagnet(magnet_id, book) {
      return (
        `magnet:?xt=urn:btih:${magnet_id}&dn=EPL_${
          book.id
        }_${encodeURIComponent(book.titulo)}` +
        "&tr=http://tracker.tfile.me/announce" +
        "&tr=udp://tracker.opentrackr.org:1337/announce" +
        "&tr=udp://tracker.openbittorrent.com:80" +
        "&tr=udp://tracker.publicbt.com:80" +
        "&tr=udp://open.demonii.com:1337/announce"
      );
    }
  },
  mounted() {
    axios
      .get("/librarian/preferences/fecha_base")
      .then(({ data }) => {
        if (data) {
          this.fechaBase = new Date(data);
        }
        this.loadAsyncData();
      })
      .catch(error => {
        throw error;
      });
  }
};
</script>

<style>
#fecha_base label {
  padding-right: 0.5em;
}
</style>