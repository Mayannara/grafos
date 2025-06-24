package AppGraficodoGrafo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    // Retorna o índice de um vértice pelo seu valor.
    public int obterIndiceVertice(T valor) { // Mudado para public para ser acessível externamente
        return mapaValorParaIndice.getOrDefault(valor, -1);
    }

    // Adiciona um novo vértice ao grafo.
    public Vertice<T> adicionarVertice(T valor) {
        if (obterIndiceVertice(valor) == -1) {
            Vertice<T> novoVertice = new Vertice<>(valor);
            vertices.add(novoVertice);
            int novoIndice = vertices.size() - 1;
            mapaValorParaIndice.put(valor, novoIndice);

            // Redimensiona a matriz de adjacências para incluir o novo vértice.
            double[][] novaMatriz = new double[vertices.size()][vertices.size()];
            for (int i = 0; i < matrizAdjacencias.length; i++) {
                System.arraycopy(matrizAdjacencias[i], 0, novaMatriz[i], 0, matrizAdjacencias[i].length);
            }
            this.matrizAdjacencias = novaMatriz;
            return novoVertice;
        } else {
            return vertices.get(obterIndiceVertice(valor));
        }
    }

    // Adiciona uma aresta entre dois vértices com um dado peso.
    public void adicionarAresta(T valorOrigem, T valorDestino, double peso) {
        int indiceOrigem = obterIndiceVertice(valorOrigem);
        int indiceDestino = obterIndiceVertice(valorDestino);

        if (indiceOrigem == -1 || indiceDestino == -1) {
            System.err.println("Erro: Um dos vértices não foi encontrado ao adicionar aresta.");
            return;
        }

        matrizAdjacencias[indiceOrigem][indiceDestino] = peso;
        matrizAdjacencias[indiceDestino][indiceOrigem] = peso; // Para grafos não direcionados
    }

    // Retorna a lista de vértices do grafo.
    public ArrayList<Vertice<T>> getVertices() {
        return vertices;
    }

    // Retorna a matriz de adjacências do grafo.
    public double[][] getMatrizAdjacencias() {
        return matrizAdjacencias;
    }

    // Retorna o peso da aresta entre dois vértices.
    public double getPesoAresta(T valorOrigem, T valorDestino) {
        int indiceOrigem = obterIndiceVertice(valorOrigem);
        int indiceDestino = obterIndiceVertice(valorDestino);
        if (indiceOrigem != -1 && indiceDestino != -1) {
            return matrizAdjacencias[indiceOrigem][indiceDestino];
        }
        return 0;
    }

    // --- Métodos de caminhamento e algoritmos (existentes no seu código) ---

    public void caminhamentoEmLargura(T valorInicial) {
        int indiceInicial = obterIndiceVertice(valorInicial);
        if (indiceInicial == -1) {
            System.err.println("Erro: Vértice inicial '" + valorInicial + "' não encontrado para o caminhamento em largura.");
            return;
        }

        boolean[] visitados = new boolean[vertices.size()];
        Queue<Integer> fila = new LinkedList<>();

        fila.offer(indiceInicial);
        visitados[indiceInicial] = true;

        System.out.println("\n--- Caminhamento em Largura (BFS) a partir de '" + valorInicial + "' ---");
        while (!fila.isEmpty()) {
            int indiceAtual = fila.poll();
            Vertice<T> verticeAtual = vertices.get(indiceAtual);
            System.out.println("Visitando: " + verticeAtual.getValor());

            for (int i = 0; i < vertices.size(); i++) {
                if (matrizAdjacencias[indiceAtual][i] != 0 && !visitados[i]) {
                    visitados[i] = true;
                    fila.offer(i);
                }
            }
        }
        System.out.println("--- Fim do Caminhamento ---");
    }

    // --- Dijkstra (Origem para Destino Específico ou Todos) ---
    // Retorna um mapa com distâncias e, opcionalmente, o caminho para o destino
    public class DijkstraResult {
        public List<T> path;
        public double cost;

        public DijkstraResult(List<T> path, double cost) {
            this.path = path;
            this.cost = cost;
        }
    }

    public DijkstraResult dijkstra(T valorOrigem, T valorDestino) {
        int indiceOrigem = obterIndiceVertice(valorOrigem);
        int indiceDestino = obterIndiceVertice(valorDestino);

        if (indiceOrigem == -1 || indiceDestino == -1) {
            return new DijkstraResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        Map<T, Double> distancias = new HashMap<>();
        Map<T, T> predecessores = new HashMap<>();
        Set<Integer> visitados = new HashSet<>();

        for (Vertice<T> v : vertices) {
            distancias.put(v.getValor(), Double.POSITIVE_INFINITY);
        }
        distancias.put(valorOrigem, 0.0);

        for (int count = 0; count < vertices.size(); count++) {
            int u = encontrarVerticeComMenorDistancia(distancias, visitados);

            if (u == -1) {
                break;
            }

            if (u == indiceDestino) { // Parar se o destino for alcançado
                break;
            }

            visitados.add(u);

            for (int v = 0; v < vertices.size(); v++) {
                if (matrizAdjacencias[u][v] != 0 && !visitados.contains(v) && distancias.get(vertices.get(u).getValor()) != Double.POSITIVE_INFINITY) {
                    double novaDistancia = distancias.get(vertices.get(u).getValor()) + matrizAdjacencias[u][v];
                    if (novaDistancia < distancias.get(vertices.get(v).getValor())) {
                        distancias.put(vertices.get(v).getValor(), novaDistancia);
                        predecessores.put(vertices.get(v).getValor(), vertices.get(u).getValor());
                    }
                }
            }
        }

        List<T> caminho = reconstruirCaminho(predecessores, valorOrigem, valorDestino);
        double custo = distancias.getOrDefault(valorDestino, Double.POSITIVE_INFINITY);
        return new DijkstraResult(caminho, custo);
    }

    private int encontrarVerticeComMenorDistancia(Map<T, Double> distancias, Set<Integer> visitados) {
        double menorDistancia = Double.POSITIVE_INFINITY;
        int indiceMenorDistancia = -1;

        for (int i = 0; i < vertices.size(); i++) {
            if (!visitados.contains(i)) {
                T valorVertice = vertices.get(i).getValor();
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
        if (!predecessores.containsKey(destino) && !origem.equals(destino)) {
            return Collections.emptyList();
        }

        T atual = destino;
        while (atual != null && !atual.equals(origem)) {
            caminho.add(atual);
            atual = predecessores.get(atual);
        }
        if (atual != null && atual.equals(origem)) {
            caminho.add(origem);
        } else if (!origem.equals(destino)) {
            return Collections.emptyList();
        }
        Collections.reverse(caminho);
        return caminho;
    }


    public void imprimirGrafo() {
        System.out.println("\n--- Representação do Grafo ---");
        System.out.println("Vértices:");
        for (Vertice<T> vertice : vertices) {
            System.out.println("  " + vertice);
        }

        System.out.println("\nMatriz de Adjacências:");
        System.out.print("       ");
        for (Vertice<T> vertice : vertices) {
            System.out.printf("%-7s", vertice.getValor());
        }
        System.out.println();
        System.out.print("       ");
        for (int i = 0; i < vertices.size(); i++) {
            System.out.print("-------");
        }
        System.out.println();

        for (int i = 0; i < matrizAdjacencias.length; i++) {
            System.out.printf("%-5s |", vertices.get(i).getValor());
            for (int j = 0; j < matrizAdjacencias[i].length; j++) {
                System.out.printf("%-7.1f", matrizAdjacencias[i][j]);
            }
            System.out.println();
        }
        System.out.println("----------------------------\n");
    }


    /**
     * Verifica se o grafo contém ciclos e identifica quais são
     * Retorna a lista de ciclos encontrados.
     */
    public List<List<T>> verificarCiclos() {
        List<List<T>> ciclosEncontrados = new ArrayList<>();
        if (vertices.isEmpty()) {
            return ciclosEncontrados; // Grafo vazio não tem ciclos
        }

        boolean[] visitados = new boolean[vertices.size()];

        // Verifica cada componente conectado
        for (int i = 0; i < vertices.size(); i++) {
            if (!visitados[i]) {
                Stack<T> caminhoAtual = new Stack<>();
                dfsEncontrarCiclos(i, -1, visitados, caminhoAtual, ciclosEncontrados);
            }
        }
        return ciclosEncontrados;
    }

    /**
     * DFS que encontra e registra os ciclos específicos
     */
    private boolean dfsEncontrarCiclos(int verticeAtual, int verticePai, boolean[] visitados,
                                       Stack<T> caminhoAtual, List<List<T>> ciclosEncontrados) {

        visitados[verticeAtual] = true;
        caminhoAtual.push(vertices.get(verticeAtual).getValor());

        boolean encontrouCiclo = false;

        // Examina todos os vizinhos
        for (int i = 0; i < vertices.size(); i++) {
            if (matrizAdjacencias[verticeAtual][i] != 0) {

                if (!visitados[i]) {
                    // Continua a busca
                    if (dfsEncontrarCiclos(i, verticeAtual, visitados, caminhoAtual, ciclosEncontrados)) {
                        encontrouCiclo = true;
                    }
                }
                else if (i != verticePai) {
                    // Encontrou um ciclo! Extrai o ciclo do caminho atual
                    List<T> ciclo = extrairCiclo(caminhoAtual, vertices.get(i).getValor());
                    if (!ciclo.isEmpty() && !cicloJaEncontrado(ciclo, ciclosEncontrados)) {
                        ciclosEncontrados.add(ciclo);
                        encontrouCiclo = true;
                    }
                }
            }
        }

        caminhoAtual.pop(); // Remove o vértice atual do caminho
        return encontrouCiclo;
    }

    /**
     * Extrai o ciclo específico do caminho atual
     */
    private List<T> extrairCiclo(Stack<T> caminho, T verticeInicioCiclo) {
        List<T> ciclo = new ArrayList<>();
        List<T> caminhoLista = new ArrayList<>(caminho);

        // Encontra onde o ciclo começa
        int indiceCiclo = -1;
        for (int i = caminhoLista.size() - 1; i >= 0; i--) {
            if (caminhoLista.get(i).equals(verticeInicioCiclo)) {
                indiceCiclo = i;
                break;
            }
        }

        // Extrai o ciclo
        if (indiceCiclo != -1) {
            for (int i = indiceCiclo; i < caminhoLista.size(); i++) {
                ciclo.add(caminhoLista.get(i));
            }
        }

        return ciclo;
    }

    /**
     * Verifica se o ciclo já foi encontrado antes (evita duplicatas)
     */
    private boolean cicloJaEncontrado(List<T> novoCiclo, List<List<T>> ciclosExistentes) {
        for (List<T> cicloExistente : ciclosExistentes) {
            if (saoMesmoCiclo(novoCiclo, cicloExistente)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se dois ciclos são o mesmo (considerando rotações)
     */
    private boolean saoMesmoCiclo(List<T> ciclo1, List<T> ciclo2) {
        if (ciclo1.size() != ciclo2.size()) {
            return false;
        }

        // Verifica todas as rotações possíveis
        for (int offset = 0; offset < ciclo1.size(); offset++) {
            boolean igual = true;
            for (int i = 0; i < ciclo1.size(); i++) {
                if (!ciclo1.get(i).equals(ciclo2.get((i + offset) % ciclo2.size()))) {
                    igual = false;
                    break;
                }
            }
            if (igual) return true;
        }

        return false;
    }

    /**
     * Versão simples que só retorna true/false
     */
    public boolean temCiclo() {
        boolean[] visitados = new boolean[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            if (!visitados[i]) {
                if (dfsVerificaCicloSimples(i, -1, visitados)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DFS simples para verificação rápida
     */
    private boolean dfsVerificaCicloSimples(int verticeAtual, int verticePai, boolean[] visitados) {
        visitados[verticeAtual] = true;

        for (int i = 0; i < vertices.size(); i++) {
            if (matrizAdjacencias[verticeAtual][i] != 0) {
                if (!visitados[i]) {
                    if (dfsVerificaCicloSimples(i, verticeAtual, visitados)) {
                        return true;
                    }
                } else if (i != verticePai) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica se o grafo está vazio (sem vértices)
     * @return true se o grafo não tiver vértices
     */
    public boolean estaVazio() {
        return vertices.isEmpty();
    }

    /**
     * Retorna o número de vértices no grafo
     * @return quantidade de vértices
     */
    public int getNumeroVertices() {
        return vertices.size();
    }

    /**
     * Retorna o número de arestas no grafo
     * @return quantidade de arestas
     */
    public int getNumeroArestas() {
        int arestas = 0;
        for (int i = 0; i < matrizAdjacencias.length; i++) {
            for (int j = i + 1; j < matrizAdjacencias[i].length; j++) {
                if (matrizAdjacencias[i][j] != 0) {
                    arestas++;
                }
            }
        }
        return arestas;
    }

    /**
     * Lista todos os valores dos vértices do grafo
     * @return lista com os valores dos vértices
     */
    public List<T> getValoresVertices() {
        List<T> listaVertices = new ArrayList<>();
        for (Vertice<T> vertice : vertices) {
            listaVertices.add(vertice.getValor());
        }
        return listaVertices;
    }
}