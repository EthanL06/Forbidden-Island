package graphics;

import graphics.panels.RootPanel;
import graphics.panels.TitlePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * JFrame of the application
 *
 * - Instantiates the RootPanel JPanel
 */

public class Window extends JFrame {

    public static final int width = 1920;
    public static final int height = 1080;
    public static int userWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int userHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

    public Window(String title) {
        super(title);
        setLayout(new BorderLayout());
        pack();

        RootPanel.setup();
        JPanel rootPanel = RootPanel.getPanel();
        RootPanel.addPanel("title", new TitlePanel());

        if (userWidth < width || userHeight < height) {
            JScrollPane scrollPane = new JScrollPane(rootPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.getVerticalScrollBar().setUnitIncrement(30);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(30);

            scrollPane.getHorizontalScrollBar().setMaximum(338);
            scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getMaximum()/2);
            add(scrollPane, BorderLayout.CENTER);
            scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    System.out.println(e.getValue());
                }
            });


        } else {
            add(rootPanel, BorderLayout.CENTER);
        }

        pack();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(screenSize);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

    }
}
