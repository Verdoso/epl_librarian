<template>
  <section>
    <div>
      <b-field>
        <p class="control">
          <b-switch
            v-model="soloIdiomasFavoritos"
            @input="cambioIdiomasFavoritos()"
          >Solo idiomas favoritos</b-switch>
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
      :checked-rows.sync="checkedRows"
      :is-row-checkable="(row) => true"
      checkable
      checkbox-position="left"
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
      @dblclick="onSelectIdioma"
    >
      <template slot-scope="props">
        <b-table-column field="POR_IDIOMA" label="Nombre" sortable searchable>{{ props.row.nombre }}</b-table-column>

        <b-table-column field="POR_LIBROS" label="# libros" sortable>{{ props.row.libros }}</b-table-column>
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
  data() {
    return {
      data: [],
      checkedRows: [],
      total: 0,
      loading: false,
      sortField: "POR_IDIOMA",
      sortOrder: "asc",
      defaultSortOrder: "asc",
      page: 1,
      filters: null,
      perPage: 15,
      soloIdiomasFavoritos: false
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
        `filtro_idioma=${this.filterOnCriteria("POR_IDIOMA")}`,
        `por_pagina=${this.perPage}`,
        `favoritos_idiomas=${this.soloIdiomasFavoritos}`
      ].join("&");

      this.loading = true;
      limiter.schedule(() => axios.get(`/librarian/idiomas?${params}`))
        .then(({ data }) => {
          this.data = [];
          this.checkedRows = [];
          this.total = data.total;
          data.results.forEach(item => {
            this.data.push(item);
            if (item.favorito) {
              this.checkedRows.push(item);
            }
          });
          this.loading = false;
        })
        .catch(error => {
          this.data = [];
          this.checkedRows = [];
          this.total = 0;
          this.loading = false;
          throw error;
        });
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
    /*
     * Handle filter-change event
     */
    onFilterChange(filters) {
      this.filters = filters;
      this.loadAsyncData();
    },
    /*
     * Handle sort event
     */
    onSort(field, order) {
      this.sortField = field;
      this.sortOrder = order;
      this.loadAsyncData();
    },
    /*
     * Handle click event
     */
    onSelectIdioma(row) {
      this.$store.commit("changeIdiomaFilter", row.nombre);
      this.$store.commit("changeTab", "biblioteca");
    },
    containsObject(obj, list) {
      return list.some(elem => elem === obj);
    },
    cambioIdiomasFavoritos() {
      this.loadAsyncData();
    },
    guardarFavoritos() {
      var formData = new FormData();
      formData.append(
        "idiomasFavoritos",
        this.checkedRows.map(row => row.nombre).join(",")
      );
      formData.append(
        "idiomasNoFavoritos",
        this.data
          .filter(row => !this.containsObject(row, this.checkedRows))
          .map(row => row.nombre)
          .join(",")
      );
      axios
        .post("/librarian/preferences/idiomasFavoritos", formData, {
          headers: { "Content-Type": "multipart/form-data;charset=UTF-8" }
        })
        .then(response => {
          this.$store.commit("markUpdate");
        })
        .catch(error => {
          this.$buefy.notification.open({
            type: "is-danger",
            duration: 5000,
            message: "Error almacenando idiomas favoritos: " + e,
            hasIcon: true
          });
          throw error;
        });
    }
  },
  watch: {
    checkedRows: function() {
      this.guardarFavoritos();
    }
  },
  mounted() {
    this.loadAsyncData();
  }
};
</script>

<style>
.idiomasFavoritosCounter {
  text-align: right;
  width: 4em;
}
</style>