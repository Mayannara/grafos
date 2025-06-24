package org.example;

import java.io.PrintStream;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        Grafo<String> grafo = new Grafo();
        System.out.println("Adicionando vértices...");
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarVertice("D");
        grafo.adicionarVertice("E");
        grafo.adicionarVertice("F");
        grafo.adicionarVertice("G");
        grafo.imprimirGrafo();
        System.out.println("Adicionando arestas...");
        grafo.adicionarAresta("A", "B", 4.0);
        grafo.adicionarAresta("A", "C", 2.0);
        grafo.adicionarAresta("B", "E", 3.0);
        grafo.adicionarAresta("C", "D", 2.0);
        grafo.adicionarAresta("C", "F", 4.0);
        grafo.adicionarAresta("D", "E", 3.0);
        grafo.adicionarAresta("D", "F", 1.0);
        grafo.adicionarAresta("E", "F", 1.0);
        grafo.adicionarAresta("F", "G", 5.0);
        grafo.imprimirGrafo();
        grafo.caminhamentoEmLargura("A");
        System.out.println("\n--- Executando Algoritmo de Dijkstra (Origem e Destino) ---");
        grafo.dijkstra("A", "E");
        grafo.dijkstra("A", "G");
        grafo.dijkstra("A", "A");
        grafo.dijkstra("A", "Z");
        grafo.dijkstra("B");
        System.out.println("\n=================================================");
        System.out.println("ANÁLISE DE CICLOS NO GRAFO");
        System.out.println("=================================================");
        grafo.verificarCiclos();
        System.out.println("=================================================");
        System.out.println("COMPARAÇÃO: TESTANDO UMA ÁRVORE (SEM CICLOS)");
        System.out.println("=================================================");
        Grafo<String> arvore = new Grafo();
        arvore.adicionarVertice("Raiz");
        arvore.adicionarVertice("Filho1");
        arvore.adicionarVertice("Filho2");
        arvore.adicionarVertice("Neto1");
        arvore.adicionarVertice("Neto2");
        arvore.adicionarAresta("Raiz", "Filho1", 1.0);
        arvore.adicionarAresta("Raiz", "Filho2", 1.0);
        arvore.adicionarAresta("Filho1", "Neto1", 1.0);
        arvore.adicionarAresta("Filho2", "Neto2", 1.0);
        arvore.verificarCiclos();
        System.out.println("=================================================");
        System.out.println("RESUMO FINAL");
        System.out.println("=================================================");
        PrintStream var10000 = System.out;
        String var10001 = grafo.temCiclo() ? "SIM" : "NÃO";
        var10000.println("Grafo original tem ciclos: " + var10001);
        System.out.println("Árvore de teste tem ciclos: " + (arvore.temCiclo() ? "SIM" : "NÃO"));
    }
}
