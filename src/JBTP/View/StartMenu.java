package JBTP.View;

import JBTP.Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Start menu for the chess game.
 */
public class StartMenu extends JFrame {
    private JComboBox<String> gameModeComboBox;
    private JCheckBox timedGameCheckBox;
    private JSpinner hoursSpinner;
    private JSpinner minutesSpinner;
    private JSpinner secondsSpinner;

    /**
     * Creates a new start menu.
     */
    public StartMenu() {
        super("Chess Game - Start Menu");

        // Set up the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(300, 250);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Game mode selection
        JPanel gameModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gameModePanel.add(new JLabel("Game Mode:"));

        String[] gameModes = {"Player vs Player", "Player vs Computer", "Computer vs Computer"};
        gameModeComboBox = new JComboBox<>(gameModes);
        gameModePanel.add(gameModeComboBox);

        // Time control options
        JPanel timeControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timedGameCheckBox = new JCheckBox("Timed Game");
        timeControlPanel.add(timedGameCheckBox);

        // Time spinners
        JPanel timeSpinnersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeSpinnersPanel.add(new JLabel("Time Control:"));

        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 59, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        timeSpinnersPanel.add(hoursSpinner);
        timeSpinnersPanel.add(new JLabel("h"));
        timeSpinnersPanel.add(minutesSpinner);
        timeSpinnersPanel.add(new JLabel("m"));
        timeSpinnersPanel.add(secondsSpinner);
        timeSpinnersPanel.add(new JLabel("s"));

        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        // Add components to main panel
        mainPanel.add(gameModePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(timeControlPanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(timeSpinnersPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startButton);

        // Add panels to frame
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Enable/disable time controls based on checkbox
        timedGameCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = timedGameCheckBox.isSelected();
                hoursSpinner.setEnabled(enabled);
                minutesSpinner.setEnabled(enabled);
                secondsSpinner.setEnabled(enabled);
            }
        });

        // Initial state
        timedGameCheckBox.setSelected(false);
        hoursSpinner.setEnabled(false);
        minutesSpinner.setEnabled(false);
        secondsSpinner.setEnabled(false);

        // Center on screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Starts a new chess game with the selected options.
     */
    private void startGame() {
        String gameMode = (String) gameModeComboBox.getSelectedItem();

        GameWindow gameWindow = new GameWindow();
        GameController controller = gameWindow.getController();

        if (timedGameCheckBox.isSelected()) {
            int hours = (Integer) hoursSpinner.getValue();
            int minutes = (Integer) minutesSpinner.getValue();
            int seconds = (Integer) secondsSpinner.getValue();
            controller.startTimedGame(gameMode, hours, minutes, seconds);
        } else {
            controller.startNewGame(gameMode);
        }

        // Close the start menu
        dispose();
    }

    /**
     * Entry point for the application.
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