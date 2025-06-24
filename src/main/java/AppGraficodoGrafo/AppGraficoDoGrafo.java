package AppGraficodoGrafo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod; // NEW IMPORT
import javafx.scene.paint.LinearGradient; // NEW IMPORT
import javafx.scene.paint.Paint; // NEW IMPORT
import javafx.scene.paint.RadialGradient; // NEW IMPORT
import javafx.scene.paint.Stop; // NEW IMPORT
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList; // NEW IMPORT

/**
 * Classe principal da aplicação JavaFX para visualização e manipulação de grafos.
 * Permite adicionar vértices, arestas, calcular o caminho mais curto usando Dijkstra
 * e detectar ciclos, simulando um problema de otimização de rotas de entrega.
 */
public class AppGraficoDoGrafo extends Application {

    // Instância do grafo que será manipulado pela aplicação
    private Grafo<String> grafo;
    // Painel onde os vértices e arestas serão desenhados
    private Pane graphPane;
    // Mapas para associar o valor do vértice a sua representação gráfica e vice-versa
    private Map<String, Circle> verticeCircleMap;
    private Map<Circle, String> circleVerticeMap;
    // Vértice selecionado para operações (e.g., adicionar aresta)
    private Circle selectedCircle = null;

    // Componentes da UI
    private TextField verticeNomeField;
    private TextField arestaOrigemField;
    private TextField arestaDestinoField;
    private TextField arestaPesoField;
    private ListView<String> dijkstraDestinosList; // Para rotas multi-destino
    private ComboBox<String> dijkstraDestinationComboBox; // Novo ComboBox
    private Button addDijkstraDestinoButton;
    private Button removeDijkstraDestinoButton;
    private TextArea outputArea;

    // Variáveis para seleção de origem/destino do Dijkstra por clique
    private String dijkstraSelectedOriginNode = null;
    private String dijkstraSelectedDestinationNode = null;
    private Label dijkstraOriginLabel;
    private Label dijkstraDestinationLabel;

    // Lista de destinos para o cálculo de Dijkstra multi-destino
    private List<String> dijkstraDestinos;

    // Constantes para o raio do círculo e tamanho da fonte do vértice
    private static final double VERTEX_RADIUS = 20;
    private static final int VERTEX_FONT_SIZE = 12;

    // --- Variáveis para o destaque de ciclos ---
    // MODIFIED: Mapeia cada nó para suas cores de ciclo
    private Map<String, List<Color>> nodeToColors;
    private Set<String> nodesInAnyCycle;
    private Set<String> nodesInMultipleCycles;
    private List<Color> cycleColors;
    private int nextCycleColorIndex;
    // ORIGINAL: private static final Color MULTI_CYCLE_NODE_COLOR = Color.DARKRED; // Removed in favor of striped pattern

    // Variáveis para controle de zoom
    private double scaleFactor = 1.0;
    private static final double ZOOM_INTENSITY = 0.05; // Intensidade do zoom

    /**
     * Método start da aplicação JavaFX.
     * Configura a interface do usuário e inicializa o grafo.
     * @param primaryStage O palco principal da aplicação.
     */
    @Override
    public void start(Stage primaryStage) {
        grafo = new Grafo<>();
        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");
        graphPane.setPrefSize(800, 600);

        verticeCircleMap = new HashMap<>();
        circleVerticeMap = new HashMap<>();
        dijkstraDestinos = new ArrayList<>();

        // Inicialização da área de output ANTES de carregar o grafo inicial
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPromptText("Resultados e Logs da Aplicação...");
        outputArea.setPrefHeight(150);
        outputArea.setStyle("-fx-background-color: #ffffff; -fx-control-inner-background: #ffffff; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-border-color: #bbbbbb; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        // Inicialização das variáveis de destaque de ciclos (MODIFIED)
        nodeToColors = new HashMap<>(); // NEW VARIABLE
        nodesInAnyCycle = new HashSet<>();
        nodesInMultipleCycles = new HashSet<>();
        cycleColors = List.of(
                Color.web("#8A2BE2"), Color.web("#FF4500"), Color.web("#32CD32"),
                Color.web("#1E90FF"), Color.web("#DAA520"), Color.web("#DC143C"),
                Color.web("#FF69B4"), Color.web("#00CED1"), Color.web("#9370DB") // Cores extras
        );
        nextCycleColorIndex = 0;

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Barra Lateral Esquerda: Controles de Vértices e Arestas ---
        VBox leftControlsVBox = new VBox(10);
        leftControlsVBox.setPadding(new Insets(10));
        leftControlsVBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbbbbb; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        leftControlsVBox.setMaxWidth(Double.MAX_VALUE); // Permite que o VBox se expanda horizontalmente

        // Seção de Vértices
        Label verticeLabel = new Label("Vértices:");
        verticeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        verticeNomeField = new TextField();
        verticeNomeField.setPromptText("Nome do Vértice (Ex: A, B, Local1)");
        Button addVerticeButton = new Button("Adicionar Vértice");
        addVerticeButton.setMaxWidth(Double.MAX_VALUE);
        addVerticeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addVerticeButton.setOnAction(e -> adicionarVertice());

        // Botão de organização automática do grafo
        Button autoArrangeButton = new Button("Organizar Grafo Automaticamente");
        autoArrangeButton.setMaxWidth(Double.MAX_VALUE);
        autoArrangeButton.setStyle("-fx-background-color: #4B0082; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        autoArrangeButton.setOnAction(e -> arrangeGraphInCircle());

        // Botão para carregar grafo de arquivo
        Button loadFromFileButton = new Button("Carregar Grafo de Arquivo");
        loadFromFileButton.setMaxWidth(Double.MAX_VALUE);
        loadFromFileButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        loadFromFileButton.setOnAction(e -> loadGraphFromFile(primaryStage));


        // Seção de Arestas
        Label arestaLabel = new Label("Arestas:");
        arestaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        arestaOrigemField = new TextField();
        arestaOrigemField.setPromptText("Origem (Ex: A)");
        arestaDestinoField = new TextField();
        arestaDestinoField.setPromptText("Destino (Ex: B)");
        arestaPesoField = new TextField();
        arestaPesoField.setPromptText("Peso (Ex: 10.5)");
        Button addArestaButton = new Button("Adicionar Aresta");
        addArestaButton.setMaxWidth(Double.MAX_VALUE);
        addArestaButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addArestaButton.setOnAction(e -> adicionarAresta());

        // Adiciona os componentes à barra lateral esquerda
        leftControlsVBox.getChildren().addAll(
                verticeLabel, verticeNomeField, addVerticeButton,
                autoArrangeButton,
                loadFromFileButton,
                new Separator(),
                arestaLabel,
                arestaOrigemField,
                arestaDestinoField,
                arestaPesoField,
                addArestaButton
        );
        root.setLeft(leftControlsVBox);


        // --- Barra Lateral Direita: Controles Dijkstra, Ciclos e Output ---
        VBox rightControlsVBox = new VBox(10);
        rightControlsVBox.setPadding(new Insets(10));
        rightControlsVBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbbbbb; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        rightControlsVBox.setMaxWidth(Double.MAX_VALUE); // Permite que o VBox se expanda horizontalmente

        // Seção Dijkstra (Seleção por cliques e Multi-destino)
        Label dijkstraTitleLabel = new Label("Algoritmo de Dijkstra:");
        dijkstraTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label dijkstraClickSelectionLabel = new Label("Seleção por Cliques (Origem -> Destino):");
        dijkstraOriginLabel = new Label("Origem: N/A");
        dijkstraDestinationLabel = new Label("Destino: N/A");
        dijkstraOriginLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dijkstraDestinationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Button calculateDirectPathButton = new Button("Calcular Caminho Direto");
        calculateDirectPathButton.setMaxWidth(Double.MAX_VALUE);
        calculateDirectPathButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        calculateDirectPathButton.setOnAction(e -> calcularCaminhoDiretoAnimado());

        Button clearDijkstraSelectionButton = new Button("Limpar Seleção Dijkstra");
        clearDijkstraSelectionButton.setMaxWidth(Double.MAX_VALUE);
        clearDijkstraSelectionButton.setStyle("-fx-background-color: #795548; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearDijkstraSelectionButton.setOnAction(e -> resetDijkstraSelection());


        Label multiDestinationLabel = new Label("Rotas com Múltiplos Destinos:");
        multiDestinationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dijkstraDestinosList = new ListView<>();
        dijkstraDestinosList.setPrefHeight(100);

        // NOVO: ComboBox para seleção de destino para rotas multi-destino
        dijkstraDestinationComboBox = new ComboBox<>();
        dijkstraDestinationComboBox.setPromptText("Selecionar Destino");
        dijkstraDestinationComboBox.setMaxWidth(Double.MAX_VALUE);

        addDijkstraDestinoButton = new Button("Add Destino");
        addDijkstraDestinoButton.setStyle("-fx-background-color: #673AB7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addDijkstraDestinoButton.setOnAction(e -> addDijkstraDestino());
        removeDijkstraDestinoButton = new Button("Remover Destino");
        removeDijkstraDestinoButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        removeDijkstraDestinoButton.setOnAction(e -> removeDijkstraDestino());

        HBox destinoButtons = new HBox(5, addDijkstraDestinoButton, removeDijkstraDestinoButton);
        destinoButtons.setAlignment(Pos.CENTER_LEFT);

        Button calculateMultiDijkstraButton = new Button("Calcular Rota Otimizada (Multi)");
        calculateMultiDijkstraButton.setMaxWidth(Double.MAX_VALUE);
        calculateMultiDijkstraButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5;");
        calculateMultiDijkstraButton.setOnAction(e -> calcularRotaDijkstraMulti());


        // Seção Ciclos
        Label cicloLabel = new Label("Verificação de Ciclos:");
        cicloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Button checkCiclosButton = new Button("Verificar Ciclos no Grafo");
        checkCiclosButton.setMaxWidth(Double.MAX_VALUE);
        checkCiclosButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        checkCiclosButton.setOnAction(e -> verificarCiclos());

        // Botão para salvar imagem
        Button saveImageButton = new Button("Salvar Grafo como Imagem");
        saveImageButton.setMaxWidth(Double.MAX_VALUE);
        saveImageButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        saveImageButton.setOnAction(e -> saveGraphAsImage(primaryStage));

        rightControlsVBox.getChildren().addAll(
                dijkstraTitleLabel,
                dijkstraClickSelectionLabel, dijkstraOriginLabel, dijkstraDestinationLabel,
                calculateDirectPathButton, clearDijkstraSelectionButton,
                new Separator(),
                multiDestinationLabel,
                dijkstraDestinationComboBox, // NOVO: Adicionado ComboBox aqui
                destinoButtons, // Botões Add/Remover Destino
                dijkstraDestinosList, // Lista dos destinos selecionados
                calculateMultiDijkstraButton,
                new Separator(),
                cicloLabel, checkCiclosButton,
                new Separator(),
                saveImageButton,
                outputArea // Adiciona a área de saída na barra lateral direita
        );
        VBox.setVgrow(outputArea, Priority.ALWAYS); // Permite que outputArea ocupe o espaço vertical restante
        root.setRight(rightControlsVBox);


        // Centraliza o painel do grafo
        StackPane centerStackPane = new StackPane(graphPane);
        centerStackPane.setPadding(new Insets(10));
        root.setCenter(centerStackPane);

        // Implementação do Zoom no graphPane
        graphPane.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = (event.getDeltaY() > 0) ? (1 + ZOOM_INTENSITY) : (1 - ZOOM_INTENSITY);
            scaleFactor *= zoomFactor;
            if (scaleFactor < 0.1) scaleFactor = 0.1;
            if (scaleFactor > 5.0) scaleFactor = 5.0;
            graphPane.setScaleX(scaleFactor);
            graphPane.setScaleY(scaleFactor);
            event.consume();
        });


        // Cria a cena e define no palco
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Simulador de Otimização de Rotas de Entrega");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Inicializa o grafo com um arquivo padrão ao iniciar, se ele existir
        // Você pode criar um arquivo 'cities.txt' no diretório raiz do seu projeto
        File defaultFile = new File("cities.txt");
        if (defaultFile.exists()) {
            loadGraphFromFile(primaryStage); // Chama o método para carregar o grafo do arquivo
        } else {
            logOutput("Arquivo 'cities.txt' não encontrado. Use o botão 'Carregar Grafo de Arquivo'.");
        }


        // Chame arrangeGraphInCircle após a janela ser exibida para garantir que o Pane tenha dimensões
        // Com o carregamento de arquivo, o arrangeGraphInCircle inicial pode ser omitido
        // se as coordenadas do arquivo já forem boas. Mantenho o listener para redimensionamento.
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (grafo.getNumeroVertices() > 0) { // Só rearranja se houver vértices
                // arrangeGraphInCircle(); // Descomente se quiser rearranjo circular ao redimensionar
            }
        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (grafo.getNumeroVertices() > 0) {
                // arrangeGraphInCircle(); // Descomente se quiser rearranjo circular ao redimensionar
            }
        });
    }

    /**
     * Atualiza a ComboBox de destinos com os vértices atualmente no grafo.
     */
    private void updateDestinationComboBox() {
        dijkstraDestinationComboBox.getItems().setAll(grafo.getValoresVertices());
        dijkstraDestinationComboBox.getSelectionModel().clearSelection(); // Limpa a seleção atual
    }

    /**
     * Limpa o grafo atual (vértices e arestas) da memória e da UI.
     */
    private void clearGraph() {
        grafo = new Grafo<>(); // Cria uma nova instância de grafo vazia
        graphPane.getChildren().clear(); // Limpa todos os elementos visuais
        verticeCircleMap.clear();
        circleVerticeMap.clear();
        dijkstraDestinos.clear();
        dijkstraDestinosList.getItems().clear(); // Limpa a lista de destinos na UI
        dijkstraSelectedOriginNode = null;
        dijkstraSelectedDestinationNode = null;
        dijkstraOriginLabel.setText("Origem: N/A");
        dijkstraDestinationLabel.setText("Destino: N/A");
        resetGraphHighlight(); // Reseta destaques e contadores de ciclo
        logOutput("Grafo atual limpo.");
        updateDestinationComboBox(); // Atualiza a ComboBox após limpar o grafo
    }

    /**
     * Carrega a estrutura do grafo a partir de um arquivo de texto selecionado pelo usuário.
     * O arquivo deve conter cidades e suas adjacências com pesos, incluindo coordenadas X e Y.
     * Formato esperado por linha: NomeDaCidade;X;Y;Vizinho1:Distancia1;Vizinho2:Distancia2...
     * @param stage O palco principal da aplicação para exibir o FileChooser.
     */
    private void loadGraphFromFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Arquivo de Grafo (.txt)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            clearGraph(); // Limpa o grafo atual antes de carregar um novo
            Map<String, List<String[]>> adjacenciesToProcess = new HashMap<>(); // Para armazenar adjacências temporariamente
            Map<String, double[]> tempVertexCoords = new HashMap<>(); // Para armazenar coords de vértices antes de desenhar

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty()) continue; // Ignora linhas vazias

                    try {
                        String[] parts = line.split(";");
                        if (parts.length < 3) {
                            logOutput("Erro na linha " + lineNumber + " do arquivo '" + selectedFile.getName() + "': Formato inválido. Esperado 'Nome;X;Y;Vizinho:Distancia...'");
                            continue;
                        }

                        String cityName = parts[0].trim();
                        double x = Double.parseDouble(parts[1].trim());
                        double y = Double.parseDouble(parts[2].trim());

                        // Armazena as coordenadas e adiciona o vértice ao grafo de dados
                        tempVertexCoords.put(cityName, new double[]{x, y});
                        grafo.adicionarVertice(cityName);

                        // Armazena as adjacências para processar depois que todos os vértices existirem
                        List<String[]> currentAdjacencies = new ArrayList<>();
                        for (int i = 3; i < parts.length; i++) {
                            String[] neighborInfo = parts[i].trim().split(":");
                            if (neighborInfo.length == 2) {
                                currentAdjacencies.add(neighborInfo);
                            } else {
                                logOutput("Erro na linha " + lineNumber + " do arquivo '" + selectedFile.getName() + "': Formato de vizinho/distância inválido: '" + parts[i] + "'");
                            }
                        }
                        adjacenciesToProcess.put(cityName, currentAdjacencies);

                    } catch (NumberFormatException e) {
                        logOutput("Erro na linha " + lineNumber + " do arquivo '" + selectedFile.getName() + "': Coordenadas ou peso inválidos. Detalhes: " + e.getMessage());
                    } catch (Exception e) {
                        logOutput("Erro inesperado na linha " + lineNumber + " do arquivo '" + selectedFile.getName() + "': " + e.getMessage());
                    }
                }

                // Agora que todos os dados foram lidos e armazenados, desenhe o grafo
                graphPane.getChildren().clear(); // Limpa todos os elementos visuais
                verticeCircleMap.clear();
                circleVerticeMap.clear();

                // Desenha os vértices usando as coordenadas lidas do arquivo
                for (Map.Entry<String, double[]> entry : tempVertexCoords.entrySet()) {
                    String cityName = entry.getKey();
                    double[] coords = entry.getValue();
                    drawVertex(cityName, coords[0], coords[1]);
                }

                // Adiciona as arestas usando o mapa de adjacências
                for (Map.Entry<String, List<String[]>> entry : adjacenciesToProcess.entrySet()) {
                    String cityName = entry.getKey();
                    for (String[] neighborInfo : entry.getValue()) {
                        String neighborName = neighborInfo[0].trim();
                        double weight = Double.parseDouble(neighborInfo[1].trim());

                        // Adiciona a aresta somente se ambos os vértices existem no grafo
                        if (grafo.obterIndiceVertice(cityName) != -1 && grafo.obterIndiceVertice(neighborName) != -1) {
                            grafo.adicionarAresta(cityName, neighborName, weight);
                            drawEdge(cityName, neighborName, weight);
                        } else {
                            logOutput("Aviso: Conexão inválida '" + cityName + "' <-> '" + neighborName + "'. Um ou ambos os vértices não existem.");
                        }
                    }
                }
                logOutput("Grafo carregado com sucesso do arquivo: " + selectedFile.getName());
                updateDestinationComboBox(); // Atualiza a ComboBox após carregar o grafo

            } catch (IOException e) {
                logOutput("Erro de leitura do arquivo: " + e.getMessage());
                showAlert("Erro de Leitura", "Não foi possível ler o arquivo:\n" + e.getMessage());
            } catch (Exception e) {
                logOutput("Erro ao processar arquivo: " + e.getMessage());
                showAlert("Erro", "Ocorreu um erro ao processar o arquivo:\n" + e.getMessage());
            }
        }
    }


    /**
     * Organiza o grafo em um layout circular.
     * Este método realoca visualmente os vértices e redesenha suas arestas.
     */
    private void arrangeGraphInCircle() {
        if (grafo.getNumeroVertices() == 0) {
            return;
        }

        // Remove apenas os Circles e Texts dos vértices para redesenhá-los.
        // As Lines são redesenhadas no loop das arestas abaixo.
        graphPane.getChildren().removeIf(node -> {
            if (node instanceof Circle) {
                return true;
            }
            if (node instanceof Text) {
                Object userDataObj = node.getUserData();
                return userDataObj == null || !(userDataObj instanceof String) || !((String) userDataObj).startsWith("peso-");
            }
            return false;
        });

        verticeCircleMap.clear();
        circleVerticeMap.clear();

        double centerX = graphPane.getWidth() / 2;
        double centerY = graphPane.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.8;

        List<Vertice<String>> verticesList = grafo.getVertices();
        int numVertices = verticesList.size();

        for (int i = 0; i < numVertices; i++) {
            String verticeName = verticesList.get(i).getValor();
            double angle = 2 * Math.PI * i / numVertices;

            double newX = centerX + radius * Math.cos(angle);
            double newY = centerY + radius * Math.sin(angle);

            drawVertex(verticeName, newX, newY); // Usa a nova assinatura de drawVertex
        }

        // Redesenha todas as arestas com as novas posições
        graphPane.getChildren().removeIf(node -> {
            if (node instanceof Line) {
                return true;
            }
            if (node instanceof Text) {
                Object userDataObj = node.getUserData();
                return userDataObj instanceof String && ((String) userDataObj).startsWith("peso-");
            }
            return false;
        });

        for (int i = 0; i < grafo.getNumeroVertices(); i++) {
            for (int j = i + 1; j < grafo.getNumeroVertices(); j++) {
                double weight = grafo.getMatrizAdjacencias()[i][j];
                if (weight != 0) {
                    String origin = grafo.getVertices().get(i).getValor();
                    String destination = grafo.getVertices().get(j).getValor();
                    drawEdge(origin, destination, weight);
                }
            }
        }
        logOutput("Grafo organizado em layout circular.");
        resetGraphHighlight();
        updateDestinationComboBox(); // Atualiza a ComboBox após rearranjar o grafo
    }

    /**
     * Adiciona uma mensagem à área de saída de texto (log).
     *
     * @param message A mensagem a ser adicionada.
     */
    private void logOutput(String message) {
        outputArea.appendText(message + "\n");
    }

    /**
     * Configura os listeners de mouse para arrastar e clicar em um vértice.
     * Este método é fatorado para ser reutilizado na criação inicial e no re-arranjo.
     * @param circle O objeto Circle que representa o vértice.
     * @param text O objeto Text que representa o rótulo do vértice.
     * @param verticeName O nome do vértice.
     */
    private void setupVertexInteraction(Circle circle, Text text, String verticeName) {
        AtomicInteger mouseX = new AtomicInteger();
        AtomicInteger mouseY = new AtomicInteger();

        circle.setOnMousePressed(event -> {
            selectedCircle = circle;
            selectedCircle.setFill(Color.DARKBLUE);
            mouseX.set((int) event.getSceneX());
            mouseY.set((int) event.getSceneY());
            event.consume();
        });

        circle.setOnMouseDragged(event -> {
            if (selectedCircle == circle) {
                double deltaX = event.getSceneX() - mouseX.get();
                double deltaY = event.getSceneY() - mouseY.get();

                circle.setCenterX(circle.getCenterX() + deltaX / scaleFactor);
                circle.setCenterY(circle.getCenterY() + deltaY / scaleFactor);
                text.setX(text.getX() + deltaX / scaleFactor);
                text.setY(text.getY() + deltaY / scaleFactor);

                redrawEdgesForVertex(verticeName);

                mouseX.set((int) event.getSceneX());
                mouseY.set((int) event.getSceneY());
                event.consume();
            }
        });

        circle.setOnMouseReleased(event -> {
            if (selectedCircle == circle) {
                selectedCircle.setFill(Color.LIGHTBLUE);
                selectedCircle = null;
            }
            event.consume();
        });

        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 1) {
                // Lógica de seleção de ARESTA (via text fields)
                if (selectedCircle == null) {
                    selectedCircle = circle;
                    selectedCircle.setFill(Color.ORANGE);
                    arestaOrigemField.setText(circleVerticeMap.get(circle));
                } else if (selectedCircle == circle) {
                    selectedCircle.setFill(Color.LIGHTBLUE);
                    selectedCircle = null;
                    arestaOrigemField.clear();
                    arestaDestinoField.clear();
                } else {
                    arestaDestinoField.setText(circleVerticeMap.get(circle));
                }

                // Lógica de seleção de ORIGEM/DESTINO para DIJKSTRA (via cliques e labels)
                if (dijkstraSelectedOriginNode == null) {
                    dijkstraSelectedOriginNode = verticeName;
                    verticeCircleMap.get(verticeName).setFill(Color.DARKGREEN);
                    dijkstraOriginLabel.setText("Origem: " + verticeName);
                    dijkstraDestinationLabel.setText("Destino: N/A");
                } else if (dijkstraSelectedDestinationNode == null && !verticeName.equals(dijkstraSelectedOriginNode)) {
                    dijkstraSelectedDestinationNode = verticeName;
                    verticeCircleMap.get(verticeName).setFill(Color.LIGHTGREEN);
                    dijkstraDestinationLabel.setText("Destino: " + verticeName);
                } else {
                    resetDijkstraSelection();
                    dijkstraSelectedOriginNode = verticeName;
                    verticeCircleMap.get(verticeName).setFill(Color.DARKGREEN);
                    dijkstraOriginLabel.setText("Origem: " + verticeName);
                }
            }
            event.consume();
        });
    }


    /**
     * Adiciona um novo vértice ao grafo e o desenha no painel gráfico.
     * O vértice é posicionado usando as coordenadas fornecidas.
     * @param verticeName O nome do vértice a ser adicionado e desenhado.
     * @param x X-coordenada para o posicionamento do vértice.
     * @param y Y-coordenada para o posicionamento do vértice.
     */
    private void drawVertex(String verticeName, double x, double y) {
        Circle circle = new Circle(x, y, VERTEX_RADIUS);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(2);

        Text text = new Text(verticeName);
        text.setFont(Font.font("Arial", FontWeight.BOLD, VERTEX_FONT_SIZE));
        text.setBoundsType(javafx.scene.text.TextBoundsType.VISUAL);
        text.setX(x - text.getLayoutBounds().getWidth() / 2);
        text.setY(y + text.getLayoutBounds().getHeight() / 4);

        setupVertexInteraction(circle, text, verticeName);

        verticeCircleMap.put(verticeName, circle);
        circleVerticeMap.put(circle, verticeName);
        graphPane.getChildren().addAll(circle, text);
    }

    /**
     * Sobrecarga de drawVertex para compatibilidade com o botão "Adicionar Vértice"
     * que não especifica coordenadas. Posiciona aleatoriamente.
     * Este método não deve ser chamado para carregamento de grafo de arquivo.
     */
    private void adicionarVertice() {
        String nomeVertice = verticeNomeField.getText().trim();
        if (nomeVertice.isEmpty()) {
            showAlert("Erro de Entrada", "O nome do vértice não pode ser vazio.");
            return;
        }
        if (grafo.obterIndiceVertice(nomeVertice) != -1) {
            showAlert("Erro de Entrada", "Vértice com este nome já existe.");
            return;
        }

        grafo.adicionarVertice(nomeVertice);
        verticeNomeField.clear();
        logOutput("Vértice '" + nomeVertice + "' adicionado.");

        // Posição aleatória para vértices adicionados manualmente
        double x = Math.random() * (graphPane.getWidth() - 2 * VERTEX_RADIUS) + VERTEX_RADIUS;
        double y = Math.random() * (graphPane.getHeight() - 2 * VERTEX_RADIUS) + VERTEX_RADIUS;

        drawVertex(nomeVertice, x, y); // Chama o método drawVertex com coordenadas
        updateDestinationComboBox(); // Atualiza a ComboBox
    }


    /**
     * Redesenha todas as arestas conectadas a um vértice específico.
     * Usado quando um vértice é arrastado para atualizar a posição das arestas.
     * @param verticeName O nome do vértice cujas arestas precisam ser redesenhadas.
     */
    private void redrawEdgesForVertex(String verticeName) {
        graphPane.getChildren().removeIf(node -> {
            if (node instanceof Line) {
                Line existingLine = (Line) node;
                Object userDataObj = existingLine.getUserData();
                return userDataObj instanceof String && (
                        ((String) userDataObj).contains("edge-" + verticeName + "-") ||
                                ((String) userDataObj).contains("-" + verticeName + "-edge")
                );
            }
            if (node instanceof Text) { // Também remove textos de peso associados
                Text existingText = (Text) node;
                Object userDataObj = existingText.getUserData();
                return userDataObj instanceof String && (
                        ((String) userDataObj).contains("peso-" + verticeName + "-") ||
                                ((String) userDataObj).contains("-" + verticeName + "-peso")
                );
            }
            return false;
        });

        int originIndex = grafo.obterIndiceVertice(verticeName);
        if (originIndex != -1) {
            for (int i = 0; i < grafo.getNumeroVertices(); i++) {
                if (grafo.getMatrizAdjacencias()[originIndex][i] != 0) {
                    String otherVerticeName = grafo.getVertices().get(i).getValor();
                    drawEdge(verticeName, otherVerticeName, grafo.getMatrizAdjacencias()[originIndex][i]);
                }
            }
        }
    }


    /**
     * Adiciona uma aresta entre dois vértices no grafo e a desenha no painel.
     * O peso da aresta deve ser um número válido.
     */
    private void adicionarAresta() {
        String origem = arestaOrigemField.getText().trim();
        String destino = arestaDestinoField.getText().trim();
        String pesoStr = arestaPesoField.getText().trim();

        if (origem.isEmpty() || destino.isEmpty() || pesoStr.isEmpty()) {
            showAlert("Erro de Entrada", "Todos os campos de aresta (origem, destino, peso) devem ser preenchidos.");
            return;
        }

        if (origem.equals(destino)) {
            showAlert("Erro de Entrada", "Não é possível adicionar uma aresta de um vértice para ele mesmo.");
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);
            if (peso <= 0) {
                showAlert("Erro de Entrada", "O peso da aresta deve ser um valor positivo.");
                return;
            }

            if (grafo.obterIndiceVertice(origem) == -1 || grafo.obterIndiceVertice(destino) == -1) {
                showAlert("Erro de Entrada", "Um ou ambos os vértices não existem no grafo. Adicione-os primeiro.");
                return;
            }

            grafo.adicionarAresta(origem, destino, peso);
            logOutput("Aresta de '" + origem + "' para '" + destino + "' com peso " + peso + " adicionada.");

            drawEdge(origem, destino, peso);

            arestaOrigemField.clear();
            arestaDestinoField.clear();
            arestaPesoField.clear();
            if (selectedCircle != null) {
                selectedCircle.setFill(Color.LIGHTBLUE);
                selectedCircle = null;
            }

        } catch (NumberFormatException e) {
            showAlert("Erro de Entrada", "Peso da aresta inválido. Por favor, insira um número.");
        }
    }

    /**
     * Desenha uma linha representando a aresta entre dois vértices no painel do grafo.
     * O peso é exibido próximo à linha.
     *
     * @param origem O nome do vértice de origem.
     * @param destino O nome do vértice de destino.
     * @param peso O peso da aresta.
     */
    private void drawEdge(String origem, String destino, double peso) {
        Circle circleOrigem = verticeCircleMap.get(origem);
        Circle circleDestino = verticeCircleMap.get(destino);

        if (circleOrigem == null || circleDestino == null) {
            logOutput("Erro: Não foi possível desenhar a aresta. Vértice(s) não encontrado(s) na UI.");
            return;
        }

        graphPane.getChildren().removeIf(node -> {
            if (node instanceof Line) {
                Line existingLine = (Line) node;
                Object userDataObj = existingLine.getUserData();
                if (userDataObj instanceof String) { // Verifica se é String
                    String userDataString = (String) userDataObj; // Faz o cast
                    if (userDataString.equals("edge-" + origem + "-" + destino) ||
                            userDataString.equals("edge-" + destino + "-" + origem)) {
                        return true;
                    }
                }
            } else if (node instanceof Text) {
                Text existingText = (Text) node;
                Object userDataObj = existingText.getUserData();
                if (userDataObj instanceof String) { // Verifica se é String
                    String userDataString = (String) userDataObj; // Faz o cast
                    if (userDataString.equals("peso-" + origem + "-" + destino) ||
                            userDataString.equals("peso-" + destino + "-" + origem)) {
                        return true;
                    }
                }
            }
            return false;
        });


        Line line = new Line(circleOrigem.getCenterX(), circleOrigem.getCenterY(),
                circleDestino.getCenterX(), circleDestino.getCenterY());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        line.setUserData("edge-" + origem + "-" + destino);

        Text pesoText = new Text(String.format("%.1f", peso));
        pesoText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        pesoText.setX((circleOrigem.getCenterX() + circleDestino.getCenterX()) / 2 - pesoText.getLayoutBounds().getWidth() / 2);
        pesoText.setY((circleOrigem.getCenterY() + circleDestino.getCenterY()) / 2 - pesoText.getLayoutBounds().getHeight() / 2 - 5);
        pesoText.setUserData("peso-" + origem + "-" + destino);


        graphPane.getChildren().addAll(line, pesoText);
        circleOrigem.toFront();
        circleDestino.toFront();
        Text textOrigem = (Text) graphPane.getChildren().stream()
                .filter(node -> node instanceof Text && ((Text) node).getText().equals(origem))
                .findFirst().orElse(null);
        if (textOrigem != null) {
            textOrigem.toFront();
        }

        Text textDestino = (Text) graphPane.getChildren().stream()
                .filter(node -> node instanceof Text && ((Text) node).getText().equals(destino))
                .findFirst().orElse(null);
        if (textDestino != null) {
            textDestino.toFront();
        }
    }

    /**
     * Limpa a seleção de origem e destino para o cálculo direto de Dijkstra.
     * Reseta as cores dos vértices e labels.
     */
    private void resetDijkstraSelection() {
        if (dijkstraSelectedOriginNode != null && verticeCircleMap.containsKey(dijkstraSelectedOriginNode)) {
            verticeCircleMap.get(dijkstraSelectedOriginNode).setFill(Color.LIGHTBLUE); // Reseta cor da origem
            verticeCircleMap.get(dijkstraSelectedOriginNode).setStroke(Color.BLUE);
        }
        if (dijkstraSelectedDestinationNode != null && verticeCircleMap.containsKey(dijkstraSelectedDestinationNode)) {
            verticeCircleMap.get(dijkstraSelectedDestinationNode).setFill(Color.LIGHTBLUE); // Reseta cor do destino
            verticeCircleMap.get(dijkstraSelectedDestinationNode).setStroke(Color.BLUE);
        }
        dijkstraSelectedOriginNode = null;
        dijkstraSelectedDestinationNode = null;
        dijkstraOriginLabel.setText("Origem: N/A");
        dijkstraDestinationLabel.setText("Destino: N/A");
        resetGraphHighlight(); // Limpa também qualquer destaque de caminho animado
        logOutput("Seleção de origem/destino do Dijkstra limpa.");
    }

    /**
     * Adiciona um vértice à lista de destinos para o cálculo de Dijkstra multi-destino.
     */
    private void addDijkstraDestino() {
        String destino = dijkstraDestinationComboBox.getSelectionModel().getSelectedItem(); // Pega da ComboBox
        if (destino == null || destino.isEmpty()) {
            showAlert("Erro de Seleção", "Por favor, selecione um vértice da lista suspensa para adicionar.");
            return;
        }
        if (grafo.obterIndiceVertice(destino) == -1) { // Verifica se o vértice existe no grafo (embora a ComboBox só mostre existentes)
            showAlert("Erro de Entrada", "Vértice '" + destino + "' não existe no grafo.");
            return;
        }
        if (!dijkstraDestinos.contains(destino)) {
            dijkstraDestinos.add(destino);
            dijkstraDestinosList.getItems().setAll(dijkstraDestinos);
            logOutput("Destino '" + destino + "' adicionado à lista de Dijkstra (Multi).");
        } else {
            showAlert("Atenção", "Destino '" + destino + "' já está na lista (Multi).");
        }
        dijkstraDestinationComboBox.getSelectionModel().clearSelection(); // Limpa a seleção na ComboBox após adicionar
    }

    /**
     * Remove um vértice da lista de destinos para o cálculo de Dijkstra multi-destino.
     */
    private void removeDijkstraDestino() {
        String destino = dijkstraDestinosList.getSelectionModel().getSelectedItem();
        if (destino == null || destino.isEmpty()) {
            showAlert("Erro de Seleção", "Selecione um destino na lista para remover.");
            return;
        }
        if (dijkstraDestinos.remove(destino)) {
            dijkstraDestinosList.getItems().setAll(dijkstraDestinos);
            logOutput("Destino '" + destino + "' removido da lista de Dijkstra (Multi).");
        } else {
            showAlert("Atenção", "Destino '" + destino + "' não encontrado na lista (Multi).");
        }
    }

    /**
     * Calcula o caminho direto entre a origem e o destino selecionados por clique e o anima.
     */
    private void calcularCaminhoDiretoAnimado() {
        resetGraphHighlight(); // Limpa destaques anteriores

        if (dijkstraSelectedOriginNode == null || dijkstraSelectedDestinationNode == null) {
            showAlert("Erro de Seleção", "Por favor, selecione a origem e o destino clicando nos vértices do grafo.");
            return;
        }
        if (dijkstraSelectedOriginNode.equals(dijkstraSelectedDestinationNode)) {
            showAlert("Caminho Inválido", "Origem e Destino são o mesmo vértice.");
            return;
        }

        Grafo.DijkstraResult result = grafo.dijkstra(dijkstraSelectedOriginNode, dijkstraSelectedDestinationNode);

        if (result.path.isEmpty() || result.cost == Double.POSITIVE_INFINITY) {
            logOutput("Não foi possível encontrar um caminho de '" + dijkstraSelectedOriginNode +
                    "' para '" + dijkstraSelectedDestinationNode + "'.");
            showAlert("Caminho Não Encontrado", "Não foi possível encontrar um caminho entre os vértices selecionados.");
        } else {
            logOutput("Caminho direto de '" + dijkstraSelectedOriginNode + "' para '" + dijkstraSelectedDestinationNode +
                    ": " + String.join(" -> ", result.path) +
                    " (Custo: " + String.format("%.2f", result.cost) + ")");
            animatePath(result.path, Color.BLUE); // Anima o caminho
        }
    }

    /**
     * Calcula a rota otimizada usando Dijkstra para múltiplos destinos sequenciais.
     * Destaque o caminho no grafo com animação.
     */
    private void calcularRotaDijkstraMulti() {
        resetGraphHighlight(); // Limpa destaques anteriores

        String origem = dijkstraSelectedOriginNode; // Usa a origem selecionada por clique para a rota multi
        if (origem == null) {
            showAlert("Erro de Entrada", "Por favor, selecione o vértice de origem clicando nele no grafo para a rota multi-destino.");
            return;
        }
        if (grafo.obterIndiceVertice(origem) == -1) {
            showAlert("Erro de Entrada", "Vértice de origem '" + origem + "' não encontrado no grafo.");
            return;
        }
        if (dijkstraDestinos.isEmpty()) {
            showAlert("Atenção", "Nenhum destino adicionado para o cálculo da rota. Adicione destinos na lista.");
            return;
        }

        StringBuilder fullPathOutput = new StringBuilder();
        double totalCost = 0.0;
        String currentOrigin = origem;
        List<String> accumulatedPath = new ArrayList<>();
        accumulatedPath.add(origem); // Adiciona a origem ao caminho acumulado

        boolean routeSegmentFailed = false; // Flag para verificar se algum segmento da rota falhou

        for (String destino : dijkstraDestinos) {
            if (currentOrigin.equals(destino)) {
                fullPathOutput.append("Origem e destino são o mesmo: ").append(currentOrigin).append("\n");
                // Acumula o destino como parte do "caminho" mesmo se for o mesmo, para a animação
                if (!accumulatedPath.contains(destino)) { // Evita duplicar se já foi a origem inicial
                    accumulatedPath.add(destino);
                }
                continue;
            }

            Grafo.DijkstraResult result = grafo.dijkstra(currentOrigin, destino);

            if (result.path.isEmpty() || result.cost == Double.POSITIVE_INFINITY) {
                fullPathOutput.append("Não foi possível encontrar um caminho de '").append(currentOrigin)
                        .append("' para '").append(destino).append("'\n");
                routeSegmentFailed = true;
                break; // Interrompe o processamento se um segmento não puder ser encontrado
            } else {
                fullPathOutput.append("Caminho de '").append(currentOrigin).append("' para '").append(destino)
                        .append("': ").append(String.join(" -> ", result.path))
                        .append(" (Custo: ").append(String.format("%.2f", result.cost)).append(")\n");
                totalCost += result.cost;

                // Adiciona os nós do caminho do segmento ao caminho acumulado (ignorando o primeiro nó, que é a origem do segmento)
                if (result.path.size() > 1) {
                    for (int i = 1; i < result.path.size(); i++) {
                        accumulatedPath.add((String) result.path.get(i));
                    }
                }
                currentOrigin = destino; // O destino atual se torna a origem para a próxima etapa
            }
        }

        if (routeSegmentFailed) {
            logOutput("--- Rota Otimizada (Multi-destino) Falhou ---\n" + fullPathOutput.toString() + "\nCusto Total (Parcial): " + String.format("%.2f", totalCost));
            showAlert("Rota Multi-destino Falhou", "A rota multi-destino não pôde ser calculada completamente. Verifique os destinos e conexões.");
        } else if (accumulatedPath.size() > 1) { // Verifica se há mais de um nó no caminho acumulado para animar
            logOutput("--- Resultado da Rota Otimizada (Multi-destino) ---\n" + fullPathOutput.toString() + "\nCusto Total da Rota: " + String.format("%.2f", totalCost));
            animatePath(accumulatedPath, Color.ORANGE); // Anima o caminho acumulado completo
        } else {
            logOutput("--- Rota Otimizada (Multi-destino) Concluída (Nenhum Caminho a Animar) ---");
            showAlert("Rota Multi-destino", "Rota multi-destino calculada, mas sem segmentos para animar.");
        }
    }


    /**
     * Anima um caminho no grafo, destacando vértices e arestas sequencialmente.
     * @param path A lista de vértices (String) que compõem o caminho.
     * @param pathColor A cor base para a animação do caminho.
     */
    private void animatePath(List<String> path, Color pathColor) {
        if (path == null || path.size() < 2) {
            return;
        }

        // Limpa o destaque atual antes de iniciar a animação
        resetGraphHighlight();

        Timeline timeline = new Timeline();
        Duration delay = Duration.ZERO;
        Duration stepDuration = Duration.millis(500); // Tempo para cada passo da animação

        // Destaque da origem (first node of the path)
        Circle originCircle = verticeCircleMap.get(path.get(0));
        if (originCircle != null) {
            timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
                originCircle.setFill(pathColor.brighter());
                originCircle.setStroke(pathColor.darker());
            }));
        }

        // Animation of path segments
        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i);
            String v = path.get(i + 1);

            Circle currentCircle = verticeCircleMap.get(u);
            Circle nextCircle = verticeCircleMap.get(v);

            // Animar a aresta
            Line pathLine = (Line) graphPane.getChildren().stream()
                    .filter(node -> node instanceof Line) // Ensure it's a Line
                    .filter(node -> { // Filter safely for userData
                        Object userDataObj = node.getUserData();
                        return userDataObj != null && userDataObj instanceof String && ( // Mais segurança
                                ((String) userDataObj).equals("edge-" + u + "-" + v) ||
                                        ((String) userDataObj).equals("edge-" + v + "-" + u)
                        );
                    })
                    .findFirst().orElse(null);


            delay = delay.add(stepDuration); // Add a delay for each segment
            timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
                if (pathLine != null) {
                    pathLine.setStroke(pathColor.darker());
                    pathLine.setStrokeWidth(3);
                }
            }));

            // Animate the next vertex
            if (nextCircle != null) {
                timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
                    nextCircle.setFill(pathColor.brighter());
                    nextCircle.setStroke(pathColor.darker());
                }));
            }
        }

        // Reset highlight after animation
        delay = delay.add(stepDuration.multiply(0.5)); // Small final delay to keep highlight before resetting
        timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
            logOutput("Animação do caminho concluída. Resetando destaque em 2 segundos...");
            //resetGraphHighlight(); // Do not reset immediately, user might want to see final path
        }));

        timeline.setOnFinished(e -> {
            Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> resetGraphHighlight()));
            resetTimeline.play();
        });

        timeline.play();
    }


    /**
     * Destaca visualmente um caminho no grafo, alterando a cor dos vértices e arestas.
     * Este método é usado para o resultado do Dijkstra (multi-destino).
     * @param path A lista de vértices (String) que compõem o caminho.
     * @param highlightColor A cor para destacar o caminho.
     */
    private void highlightPath(List<String> path, Color highlightColor) {
        if (path == null || path.size() < 2) {
            return;
        }

        for (String verticeName : path) {
            Circle circle = verticeCircleMap.get(verticeName);
            if (circle != null) {
                // Ensure nodes in multiple cycles are not overridden by path highlight
                if (!nodesInMultipleCycles.contains(verticeName)) {
                    circle.setFill(highlightColor.brighter());
                    circle.setStroke(highlightColor.darker());
                }
            }
        }

        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i);
            String v = path.get(i + 1);

            graphPane.getChildren().forEach(node -> {
                if (node instanceof Line) {
                    Line line = (Line) node;
                    Object userDataObj = line.getUserData();
                    // Correção: Verifica se userDataObj é uma String antes de chamar equals
                    if (userDataObj != null && userDataObj instanceof String) { // Mais segurança
                        String userDataString = (String) userDataObj; // Faz o cast
                        if (userDataString.equals("edge-" + u + "-" + v) ||
                                userDataString.equals("edge-" + v + "-" + u)) {
                            line.setStroke(highlightColor.darker());
                            line.setStrokeWidth(3);
                        }
                    }
                }
            });
        }
    }

    /**
     * NOVO MÉTODO: Criar gradiente radial com múltiplas cores (for future use or alternative display)
     * This method is currently not used but kept for demonstrating multi-color fill options.
     * @param colors A lista de cores para o gradiente.
     * @return Um objeto Paint que representa o gradiente radial.
     */
    private Paint createMultiColorFill(List<Color> colors) {
        if (colors.size() == 1) {
            return colors.get(0);
        }

        List<Stop> stops = new ArrayList<>();
        double step = 1.0 / (colors.size() - 1);
        for (int i = 0; i < colors.size(); i++) {
            stops.add(new Stop(i * step, colors.get(i)));
        }

        return new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                stops
        );
    }

    /**
     * NOVO MÉTODO: Criar padrão listrado para múltiplas cores (used for multi-cycle nodes)
     * @param colors A lista de cores para o padrão listrado.
     * @return Um objeto Paint que representa o padrão listrado.
     */
    private Paint createStripedPattern(List<Color> colors) {
        if (colors.size() == 1) {
            return colors.get(0);
        }

        List<Stop> stops = new ArrayList<>();
        double sectionSize = 1.0 / colors.size();

        for (int i = 0; i < colors.size(); i++) {
            double start = i * sectionSize;
            double end = (i + 1) * sectionSize;

            stops.add(new Stop(start, colors.get(i)));
            // Create an abrupt transition for striped effect
            if (i < colors.size() - 1) {
                stops.add(new Stop(end - 0.001, colors.get(i)));
            }
        }

        return new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE, // Linear gradient from top-left to bottom-right
                stops
        );
    }


    /**
     * Destaque um ciclo específico no grafo com uma cor distinta.
     * Gerencia cores para vértices em múltiplos ciclos.
     * MODIFIED: Agora usa `nodeToColors` para aplicar padrões multi-coloridos.
     * @param cycle A lista de vértices (String) que compõem o ciclo.
     */
    private void highlightCycle(List<String> cycle) {
        if (cycle == null || cycle.isEmpty()) {
            return;
        }

        Color currentCycleColor = cycleColors.get(nextCycleColorIndex);
        nextCycleColorIndex = (nextCycleColorIndex + 1) % cycleColors.size();

        // Atualiza o mapeamento de cores para cada nó do ciclo
        for (String verticeName : cycle) {
            nodeToColors.computeIfAbsent(verticeName, k -> new ArrayList<>()).add(currentCycleColor);

            if (nodesInAnyCycle.contains(verticeName)) {
                nodesInMultipleCycles.add(verticeName);
            } else {
                nodesInAnyCycle.add(verticeName);
            }
        }

        // Aplica as cores aos vértices
        for (String verticeName : cycle) {
            Circle circle = verticeCircleMap.get(verticeName);
            if (circle != null) {
                List<Color> nodeColors = nodeToColors.get(verticeName);

                if (nodeColors.size() == 1) {
                    // Apenas um ciclo: cor sólida
                    circle.setFill(nodeColors.get(0));
                    circle.setStroke(nodeColors.get(0).darker());
                } else {
                    // Múltiplos ciclos: padrão multi-cor (listrado)
                    Paint multiColorFill = createStripedPattern(nodeColors); // Using striped pattern
                    circle.setFill(multiColorFill);
                    circle.setStroke(Color.BLACK); // Borda preta para destaque
                    circle.setStrokeWidth(3); // Borda mais espessa
                }
            }
        }

        // Destaca as arestas do ciclo
        for (int i = 0; i < cycle.size(); i++) {
            String u = cycle.get(i);
            String v = (i == cycle.size() - 1) ? cycle.get(0) : cycle.get(i + 1);

            graphPane.getChildren().forEach(node -> {
                if (node instanceof Line) {
                    Line line = (Line) node;
                    Object userDataObj = line.getUserData();
                    if (userDataObj != null && userDataObj instanceof String) {
                        String userDataString = (String) userDataObj;
                        if (userDataString.equals("edge-" + u + "-" + v) ||
                                userDataString.equals("edge-" + v + "-" + u)) {
                            line.setStroke(currentCycleColor.darker());
                            line.setStrokeWidth(3);
                        }
                    }
                }
            });
        }
    }


    /**
     * Reseta as cores de todos os vértices e arestas para o estado padrão (não destacados).
     * Limpa os conjuntos de rastreamento de ciclos e o mapeamento de cores.
     * MODIFIED: Agora limpa `nodeToColors` e reseta `strokeWidth` dos círculos.
     */
    private void resetGraphHighlight() {
        verticeCircleMap.values().forEach(circle -> {
            circle.setFill(Color.LIGHTBLUE);
            circle.setStroke(Color.BLUE);
            circle.setStrokeWidth(2); // Reseta espessura da borda
        });

        graphPane.getChildren().forEach(node -> {
            if (node instanceof Line) {
                Line line = (Line) node;
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(2);
            }
        });

        // Limpa os mapeamentos de cores
        nodeToColors.clear(); // NEW LINE
        nodesInAnyCycle.clear();
        nodesInMultipleCycles.clear();
        nextCycleColorIndex = 0;
    }

    /**
     * Verifica a existência de ciclos no grafo e exibe os resultados na área de saída.
     * Destaca os ciclos encontrados visualmente com cores distintas e trata vértices em múltiplos ciclos.
     * MODIFIED: Melhorado o log e a mensagem de alerta para refletir o novo destaque visual.
     */
    private void verificarCiclos() {
        resetGraphHighlight();
        List<List<String>> ciclos = grafo.verificarCiclos();

        if (ciclos.isEmpty()) {
            logOutput("Não foram encontrados ciclos no grafo.");
        } else {
            logOutput("--- Ciclos Encontrados no Grafo ---");
            for (int i = 0; i < ciclos.size(); i++) {
                List<String> ciclo = ciclos.get(i);
                String corAtualHex = cycleColors.get(i % cycleColors.size()).toString();
                logOutput("Ciclo " + (i + 1) + " (Cor: " + corAtualHex + "): " + String.join(" -> ", ciclo));
                highlightCycle(ciclo);
            }

            // Log de vértices em múltiplos ciclos
            if (!nodesInMultipleCycles.isEmpty()) {
                logOutput("\n--- Vértices em Múltiplos Ciclos ---");
                for (String node : nodesInMultipleCycles) {
                    List<Color> cores = nodeToColors.get(node);
                    logOutput("Vértice '" + node + "' pertence a " + cores.size() + " ciclos.");
                }
            }

            logOutput("--- Fim da Verificação de Ciclos ---");

            String alertMessage = "Foram encontrados " + ciclos.size() + " ciclo(s) no grafo.";
            if (!nodesInMultipleCycles.isEmpty()) {
                alertMessage += "\nVértices em múltiplos ciclos são exibidos com um padrão listrado.";
            }
            alertMessage += "\nVeja os detalhes na área de log.";

            showAlert("Ciclos Encontrados!", alertMessage);
        }
    }

    /**
     * Salva a representação atual do grafo como uma imagem PNG.
     * @param stage O palco principal da aplicação para exibir o FileChooser.
     */
    private void saveGraphAsImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Grafo como Imagem");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage(
                        (int) graphPane.getWidth(), (int) graphPane.getHeight());
                graphPane.snapshot(new SnapshotParameters(), writableImage);

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                logOutput("Grafo salvo como imagem: " + file.getAbsolutePath());
            } catch (IOException ex) {
                logOutput("Erro ao salvar imagem: " + ex.getMessage());
                showAlert("Erro ao Salvar Imagem", "Ocorreu um erro ao salvar o grafo como imagem:\n" + ex.getMessage());
            }
        }
    }

    /**
     * Exibe um alerta com um título e mensagem especificados.
     *
     * @param title O título do alerta.
     * @param message A mensagem a ser exibida no alerta.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Método main para iniciar a aplicação.
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
