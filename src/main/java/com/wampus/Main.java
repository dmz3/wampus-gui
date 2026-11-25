package com.wampus;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int SIZE = 4;

    private World world;
    private Hunter hunter;

    private GridPane gridPane;;
    private Label scoreLabel;
    private Label infoLabel;
    private CheckBox revealMapCheckBox;
    private Button stepButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Мир Вампуса — GUI");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #1e293b;");

        gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setAlignment(Pos.CENTER);
        root.setCenter(gridPane);

        VBox controlPanel = new VBox(12);
        controlPanel.setPadding(new Insets(10, 10, 10, 20));
        controlPanel.setPrefWidth(220);

        scoreLabel = new Label("Счет: 0");
        scoreLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 15px; -fx-font-weight: bold;");

        infoLabel = new Label("Стрела: Есть | Золото: Нет");
        infoLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        stepButton = new Button("Следующий шаг");
        stepButton.setMaxWidth(Double.MAX_VALUE);
        stepButton.setOnAction(e -> doStep());

        Button restartButton = new Button("Новая карта");
        restartButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setOnAction(e -> resetGame());

        revealMapCheckBox = new CheckBox("Показать всю карту");
        revealMapCheckBox.setStyle("-fx-text-fill: #e2e8f0;");
        revealMapCheckBox.setOnAction(e -> updateView());

        Label legend = new Label(
                "Обозначения:\n" +
                "▲▼◄► — Охотник\n" +
                "W — Вампус\n" +
                "P — Яма\n" +
                "G — Золото\n" +
                "S — Запах | B — Ветерок"
        );
        legend.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        controlPanel.getChildren().addAll(
                scoreLabel, infoLabel,
                new Separator(),
                stepButton, restartButton,
                revealMapCheckBox,
                new Separator(),
                legend
        );
        root.setRight(controlPanel);

        resetGame();

        Scene scene = new Scene(root, 720, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void resetGame() {
        world = new World(SIZE);
        hunter = new Hunter(world);
        hunter.initStart();
        stepButton.setDisable(false);
        updateView();
    }

    private void doStep() {
        if (!hunter.isAlive() || hunter.isFinished()) {
            return;
        }

        hunter.step();
        updateView();

        if (!hunter.isAlive() || hunter.isFinished()) {
            stepButton.setDisable(true);
        }
    }

    private void updateView() {
        gridPane.getChildren().clear();

        KnowledgeBase kb = hunter.getKb();
        Point hunterPos = hunter.getPos();
        boolean revealAll = revealMapCheckBox.isSelected();

        for (int y = SIZE - 1; y >= 0; y--) {
            for (int x = 0; x < SIZE; x++) {
                Point p = new Point(x, y);
                Room room = world.getRoom(x, y);
                Percept percept = world.getPercept(x, y);

                boolean visited = kb.isVisited(p);
                boolean safe = kb.isSafe(p);
                boolean isHunterHere = hunterPos.equals(p);
                boolean definiteWumpus = p.equals(kb.getDefiniteWumpus());

                StackPane cell = new StackPane();
                cell.setPrefSize(105, 105);

                String bgColor;
                if (isHunterHere) {
                    bgColor = hunter.isAlive() ? "#3b82f6" : "#ef4444";
                } else if (visited) {
                    bgColor = "#e2e8f0";
                } else if (definiteWumpus) {
                    bgColor = "#fca5a5";
                } else if (safe) {
                    bgColor = "#bbf7d0";
                } else {
                    bgColor = "#475569";
                }

                String borderColor = isHunterHere ? "#facc15" : "#0f172a";
                cell.setStyle(
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: " + (isHunterHere ? "3;" : "1;") +
                        "-fx-background-radius: 6; -fx-border-radius: 6;"
                );

                StringBuilder text = new StringBuilder();
                text.append("[").append(x).append(",").append(y).append("]\n");

                if (isHunterHere) {
                    text.append(getDirectionSymbol(hunter.getDir())).append(" ОХОТНИК\n");
                }

                if (visited || revealAll) {
                    if (room.hasWampus()) text.append("W ");
                    if (room.hasPit()) text.append("P ");
                    if (room.hasGold()) text.append("G ");
                    if (room.hasWampus() || room.hasPit() || room.hasGold()) text.append("\n");

                    if (percept.smell()) text.append("S ");
                    if (percept.breese()) text.append("B ");
                } else if (definiteWumpus) {
                    text.append("W! (точно)");
                } else if (safe) {
                    text.append("OK");
                } else {
                    text.append("?");
                }

                Label label = new Label(text.toString().trim());
                String textColor = (!visited && !safe && !isHunterHere && !definiteWumpus) ? "#cbd5e1" : "#0f172a";
                label.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-alignment: center;");
                cell.getChildren().add(label);

                int displayRow = (SIZE - 1) - y;
                gridPane.add(cell, x, displayRow);
            }
        }

        // Обновляем боковую панель
        scoreLabel.setText("Счет: " + hunter.getScore());
        infoLabel.setText("Стрела: " + (hunter.hasArrow() ? "Есть" : "Нет") +
                " | Золото: " + (hunter.hasGold() ? "Да" : "Нет"));
    }

    private String getDirectionSymbol(Direction dir) {
        return switch (dir) {
            case NORTH -> "▲";
            case SOUTH -> "▼";
            case EAST  -> "►";
            case WEST  -> "◄";
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}