package AppGraficodoGrafo;


import java.util.Objects;

public class Vertice<T> {
    private T valor;
    private int x, y; // Coordenadas para a GUI
    private boolean isSelected; // Indica se o vértice está selecionado (para Dijkstra)
    private boolean isPathHighlighted; // Indica se o vértice faz parte de um caminho destacado

    public Vertice(T valor) {
        this.valor = valor;
        this.x = 0; // Posições iniciais
        this.y = 0;
        this.isSelected = false;
        this.isPathHighlighted = false;
    }

    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        this.valor = valor;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isPathHighlighted() {
        return isPathHighlighted;
    }

    public void setPathHighlighted(boolean pathHighlighted) {
        isPathHighlighted = pathHighlighted;
    }

    @Override
    public String toString() {
        return "Vertice{" + "valor=" + valor + ", x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertice<?> vertice = (Vertice<?>) o;
        return Objects.equals(valor, vertice.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}