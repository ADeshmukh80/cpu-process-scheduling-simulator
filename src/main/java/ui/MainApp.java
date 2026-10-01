package ui;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.Process;
import result.GanttSegment;
import result.ScheduleResult;
import service.SchedulingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CPU Process Scheduling Simulator - JavaFX GUI.
 *
 * This is intentionally written as one class, built up programmatically
 * (no FXML) so the whole UI is easy to read top to bottom as a beginner.
 * In a bigger production app you would typically split this into an FXML
 * view + a separate Controller class.
 */
public class MainApp extends Application {

    // ----- Core data + services -----
    private final ObservableList<Process> inputProcesses = FXCollections.observableArrayList();
    private final SchedulingService schedulingService = new SchedulingService();
    private final Map<String, Color> processColors = new HashMap<>();
    private static final double PIXELS_PER_TIME_UNIT = 40.0;

    // ----- Widgets referenced across methods -----
    private TableView<Process> inputTable;
    private TableView<Process> resultTable;
    private TableView<ComparisonRow> comparisonTable;
    private ComboBox<String> algorithmDropdown;
    private TextField quantumField;
    private Label avgWaitingLabel;
    private Label avgTurnaroundLabel;
    private Label avgResponseLabel;
    private HBox ganttBox;
    private Label ganttTimelineLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("CPU Process Scheduling Simulator");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));
        root.setTop(title);

        VBox center = new VBox(15);
        center.getChildren().addAll(
                buildInputSection(),
                buildControlsSection(),
                new Separator(),
                buildGanttSection(),
                buildResultSection(),
                buildComparisonSection()
        );

        ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1000, 850);
        stage.setTitle("CPU Process Scheduling Simulator");
        stage.setScene(scene);
        stage.show();
    }

    // ---------------------------------------------------------------
    // SECTION 1: Process input (add / remove / clear + table)
    // ---------------------------------------------------------------
    private VBox buildInputSection() {
        Label header = sectionHeader("1. Add Processes");

        TextField idField = new TextField();
        idField.setPromptText("Process ID (e.g. P1)");
        TextField arrivalField = new TextField();
        arrivalField.setPromptText("Arrival Time");
        TextField burstField = new TextField();
        burstField.setPromptText("Burst Time");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority (lower = higher)");

        Button addButton = new Button("Add Process");
        addButton.setOnAction(e -> onAddProcess(idField, arrivalField, burstField, priorityField));

        HBox form = new HBox(10, idField, arrivalField, burstField, priorityField, addButton);
        form.setAlignment(Pos.CENTER_LEFT);

        inputTable = new TableView<>(inputProcesses);
        inputTable.setPrefHeight(180);
        inputTable.getColumns().addAll(
                textColumn("Process ID", Process::getProcessId, 100),
                intColumn("Arrival Time", Process::getArrivalTime, 100),
                intColumn("Burst Time", Process::getBurstTime, 100),
                intColumn("Priority", Process::getPriority, 100)
        );

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            Process selected = inputTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                inputProcesses.remove(selected);
            }
        });

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> inputProcesses.clear());

        HBox tableButtons = new HBox(10, removeButton, clearButton);

        VBox box = new VBox(8, header, form, inputTable, tableButtons);
        return box;
    }

    private void onAddProcess(TextField idField, TextField arrivalField, TextField burstField, TextField priorityField) {
        try {
            String id = idField.getText().trim();
            int arrival = Integer.parseInt(arrivalField.getText().trim());
            int burst = Integer.parseInt(burstField.getText().trim());
            int priority = priorityField.getText().trim().isEmpty() ? 0 : Integer.parseInt(priorityField.getText().trim());

            Process process = new Process(id, arrival, burst, priority);
            inputProcesses.add(process);

            idField.clear();
            arrivalField.clear();
            burstField.clear();
            priorityField.clear();
            idField.requestFocus();
        } catch (NumberFormatException ex) {
            showError("Arrival Time, Burst Time and Priority must be whole numbers.");
        }
    }

    // ---------------------------------------------------------------
    // SECTION 2: Algorithm selection + Run / Compare
    // ---------------------------------------------------------------
    private VBox buildControlsSection() {
        Label header = sectionHeader("2. Choose Algorithm & Run");

        algorithmDropdown = new ComboBox<>();
        algorithmDropdown.getItems().addAll(
                "FCFS",
                "SJF (Non-Preemptive)",
                "SRTF (Preemptive)",
                "Priority (Non-Preemptive)",
                "Priority (Preemptive)",
                "Round Robin"
        );
        algorithmDropdown.getSelectionModel().selectFirst();

        quantumField = new TextField();
        quantumField.setPromptText("Time Quantum (Round Robin only)");
        quantumField.setPrefWidth(220);

        Button runButton = new Button("Run Simulation");
        runButton.setOnAction(e -> onRunSimulation());

        Button compareButton = new Button("Compare All Algorithms");
        compareButton.setOnAction(e -> onCompareAll());

        HBox controls = new HBox(10, algorithmDropdown, quantumField, runButton, compareButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, header, controls);
    }

    private void onRunSimulation() {
        try {
            SchedulingService.AlgorithmType type = mapDropdownToAlgorithmType(algorithmDropdown.getValue());
            int quantum = parseQuantumOrDefault();

            ScheduleResult result = schedulingService.run(List.copyOf(inputProcesses), type, quantum);
            displayResult(result);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onCompareAll() {
        try {
            int quantum = parseQuantumOrDefault();
            List<ScheduleResult> results = schedulingService.runAllForComparison(List.copyOf(inputProcesses), quantum);

            ObservableList<ComparisonRow> rows = FXCollections.observableArrayList();
            for (ScheduleResult r : results) {
                rows.add(new ComparisonRow(r.getAlgorithmName(), r.getAverageWaitingTime(),
                        r.getAverageTurnaroundTime(), r.getAverageResponseTime()));
            }
            comparisonTable.setItems(rows);

            // Also show the first result (FCFS) in the main result/Gantt views for context.
            if (!results.isEmpty()) {
                displayResult(results.get(0));
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private int parseQuantumOrDefault() {
        String text = quantumField.getText() == null ? "" : quantumField.getText().trim();
        if (text.isEmpty()) {
            return 2; // sensible default so non-RR algorithms never need it
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Time Quantum must be a whole number.");
        }
    }

    private SchedulingService.AlgorithmType mapDropdownToAlgorithmType(String selection) {
        switch (selection) {
            case "FCFS":
                return SchedulingService.AlgorithmType.FCFS;
            case "SJF (Non-Preemptive)":
                return SchedulingService.AlgorithmType.SJF;
            case "SRTF (Preemptive)":
                return SchedulingService.AlgorithmType.SRTF;
            case "Priority (Non-Preemptive)":
                return SchedulingService.AlgorithmType.PRIORITY_NON_PREEMPTIVE;
            case "Priority (Preemptive)":
                return SchedulingService.AlgorithmType.PRIORITY_PREEMPTIVE;
            case "Round Robin":
                return SchedulingService.AlgorithmType.ROUND_ROBIN;
            default:
                throw new IllegalArgumentException("Please select an algorithm.");
        }
    }

    // ---------------------------------------------------------------
    // SECTION 3: Gantt chart
    // ---------------------------------------------------------------
    private VBox buildGanttSection() {
        Label header = sectionHeader("3. Gantt Chart");
        ganttBox = new HBox(0);
        ganttBox.setAlignment(Pos.CENTER_LEFT);
        ganttTimelineLabel = new Label();
        ScrollPane scroll = new ScrollPane(ganttBox);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(90);
        return new VBox(8, header, scroll, ganttTimelineLabel);
    }

    private void renderGantt(List<GanttSegment> segments) {
        ganttBox.getChildren().clear();
        StringBuilder timeline = new StringBuilder();

        for (GanttSegment seg : segments) {
            double width = Math.max(seg.getDuration() * PIXELS_PER_TIME_UNIT, 30);
            Rectangle rect = new Rectangle(width, 50);
            rect.setFill(colorFor(seg.getProcessId()));
            rect.setStroke(Color.BLACK);

            Label label = new Label(seg.getProcessId());
            label.setStyle("-fx-font-weight: bold;");

            StackPane block = new StackPane(rect, label);
            Tooltip.install(block, new Tooltip(
                    seg.getProcessId() + ": " + seg.getStartTime() + " to " + seg.getEndTime()));

            ganttBox.getChildren().add(block);
            timeline.append(seg.getStartTime()).append("   ");
        }

        if (!segments.isEmpty()) {
            timeline.append(segments.get(segments.size() - 1).getEndTime());
        }
        ganttTimelineLabel.setText("Time: " + timeline);
    }

    private Color colorFor(String processId) {
        if (GanttSegment.IDLE.equals(processId)) {
            return Color.LIGHTGRAY;
        }
        return processColors.computeIfAbsent(processId, id -> {
            // Deterministic pseudo-random color per process id, so the same
            // process always gets the same color across re-runs.
            int hash = Math.abs(id.hashCode());
            double hue = (hash % 360);
            return Color.hsb(hue, 0.55, 0.85);
        });
    }

    // ---------------------------------------------------------------
    // SECTION 4: Result table + averages
    // ---------------------------------------------------------------
    private VBox buildResultSection() {
        Label header = sectionHeader("4. Scheduling Result");

        resultTable = new TableView<>();
        resultTable.setPrefHeight(220);
        resultTable.getColumns().addAll(
                textColumn("Process", Process::getProcessId, 80),
                intColumn("AT", Process::getArrivalTime, 60),
                intColumn("BT", Process::getBurstTime, 60),
                intColumn("Priority", Process::getPriority, 70),
                intColumn("Start", Process::getStartTime, 60),
                intColumn("CT", Process::getCompletionTime, 60),
                intColumn("TAT", Process::getTurnaroundTime, 60),
                intColumn("WT", Process::getWaitingTime, 60),
                intColumn("RT", Process::getResponseTime, 60)
        );

        avgWaitingLabel = new Label("Average Waiting Time: -");
        avgTurnaroundLabel = new Label("Average Turnaround Time: -");
        avgResponseLabel = new Label("Average Response Time: -");

        VBox averages = new VBox(4, avgWaitingLabel, avgTurnaroundLabel, avgResponseLabel);

        return new VBox(8, header, resultTable, averages);
    }

    private void displayResult(ScheduleResult result) {
        resultTable.setItems(FXCollections.observableArrayList(result.getProcesses()));
        renderGantt(result.getGanttChart());

        avgWaitingLabel.setText(String.format("Average Waiting Time: %.2f", result.getAverageWaitingTime()));
        avgTurnaroundLabel.setText(String.format("Average Turnaround Time: %.2f", result.getAverageTurnaroundTime()));
        avgResponseLabel.setText(String.format("Average Response Time: %.2f", result.getAverageResponseTime()));
    }

    // ---------------------------------------------------------------
    // SECTION 5: Algorithm comparison table
    // ---------------------------------------------------------------
    private VBox buildComparisonSection() {
        Label header = sectionHeader("5. Algorithm Comparison");
        Label note = new Label("No single algorithm is universally best - each optimizes different goals.");
        note.setStyle("-fx-font-style: italic;");

        comparisonTable = new TableView<>();
        comparisonTable.setPrefHeight(180);
        comparisonTable.getColumns().addAll(
                textColumnGeneric("Algorithm", ComparisonRow::getAlgorithmName, 320),
                doubleColumn("Avg WT", ComparisonRow::getAvgWaitingTime, 100),
                doubleColumn("Avg TAT", ComparisonRow::getAvgTurnaroundTime, 100),
                doubleColumn("Avg RT", ComparisonRow::getAvgResponseTime, 100)
        );

        return new VBox(8, header, note, comparisonTable);
    }

    // ---------------------------------------------------------------
    // Small helpers
    // ---------------------------------------------------------------
    private Label sectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        return label;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Invalid Input");
        alert.showAndWait();
    }

    private TableColumn<Process, String> textColumn(String title, java.util.function.Function<Process, String> extractor, double width) {
        TableColumn<Process, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.apply(data.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private TableColumn<Process, Integer> intColumn(String title, java.util.function.ToIntFunction<Process> extractor, double width) {
        TableColumn<Process, Integer> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.applyAsInt(data.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private TableColumn<ComparisonRow, String> textColumnGeneric(String title, java.util.function.Function<ComparisonRow, String> extractor, double width) {
        TableColumn<ComparisonRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.apply(data.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private TableColumn<ComparisonRow, String> doubleColumn(String title, java.util.function.ToDoubleFunction<ComparisonRow> extractor, double width) {
        TableColumn<ComparisonRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(String.format("%.2f", extractor.applyAsDouble(data.getValue()))));
        col.setPrefWidth(width);
        return col;
    }

    /** Simple row object just for the comparison table. */
    public static class ComparisonRow {
        private final String algorithmName;
        private final double avgWaitingTime;
        private final double avgTurnaroundTime;
        private final double avgResponseTime;

        public ComparisonRow(String algorithmName, double avgWaitingTime, double avgTurnaroundTime, double avgResponseTime) {
            this.algorithmName = algorithmName;
            this.avgWaitingTime = avgWaitingTime;
            this.avgTurnaroundTime = avgTurnaroundTime;
            this.avgResponseTime = avgResponseTime;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public double getAvgWaitingTime() {
            return avgWaitingTime;
        }

        public double getAvgTurnaroundTime() {
            return avgTurnaroundTime;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
