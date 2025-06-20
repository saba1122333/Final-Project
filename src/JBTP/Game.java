package JBTP;

import JBTP.View.StartMenu;

import javax.swing.*;

public class Game implements Runnable {
    public void run() {
        // Just create a new StartMenu with no parameters
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StartMenu();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}