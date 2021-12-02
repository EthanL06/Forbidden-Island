package game.decks;

import game.Randomizer;
import game.board.Board;
import game.cards.FloodCard;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;

public class FloodDeck implements Deck<FloodCard> {

    private Stack<FloodCard> floodDeck;
    private Stack<FloodCard> discard;
    private BufferedImage top;

    public FloodDeck() {
        System.err.println("FloodDeck initialized.");

        floodDeck = new Stack<>();
        discard = new Stack<>();

        fillDeck();
        shuffle();
    }

    private void fillDeck() {
        InputStream is = Board.class.getResourceAsStream("/game/txt/island_names.txt");
        Scanner floodCardNames = new Scanner(is);

        while (floodCardNames.hasNextLine()) {
            String name = floodCardNames.nextLine();
            System.out.println(name);
            floodDeck.push(new FloodCard(name));
        }
    }

    public FloodCard drawCard() {
        FloodCard card = floodDeck.pop();
        discard.push(card);

        return card;
    }

    public Stack<FloodCard> shuffle() {
        Collections.shuffle(floodDeck, Randomizer.getRandom());
        return floodDeck;
    }

    public void shuffleDiscard() {
        Collections.shuffle(discard, Randomizer.getRandom());
    }

    public FloodCard topOfDiscard(){
        return discard.peek();
    }

    public int discardSize() {
        return discard.size();
    }

    public void removeFromDiscard(FloodCard card) {
        discard.remove(card);
    }

    public boolean isEmpty() {
        return floodDeck.isEmpty();
    }

    public int size() {
        return floodDeck.size();
    }

    public void placeDiscardOnDeck() {
        Stack<FloodCard> newDeck = new Stack<>();

        newDeck.addAll(floodDeck);
        newDeck.addAll(discard);
        floodDeck = newDeck;
        discard = new Stack<>();
    }


    @Override
    public void switchDeckToDiscard() {
        while (!discard.isEmpty()) {
            floodDeck.push(discard.pop());
        }

        shuffle();
    }

    @Override
    public BufferedImage getImage() {
        return null;
    }

    @Override
    public String toString() {
        return floodDeck.toString();
    }
}
