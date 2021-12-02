package graphics.panels;

import graphics.Window;
import graphics.util.ButtonFactory;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HelpPanel extends JPanel {
    private final JButton exit;
    private Container instr;
    private JTextArea area;

    public HelpPanel () {
        setLayout(null);
        JLabel title = null;
        add(title);
        exit = ButtonFactory.create("/images/buttons/title/exit.png", "/images/buttons/title/exit-pressed.png", 200, 100);
        setActionListeners();

        int width = exit.getPreferredSize().width;
        int height = exit.getPreferredSize().height;
        exit.setBounds(800, 800, width, height);
        add(exit);





    }

    private void setActionListeners() {
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed exit");
                RootPanel.switchPanel("Title");
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

        icon = ImageScaler.scale(icon, graphics.Window.width, Window.height);
        g.drawImage(icon.getImage(), 0, 0, null);
    }
}
