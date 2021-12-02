package game.cards;
import game.enums.*;

import game.enums.Treasure;
import graphics.util.ImageCreate;

import javax.imageio.ImageIO;
import javax.swing.*;

public class TreasureCard {
    private final Treasure treasure;
    private TreasureState state;
    private final String name;
    private ImageIcon image, selectedImage;

    public TreasureCard(Treasure treasure) {
        state = TreasureState.UNCAPTURED;
        this.treasure = treasure;
        this.name = treasure.name();
        setImage();
    }

    private void setImage() {
        String fileName = name.replaceAll(" ", "_") + ".png";
//            image = ImageIO.read(getClass().getResourceAsStream("/images/cards/treasure/" + fileName));

        try {
            image = ImageCreate.get("/images/cards/treasure/" + fileName);
            selectedImage = ImageCreate.get("/images/cards/treasure/selected/" + fileName);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void changeState(){
        state = TreasureState.CAPTURED;
    }

    public Treasure getTreasure() {
        return treasure;
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
        return name;
    }
}
