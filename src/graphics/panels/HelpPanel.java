package graphics.panels;

import graphics.util.ButtonFactory;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class HelpPanel extends JPanel implements ActionListener {
    private int page;
    private JButton back;
    private JButton close;
    private JButton next;
    private static String previousPanel = "";

    public HelpPanel () {
        page = 1;
        setLayout(null);
        RootPanel.addPanel("help", this);

        back = ButtonFactory.create("/images/buttons/help/back.png", "/images/buttons/help/back-pressed.png", 272, 126);
        back.setBounds(72, 894, back.getIcon().getIconWidth(), 126);
        back.setActionCommand("back");
        back.addActionListener(this);
        back.setVisible(false);
        add(back);

        close = ButtonFactory.create("/images/buttons/help/close.png", "/images/buttons/help/close-pressed.png", 272, 126);
        int x = CenteredPosition.getPosition(272, 126).width;
        close.setBounds(x, 894, 272, 126);
        close.setActionCommand("close");
        close.addActionListener(this);
        add(close);

        next = ButtonFactory.create("/images/buttons/help/next.png", "/images/buttons/help/next-pressed.png", 272, 126);
        next.setBounds(1572, 894, 272, 126);
        next.setActionCommand("next");
        next.addActionListener(this);
        add(next);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "back":
                page--;
                break;
            case "next":
                page++;
                break;
            case "close":
                RootPanel.switchPanel(previousPanel);
                return;
        }

        if (page == 1) {
            back.setVisible(false);
            next.setVisible(true);
        } else if (page == 2) {
            next.setVisible(false);
            back.setVisible(true);
        }

        repaint();
    }

    public static void setPreviousPanel(String panelName) {
        previousPanel = panelName;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ImageIcon icon = null;

        try {
            icon = ImageCreate.get("/images/help-" + page + ".png");
        } catch (Exception e) {
            System.out.println(e);
        }

        //icon = ImageScaler.scale(icon, graphics.Window.width, Window.height);
        g.drawImage(icon.getImage(), 0, 0, null);
    }


}
