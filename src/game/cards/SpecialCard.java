package game.cards;

import game.enums.Special;
import graphics.util.ImageCreate;

import javax.imageio.ImageIO;
import javax.swing.*;

public class SpecialCard {
    private final Special type;
    private final String name;
    private ImageIcon image, selectedImage;

    public SpecialCard(Special special) {
        this.type = special;
        name = special.name();
        setImage();
    }

    private void setImage() {
        String fileName = name.replaceAll(" ", "_") + ".png";
//            image = ImageIO.read(getClass().getResourceAsStream("/images/cards/treasure/" + fileName));
        image = null;
        selectedImage = null;

        try {
            image = ImageCreate.get("/images/cards/treasure/" + fileName);
            selectedImage = ImageCreate.get("/images/cards/treasure/selected/" + fileName);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Special getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ImageIcon getImage() {
        return image;
    }

    public ImageIcon getSelectedImage() {
        return selectedImage;
    }

    public String toString() {
        return type.name();
    }
}
