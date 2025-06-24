package org.example;

import java.util.Objects;

public class Vertice<T> {
    private static int nextId = 0;
    private int id;
    private T valor;

    public Vertice(T valor) {
        this.valor = valor;
        this.id = nextId++;
    }

    public int getId() {
        return this.id;
    }

    public T getValor() {
        return this.valor;
    }

    public String toString() {
        int var10000 = this.id;
        return "Vertice(ID: " + var10000 + ", Valor: " + String.valueOf(this.valor) + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Vertice<?> vertice = (Vertice)o;
            return this.id == vertice.id && Objects.equals(this.valor, vertice.valor);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.valor});
    }
}
