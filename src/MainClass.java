/**
 * Author: Declan ONUNKWO
 * College: SUNY Oswego
 * CSC 365 Project 2
 * Fall 2023
 */

import javax.swing.*;
import java.io.IOException;

public class MainClass {

    public static void main(String[] args) {

        // look and feel for my gui
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                runGui();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void runGui() throws IOException, ClassNotFoundException {
        SimilarityAlgorithm gui = new SimilarityAlgorithm();
        JFrame frame = new JFrame("Wikipedia Page Similarity III");
        frame.setSize(650,500);
        frame.setContentPane(gui.getPanel());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
