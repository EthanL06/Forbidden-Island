package game;

import game.enums.Difficulty;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;

public class WaterLevel {

    private int waterLevel; // Determines the amount of cards to draw (1-5)
    private int waterMarker; // The actual marker on the water meter (1-10)
    private ImageIcon image, markerImage;

    public WaterLevel(Difficulty difficulty) {
        System.err.println("WaterLevel initialized.");

        switch (difficulty) {
            case NOVICE:
                waterMarker = 1;
                break;
            case NORMAL:
                waterMarker = 2;
                break;
            case ELITE:
                waterMarker = 3;
                break;
            case LEGENDARY:
                waterMarker = 4;
                break;
        }

        setWaterLevel();
        setImage();
    }


    public void increaseWaterMarker() {
        waterMarker++;
        setWaterLevel();
    }

    // Max water level is 10
    public boolean hasReachedMax() {
        return waterMarker >= 10;
    }

    // Sets water level depending on waterMarker
    private void setWaterLevel() {
        if (waterMarker <= 2)
            waterLevel = 2;
        else if (waterMarker <= 5)
            waterLevel = 3;
        else if (waterMarker <= 7)
            waterLevel = 4;
        else
            waterLevel = 5;
    }

    private void setImage() {
        image = null;
        markerImage = null;

        try {
            image = ImageCreate.get("/images/water/meter2.png");
            markerImage = ImageCreate.get("/images/water/marker.png");

            image = ImageScaler.scale(image, (int) (image.getIconWidth() * 5.0/6), (int) (image.getIconHeight() * 5.0/6));
            markerImage = ImageScaler.scale(markerImage, (int) (markerImage.getIconWidth()/8), (int) (markerImage.getIconHeight()/8));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getWaterMarker() {
        return waterMarker;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public ImageIcon getImage() {
        return image;
    }
    public ImageIcon getMarkerImage() {
        return markerImage;
    }
}
