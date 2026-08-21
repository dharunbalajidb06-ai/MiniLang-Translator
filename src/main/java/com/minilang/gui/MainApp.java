package com.minilang.gui;

import com.minilang.MiniLangTranslator;
import com.minilang.TranslationResult;
import com.minilang.errors.CompilerException;
import com.minilang.lexer.Token;
import com.minilang.symbol.Symbol;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Professional JavaFX GUI for the MiniLang Multi-Target Translator.
 * Provides live source editing, translation orchestration, and multi-tab stage inspection.
 */
public class MainApp extends Application {

    private final MiniLangTranslator translator = new MiniLangTranslator();

    private TextArea sourceCodeEditor;
    private ComboBox<String> targetLanguageCombo;
    private ComboBox<String> exampleSelectorCombo;

    // Inspection tabs
    private TableView<Token> tokenTable;
    private TextArea astArea;
    private TableView<Symbol> symbolTableTable;
    private TextArea rawTacArea;
    private TextArea optTacArea;
    private TextArea targetCodeArea;
    private TextArea performanceArea;
    private TextArea diagnosticsArea;
    private TabPane tabPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MiniLang Multi-Target Translator [Final-Year Project]");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-background-color: #f4f6f9;");

        // Header Title
        VBox headerBox = createHeader();
        root.setTop(headerBox);

        // Center SplitPane: Left = Source Editor & Controls, Right = Multi-Tab Inspection
        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.HORIZONTAL);
        mainSplit.setDividerPositions(0.42);

        VBox leftPane = createEditorPane();
        VBox rightPane = createInspectionPane();

        mainSplit.getItems().addAll(leftPane, rightPane);
        root.setCenter(mainSplit);

        // Status Bar
        Label statusBar = new Label("Ready | Java 17 | Multi-Target: Python, Java, C");
        statusBar.setPadding(new Insets(6, 4, 2, 4));
        statusBar.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1180, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load default example
        loadExample("Arithmetic & Variables");
    }

    private VBox createHeader() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("MiniLang Multi-Target Translator");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        Label subtitle = new Label("Source-to-Source Compiler Pipeline: Lexer → Parser → AST → Semantic Analysis → TAC IR → Optimizer → Code Gen");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private VBox createEditorPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8));

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label exampleLbl = new Label("Sample:");
        exampleSelectorCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Arithmetic & Variables",
                "If-Else Decision",
                "While Loop Counter",
                "String Concatenation",
                "Type Error Demo"
        ));
        exampleSelectorCombo.setValue("Arithmetic & Variables");
        exampleSelectorCombo.setOnAction(e -> loadExample(exampleSelectorCombo.getValue()));

        Label targetLbl = new Label("Target:");
        targetLanguageCombo = new ComboBox<>(FXCollections.observableArrayList("Python", "Java", "C"));
        targetLanguageCombo.setValue("Python");
        targetLanguageCombo.setOnAction(e -> handleTranslate());

        Button translateBtn = new Button("▶ Translate");
        translateBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        translateBtn.setOnAction(e -> handleTranslate());

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            sourceCodeEditor.clear();
            clearOutputs();
        });

        toolbar.getChildren().addAll(exampleLbl, exampleSelectorCombo, targetLbl, targetLanguageCombo, translateBtn, clearBtn);

        Label editorLabel = new Label("MiniLang Source Code (.ml):");
        editorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        sourceCodeEditor = new TextArea();
        sourceCodeEditor.setFont(Font.font("Consolas", 13));
        sourceCodeEditor.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-highlight-fill: #264f78;");
        VBox.setVgrow(sourceCodeEditor, Priority.ALWAYS);

        box.getChildren().addAll(toolbar, editorLabel, sourceCodeEditor);
        return box;
    }

    private VBox createInspectionPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8));

        tabPane = new TabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // 1. Target Code Tab
        Tab targetCodeTab = new Tab("💻 Target Code");
        targetCodeTab.setClosable(false);
        targetCodeArea = createMonospacedTextArea(true);
        targetCodeTab.setContent(targetCodeArea);

        // 2. Tokens Tab
        Tab tokenTab = new Tab("🏷️ Tokens");
        tokenTab.setClosable(false);
        tokenTable = createTokenTable();
        tokenTab.setContent(tokenTable);

        // 3. AST Tab
        Tab astTab = new Tab("🌳 AST");
        astTab.setClosable(false);
        astArea = createMonospacedTextArea(false);
        astTab.setContent(astArea);

        // 4. Symbol Table Tab
        Tab symbolTab = new Tab("📋 Symbol Table");
        symbolTab.setClosable(false);
        symbolTableTable = createSymbolTable();
        symbolTab.setContent(symbolTableTable);

        // 5. Raw TAC Tab
        Tab rawTacTab = new Tab("⚙️ TAC IR");
        rawTacTab.setClosable(false);
        rawTacArea = createMonospacedTextArea(false);
        rawTacTab.setContent(rawTacArea);

        // 6. Optimized TAC Tab
        Tab optTacTab = new Tab("⚡ Optimized TAC");
        optTacTab.setClosable(false);
        optTacArea = createMonospacedTextArea(false);
        optTacTab.setContent(optTacArea);

        // 7. Performance Tab
        Tab perfTab = new Tab("⏱️ Performance");
        perfTab.setClosable(false);
        performanceArea = createMonospacedTextArea(false);
        perfTab.setContent(performanceArea);

        // 8. Diagnostics / Errors Tab
        Tab diagTab = new Tab("⚠️ Diagnostics");
        diagTab.setClosable(false);
        diagnosticsArea = createMonospacedTextArea(false);
        diagnosticsArea.setStyle("-fx-text-fill: #b91c1c; -fx-font-family: Consolas;");
        diagTab.setContent(diagnosticsArea);

        tabPane.getTabs().addAll(targetCodeTab, tokenTab, astTab, symbolTab, rawTacTab, optTacTab, perfTab, diagTab);

        box.getChildren().add(tabPane);
        return box;
    }

    private TableView<Token> createTokenTable() {
        TableView<Token> table = new TableView<>();

        TableColumn<Token, String> typeCol = new TableColumn<>("Token Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        typeCol.setPrefWidth(140);

        TableColumn<Token, String> lexemeCol = new TableColumn<>("Lexeme");
        lexemeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLexeme()));
        lexemeCol.setPrefWidth(120);

        TableColumn<Token, Number> lineCol = new TableColumn<>("Line");
        lineCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLine()));
        lineCol.setPrefWidth(60);

        TableColumn<Token, Number> colCol = new TableColumn<>("Column");
        colCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getColumn()));
        colCol.setPrefWidth(70);

        TableColumn<Token, String> valCol = new TableColumn<>("Literal Value");
        valCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLiteralValue() != null ? String.valueOf(data.getValue().getLiteralValue()) : ""
        ));
        valCol.setPrefWidth(120);

        table.getColumns().addAll(typeCol, lexemeCol, lineCol, colCol, valCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<Symbol> createSymbolTable() {
        TableView<Symbol> table = new TableView<>();

        TableColumn<Symbol, String> nameCol = new TableColumn<>("Variable Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(130);

        TableColumn<Symbol, String> typeCol = new TableColumn<>("Data Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        typeCol.setPrefWidth(100);

        TableColumn<Symbol, Number> scopeCol = new TableColumn<>("Scope Level");
        scopeCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getScopeLevel()));
        scopeCol.setPrefWidth(90);

        TableColumn<Symbol, Number> lineCol = new TableColumn<>("Decl Line");
        lineCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLine()));
        lineCol.setPrefWidth(80);

        TableColumn<Symbol, String> initCol = new TableColumn<>("Initialized");
        initCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isInitialized() ? "Yes" : "No"));
        initCol.setPrefWidth(90);

        table.getColumns().addAll(nameCol, typeCol, scopeCol, lineCol, initCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TextArea createMonospacedTextArea(boolean isTargetCode) {
        TextArea area = new TextArea();
        area.setFont(Font.font("Consolas", 13));
        area.setEditable(false);
        if (isTargetCode) {
            area.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #9cdcfe;");
        }
        return area;
    }

    private void handleTranslate() {
        String source = sourceCodeEditor.getText();
        clearOutputs();

        if (source.trim().isEmpty()) {
            diagnosticsArea.setText("Source code is empty. Please enter MiniLang code.");
            tabPane.getSelectionModel().select(7); // Select diagnostics tab
            return;
        }

        try {
            TranslationResult result = translator.translate(source);

            // Populate Tokens
            tokenTable.setItems(FXCollections.observableArrayList(result.getTokens()));

            // Populate AST
            astArea.setText(result.getAstTreeString());

            // Populate Symbol Table
            symbolTableTable.setItems(FXCollections.observableArrayList(result.getSymbolTable().getAllDeclaredSymbols()));

            // Populate TAC
            rawTacArea.setText(result.getRawTAC().toFormattedString());
            optTacArea.setText(result.getOptimizedTAC().toFormattedString());

            // Populate Target Code based on user selection
            String selectedTarget = targetLanguageCombo.getValue();
            if ("Python".equalsIgnoreCase(selectedTarget)) {
                targetCodeArea.setText(result.getPythonCode());
            } else if ("Java".equalsIgnoreCase(selectedTarget)) {
                targetCodeArea.setText(result.getJavaCode());
            } else {
                targetCodeArea.setText(result.getCCode());
            }

            // Populate Performance timings
            StringBuilder perfSb = new StringBuilder("=== Translation Phase Performance Metrics ===\n\n");
            for (Map.Entry<String, Double> entry : result.getPhaseTimingsMs().entrySet()) {
                perfSb.append(String.format("%-28s : %8.4f ms\n", entry.getKey(), entry.getValue()));
            }
            performanceArea.setText(perfSb.toString());

            diagnosticsArea.setText("✓ Compilation & Translation completed successfully with 0 errors.");
            diagnosticsArea.setStyle("-fx-text-fill: #15803d; -fx-font-family: Consolas;");

        } catch (CompilerException ex) {
            diagnosticsArea.setText(ex.getMessage() + "\n\nError Phase: " + ex.getErrorType() +
                    "\nLine: " + ex.getLine() + "\nColumn: " + ex.getColumn());
            diagnosticsArea.setStyle("-fx-text-fill: #b91c1c; -fx-font-family: Consolas;");
            tabPane.getSelectionModel().select(7); // Jump to Diagnostics tab
        } catch (Exception ex) {
            diagnosticsArea.setText("Unexpected error: " + ex.getMessage());
            diagnosticsArea.setStyle("-fx-text-fill: #b91c1c; -fx-font-family: Consolas;");
            tabPane.getSelectionModel().select(7);
        }
    }

    private void clearOutputs() {
        tokenTable.getItems().clear();
        astArea.clear();
        symbolTableTable.getItems().clear();
        rawTacArea.clear();
        optTacArea.clear();
        targetCodeArea.clear();
        performanceArea.clear();
        diagnosticsArea.clear();
    }

    private void loadExample(String exampleName) {
        String code = switch (exampleName) {
            case "Arithmetic & Variables" -> """
                    // Program 1: Arithmetic & Variables
                    int a = 10;
                    int b = 20;
                    int result;
                    
                    result = a + b * 2;
                    print(result);
                    """;
            case "If-Else Decision" -> """
                    // Program 2: If-Else Decision Logic
                    int score = 85;
                    
                    if (score >= 50) {
                        print("Pass");
                    } else {
                        print("Fail");
                    }
                    """;
            case "While Loop Counter" -> """
                    // Program 3: While Loop Counter
                    int counter = 1;
                    int total = 0;
                    
                    while (counter <= 5) {
                        total = total + counter;
                        counter = counter + 1;
                    }
                    
                    print(total);
                    """;
            case "String Concatenation" -> """
                    // Program 4: String Concatenation & Types
                    string greeting = "Hello, ";
                    string user = "Dharun";
                    string message = greeting + user;
                    
                    print(message);
                    """;
            case "Type Error Demo" -> """
                    // Program 5: Intentional Type Mismatch Error
                    int x = 10;
                    x = "This should trigger a semantic error";
                    print(x);
                    """;
            default -> "";
        };

        sourceCodeEditor.setText(code);
        handleTranslate();
    }
}
