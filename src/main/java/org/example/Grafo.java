package org.example;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Grafo<T> {
    private ArrayList<Vertice<T>> vertices;
    private double[][] matrizAdjacencias;
    private Map<T, Integer> mapaValorParaIndice;

    public Grafo() {
        this.vertices = new ArrayList<>();
        this.matrizAdjacencias = new double[0][0];
        this.mapaValorParaIndice = new HashMap<>();
    }

    private int obterIndiceVertice(T valor) {
        return this.mapaValorParaIndice.getOrDefault(valor, -1);
    }

    public Vertice<T> adicionarVertice(T valor) {
        if (this.obterIndiceVertice(valor) != -1) {
            System.out.println("Vértice com valor '" + valor + "' já existe no grafo.");
            return this.vertices.get(this.obterIndiceVertice(valor));
        } else {
            Vertice<T> novoVertice = new Vertice<>(valor);
            this.vertices.add(novoVertice);
            int novoIndice = this.vertices.size() - 1;
            this.mapaValorParaIndice.put(valor, novoIndice);

            // Redimensiona a matriz de adjacências
            double[][] novaMatriz = new double[this.vertices.size()][this.vertices.size()];
            for (int i = 0; i < this.matrizAdjacencias.length; ++i) {
                System.arraycopy(this.matrizAdjacencias[i], 0, novaMatriz[i], 0, this.matrizAdjacencias[i].length);
            }
            this.matrizAdjacencias = novaMatriz;

            System.out.println("Vértice '" + valor + "' adicionado.");
            return novoVertice;
        }
    }

    public void adicionarAresta(T valorOrigem, T valorDestino, double peso) {
        int indiceOrigem = this.obterIndiceVertice(valorOrigem);
        int indiceDestino = this.obterIndiceVertice(valorDestino);

        if (indiceOrigem == -1) {
            System.err.println("Erro: Vértice de origem '" + valorOrigem + "' não encontrado.");
        } else if (indiceDestino == -1) {
            System.err.println("Erro: Vértice de destino '" + valorDestino + "' não encontrado.");
        } else if (indiceOrigem == indiceDestino) {
            System.err.println("Erro: Não é possível adicionar uma aresta de um vértice para ele mesmo.");
        }
        else {
            this.matrizAdjacencias[indiceOrigem][indiceDestino] = peso;
            this.matrizAdjacencias[indiceDestino][indiceOrigem] = peso; // Para grafos não direcionados
            System.out.println("Aresta adicionada: " + valorOrigem + " <-> " + valorDestino + " (Peso: " + peso + ")");
        }
    }

    public void caminhamentoEmLargura(T valorInicial) {
        int indiceInicial = this.obterIndiceVertice(valorInicial);
        if (indiceInicial == -1) {
            System.err.println("Erro: Vértice inicial '" + valorInicial + "' não encontrado para o caminhamento em largura.");
        } else {
            boolean[] visitados = new boolean[this.vertices.size()];
            Queue<Integer> fila = new LinkedList<>();
            fila.offer(indiceInicial);
            visitados[indiceInicial] = true;
            System.out.println("\n--- Caminhamento em Largura (BFS) a partir de '" + valorInicial + "' ---");

            while (!fila.isEmpty()) {
                int indiceAtual = fila.poll();
                Vertice<T> verticeAtual = this.vertices.get(indiceAtual);
                System.out.println("Visitando: " + verticeAtual.getValor());

                for (int i = 0; i < this.vertices.size(); ++i) {
                    if (this.matrizAdjacencias[indiceAtual][i] != 0.0 && !visitados[i]) {
                        visitados[i] = true;
                        fila.offer(i);
                    }
                }
            }
            System.out.println("--- Fim do Caminhamento ---");
        }
    }

    public Map<T, Double> dijkstra(T valorOrigem) {
        return this.dijkstra(valorOrigem, null);
    }

    public Map<T, Double> dijkstra(T valorOrigem, T valorDestino) {
        int indiceOrigem = this.obterIndiceVertice(valorOrigem);
        if (indiceOrigem == -1) {
            System.err.println("Erro: Vértice de origem '" + valorOrigem + "' não encontrado para Dijkstra.");
            return null;
        } else {
            int indiceDestino = -1;
            if (valorDestino != null) {
                indiceDestino = this.obterIndiceVertice(valorDestino);
                if (indiceDestino == -1) {
                    System.err.println("Erro: Vértice de destino '" + valorDestino + "' não encontrado para Dijkstra.");
                    return null;
                }
            }

            Map<T, Double> distancias = new HashMap<>();
            Map<T, T> predecessores = new HashMap<>();
            Set<Integer> visitados = new HashSet<>();

            for (Vertice<T> v : this.vertices) {
                distancias.put(v.getValor(), Double.POSITIVE_INFINITY);
            }
            distancias.put(valorOrigem, 0.0);

            System.out.println("\n--- Executando Dijkstra de '" + valorOrigem + "' para " + (valorDestino != null ? "'" + valorDestino + "'" : "todos os vértices") + " ---");

            for (int count = 0; count < this.vertices.size(); ++count) {
                int u = this.encontrarVerticeComMenorDistancia(distancias, visitados);

                if (u == -1 || (valorDestino != null && u == indiceDestino)) {
                    break;
                }

                visitados.add(u);

                for (int v = 0; v < this.vertices.size(); ++v) {
                    // Verifica se há aresta, se o vértice não foi visitado e se a distância de 'u' é finita
                    if (this.matrizAdjacencias[u][v] != 0.0 && !visitados.contains(v) && distancias.get(this.vertices.get(u).getValor()) != Double.POSITIVE_INFINITY) {
                        double novaDistancia = distancias.get(this.vertices.get(u).getValor()) + this.matrizAdjacencias[u][v];
                        if (novaDistancia < distancias.get(this.vertices.get(v).getValor())) {
                            distancias.put(this.vertices.get(v).getValor(), novaDistancia);
                            predecessores.put(this.vertices.get(v).getValor(), this.vertices.get(u).getValor());
                        }
                    }
                }
            }

            if (valorDestino != null) {
                Double distancia = distancias.get(valorDestino);
                System.out.print("Caminho mais curto de '" + valorOrigem + "' para '" + valorDestino + "': Distância = " + String.format("%.2f", distancia));
                List<T> caminho = this.reconstruirCaminho(predecessores, valorOrigem, valorDestino);
                System.out.println(" | Caminho: " + caminho);
            } else {
                for (Map.Entry<T, Double> entry : distancias.entrySet()) {
                    T destino = entry.getKey();
                    Double distancia = entry.getValue();
                    System.out.print("Caminho mais curto de '" + valorOrigem + "' para " + destino + ": Distância = " + String.format("%.2f", distancia));
                    List<T> caminho = this.reconstruirCaminho(predecessores, valorOrigem, destino);
                    System.out.println(" | Caminho: " + caminho);
                }
            }
            return distancias;
        }
    }

    private int encontrarVerticeComMenorDistancia(Map<T, Double> distancias, Set<Integer> visitados) {
        double menorDistancia = Double.POSITIVE_INFINITY;
        int indiceMenorDistancia = -1;

        for (int i = 0; i < this.vertices.size(); ++i) {
            if (!visitados.contains(i)) {
                T valorVertice = this.vertices.get(i).getValor();
                if (distancias.containsKey(valorVertice) && distancias.get(valorVertice) < menorDistancia) {
                    menorDistancia = distancias.get(valorVertice);
                    indiceMenorDistancia = i;
                }
            }
        }
        return indiceMenorDistancia;
    }

    private List<T> reconstruirCaminho(Map<T, T> predecessores, T origem, T destino) {
        List<T> caminho = new LinkedList<>();
        // Se não há predecessor para o destino e não é o próprio destino, não há caminho
        if (!predecessores.containsKey(destino) && !origem.equals(destino)) {
            return Collections.emptyList();
        }

        T atual = destino;
        while (atual != null && !atual.equals(origem)) {
            caminho.add(atual);
            atual = predecessores.get(atual);
        }

        // Adiciona o vértice de origem se ele foi alcançado
        if (atual != null && atual.equals(origem)) {
            caminho.add(origem);
        } else if (!origem.equals(destino)) { // Se origem e destino são diferentes, e origem não foi alcançada, não há caminho completo
            return Collections.emptyList();
        }

        Collections.reverse(caminho);
        return caminho;
    }

    public void imprimirGrafo() {
        System.out.println("\n--- Representação do Grafo ---");
        System.out.println("Vértices:");
        for (Vertice<T> vertice : this.vertices) {
            System.out.println("  " + vertice.getValor());
        }

        System.out.println("\nMatriz de Adjacências:");
        System.out.print("       ");
        for (Vertice<T> vertice : this.vertices) {
            System.out.printf("%-7s", vertice.getValor());
        }
        System.out.println();
        System.out.print("       ");
        for (int i = 0; i < this.vertices.size(); ++i) {
            System.out.print("-------");
        }
        System.out.println();

        for (int i = 0; i < this.matrizAdjacencias.length; ++i) {
            System.out.printf("%-5s |", this.vertices.get(i).getValor());
            for (int j = 0; j < this.matrizAdjacencias[i].length; ++j) {
                System.out.printf("%-7.1f", this.matrizAdjacencias[i][j]);
            }
            System.out.println();
        }
        System.out.println("----------------------------\n");
    }

    public void verificarCiclos() {
        System.out.println("\n--- VERIFICAÇÃO DE CICLOS ---");
        boolean[] visitados = new boolean[this.vertices.size()];
        // Usar Set para evitar ciclos duplicados, considerando rotações e inversões
        Set<List<T>> ciclosEncontrados = new HashSet<>();
        boolean temCiclo = false;

        for (int i = 0; i < this.vertices.size(); ++i) {
            if (!visitados[i]) {
                Stack<T> caminhoAtual = new Stack<>();
                // O método dfsEncontrarCiclos agora retorna uma lista de ciclos encontrados para adicionar ao conjunto
                List<List<T>> ciclosNoComponente = dfsEncontrarCiclos(i, -1, new boolean[this.vertices.size()], caminhoAtual);
                if (!ciclosNoComponente.isEmpty()) {
                    temCiclo = true;
                    ciclosEncontrados.addAll(ciclosNoComponente);
                }
            }
        }

        if (temCiclo) {
            System.out.println("RESULTADO: O grafo CONTÉM ciclos!");
            System.out.println("\n\uD83D\uDCCB Ciclos encontrados:");

            int count = 1;
            for (List<T> ciclo : ciclosEncontrados) {
                System.out.print("   Ciclo " + count++ + ": ");
                for (int j = 0; j < ciclo.size(); ++j) {
                    System.out.print(ciclo.get(j));
                    if (j < ciclo.size() - 1) {
                        System.out.print(" → ");
                    }
                }
                if (!ciclo.isEmpty()) {
                    System.out.print(" → " + ciclo.get(0)); // Completa o ciclo visualmente
                }
                System.out.println();
            }
            System.out.println("\n\uD83D\uDCA1 Explicação: Um ciclo é um caminho que começa e termina no mesmo vértice.");
        } else {
            System.out.println("RESULTADO: O grafo NÃO contém ciclos!");
            System.out.println("Este é um grafo acíclico (árvore ou floresta).");
        }
        System.out.println("--- Fim da Verificação ---\n");
    }

    private List<List<T>> dfsEncontrarCiclos(int verticeAtual, int verticePai, boolean[] visitadosDFS, Stack<T> caminhoAtual) {
        visitadosDFS[verticeAtual] = true;
        caminhoAtual.push(this.vertices.get(verticeAtual).getValor());
        List<List<T>> ciclosComponente = new ArrayList<>();

        for (int i = 0; i < this.vertices.size(); ++i) {
            if (this.matrizAdjacencias[verticeAtual][i] != 0.0) {
                if (!visitadosDFS[i]) {
                    ciclosComponente.addAll(dfsEncontrarCiclos(i, verticeAtual, visitadosDFS, caminhoAtual));
                } else if (i != verticePai) {
                    // Ciclo encontrado
                    List<T> ciclo = extrairCiclo(caminhoAtual, this.vertices.get(i).getValor());
                    if (!ciclo.isEmpty()) {
                        ciclosComponente.add(ciclo);
                    }
                }
            }
        }
        caminhoAtual.pop();
        return ciclosComponente;
    }

    private List<T> extrairCiclo(Stack<T> caminho, T verticeInicioCiclo) {
        List<T> ciclo = new ArrayList<>();
        int indiceCiclo = -1;

        // Encontra o índice do vértice de início do ciclo no caminho atual
        for (int i = caminho.size() - 1; i >= 0; --i) {
            if (caminho.get(i).equals(verticeInicioCiclo)) {
                indiceCiclo = i;
                break;
            }
        }

        if (indiceCiclo != -1) {
            // Adiciona os elementos do ciclo
            for (int i = indiceCiclo; i < caminho.size(); ++i) {
                ciclo.add(caminho.get(i));
            }
        }
        return normalizeCycle(ciclo);
    }

    // Normaliza um ciclo para que rotações e inversões sejam consideradas o mesmo ciclo
    private List<T> normalizeCycle(List<T> cycle) {
        if (cycle.isEmpty()) {
            return cycle;
        }

        List<T> normalized = new ArrayList<>(cycle);
        Collections.sort(normalized, (a, b) -> a.toString().compareTo(b.toString())); // Ordena para ter um ponto de partida consistente

        return normalized;
    }

    // Compara dois ciclos de forma robusta, considerando rotações e inversões
    private boolean saoMesmoCiclo(List<T> ciclo1, List<T> ciclo2) {
        if (ciclo1.size() != ciclo2.size()) {
            return false;
        }

        // Converte para String para facilitar a comparação para tipos T genéricos
        List<String> sCiclo1 = new ArrayList<>();
        for (T item : ciclo1) {
            sCiclo1.add(item.toString());
        }

        List<String> sCiclo2 = new ArrayList<>();
        for (T item : ciclo2) {
            sCiclo2.add(item.toString());
        }

        Collections.sort(sCiclo1);
        Collections.sort(sCiclo2);

        return sCiclo1.equals(sCiclo2);
    }


    public boolean temCiclo() {
        boolean[] visitados = new boolean[this.vertices.size()];
        boolean[] emPilhaRecursao = new boolean[this.vertices.size()]; // Usado para detectar ciclos em grafos direcionados

        for (int i = 0; i < this.vertices.size(); ++i) {
            if (!visitados[i] && dfsVerificaCicloSimples(i, -1, visitados, emPilhaRecursao)) {
                return true;
            }
        }
        return false;
    }

    // Este DFS é para detectar *qualquer* ciclo, não para listar todos
    private boolean dfsVerificaCicloSimples(int verticeAtual, int verticePai, boolean[] visitados, boolean[] emPilhaRecursao) {
        visitados[verticeAtual] = true;
        emPilhaRecursao[verticeAtual] = true; // Marca como estando no caminho atual da recursão

        for (int i = 0; i < this.vertices.size(); ++i) {
            if (this.matrizAdjacencias[verticeAtual][i] != 0.0) { // Se há uma aresta
                if (!visitados[i]) {
                    if (dfsVerificaCicloSimples(i, verticeAtual, visitados, emPilhaRecursao)) {
                        return true;
                    }
                } else if (i != verticePai) { // Se o vizinho já foi visitado e não é o pai, é um ciclo
                    return true;
                }
            }
        }
        emPilhaRecursao[verticeAtual] = false; // Remove do caminho da recursão ao sair
        return false;
    }

    public boolean estaVazio() {
        return this.vertices.isEmpty();
    }

    public int getNumeroVertices() {
        return this.vertices.size();
    }

    public int getNumeroArestas() {
        int arestas = 0;
        for (int i = 0; i < this.matrizAdjacencias.length; ++i) {
            for (int j = i + 1; j < this.matrizAdjacencias[i].length; ++j) { // Começa de i+1 para evitar contar duplicatas (grafo não direcionado)
                if (this.matrizAdjacencias[i][j] != 0.0) {
                    ++arestas;
                }
            }
        }
        return arestas;
    }

    public List<T> getVertices() {
        List<T> listaVertices = new ArrayList<>();
        for (Vertice<T> vertice : this.vertices) {
            listaVertices.add(vertice.getValor());
        }
        return listaVertices;
    }
}