package org.example;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.Scanner;


public class SistemaGrafos {
    private Grafo<String> grafo = new Grafo();
    private Scanner scanner;
    private String nomeProblema;

    public SistemaGrafos() {
        this.scanner = new Scanner(System.in);
        this.nomeProblema = "";
    }

    public static void main(String[] args) {
        SistemaGrafos sistema = new SistemaGrafos();
        sistema.executar();
    }

    public void executar() {
        this.exibirBanner();

        while(true) {
            while(true) {
                try {
                    this.exibirMenuPrincipal();
                    int opcao = this.scanner.nextInt();
                    this.scanner.nextLine();
                    switch (opcao) {
                        case 0:
                            System.out.println("\n\ud83c\udfaf Obrigado por usar o Sistema de Grafos!");
                            System.out.println("\ud83d\udcda Continue estudando algoritmos! \ud83d\ude80");
                            return;
                        case 1:
                            this.criarGrafoManualmente();
                            break;
                        case 2:
                            this.carregarGrafoDeArquivo();
                            break;
                        case 3:
                            if (!this.grafoVazio()) {
                                this.menuProblemas();
                            }
                            break;
                        case 4:
                            if (!this.grafoVazio()) {
                                this.grafo.imprimirGrafo();
                            }
                            break;
                        case 5:
                            this.gerarExemplosDeArquivos();
                            break;
                        default:
                            System.out.println(" Opção inválida! Tente novamente.");
                    }

                    if (opcao != 0) {
                        System.out.println("\n  Pressione ENTER para continuar...");
                        this.scanner.nextLine();
                    }
                } catch (InputMismatchException var2) {
                    System.out.println(" Entrada inválida! Digite apenas números.");
                    this.scanner.nextLine();
                } catch (Exception var3) {
                    Exception e = var3;
                    System.out.println(" Erro: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void exibirBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                SISTEMA DE GRAFOS                         ║");
        System.out.println("║          Resolução de Problemas Práticos                 ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Menor Caminho     Detecção de Ciclos                    ║");
        System.out.println("║  Navegação       Análise de Redes                        ║");
        System.out.println("║  Logística        Sistemas Eficientes                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private void exibirMenuPrincipal() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("MENU PRINCIPAL");
        System.out.println("=".repeat(50));
        System.out.println("1️  Criar grafo manualmente");
        System.out.println("2️ Carregar grafo de arquivo");
        System.out.println("3️  Resolver problemas");
        System.out.println("4️  Visualizar grafo atual");
        System.out.println("5️  Gerar exemplos de arquivos");
        System.out.println("0️  Sair");
        System.out.println("=".repeat(50));
        System.out.print(" Escolha uma opção: ");
    }

    private void criarGrafoManualmente() {
        System.out.println("\n\ud83d\udee0️  CRIAÇÃO MANUAL DO GRAFO");
        System.out.println("━".repeat(40));
        System.out.print("\ud83d\udcdd Nome/Descrição do problema: ");
        this.nomeProblema = this.scanner.nextLine();
        this.grafo = new Grafo();
        System.out.print("\ud83d\udd22 Quantos vértices deseja adicionar? ");
        int numVertices = this.scanner.nextInt();
        this.scanner.nextLine();

        String destino;
        for(int i = 0; i < numVertices; ++i) {
            System.out.print("\ud83d\udccd Nome do vértice " + (i + 1) + ": ");
            destino = this.scanner.nextLine().trim();
            this.grafo.adicionarVertice(destino);
        }

        System.out.println("\n\ud83d\udd17 ADIÇÃO DE ARESTAS");
        System.out.println("Digite 'fim' como origem para terminar");

        while(true) {
            System.out.print("\ud83c\udfaf Vértice origem (ou 'fim'): ");
            String origem = this.scanner.nextLine().trim();
            if (origem.equalsIgnoreCase("fim")) {
                System.out.println(" Grafo criado com sucesso!");
                this.grafo.imprimirGrafo();
                return;
            }

            System.out.print("\ud83c\udfaf Vértice destino: ");
            destino = this.scanner.nextLine().trim();
            System.out.print("  Peso da aresta: ");
            double peso = this.scanner.nextDouble();
            this.scanner.nextLine();
            this.grafo.adicionarAresta(origem, destino, peso);
        }
    }

    private void carregarGrafoDeArquivo() {
        System.out.println("\n\ud83d\udcc1 CARREGAMENTO DE ARQUIVO");
        System.out.println("━".repeat(40));
        System.out.print("\ud83d\udcc4 Nome do arquivo (ex: grafo.txt): ");
        String nomeArquivo = this.scanner.nextLine().trim();

        try {
            this.carregarGrafo(nomeArquivo);
            System.out.println("Grafo carregado com sucesso!");
            this.grafo.imprimirGrafo();
        } catch (IOException var3) {
            IOException e = var3;
            System.out.println(" Erro ao carregar arquivo: " + e.getMessage());
            System.out.println("\ud83d\udca1 Dica: Use a opção 5 para gerar exemplos de arquivos");
        }

    }

    private void carregarGrafo(String nomeArquivo) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(nomeArquivo));
        this.grafo = new Grafo();
        boolean primeiraLinha = true;

        String linha;
        while((linha = reader.readLine()) != null) {
            linha = linha.trim();
            if (!linha.isEmpty() && !linha.startsWith("#")) {
                if (primeiraLinha) {
                    this.nomeProblema = linha;
                    primeiraLinha = false;
                } else {
                    String[] partes = linha.split(",");
                    if (partes.length == 3) {
                        String origem = partes[0].trim();
                        String destino = partes[1].trim();
                        double peso = Double.parseDouble(partes[2].trim());
                        this.grafo.adicionarVertice(origem);
                        this.grafo.adicionarVertice(destino);
                        this.grafo.adicionarAresta(origem, destino, peso);
                    }
                }
            }
        }

        reader.close();
    }

    private void menuProblemas() {
        while(true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("\ud83c\udfaf RESOLUÇÃO DE PROBLEMAS");
            if (!this.nomeProblema.isEmpty()) {
                System.out.println("\ud83d\udccb Problema: " + this.nomeProblema);
            }

            System.out.println("=".repeat(50));
            System.out.println("1 \ud83d\uddfa️  Encontrar menor caminho (Dijkstra)");
            System.out.println("2 \ud83d\udd04 Verificar ciclos");
            System.out.println("3 ️  \ud83c\udf10 Análise completa do grafo");
            System.out.println("4️  \ud83d\udd0d Busca em largura (BFS)");
            System.out.println("0️  Voltar ao menu principal");
            System.out.println("=".repeat(50));
            System.out.print("\ud83c\udfaf Escolha o problema: ");

            try {
                int opcao = this.scanner.nextInt();
                this.scanner.nextLine();
                switch (opcao) {
                    case 0 -> {
                        return;
                    }
                    case 1 -> this.resolverMenorCaminho();
                    case 2 -> this.resolverVerificacaoCiclos();
                    case 3 -> this.analiseCompleta();
                    case 4 -> this.resolverBuscaLargura();
                    default -> System.out.println("Opção inválida!");
                }

                if (opcao != 0) {
                    System.out.println("\n Pressione ENTER para continuar...");
                    this.scanner.nextLine();
                }
            } catch (InputMismatchException var2) {
                System.out.println("Entrada inválida! Digite apenas números.");
                this.scanner.nextLine();
            }
        }
    }

    private void resolverMenorCaminho() {
        System.out.println("\n\ud83d\uddfa️  PROBLEMA: MENOR CAMINHO");
        System.out.println("━".repeat(40));
        System.out.println("\ud83d\udca1 Casos de uso: GPS, Roteamento, Logística, Telecomunicações");
        System.out.print("\ud83c\udfaf Vértice de origem: ");
        String origem = this.scanner.nextLine().trim();
        System.out.print("\ud83c\udfaf Vértice de destino (ou ENTER para todos): ");
        String destino = this.scanner.nextLine().trim();
        if (destino.isEmpty()) {
            System.out.println("\n\ud83d\udcca Calculando menores caminhos de '" + origem + "' para todos os vértices...");
            this.grafo.dijkstra(origem);
        } else {
            System.out.println("\n\ud83d\udcca Calculando menor caminho de '" + origem + "' para '" + destino + "'...");
            this.grafo.dijkstra(origem, destino);
        }

        System.out.println("\n\ud83d\udca1 O algoritmo de Dijkstra encontra o caminho com menor custo total.");
    }

    private void resolverVerificacaoCiclos() {
        System.out.println("\n\ud83d\udd04 PROBLEMA: DETECÇÃO DE CICLOS");
        System.out.println("━".repeat(40));
        System.out.println("\ud83d\udca1 Casos de uso: Prevenção de deadlocks, Validação de dependências,");
        System.out.println("   Análise de redes sociais, Detecção de loops em sistemas");
        this.grafo.verificarCiclos();
        if (this.grafo.temCiclo()) {
            System.out.println("IMPORTANTE: Este grafo contém ciclos!");
            System.out.println("   Em alguns contextos, ciclos podem indicar:");
            System.out.println("   • Deadlocks em sistemas");
            System.out.println("   • Dependências circulares");
            System.out.println("   • Rotas alternativas (positivo em redes)");
        } else {
            System.out.println("EXCELENTE: Este é um grafo acíclico!");
            System.out.println("   Vantagens:");
            System.out.println("   • Sem deadlocks");
            System.out.println("   • Hierarquia clara");
            System.out.println("   • Processamento linear possível");
        }

    }

    private void analiseCompleta() {
        System.out.println("\n\ud83c\udf10 ANÁLISE COMPLETA DO GRAFO");
        System.out.println("━".repeat(50));
        System.out.println("\ud83d\udcca INFORMAÇÕES BÁSICAS:");
        PrintStream var10000 = System.out;
        String var10001 = this.nomeProblema.isEmpty() ? "Não especificado" : this.nomeProblema;
        var10000.println("   • Problema: " + var10001);
        System.out.println("\n\ud83d\udd04 ANÁLISE DE CICLOS:");
        this.grafo.verificarCiclos();
        System.out.println("\n\ud83d\udd17 ANÁLISE DE CONECTIVIDADE:");
        System.out.println("   Executando busca em largura de cada vértice...");
        System.out.println("\n\ud83d\udca1 SUGESTÕES DE USO:");
        if (this.grafo.temCiclo()) {
            System.out.println("  Ideal para: Redes de transporte, Redes sociais");
            System.out.println("  Cuidado com: Sistemas de dependências, Pipelines");
        } else {
            System.out.println(" Ideal para: Hierarquias, Fluxos de trabalho, Árvores de decisão");
            System.out.println(" Permite: Ordenação topológica, Processamento em níveis");
        }

    }

    private void resolverBuscaLargura() {
        System.out.println("\n\ud83d\udd0d PROBLEMA: BUSCA EM LARGURA");
        System.out.println("━".repeat(40));
        System.out.println("\ud83d\udca1 Casos de uso: Exploração de redes, Análise de proximidade,");
        System.out.println("   Disseminação de informações, Algoritmos de IA");
        System.out.print("\ud83c\udfaf Vértice inicial para a busca: ");
        String inicio = this.scanner.nextLine().trim();
        this.grafo.caminhamentoEmLargura(inicio);
        System.out.println("\n\ud83d\udca1 A busca em largura explora primeiro os vizinhos mais próximos,");
        System.out.println("   expandindo gradualmente para vértices mais distantes.");
    }

    private void gerarExemplosDeArquivos() {
        System.out.println("\n\ud83d\udcc1 GERAÇÃO DE EXEMPLOS");
        System.out.println("━".repeat(40));

        try {
            this.gerarExemploRedeCidades();
            System.out.println("exemplo_cidades.txt criado");
            this.gerarExemploRedeComputadores();
            System.out.println("exemplo_rede.txt criado");
            this.gerarExemploDependencias();
            System.out.println(" exemplo_dependencias.txt criado");
            System.out.println("\n\ud83d\udccb COMO USAR OS ARQUIVOS:");
            System.out.println("1. Escolha a opção 2 no menu principal");
            System.out.println("2. Digite o nome do arquivo (ex: exemplo_cidades.txt)");
            System.out.println("3. O grafo será carregado automaticamente");
        } catch (IOException var2) {
            IOException e = var2;
            System.out.println(" Erro ao gerar exemplos: " + e.getMessage());
        }

    }

    private void gerarExemploRedeCidades() throws IOException {
        PrintWriter writer = new PrintWriter("exemplo_cidades.txt");
        writer.println("Rede de Transporte - Cidades do Brasil");
        writer.println("# Formato: origem,destino,distancia_km");
        writer.println("São Paulo,Rio de Janeiro,430");
        writer.println("São Paulo,Belo Horizonte,586");
        writer.println("Rio de Janeiro,Belo Horizonte,434");
        writer.println("São Paulo,Curitiba,408");
        writer.println("Curitiba,Florianópolis,300");
        writer.println("Belo Horizonte,Brasília,741");
        writer.println("São Paulo,Brasília,1015");
        writer.close();
    }

    private void gerarExemploRedeComputadores() throws IOException {
        PrintWriter writer = new PrintWriter("exemplo_rede.txt");
        writer.println("Rede de Computadores - Latência em ms");
        writer.println("# Formato: servidor_origem,servidor_destino,latencia_ms");
        writer.println("ServerA,ServerB,10");
        writer.println("ServerA,ServerC,25");
        writer.println("ServerB,ServerD,15");
        writer.println("ServerC,ServerD,8");
        writer.println("ServerD,ServerE,12");
        writer.println("ServerB,ServerE,30");
        writer.println("ServerC,ServerE,20");
        writer.close();
    }

    private void gerarExemploDependencias() throws IOException {
        PrintWriter writer = new PrintWriter("exemplo_dependencias.txt");
        writer.println("Sistema de Dependências - Projeto de Software");
        writer.println("# Formato: modulo_dependente,modulo_requerido,prioridade");
        writer.println("Frontend,Backend,1");
        writer.println("Backend,Database,1");
        writer.println("Frontend,UI_Library,1");
        writer.println("Backend,API_Framework,1");
        writer.println("Database,Driver,1");
        writer.println("UI_Library,CSS_Framework,1");
        writer.close();
    }

    private boolean grafoVazio() {
        if (this.grafo != null && !this.grafo.estaVazio()) {
            return false;
        } else {
            System.out.println("Nenhum grafo carregado! Crie ou carregue um grafo primeiro.");
            return true;
        }
    }
}
