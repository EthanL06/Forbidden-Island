package graphics.util;

import javax.swing.*;
import java.awt.*;

public class ImageScaler {

    public static ImageIcon scale(ImageIcon icon, int newWidth, int newHeight) {
        int oldWidth = icon.getIconWidth();
        int oldHeight = icon.getIconHeight();

        Image image = icon.getImage().getScaledInstance(oldWidth * newWidth / oldWidth, oldHeight * newHeight / oldHeight, Image.SCALE_SMOOTH);

        return new ImageIcon(image);
    }
}
