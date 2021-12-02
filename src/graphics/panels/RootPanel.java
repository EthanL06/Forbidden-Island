package graphics.panels;

import graphics.Window;

import javax.swing.*;
import java.awt.*;

/**
 * Class that holds a JPanel with a CardLayout
 * Allows application to switch between JPanels
 */

public class RootPanel {

    private static final CardLayout cl = new CardLayout();
    private static final JPanel rootPanel = new JPanel(cl);

    public RootPanel() {
//        addPanel("title", new TitlePanel());
//        addPanel("input", new InputPanel());
//        addPanel("instructions", new InstructionsPanel());
//        addPanel("board", new BoardPanel());
//        switchPanel("instructions");
    }

    public static void setup() {
        rootPanel.setPreferredSize(new Dimension(1920, 1080));
    }

    public static JPanel getPanel() {
        return rootPanel;
    }

    public static void addPanel(String panelName, JPanel panel) {
        rootPanel.add(panel, panelName);
    }

    public static void switchPanel(String panelName) {
        cl.show(rootPanel, panelName);
    }


}

