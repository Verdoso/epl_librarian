<template>
  <section>
    <b-table
      ref="table"
      :data="data"
      :loading="loading"
      :total="total"
      :per-page="perPage"
      :default-sort-direction="defaultSortOrder"
      :default-sort="[sortField, sortOrder]"
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
      @dblclick="onSelectGenero"
    >
      <template slot-scope="props">
        <b-table-column
          field="POR_GENERO"
          label="Nombre"
          sortable
          searchable
        >{{ props.row.nombre }}</b-table-column>

        <b-table-column
          field="POR_LIBROS"
          label="# libros"
          sortable
        >{{ props.row.libros }}</b-table-column>
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
  data() {
    return {
      data: [],
      total: 0,
      loading: false,
      sortField: "POR_GENERO",
      sortOrder: "asc",
      defaultSortOrder: "asc",
      page: 1,
      filters: null,
      perPage: 15
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
        `filtro_genero=${this.filterOnCriteria("POR_GENERO")}`,
        `por_pagina=${this.perPage}`
      ].join("&");

      this.loading = true;
      axios
        .get(`/librarian/generos?${params}`)
        .then(({ data }) => {
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
    onSelectGenero(row) {
      this.$store.commit('changeGeneroFilter',row.nombre)
      this.$store.commit('changeTab','biblioteca')
    },
    /*
     * Handle filter-change event
     */
    changedTituloFilter(e) {
      log.console('Nothing to do in author')
    }
  },
  mounted() {
    this.loadAsyncData();
  }
};
</script>