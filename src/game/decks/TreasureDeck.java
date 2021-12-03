package game.decks;

import game.Randomizer;
import game.cards.SpecialCard;
import game.cards.TreasureCard;
import game.enums.Special;
import game.enums.Treasure;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Stack;

public class TreasureDeck implements Deck<Object> {

    private Stack treasureDeck;
    private Stack discard;

    public TreasureDeck() {
        System.err.println("TreasureDeck initialized.");

        treasureDeck = new Stack<>();
        discard = new Stack<>();

        fillDeck();
        shuffle();
    }

    private void fillDeck() {
        // Creates five of each treasure card
        for (int i = 0; i < 5; i++) {
            treasureDeck.push(new TreasureCard(Treasure.EARTH_STONE));
            treasureDeck.push(new TreasureCard(Treasure.STATUE_OF_THE_WIND));
            treasureDeck.push(new TreasureCard(Treasure.CRYSTAL_OF_FIRE));
            treasureDeck.push(new TreasureCard(Treasure.OCEANS_CHALICE));
        }

        // Creates three Waters Rise! and Helicopter Lift cards
        for (int i = 0; i < 3; i++) {
            treasureDeck.push(new SpecialCard(Special.WATERS_RISE));
            treasureDeck.push(new SpecialCard(Special.HELICOPTER_LIFT));
        }

        // Creates 2 Sandbag cards
        for (int i = 0; i < 2; i++) {
            treasureDeck.push(new SpecialCard(Special.SANDBAGS));
        }
    }

    @Override
    public Object drawCard() {
        return treasureDeck.pop();
    }

    public Object peekNextCard() {
        return treasureDeck.peek();
    }

    public boolean isEmpty() {
        return treasureDeck.isEmpty();
    }

    public int size() {
        return treasureDeck.size();
    }


    public boolean isNextWatersRise() {
        Object card = treasureDeck.peek();

        if (card.getClass().getSimpleName().equals("TreasureCard")) {
            return false;
        }

        SpecialCard specialAction = (SpecialCard) card;
        return specialAction.getType() == Special.WATERS_RISE;
    }

    public void addToDiscard(Object card) {
        discard.push(card);
    }

    public Object topOfDiscard(){
        return discard.peek();
    }

    public int discardSize() {
        return discard.size();
    }

    public String toString() {
        return treasureDeck.toString();
    }

    @Override
    public Stack shuffle() {
        Collections.shuffle(treasureDeck, Randomizer.getRandom());
        return treasureDeck;
    }

    @Override
    public void switchDeckToDiscard() {
        while (!discard.isEmpty()) {
            treasureDeck.push(discard.pop());
        }

        shuffle();
    }

    @Override
    public BufferedImage getImage() {
        return null;
    }
}



