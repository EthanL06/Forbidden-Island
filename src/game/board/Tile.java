package game.board;

import game.enums.TileState;
import game.enums.Treasure;
import game.enums.TreasureState;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class Tile {

    public static final int IMAGE_SIZE = 110;
    private final String name;
    private Treasure treasure;
    private TileState state;
    private TreasureState treasureState;
    private ImageIcon normal, normalPressed, normalSelected, normalHelicopterSelected, flooded, floodedPressed, floodedSelected, floodedHelicopterSelected;

    // Constructor for tile that can't be moved to
    public Tile() {
        this.name = "Empty";
        this.treasure = null;
        state = TileState.EMPTY;
        treasureState = null;
        normal = flooded = normalPressed = floodedPressed = normalSelected = floodedSelected = null;
    }

    public Tile(String name) {
        this.name = name;
        this.state = TileState.NORMAL;

        setTreasure();
    }


    public TileState flood() {
        if (state == TileState.FLOODED) {
            state = TileState.SUNK;
        } else {
            state = TileState.FLOODED;
        }

        return state;
    }

    public boolean shore() {
        if (state == TileState.FLOODED) {
            state = TileState.NORMAL;
            return true;
        }

        return false;
    }

    // Sets Treasure of Tile depending on name
    private void setTreasure() {
        switch (name) {
            case "Temple Of The Moon": case "Temple Of The Sun":
                treasure = Treasure.EARTH_STONE;
                break;
            case "Whispering Garden": case "Howling Garden":
                treasure = Treasure.STATUE_OF_THE_WIND;
                break;
            case "Cave Of Embers": case "Cave Of Shadows":
                treasure = Treasure.CRYSTAL_OF_FIRE;
                break;
            case "Coral Palace": case "Tidal Palace":
                treasure = Treasure.OCEANS_CHALICE;
                break;
            default:
                treasure = null;
        }

        if (treasure != null) {
            treasureState = TreasureState.UNCAPTURED;
        } else {
            treasureState = null;
        }
    }

    public void setImages() {
        String fileName = name.replaceAll(" ", "_");

        String normalName = fileName + ".png";
        String floodName = fileName + "_Flooded.png";

        String normalPressedName = fileName + "_Pressed.png";
        String floodPressedName = fileName + "_Flooded_Pressed.png";

        String normalSelectedName = fileName + "_Selected.png";
        String floodSelectedName = fileName + "_Flooded_Selected.png";

            /*normal = ImageIO.read(getClass().getResourceAsStream("/images/tiles/" + normalName));
            flooded = ImageIO.read(getClass().getResourceAsStream("/images/tiles/" + floodName));*/

        //Image image = Toolkit.getDefaultToolkit().createImage(Tile.class.getResource("/images/tiles/normal/" + normalName));
        try {
            Image image = Toolkit.getDefaultToolkit().createImage(Tile.class.getResource("/images/tiles/normal/" + normalName));
            normal = new ImageIcon(image);
            normalPressed = ImageCreate.get("/images/tiles/normal_pressed/" + normalPressedName);
            normalSelected = ImageCreate.get("/images/tiles/normal_selected/" + normalSelectedName);
            normalHelicopterSelected = ImageCreate.get("/images/tiles/normal_helicopter_selected/" + normalName);

            flooded = ImageCreate.get("/images/tiles/flooded/" + floodName);
            floodedPressed = ImageCreate.get("/images/tiles/flooded/" + floodName);
            floodedSelected = ImageCreate.get("/images/tiles/flooded_selected/" + floodSelectedName);
            floodedHelicopterSelected = ImageCreate.get("/images/tiles/flooded_helicopter_selected/" + normalName);

            normal = ImageScaler.scale(normal, IMAGE_SIZE, IMAGE_SIZE);
            normalPressed = ImageScaler.scale(normalPressed, IMAGE_SIZE, IMAGE_SIZE);
            normalSelected = ImageScaler.scale(normalSelected, IMAGE_SIZE, IMAGE_SIZE);
            normalHelicopterSelected = ImageScaler.scale(normalHelicopterSelected, IMAGE_SIZE, IMAGE_SIZE);

            flooded = ImageScaler.scale(flooded, IMAGE_SIZE, IMAGE_SIZE);
            floodedPressed = ImageScaler.scale(floodedPressed, IMAGE_SIZE, IMAGE_SIZE);
            floodedSelected = ImageScaler.scale(floodedSelected, IMAGE_SIZE, IMAGE_SIZE);
            floodedHelicopterSelected = ImageScaler.scale(floodedHelicopterSelected, IMAGE_SIZE, IMAGE_SIZE);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // True if the treasure is successfully captured, else false
    public boolean captureTreasure() {
        if (treasureState == TreasureState.UNCAPTURED) {
            treasureState = TreasureState.CAPTURED;
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Tile o = (Tile) obj;
        return name.equalsIgnoreCase(o.getName());
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public TileState getState() {
        return state;
    }

    public TreasureState getTreasureState() {
        return treasureState;
    }

    public String toString() {
        return name;
    }

    public ImageIcon getImage() {
        if (state == TileState.NORMAL)
            return normal;
        else
            return flooded;
    }

    public ImageIcon getImagePressed() {
        if (state == TileState.NORMAL)
            return normalPressed;
        else
            return floodedPressed;
    }

    public ImageIcon getImageSelected() {
        if (state == TileState.NORMAL)
            return normalSelected;
        else
            return floodedSelected;
    }

    public ImageIcon getHelicopterSelected() {
        if (state == TileState.NORMAL)
            return normalHelicopterSelected;
        else
            return floodedHelicopterSelected;
    }
}
