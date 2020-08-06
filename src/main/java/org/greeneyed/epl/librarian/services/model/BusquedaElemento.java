package org.greeneyed.epl.librarian.services.model;

import lombok.Data;

@Data
public class BusquedaElemento<O> {
    private final int numeroPagina;
    private final int porPagina;
    private final ElementOrdering<O> ordering;
    private final boolean reversed;
    private final boolean soloFavoritos;
    private final String filtro;

    public static interface ElementOrdering<T> {
        Class<T> getOrderingClass();
        String name();
    }
}