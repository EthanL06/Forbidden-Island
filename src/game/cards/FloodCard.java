package game.cards;

import graphics.util.ButtonFactory;
import graphics.util.ImageCreate;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

public class FloodCard {
    private final String name;
    private ImageIcon image = null;

    public FloodCard(String name) {
        this.name = name;
        setImage();
    }

    private void setImage() {
        try {
            String fileName = name.replaceAll(" ", "_") + ".png";
            image = ImageCreate.get("/images/cards/flood/" + fileName);
            // images/cards/flood/Cave_Of_Embers.png
//            image = ButtonFactory.getImage("/images/cards/flood/" + fileName);
        } catch (Exception e) {
            System.err.println("Error reading image in FloodCard class with name " + name);
            System.err.println(e);
        }
    }

    public String getName() {
        return name;
    }

    public ImageIcon getImage() {
        return image;
    }
}
