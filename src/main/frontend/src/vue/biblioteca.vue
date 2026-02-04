<template>
  <section>
    <div id="fecha_base">
      <b-field grouped label="Fecha corte de las novedades" horizontal>        
        <b-datepicker
          placeholder="Selecciona una fecha para filtrar"
          icon="calendar-today"
          :first-day-of-week="1"
          :day-names="['D', 'L', 'M', 'X', 'J', 'V', 'S']"
          :month-names="['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']"
          trap-focus
          v-model="fechaBase"
          @input="cambioFechaNovedades()"
          class="fechaBaseSelector"          
        >
          <button class="button is-primary" @click="fechaBaseHoy()">
            <b-icon icon="calendar-today"></b-icon>
            <span>hoy</span>
          </button>
        </b-datepicker>
      </b-field>      
    </div>
    <div id="numero_por_pagina">
        <b-field
            label="Por pàgina"
            horizontal
        >
            <b-select v-model="perPage" @change="cambioPorPagina">
                <option value="10">10</option>
                <option value="15">15</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
            </b-select>
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
      :page-input="true"
      detailed
      detail-key="id"
      paginated
      backend-pagination
      backend-sorting
      backend-filtering
      pagination-position="both"
      aria-next-label="Next page"
      aria-previous-label="Previous page"
      aria-page-label="Page"
      aria-current-label="Current page"
      @filters-change="onFilterChange"
      @page-change="onPageChange"
      @sort="onSort"
      striped
    >
      <b-table-column
        field="POR_TITULO"
        width="10%"
        :visible="miniaturasEnTabla"
      >
        <template  v-slot="props">
          <figure class="media-left">
            <p class="image">
                <img :src="props.row | toPortada">
            </p>
          </figure>
        </template>
      </b-table-column>
      <b-table-column
        field="POR_TITULO"
        label="Titulo"
        searchable
        sortable
        width="25%"
      >
        <template #searchable="props">
          <b-input
            v-model="props.filters[props.column.field]"
            placeholder="titulo..."
            icon-right="close-circle"
            icon-right-clickable
            @icon-right-click="clearTituloFilter(props)"
          />
        </template>
        <template  v-slot="props">
          {{ props.row.titulo }}
        </template>
      </b-table-column>

      <b-table-column
        field="POR_COLECCION"
        label="Coleccion"
        sortable
        searchable
        width="20%"
      >
        <template #searchable="props">
          <b-input
            v-model="props.filters[props.column.field]"
            placeholder="colección..."
            icon-right="close-circle"
            icon-right-clickable
            @icon-right-click="clearColeccionFilter(props)"
          />
        </template>
        <template  v-slot="props">
          {{ props.row.coleccionCompleta }}
        </template>
      </b-table-column>

      <b-table-column field="POR_AUTOR" label="Autor" sortable searchable width="20%">
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip class="onlyFilter" :label="soloAutoresFavoritos?'Click para todos los autores':'Click para solo autores favoritos'" position="is-left" dashed>
            <b-switch v-model="soloAutoresFavoritos" @input="cambioAutoresFavoritos()" />          
          </b-tooltip>
        </template>
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

      <b-table-column field="POR_IDIOMA" label="Idiomas" sortable searchable width="5em">
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip class="onlyFilter" :label="soloIdiomasFavoritos?'Click para todos los idiomas':'Click para solo idiomas favoritos'" position="is-left" dashed>
            <b-switch v-model="soloIdiomasFavoritos" @input="cambioIdiomasFavoritos()"/>          
          </b-tooltip>
        </template>
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

      <b-table-column field="POR_GENERO" label="Generos" searchable width="20%">
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip class="onlyFilter" :label="soloGenerosFavoritos?'Click para todos los géneros':'Click para solo géneros favoritos'" position="is-top" dashed>
            <b-switch v-model="soloGenerosFavoritos" @input="cambioGenerosFavoritos()" />          
          </b-tooltip>
        </template>
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
        label="En epub."
        sortable
        width="5em"
      >
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip v-if="fechaBase" class="onlyFilter" :label="soloNovedades?'Mostrando novedades posteriores a ' + fechaBase.toLocaleDateString() + ', click para mostrar todos los libros':'Click para mostrar las novedades posteriores a ' + fechaBase.toLocaleDateString()" position="is-top" dashed>
            <b-switch v-model="soloNovedades" @input="cambioNovedades()"/>          
          </b-tooltip>
        </template>
        <template  v-slot="props">
          <b-tooltip :label="timeAgo.format(new Date(props.row.fechaPublicacion))" position="is-left">
            <span class="tag is-black">
                {{ props.row.publicado.substring(2) }}
            </span>
          </b-tooltip>
        </template>
      </b-table-column>

      <b-table-column
        field="POR_CALIBRE"
        label="Calibre"
        :visible="integracioncalibre"
        sortable
        centered
        width="5em"
      >
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip class="onlyFilter" :label="soloNoEnPropiedad?'Click para mostrar también los que tengo':'Click para ocultar los que tengo'" position="is-left" dashed>
          <b-switch v-model="soloNoEnPropiedad" @input="cambioSoloNoEnPropiedad()" v-if="integracioncalibre"/>          
          </b-tooltip>
        </template>
        <template v-slot="props">
          <b-icon
            pack="fa"
            :type="props.row.inCalibre ? 'is-success' : 'is-danger'"
            :icon="props.row.inCalibre ? 'check' : 'times'"
          ></b-icon>
        </template>
      </b-table-column>
      <b-table-column
        field="POR_DESCARTE"
        label="Descarte"
        :visible="integracioncalibre"
        sortable
        centered
        width="5em"
      >
        <template v-slot:header="{ column }">
          {{ column.label }}
          <br/>
          <b-tooltip class="onlyFilter" :label="ocultarDescartados?'Click para mostrar también los descartados':'Click para ocultar los descartados'" position="is-left" dashed>
          <b-switch v-model="ocultarDescartados" @input="cambioOcultarDescartados()" />          
          </b-tooltip>
        </template>
        <template v-slot="props">
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
        </template>
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
              <small>v.{{ props.row.revision }} - {{ props.row.paginas }} pàginas ( {{props.row.autor}} ) - Publicado en {{ props.row.anyoPublicacion }}</small>
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
import { EventBus } from '../event-bus';
import es from 'javascript-time-ago/locale/es'

Vue.use(Vuex);
import TimeAgo from 'javascript-time-ago'
TimeAgo.addLocale(es)

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
      ocultarDescartados: false,
      fechaBase: null,
      timeAgo: new TimeAgo('es-ES'),
    };
  },
  computed: {
    miniaturasEnTabla: function() {
      return this.$store.getters.miniaturasEnTabla;
    },
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
        `ocultar_descartados=${this.ocultarDescartados}`,
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
    clearTituloFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeTituloFilter", "");
    },
    clearColeccionFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeColeccionFilter", "");
    },
    clearIdiomaFilter(props) {
      props.filters[props.column.field] = '';
      this.$store.commit("changeIdiomaFilter", "");
    },
    fechaBaseHoy() {
      this.fechaBase = new Date();
      this.guardarFechaBase();
    },
    cambioFechaNovedades() {
      if (this.soloNovedades) {
        this.loadAsyncData();
      }
      this.guardarFechaBase();
    },
    cambioPorPagina() {
      this.loadAsyncData();
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
    cambioOcultarDescartados() {
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
          if (libro.descartado && this.ocultarDescartados) {
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
    axios
      .get("/librarian/preferences/valores_por_defecto")
      .then(({ data }) => {
        if (data) {
          this.soloIdiomasFavoritos = data.idiomasPreferidosMarcado;
          this.soloAutoresFavoritos = data.autoresPreferidosMarcado;
          this.soloGenerosFavoritos = data.generosPreferidosMarcado;
          this.ocultarDescartados = data.descartadosOcultosMarcado;
          this.soloNoEnPropiedad = data.soloNoEnPropiedadMarcado;          
        }
        this.loadAsyncData();
      })
      .catch(error => {
        throw error;
      });    
    EventBus.$on('updatedCalibre', () => {
      this.loadAsyncData();      
    });
    EventBus.$on('updatedData', (payload) => {
      if (payload === 'Correct') {
        this.loadAsyncData();
      }
    });
  }
};
</script>

<style>
#fecha_base label {
  width: max-content;
}

#fecha_base {
  margin-bottom: 1em;
  padding-bottom: 1.5em;
  float: left;
  width: 30%
}

#pagina {
  text-align: center;
  width: 5em;
}

p.control {
  padding-top: 0.25em;
  margin-left: 1em;
}

#paginacion .field-label {
  margin-right: 0.5em;
}

div.fechaBaseSelector {
  width: 10em;
}

.onlyFilter .switch{
  margin-right: 0;
}

#numero_por_pagina {
  float: right;
  margin-bottom: 1em;
  white-space: nowrap;
  margin-left: 1em;
}

</style>