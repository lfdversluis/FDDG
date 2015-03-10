package nl.tud.gui;

import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.entities.Unit;
import nl.tud.gameobjects.Field;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Martijn on 10-03-15.
 */
public class VisualizerGUI {

    private JPanel[][] panels;
    private Field field;

    public VisualizerGUI(Field field) {
        this.panels = new JPanel[Field.BOARD_HEIGHT][Field.BOARD_WIDTH];
        this.field = field;
        setupGUI();
    }

    private void setupGUI() {
        JFrame frame = new JFrame("FDDG Visualizer");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(Field.BOARD_WIDTH, Field.BOARD_HEIGHT));

        // create squares
        for(int x = 0; x < Field.BOARD_WIDTH; x++) {
            for(int y = 0; y < Field.BOARD_HEIGHT; y++) {
                JPanel p = new JPanel();
                p.setLayout(new GridBagLayout());
                p.setOpaque(true);

                // add health bar
                JLabel healthLabel = new JLabel("");
                healthLabel.setBackground(Color.YELLOW);
                healthLabel.setOpaque(true);
                p.add(healthLabel);

                // add label
                JLabel b = new JLabel("", SwingConstants.CENTER);
                p.add(b);
                panel.add(p);

                panels[y][x] = p;
            }
        }

        frame.add(panel);
        frame.setSize(900, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public JLabel getLabelInPanel(JPanel panel) {
        Component[] components = panel.getComponents();
        for(int i = 0; i < components.length; i++) {
            if(components[i] instanceof JLabel) {
                return (JLabel) components[i];
            }
        }
        return null;
    }

    public void updateGUI() {
        for(int x = 0; x < field.BOARD_WIDTH; x++) {
            for (int y = 0; y < field.BOARD_HEIGHT; y++) {
                JPanel panel = panels[y][x];
                JLabel label = getLabelInPanel(panels[y][x]);
                Unit unit = field.getUnit(x, y);
                if(unit == null) {
                    panel.setBackground(Color.WHITE);
                    label.setText("");
                } else if(unit instanceof Dragon) {
                    panel.setBackground(Color.RED);
                    label.setText("D");
                } else if(unit instanceof Player) {
                    panel.setBackground(Color.GREEN);
                    label.setText("P");
                }
            }
        }
    }
}
