package graphics.panels;

import graphics.Window;
import graphics.util.ButtonFactory;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TitlePanel extends JPanel {

    private final JButton play;
    private final JButton help;
    private final JButton exit;

    public TitlePanel() {
        setLayout(null);
        JLabel title = null;

        try {
            title = new JLabel(ImageCreate.get("/images/text/title.png"));
        } catch (Exception e) {
            System.out.println(e);
        }

        play = ButtonFactory.create("/images/buttons/title/play.png", "/images/buttons/title/play-pressed.png", 200, 100);
        help = ButtonFactory.create("/images/buttons/title/help.png", "/images/buttons/title/help-pressed.png", 200, 100);
        exit = ButtonFactory.create("/images/buttons/title/exit.png", "/images/buttons/title/exit-pressed.png", 200, 100);
        setActionListeners();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        int width = play.getPreferredSize().width;
        int height = play.getPreferredSize().height;

        int centeredX = CenteredPosition.getPosition(play.getIcon().getIconWidth(), play.getIcon().getIconHeight()).width;

        int centeredY = ((screenHeight-height)/2) + 50;
        int vGap = 40;

        //int x = CenteredPosition.getPosition(title.getPreferredSize().width, title.getPreferredSize().height).width;
        int x = CenteredPosition.getPosition(title.getIcon().getIconWidth(), title.getIcon().getIconHeight()).width;
        int y = 60;

        // System.out.println(y);

        title.setBounds(x, y, title.getIcon().getIconWidth(), title.getIcon().getIconHeight());
        play.setBounds(centeredX, (centeredY-height) - vGap, play.getPreferredSize().width, play.getPreferredSize().height);
        help.setBounds(centeredX, centeredY, play.getPreferredSize().width, play.getPreferredSize().height);
        exit.setBounds(centeredX, (centeredY+height) + vGap, play.getPreferredSize().width, play.getPreferredSize().height);

        add(title);
        add(play);
        add(help);
        add(exit);
    }

    private void setActionListeners() {
        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed play");
                new InputPanel();
                RootPanel.switchPanel("input");
//                RootPanel.addPanel("loading", new LoadingPanel(20007, 4, Difficulty.NORMAL)); // input here for debugging purposes
//                RootPanel.switchPanel("loading");
            }
        });

        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed help");
                RootPanel.switchPanel("help");
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ImageIcon icon = null;

        try {
            icon = ImageCreate.get("/images/background-blurred.png");
        } catch (Exception e) {
            System.out.println(e);
        }

        icon = ImageScaler.scale(icon, Window.width, Window.height);
        g.drawImage(icon.getImage(), 0, 0, null);
    }
}
