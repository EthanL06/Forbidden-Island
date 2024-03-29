package game;

import game.board.Board;
import game.board.Tile;
import game.cards.FloodCard;
import game.cards.SpecialCard;
import game.cards.TreasureCard;
import game.decks.FloodDeck;
import game.decks.TreasureDeck;
import game.enums.*;
import game.enums.Action;
import graphics.panels.GamePanel;
import graphics.panels.helper.CardButton;
import graphics.panels.helper.Listeners;
import graphics.util.ButtonFactory;
import graphics.util.ImageScaler;
import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.util.*;

/**
 * Central class of the game logic of Forbidden Island
 */

public class ForbiddenIsland implements Runnable {

    /*
    TODO:
        - Add navigator exception in movement (done)
        - Add engineer exception in shoring (done)
        - Implement helicopter lift code (done)
        - Implement sandbags code (done)
        - Implement win conditions (done)
        - Ability to look through Treasure and Flood discard piles
     */

    private final long seed;
    private final int numOfPlayers;
    private final Difficulty difficulty;

    private Board board;
    private WaterLevel waterLevel;

    private FloodDeck floodDeck;
    private TreasureDeck treasureDeck;
    private ArrayList<Treasure> capturedTreasure;

    private Player[] players;
    private Player currentPlayer;
    private int currentPlayerIndex;
    private boolean hasWon;
    private boolean hasLost;
    private boolean pilotUsedAbility;

    private GamePanel gamePanel;
    private int actionsLeft;
    private int cardsDrawn;
    private int floodCardsDrawn;

    public ForbiddenIsland(long seed, int numOfPlayers, Difficulty difficulty) {
        System.err.println("Forbidden Island initialized.");
        this.seed = seed;
        this.numOfPlayers = numOfPlayers;
        this.difficulty = difficulty;
        this.actionsLeft = 3;
        this.cardsDrawn = 0;
        this.floodCardsDrawn = 0;
        this.hasWon = false;
        this.hasLost = false;
        this.pilotUsedAbility = false;
    }

    @Override
    public void run() {
        setup(seed, numOfPlayers, difficulty);
    }

    public void setup(long seed, int numOfPlayers, Difficulty difficulty) {
        System.err.println("\n=== START OF GAME SET UP ===");
        Randomizer.setRandom(seed);
        players = new Player[numOfPlayers];
        board = new Board(this);

        floodDeck = new FloodDeck();
        treasureDeck = new TreasureDeck();
        capturedTreasure = new ArrayList<>();

        waterLevel = new WaterLevel(difficulty);
        hasWon = false;

        // Creating player objects, assigning roles, and giving Treasure cards
        ArrayList<Role> roles = new ArrayList<>(Arrays.asList(Role.values()));
        Collections.shuffle(roles, Randomizer.getRandom());
        for (int i = 0; i < numOfPlayers; i++) {
            players[i] = new Player(roles.remove(0), i + 1);

            for (int drawnCards = 0; drawnCards < 2; drawnCards++) {
                while (treasureDeck.isNextWatersRise()) { // If a Waters Rise! card is drawn during setup, it is put back into stack and shuffled
                    treasureDeck.shuffle();
                }

                players[i].addCard(treasureDeck.drawCard());
            }
        }

        // Flooding six Island Tiles
        for (int i = 0; i < 6; i++) {
            FloodCard floodCard = floodDeck.drawCard();
            board.floodTile(floodCard);
        }

        board.setStartingPositions(players);
        currentPlayer = players[0]; // Assigns the player's turn to the first player
        currentPlayerIndex = 0;
        System.err.println("=== END OF GAME SET UP ===\n");
    }

    public void setGamePanelReference(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void move(Player player, Tile tile) {
        if (gamePanel.getPlayerSunkTile() == null)
            actionsLeft--;

        board.movePlayer(player, tile);

        if (gamePanel.getSelectedPlayer() != null && !gamePanel.getSelectedPlayer().equals(currentPlayer)) {
            gamePanel.updateActionLogCustom(currentPlayer.getName() + " moved " + gamePanel.getSelectedPlayer().getName() + " to " + tile);
        } else {
            gamePanel.updateActionLog("moved to " + tile);
        }

        gamePanel.updateActionLogActionsLeft();
    }

    public void shore(Tile tile) {
        actionsLeft--;
        board.shoreTile(tile);
        gamePanel.updateActionLog("shored " + tile);
        // gamePanel.updateActionLogActionsLeft();
    }

    public void give(Player recipient, Object card) {
        actionsLeft--;
        board.give(currentPlayer, recipient, card);
        gamePanel.updateActionLog("traded a " + card.toString() + " card with " + recipient.getName());
        gamePanel.updateActionLogActionsLeft();
    }

    public boolean capture() {

        Treasure treasure = currentPlayer.getOccupiedTile().getTreasure();
        ArrayList<TreasureCard> treasureCards = new ArrayList<>();

        for (Object card: currentPlayer.getHand()) {
            if (card.getClass().getSimpleName().equals("TreasureCard")) {
                TreasureCard treasureCard = (TreasureCard) card;
                if (treasureCard.getTreasure().equals(treasure)) {
                    treasureCards.add(treasureCard);
                }
            }
        }

        if (treasureCards.size() >= 4) {
            currentPlayer.getOccupiedTile().captureTreasure();
            capturedTreasure.add(treasure);
            currentPlayer.addCapturedTreasure(treasure);

            // Removes four of the corresponding Treasure cards
            for (int i = 0; i < 4; i++) {
                currentPlayer.removeCard(treasureCards.get(i));
                treasureDeck.addToDiscard(treasureCards.get(i));
            }

            // Add treasure cards to discard pile

            // Removes the corresponding CardButtons from screen
            ArrayList<CardButton> cardButtons = gamePanel.getPlayerCards().get(currentPlayer);
            for (int i = 0; i < cardButtons.size(); i++) {
                if (cardButtons.get(i).getCard().getClass().getSimpleName().equals("TreasureCard")) {
                    TreasureCard treasureCard = (TreasureCard) cardButtons.get(i).getCard();

                    if (treasureCard.getName().equals(treasureCards.get(i).getName())) {
                        CardButton removed = cardButtons.remove(i);
                        gamePanel.remove(removed);
                        i--;
                    }
                }
            }

            gamePanel.updateActionLog("captured " + treasure.name());
            actionsLeft--;
            return true;
        }

        gamePanel.updateActionLogError("Not enough corresponding Treasure cards to capture!");
        return false;
    }

    public void special(SpecialCard specialCard) {
        switch (specialCard.getType()) {
            case SANDBAGS:
                board.shoreTile(gamePanel.getSelectedTile());
                gamePanel.updateTile(gamePanel.getSelectedTile());
                gamePanel.updateActionLog("used Sandbags on " + gamePanel.getSelectedTile());
                break;

            case HELICOPTER_LIFT:
                board.moveHelicopterLift(gamePanel.getSelectedTile(), gamePanel.getLandingTile(), gamePanel);
                break;
        }

        for (Player player: players) {
            if (player.getHand().contains(specialCard)) {
                for (ArrayList<CardButton> cardButtonList: gamePanel.getPlayerCards().values()) {
                    for (CardButton cardButton: cardButtonList) {
                        if (cardButton.getCard().equals(specialCard)) {
                            gamePanel.remove(cardButton);
                            cardButtonList.remove(cardButton);

                            player.removeCard(specialCard);

                            treasureDeck.addToDiscard(specialCard);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void nextPlayerTurn() {
        System.out.println("Called next player turn");
        System.out.println("cardsDrawn: " + cardsDrawn);
        System.out.println("isDiscarding: " + gamePanel.isDiscardingCard());
        System.out.println("Player discarding: " + gamePanel.getPlayerDiscarding());
        System.out.println("floodCardsDrawn: " + floodCardsDrawn);
        gamePanel.updateActionLogError("");

        // if player needs to discard excess cards
        if (cardsDrawn < 2) {
            if (!distributeTreasureCards()) {
                return;
            }
        }

        gamePanel.setDiscardingCard(false);
        gamePanel.setPlayerDiscarding(null);

        if (floodCardsDrawn < waterLevel.getWaterLevel()) {
            if (!drawFloodCards()) {
                return;
            }
        }

        if (hasLost()) {
            hasLost = true;
            return;
        }

        actionsLeft = 3;

        if (currentPlayerIndex == players.length - 1)
            currentPlayerIndex = 0;
        else
            currentPlayerIndex++;

        currentPlayer = players[currentPlayerIndex];

        cardsDrawn = 0;
        floodCardsDrawn = 0;
        gamePanel.setPlayerDiscarding(null);
    }


    private boolean distributeTreasureCards() {
        // if there's only one treasure card, it will cause ArrayOutOfBounds

        if (treasureDeck.isEmpty() || treasureDeck.size() <= 1) {
            gamePanel.updateActionLogCustom("Treasure draw pile is empty. Using shuffled treasure discard pile as new treasure draw pile.");
            treasureDeck.switchDeckToDiscard();
        }

        int drawnWatersRise = 0;
        // Draws two treasure cards
        for (int i = cardsDrawn; i < 2; i++) {
            cardsDrawn++;
            if (treasureDeck.isNextWatersRise()) {
                gamePanel.updateActionLog("drew a Waters Rise! card");

                Object card = treasureDeck.drawCard(); // Draws card and adds it to discard pile - not given to player

                treasureDeck.addToDiscard(card);
                waterLevel.increaseWaterMarker();
                gamePanel.updateWaterLevel();
                gamePanel.updateActionLogCustom("Water level is currently at " + waterLevel.getWaterLevel());


                drawnWatersRise++;

                if (drawnWatersRise == 1) {
                    // Shuffle Flood discard pile
                    // Place discard pile on top of Flood draw pile
                    gamePanel.updateActionLog("drew a Waters Rise! card.\n\tFlood discard pile is shuffled and placed on top of draw flood draw pile");
                    floodDeck.shuffleDiscard();
                    floodDeck.placeDiscardOnDeck();
                }

            } else {
                Object temp = treasureDeck.drawCard();
                currentPlayer.addCard(temp);
                gamePanel.updateActionLog("drew a " + temp + " card");

                CardButton cardButton;

                if (temp.getClass().getSimpleName().equals("TreasureCard")) {
                    TreasureCard card = (TreasureCard) temp;
                    ImageIcon cardImage = ImageScaler.scale(card.getImage(), 72, 102);
                    ImageIcon selectedImage = ImageScaler.scale(card.getSelectedImage(), 72, 102);

                    cardButton = ButtonFactory.createCardButton(cardImage, cardImage, selectedImage);
                    cardButton.setCard(card);
                    cardButton.setBounds(0, 0, cardButton.getIcon().getIconWidth(), cardButton.getIcon().getIconHeight());
                    cardButton.setEnabled(false);
                    Listeners.setCardListener(cardButton);
                } else {
                    SpecialCard card = (SpecialCard) temp;

                    ImageIcon cardImage = ImageScaler.scale(card.getImage(), 72, 102);
                    ImageIcon selectedImage = ImageScaler.scale(card.getSelectedImage(), 72, 102);

                    cardButton = ButtonFactory.createCardButton(cardImage, cardImage, selectedImage);
                    cardButton.setCard(card);
                    cardButton.setBounds(0, 0, cardButton.getIcon().getIconWidth(), cardButton.getIcon().getIconHeight());
                    Listeners.setCardListener(cardButton);
                }

                gamePanel.getPlayerCards().get(currentPlayer).add(cardButton);
                gamePanel.updateHands();

                // Run check if player's hand exceeds hand limit
                if (currentPlayer.getHandSize() > 5){
                    gamePanel.setDiscardingCard(true);
                    gamePanel.discardExcessCard(currentPlayer);
                    return false;
                }
            }
        }

        return true;
    }


    private boolean drawFloodCards() {
        // ArrayOutOfBounds if flood deck's size is less than the amount of flood cards to draw
        if (floodDeck.isEmpty() || floodDeck.size() < waterLevel.getWaterLevel()) {
            gamePanel.updateActionLogError("Flood draw pile is empty. Using shuffled flood discard pile as new draw pile.");
            floodDeck.shuffleDiscard();
            floodDeck.switchDeckToDiscard();
        }

        HashMap<Tile, Player> playerTiles = new HashMap<>();

        for (Player player: players)
            playerTiles.put(player.getOccupiedTile(), player);

        // Drawing of flood cards
        for (int i = floodCardsDrawn; i < waterLevel.getWaterLevel(); i++) {
            floodCardsDrawn++;
            FloodCard floodCard = floodDeck.drawCard();
            Tile tile = board.floodTile(floodCard, gamePanel);
            gamePanel.updateActionLogCustom(tile.getName() + " has been flooded");

            if (tile.getState() == TileState.SUNK) {
                floodDeck.removeFromDiscard(floodCard);
                gamePanel.updateActionLogError(tile.getName() + " has sunk!");

                if (playerTiles.containsKey(tile)) {
                    Player player = playerTiles.get(tile);

                    gamePanel.setPlayerSunkTile(player);
                    if (!gamePanel.playerSunkTile(player)) {
                        hasLost = true;
                        break;
                    }

                    return false;
                }
            }


        }

        return true;
    }

    // Runs all lose conditions
    private boolean hasLost() {
        if (waterLevel.hasReachedMax()) {
            gamePanel.updateActionLogError("Game over! Water level has reached the max level!");
            return true;
        }

        if (board.getTile("Fools' Landing").getState() == TileState.SUNK) {
            gamePanel.updateActionLogError("Game over! Fools' Landing tile has sunk!");
            return true;
        }

        if (board.haveTreasureTilesSunk(gamePanel)) {
            return true;
        }

        /*
        Checks if each player's current island tile has sunk, and if it has,
        checks if the player has any available island tiles to move to.
         */
        for (Player player : players) {
            if (player.getOccupiedTile().getState() == TileState.SUNK && board.getAvailableMovementTiles(player).size() == 0) {
                gamePanel.updateActionLogError("Game over! " + player + "'s tile has sunk and is unable to move!");
                return true;
            }
        }

        return false;
    }

    public boolean hasWon() {
        Tile foolsLanding = new Tile("Fools' Landing");

        for (Player player: players) {
            if (!player.getOccupiedTile().equals(foolsLanding))
                return false;
        }

        return capturedTreasure.size() == 4;
    }

    public boolean getHasLost() {
        return hasLost;
    }

    private boolean isAbleToWin() {
        // If all treasure types have been captured and all players are on Fools' Landing
        Tile foolsLanding = new Tile("Fools' Landing");
        // Short circuit if any player isn't on Fools' Landing
        for (Player player: players) {
            if (!player.getOccupiedTile().equals(foolsLanding))
                return false;
        }
        return capturedTreasure.size() == 4;
    }

    // Returns ArrayList of player's whose occupied tile sank
    private ArrayList<Player> getPlayersOccupiedTileSunk() {
        ArrayList<Player> output = new ArrayList<>();

        for (Player player : players) {
            if (player.getOccupiedTile().getState() == TileState.SUNK)
                output.add(player);
        }

        return output;
    }

    // Temporary; for testing purposes
    private Action getActionInput(Scanner input) {
        try {
            return Action.valueOf(input.nextLine().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Action.INVALID;
        }
    }

    public Board getBoard() {
        return board;
    }

    public WaterLevel getWaterLevel() {
        return waterLevel;
    }

    public FloodDeck getFloodDeck() {
        return floodDeck;
    }

    public TreasureDeck getTreasureDeck() {
        return treasureDeck;
    }

    public ArrayList<Treasure> getCapturedTreasure() {
        return capturedTreasure;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getActionsLeft() {
        return actionsLeft;
    }

    public void setActionsLeft(int actionsLeft) {
        this.actionsLeft = actionsLeft;
    }

    public boolean decrementActionsLeft() {
        actionsLeft--;

        return actionsLeft > 0;
    }

    public void setCardsDrawn(int cardsDrawn) {
        this.cardsDrawn = cardsDrawn;
    }

    public int getCardsDrawn() {
        return cardsDrawn;
    }

    public int getFloodCardsDrawn() {
        return floodCardsDrawn;
    }

    public void setFloodCardsDrawn(int floodCardsDrawn) {
        this.floodCardsDrawn = floodCardsDrawn;
    }

    public boolean isPilotUsedAbility() {
        return pilotUsedAbility;
    }

    public void setPilotUsedAbility(boolean pilotUsedAbility) {
        this.pilotUsedAbility = pilotUsedAbility;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
