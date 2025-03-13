package chess;

import gui.ChessGUI;

import javax.swing.*;

/**
 * Main class to start the chess application
 */
public class ChessMain {
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the GUI on the EDT
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}