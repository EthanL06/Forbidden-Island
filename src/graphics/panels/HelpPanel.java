package graphics.panels;

import graphics.Window;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HelpPanel extends JPanel {

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
