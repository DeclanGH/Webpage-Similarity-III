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
        JFrame frame = new JFrame();
        frame.setContentPane(gui.getPanel());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
