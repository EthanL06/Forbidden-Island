package graphics.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ImageCreate {

    private static HashMap<String, ImageIcon> images = new HashMap<>();

    public static ImageIcon get(String url) {
        if (images.containsKey(url))
            return images.get(url);

        ImageIcon imageIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ImageCreate.class.getResource(url)));
        images.put(url, imageIcon);

        return imageIcon;
    }

}
