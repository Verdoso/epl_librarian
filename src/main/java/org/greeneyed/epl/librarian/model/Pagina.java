package org.greeneyed.epl.librarian.model;

import java.util.List;

import lombok.Data;

@Data
public class Pagina<T> {
  private List<T> results;
  private int total;
}