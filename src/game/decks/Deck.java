package game.decks;

import java.awt.image.BufferedImage;
import java.util.Stack;

public interface Deck<T> {
    Object drawCard();
    Stack<T> shuffle();
    void switchDeckToDiscard();
    BufferedImage getImage();
}
