package JBTP.View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main game window that displays the chess board and game controls.
 */
public class GameWindow extends JFrame {
    private GameController controller;
    private ChessBoardUI boardUI;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JButton pauseResumeButton; // For timed games

    /**
     * Creates a new game window.
     */
    public GameWindow() {
        super("Chess Game");

        // Create controller
        controller = new GameController();
        controller.setView(this);

        // Set up the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create board UI
        boardUI = new ChessBoardUI(controller);
        add(boardUI, BorderLayout.CENTER);

        // Create status bar
        statusLabel = new JLabel("White's turn");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(statusLabel, BorderLayout.SOUTH);

        // Create control panel
        setupControlPanel();
        add(controlPanel, BorderLayout.EAST);

        // Pack and display
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Sets up the control panel with game buttons.
     */
    private void setupControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // New Game button
        JButton newGameButton = new JButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGameOptions();
            }
        });

        // Resign button
        JButton resignButton = new JButton("Resign");
        resignButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.surrender();
            }
        });

        // Flip Board button
        JButton flipBoardButton = new JButton("Flip Board");
        flipBoardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        flipBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardUI.setFlipped(!boardUI.isFlipped());
            }
        });

        // Pause/Resume button for timed games
        pauseResumeButton = new JButton("Pause");
        pauseResumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseResumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isRunning = controller.toggleClockPause();
                pauseResumeButton.setText(isRunning ? "Pause" : "Resume");
            }
        });

        controlPanel.add(newGameButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(resignButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(flipBoardButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(pauseResumeButton);

        pauseResumeButton.setVisible(false); // Hidden until we have a timed game
    }

    /**
     * Shows a dialog for selecting game options.
     */
    private void showGameOptions() {
        JDialog dialog = new JDialog(this, "New Game Options", true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Game mode selection
        JComboBox<String> gameModeCombo = new JComboBox<>(new String[] {
                "Player vs Player", "Player vs Computer", "Computer vs Computer"
        });
        panel.add(new JLabel("Game Mode:"));
        panel.add(gameModeCombo);

        // Time control
        JCheckBox timedGameCheckbox = new JCheckBox("Timed Game");
        panel.add(timedGameCheckbox);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(0, 0, 10, 1);
        SpinnerNumberModel minutesModel = new SpinnerNumberModel(15, 0, 59, 1);
        SpinnerNumberModel secondsModel = new SpinnerNumberModel(0, 0, 59, 5);

        JSpinner hoursSpinner = new JSpinner(hoursModel);
        JSpinner minutesSpinner = new JSpinner(minutesModel);
        JSpinner secondsSpinner = new JSpinner(secondsModel);

        timePanel.add(new JLabel("Time: "));
        timePanel.add(hoursSpinner);
        timePanel.add(new JLabel("h "));
        timePanel.add(minutesSpinner);
        timePanel.add(new JLabel("m "));
        timePanel.add(secondsSpinner);
        timePanel.add(new JLabel("s"));

        panel.add(timePanel);

        // Enable/disable time controls based on checkbox
        timedGameCheckbox.addActionListener(e -> {
            boolean enabled = timedGameCheckbox.isSelected();
            hoursSpinner.setEnabled(enabled);
            minutesSpinner.setEnabled(enabled);
            secondsSpinner.setEnabled(enabled);
        });

        // Initial state
        hoursSpinner.setEnabled(false);
        minutesSpinner.setEnabled(false);
        secondsSpinner.setEnabled(false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Game");
        JButton cancelButton = new JButton("Cancel");

        startButton.addActionListener(e -> {
            String gameMode = (String) gameModeCombo.getSelectedItem();
            if (timedGameCheckbox.isSelected()) {
                int hours = (Integer) hoursSpinner.getValue();
                int minutes = (Integer) minutesSpinner.getValue();
                int seconds = (Integer) secondsSpinner.getValue();
                controller.startTimedGame(gameMode, hours, minutes, seconds);
                pauseResumeButton.setVisible(true);
                pauseResumeButton.setText("Pause");
            } else {
                controller.startNewGame(gameMode);
                pauseResumeButton.setVisible(false);
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Gets the game controller.
     *
     * @return The game controller
     */
    public GameController getController() {
        return controller;
    }

    /**
     * Refreshes the board display.
     */
    public void refreshBoard() {
        boardUI.updateBoard();
    }

    /**
     * Updates the status text.
     *
     * @param status The status message to display
     */
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    /**
     * Shows a game over message.
     *
     * @param result The game result message
     */
    public void showGameOver(String result) {
        boardUI.clearSelection();
        JOptionPane.showMessageDialog(this, result, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Entry point for running the chess game.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StartMenu();
            }
        });
    }
}