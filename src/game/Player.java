package game;

import game.board.Tile;
import game.enums.Role;
import game.enums.Treasure;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Locale;

public class Player {

    private final Role role;
    private final int number;
    private ArrayList<Object> hand;
    private ArrayList<Treasure> capturedTreasure;
    private Tile occupiedTile;
    private ImageIcon image, highlightImage, selectedImage;

    public Player(Role role, int number) {
        System.err.println("Player of role " + role.name() + " initialized.");
        this.role = role;
        this.hand = new ArrayList<>();
        this.capturedTreasure = new ArrayList<>();
        this.number = number;
        this.occupiedTile = null;
        setImage();
    }

    public void addCard(Object card) {
        hand.add(card);
    }

    public void addCapturedTreasure(Treasure treasure) {
        capturedTreasure.add(treasure);
    }

    public ArrayList<Treasure> getCapturedTreasure() {
        return capturedTreasure;
    }

    public Tile getOccupiedTile() {
        return occupiedTile;
    }

    public void setOccupiedTile(Tile tile) {
        this.occupiedTile = tile;
    }

    public Object getCard(int index) {
        return hand.remove(index);
    }

    public void removeCard(Object card) {
        hand.remove(card);
    }

    public ArrayList<Object> getHand() {
        return hand;
    }

    public int getHandSize() {
        return hand.size();
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        String name = role.name();
        name = name.substring(0, 1) + name.substring(1).toLowerCase(Locale.ROOT);

        return name;
    }

    private void setImage() {
        image = null;
        highlightImage = null;
        selectedImage = null;

        try {
            image = ImageCreate.get("/images/players/pawns/" + role.name().toLowerCase(Locale.ROOT) + ".png");
            image = ImageScaler.scale(image, (int)(image.getIconWidth()*0.29), (int)(image.getIconHeight()*(0.29)));

            highlightImage = ImageCreate.get("/images/players/pawns/highlight/" + role.name().toLowerCase(Locale.ROOT) + ".png");
            highlightImage = ImageScaler.scale(highlightImage, (int)(highlightImage.getIconWidth()*0.29), (int)(highlightImage.getIconHeight()*(0.29)));

            selectedImage = ImageCreate.get("/images/players/pawns/selected/" + role.name().toLowerCase(Locale.ROOT) + ".png");
            selectedImage = ImageScaler.scale(selectedImage, (int)(selectedImage.getIconWidth()*0.29), (int)(selectedImage.getIconHeight()*(0.29)));
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    public ImageIcon getImage() {
        return image;
    }

    public ImageIcon getHighlightImage() {
        return highlightImage;
    }

    public ImageIcon getSelectedImage() {
        return selectedImage;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        return "" + number;
    }
}
