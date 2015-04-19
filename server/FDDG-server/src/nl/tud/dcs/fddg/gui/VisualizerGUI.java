package nl.tud.dcs.fddg.gui;

import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.game.entities.Unit;
import nl.tud.dcs.fddg.game.Field;

import javax.swing.*;
import java.awt.*;

public class VisualizerGUI {

    private JPanel[][] panels;
    private JLabel[][] unitLabels;
    private JPanel[][] healthPanels;
    private Field field;

    public VisualizerGUI(Field field) {
        this.panels = new JPanel[Field.BOARD_HEIGHT][Field.BOARD_WIDTH];
        this.unitLabels = new JLabel[Field.BOARD_HEIGHT][Field.BOARD_WIDTH];
        this.healthPanels = new JPanel[Field.BOARD_HEIGHT][Field.BOARD_WIDTH];
        this.field = field;
        setupGUI();
    }

    private void setupGUI() {
        JFrame frame = new JFrame("FDDG Visualizer");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(Field.BOARD_WIDTH, Field.BOARD_HEIGHT));

        // create squares
        for (int x = 0; x < Field.BOARD_WIDTH; x++) {
            for (int y = 0; y < Field.BOARD_HEIGHT; y++) {
                JPanel p = new JPanel();
                SpringLayout layout = new SpringLayout();
                p.setLayout(null);
                p.setOpaque(true);
                panels[y][x] = p;

                panel.add(p);
            }
        }

        frame.add(panel);
        frame.setSize(900, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        addHealthAndLabels();
    }

    public void addHealthAndLabels() {
        for (int x = 0; x < Field.BOARD_WIDTH; x++) {
            for (int y = 0; y < Field.BOARD_HEIGHT; y++) {

                // add health panel
                JPanel healthPanel = new JPanel();
                healthPanel.setBackground(Color.YELLOW);
                healthPanel.setOpaque(true);
                panels[y][x].add(healthPanel);
                healthPanel.setBounds(0, 0, panels[y][x].getWidth(), 6);
                healthPanel.setVisible(false);
                healthPanels[y][x] = healthPanel;

                // add label
                JLabel b = new JLabel("", SwingConstants.CENTER);
                panels[y][x].add(b);
                b.setBounds(0, 6, panels[y][x].getWidth(), panels[y][x].getHeight() - 6);
                unitLabels[y][x] = b;
            }
        }
    }

    public void updateGUI() {
        for (int x = 0; x < field.BOARD_WIDTH; x++) {
            for (int y = 0; y < field.BOARD_HEIGHT; y++) {
                JPanel panel = panels[y][x];
                JLabel label = unitLabels[y][x];
                JPanel healthPanel = healthPanels[y][x];
                Unit unit = field.getUnit(x, y);
                if (unit == null) {
                    panel.setBackground(Color.WHITE);
                    label.setText("");
                    healthPanel.setVisible(false);
                } else if (unit instanceof Dragon) {
                    panel.setBackground(Color.RED);
                    label.setText("D");
                    healthPanel.setVisible(true);

                    double percentage = unit.getHitPointsPercentage();
                    healthPanel.setBounds(0, 0, (int) (panel.getWidth() * percentage), 6);

                } else if (unit instanceof Player) {
                    panel.setBackground(Color.GREEN);
                    label.setText("P " + unit.getUnitId());
                    healthPanel.setVisible(true);

                    double percentage = unit.getHitPointsPercentage();
                    healthPanel.setBounds(0, 0, (int) (panel.getWidth() * percentage), 6);
                }
            }
        }
    }
}
