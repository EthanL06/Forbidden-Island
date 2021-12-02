package game;

import game.board.Board;
import game.board.Tile;
import game.cards.FloodCard;
import game.cards.SpecialCard;
import game.cards.TreasureCard;
import game.decks.FloodDeck;
import game.decks.TreasureDeck;
import game.enums.*;
import graphics.panels.GamePanel;
import graphics.panels.helper.CardButton;

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

    private final int seed;
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

    private GamePanel gamePanel;
    private int actionsLeft;

    public ForbiddenIsland(int seed, int numOfPlayers, Difficulty difficulty) {
        System.err.println("Forbidden Island initialized.");
        this.seed = seed;
        this.numOfPlayers = numOfPlayers;
        this.difficulty = difficulty;
        this.actionsLeft = 3;
    }

    @Override
    public void run() {
        setup(seed, numOfPlayers, difficulty);
    }

    public void setup(int seed, int numOfPlayers, Difficulty difficulty) {
        System.err.println("\n=== START OF GAME SET UP ===");
        Randomizer.setRandom(seed);
        players = new Player[numOfPlayers];
        board = new Board();

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

  /*  private void startGameLoop() {
        System.err.println("=== START OF GAME LOOP ===\n");

        Scanner input = new Scanner(System.in);
//        Scanner input = null;
//        try {
//            input = new Scanner(new File("src/game/txt/test_data.txt"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        board.printBoard();

        gameLoop:
        while (true) {
            // Game loop to be implemented
            int actionsLeft = 3;
            System.err.println("PLAYER " + currentPlayer.getNumber() + "'S TURN (" + currentPlayer.getRole() + "):\n");

            actionLoop:
            while (actionsLeft > 0) {
                //BoardPanel.getInput() Will be implemented when finished
                board.printBoardStatus(currentPlayer);
                System.err.println("ENTER IN ACTION (" + actionsLeft + " actions left): ");

                Action action = getActionInput(input);
                switch (action) {
                    case MOVE -> {
                        if (currentPlayer.getRole() == Role.NAVIGATOR) {
                            System.err.println(Arrays.toString(players) + "\nENTER PLAYER TO MOVE: ");
                            int playerNum = input.nextInt();
                            input.nextLine();

                            if (playerNum == currentPlayer.getNumber()) { // if navigator chooses themselves, move like normally
                                if (!move(currentPlayer, false, input))
                                    continue;
                            } else {
                                boolean found = false;

                                // Navigator moves selected player
                                for (Player player: players) {
                                    if (player.getNumber() == playerNum) {
                                        found = true;
                                        if (!move(player, true, input))
                                            continue actionLoop;
                                    }
                                }

                                if (!found) {
                                    System.err.println("INVALID PLAYER");
                                    continue;
                                }
                                // TODO: navigator moves another player
                            }

                        } else {
                            if (!move(currentPlayer, false, input)) // if movement input is invalid
                                continue;
                        }

                    }
                    case SHORE -> {
                        // Engineer can shore 2 tiles for 1 action
                        if (!shore(input, false))
                            continue;

                        if (currentPlayer.getRole() == Role.ENGINEER && board.getAvailableShoreTiles(currentPlayer).size() > 0) {
                            System.err.println("SHORE AGAIN? (Y/N): ");
                            String answer = input.nextLine();

                            // 2nd shore
                            if (answer.toLowerCase(Locale.ROOT).equals("y")) {
                                boolean flag = false; // to account for invalid inputs

                                while (!flag) {
                                    if (shore(input, false))
                                        flag = true;
                                }
                            }
                        }

                    }
                    case GIVE -> {
                        if (!give(input))
                            continue;
                    }
                    case SPECIAL -> {
                        // Can use special cards from own hand or other players
                        if (!special(input))
                            continue;

                        if (hasWon) {
                            System.err.println("YOU HAVE WON THE GAME");
                            break gameLoop;
                        }
                    }
                    case CAPTURE -> {
                        if (!capture())
                            continue;
                    }
                    case END -> {
                        System.err.println("\nPLAYER " + currentPlayer.getNumber() + " ENDED TURN");
                        break actionLoop;
                    }
                    case INVALID -> {
                        System.err.println("NOT A COMMAND");
                        continue;
                    }

                    // Add case statements to print out status messages
                    // such as flood status, island tile names, and locations of each player and decks
                }

                actionsLeft--;
                System.err.println("Player " + currentPlayer.getNumber() + " has used action: " + action + " (" + actionsLeft + " actions left)");
            }

            if (!distributeTreasureCards(input)) { // Water level has reached max if drawn Waters Rise
                System.err.println("YOU HAVE LOST THE GAME");
                break;
            }

            if (hasWon) {
                System.err.println("YOU HAVE WON THE GAME");
                break;
            }

            drawFloodCards();

            if (hasLost()) {
                System.err.println("YOU HAVE LOST THE GAME");
                break;
            }

            ArrayList<Player> playersToMove = getPlayersOccupiedTileSunk(); // Gets the players required to move bc of a sunk tile
            for (Player player : playersToMove) {
                System.err.println("PLAYER " + player.getNumber() + "'S TILE SUNK.\n");
                boolean flag = false; // to account for invalid inputs

                while (!flag) {
                    if (move(player,false, input))
                        flag = true;
                }
            }

            nextPlayerTurn();
        }

        System.err.println("\n=== END OF GAME LOOP ===");
    }*/

/*    public boolean move(Player player, boolean movedByNavigator, Scanner input) {
        HashSet<Tile> availableMoves = new HashSet<>();

        if (movedByNavigator) {
            availableMoves.addAll(board.getNavigatorMovementTiles(player));
        } else {
            availableMoves.addAll(board.getAvailableMovementTiles(player));
        }

        if (availableMoves.size() == 0) {
            System.err.println("NO AVAILABLE TILES");
            return false;
        }

        System.err.println("AVAILABLE MOVES: " + availableMoves);
        String tileName = input.nextLine();

        if (!availableMoves.contains(new Tile(tileName))) {
            System.err.println("NOT AVAILABLE TILE");
            return false;
        }

        if (!board.movePlayer(player, tileName)) {
            System.err.println("NAMES ARE CASE SENSITIVE");
            return false;
        }

        return true;
    }*/

    public void move(Player player, Tile tile) {
        actionsLeft--;
        board.movePlayer(player, tile);

        if (gamePanel.getSelectedPlayer() != null && !gamePanel.getSelectedPlayer().equals(currentPlayer)) {
            gamePanel.updateActionLogCustom(currentPlayer.getName() + " moved " + gamePanel.getSelectedPlayer().getName() + " to " + tile);
        } else {
            gamePanel.updateActionLog("moved to " + tile);
        }

        gamePanel.updateActionLogActionsLeft();
    }

    // Special if user uses sandbags
    public void shore(Tile tile) {
//        HashSet<Tile> availableShores;
//        if (special) { // Sandbags can shore any flooded tile
//            availableShores = board.getAllFloodedTiles();
//        } else {
//            availableShores = board.getAvailableShoreTiles(currentPlayer);
//        }


//        if (availableShores.size() == 0) {
//            System.err.println("NO AVAILABLE SHORES");
//            return false;
//        }

//        System.err.println("AVAILABLE SHORES: " + availableShores);
//        String tileName = input.nextLine();
//
//        if (!availableShores.contains(new Tile(tileName))) {
//            System.err.println("NOT AVAILABLE SHORE");
//            return false;
//        }

        actionsLeft--;
        board.shoreTile(tile);
        gamePanel.updateActionLog("shored " + tile);
        gamePanel.updateActionLogActionsLeft();
    }

    public void give(Player recipient, Object card) {
        actionsLeft--;
        board.give(currentPlayer, recipient, card);
        gamePanel.updateActionLog("traded a " + card.toString() + " card with " + recipient.getName());
        gamePanel.updateActionLogActionsLeft();
    }

//    private boolean give(Scanner input) {
//        HashSet<Player> sameTilePlayers = board.getPlayersToTradeWith(currentPlayer);
//        ArrayList<Object> playerHand = currentPlayer.getHand();
//
//        if (sameTilePlayers.size() == 0) {
//            System.err.println("NO ONE TO TRADE WITH");
//            return false;
//        } else if (playerHand.size() == 0) {
//            System.err.println("CAN'T TRADE - NO CARDS IN HAND");
//            return false;
//        }
//
//        System.err.println("AVAILABLE PLAYERS TO TRADE WITH: " + sameTilePlayers);
//        System.err.println("ENTER IN PLAYER NUMBER TO TRADE WITH: ");
//        int recipientNumber = input.nextInt(); // May change type later
//        input.nextLine();
//
//        // Gets the player to trade with
//        Player recipient = null;
//        for (Player player : sameTilePlayers) {
//            if (player.getNumber() == recipientNumber) {
//                recipient = player;
//            }
//        }
//
//        if (recipient == null) {
//            System.err.println("NOT AN AVAILABLE PLAYER TO TRADE WITH");
//            return false;
//        }
//
//        System.err.println("PLAYER HAND: " + playerHand);
//        System.err.println("ENTER IN CARD INDEX TO GIVE (0-" + (playerHand.size() - 1) + "):");
//        int cardToTrade = input.nextInt(); // May change type later
//        input.nextLine();
//
//        if (cardToTrade < 0 || cardToTrade >= playerHand.size()) {
//            System.err.println("NOT AN AVAILABLE INDEX");
//            return false;
//        }
//
//        Object card = currentPlayer.getCard(cardToTrade);
//
//        // If recipient's hand size exceeds card limit
//        if (board.give(recipient, card) > 5)
//            discardExcessCards(recipient, input);
//
//        System.err.println("TRADE SUCCESS");
//        return true;

//    }

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

//    public boolean special(Scanner input) {
//        // Can use special cards from own hand or other players
//        System.err.println("USE SPECIAL CARDS FROM OWN HAND? (Y/N):");
//        String decision = input.nextLine();
//
//        ArrayList<Object> hand = new ArrayList<>();
//        Player chosenPlayer = currentPlayer;
//        if (decision.toLowerCase(Locale.ROOT).equals("y")) {
//            hand = currentPlayer.getHand();
//        } else {
//            ArrayList<Player> otherPlayers = new ArrayList<>(Arrays.asList(players));
//            otherPlayers.remove(currentPlayer);
//
//            System.err.println(otherPlayers);
//            System.err.println("ENTER IN TEAMMATE'S NUMBER TO USE SPECIAL CARDS: ");
//            int playerNumber = input.nextInt();
//            input.nextLine();
//
//            boolean flag = false;
//            for (Player player: otherPlayers) {
//                if (player.getNumber() == playerNumber) {
//                    hand = player.getHand();
//                    chosenPlayer = player;
//                    flag = true;
//                    break;
//                }
//            }
//
//            if (!flag) {
//                System.err.println("INVALID PLAYER NUMBER");
//                return false;
//            }
//        }
//
//        ArrayList<SpecialCard> specialCards = new ArrayList<>();
//
//        for (Object card : hand) {
//            if (card.getClass().getSimpleName().equals("SpecialCard")) {
//                specialCards.add((SpecialCard) card);
//            }
//        }
//
//        if (specialCards.size() == 0) {
//            System.err.println("PLAYER DOES NOT HAVE ANY SPECIAL ACTION CARDS");
//            return false;
//        }
//
//        System.err.println(specialCards);
//        System.err.println("ENTER IN SPECIAL ACTION CARD INDEX: ");
//
//        int index = input.nextInt();
//        input.nextLine();
//
//        if (index < 0 || index >= specialCards.size()) {
//            System.err.println("NOT VALID INDEX");
//            return false;
//        }
//
//        SpecialCard card = specialCards.get(index);
//
//        if (card.getType() == Special.SANDBAGS) {
//            System.err.println("PLAYED SAND BAGS");
///*            if (!shore(input, true))
//                return false;*/
//
//        } else {
//            if (!playHelicopterLift(input))
//                return false;
//        }
//
//        chosenPlayer.removeCard(card);
//        treasureDeck.addToDiscard(card);
//        return true;
//    }

//    private boolean distributeTreasureCards(Scanner input) {
//        // if there's only one treasure card, it will cause ArrayOutOfBounds
//        if (treasureDeck.isEmpty() || treasureDeck.size() == 1) {
//            System.err.println("TREASURE DISCARD PILE SWITCHED TO DRAW PILE");
//            treasureDeck.switchDeckToDiscard();
//        }
//
//        boolean drawnWatersRise = false;
//        // Draws two treasure cards
//        for (int i = 0; i < 2; i++) {
//            if (treasureDeck.isNextWatersRise()) {
//                System.err.println("PLAYER " + currentPlayer.getNumber() + " DREW WATERS RISE");
//
//                treasureDeck.drawCard(); // Draws card and adds it to discard pile - not given to player
//                waterLevel.increaseWaterMarker();
//
//                drawnWatersRise = true;
//                // Run lose condition here
//                if (waterLevel.hasReachedMax())
//                    return false;
//
//            } else {
//                Object card = treasureDeck.drawCard();
//                System.err.println("PLAYER " + currentPlayer.getNumber() + " DREW " + card);
//                currentPlayer.addCard(card);
//
//                // Run check if player's hand exceeds hand limit
//                if (currentPlayer.getHandSize() > 5)
//                    discardExcessCards(currentPlayer, input);
//            }
//        }
//
//        if (drawnWatersRise) {
//            // Shuffle Flood discard pile
//            // Place discard pile on top of Flood draw pile
//            floodDeck.shuffleDiscard();
//            floodDeck.placeDiscardOnDeck();
//        }
//
//        System.err.println();
//
//        return true;
//    }

    private void drawFloodCards() {
        // ArrayOutOfBounds if flood deck's size is less than the amount of flood cards to draw
        if (floodDeck.isEmpty() || floodDeck.size() < waterLevel.getWaterLevel()) {
            System.err.println("FLOOD DISCARD PILE SWITCHED TO DRAW PILE");
            floodDeck.shuffleDiscard();
            floodDeck.switchDeckToDiscard();
        }

        // Drawing of flood cards
        for (int i = 0; i < waterLevel.getWaterLevel(); i++) {
            FloodCard floodCard = floodDeck.drawCard();
            board.floodTile(floodCard);
        }

        System.err.println();
    }

//    private void discardExcessCards(Player player, Scanner input) {
//        int amountToDiscard = player.getHandSize() - 5;
//        System.err.println("PLAYER'S HAND EXCEEDED HAND SIZE");
//
//        while (amountToDiscard > 0) {
//            System.err.println(player.getHand());
//            System.err.println("ENTER IN CARD INDEX TO REMOVE:");
//
//            int cardIndex = input.nextInt();
//            input.nextLine();
//
//            Object obj = player.getCard(cardIndex);
//
//            if (obj.getClass().getSimpleName().equals("SpecialCard")) {
//                SpecialCard card = (SpecialCard) obj;
//
//                if (card.getType() == Special.HELICOPTER_LIFT) {
//                    if (!playHelicopterLift(input))
//                        continue;
//                } else {
///*                    if (!shore(input, true))
//                        continue;*/
//                }
//            }
//
//            if (hasWon) {
//                break;
//            }
//
//            treasureDeck.addToDiscard(obj);
//            amountToDiscard--;
//        }
//    }

//    private boolean playHelicopterLift(Scanner input) {
//        // check if can lift off Fools' Landing and ask if player wants to
//        // if not, play card like normally
//        // add error handling
//        System.err.println("PLAYED HELICOPTER LIFT");
//
//        if (isAbleToWin()) {
//            System.err.println("ESCAPE FOOLS' LANDING (Y/N)?");
//            String decision = input.nextLine();
//            if (decision.equalsIgnoreCase("y")) {
//                hasWon = true;      // WIN CONDITION
//                return true;
//            }
//        }
//
//        HashMap<Tile, ArrayList<Player>> playerTiles = new HashMap<>();
//
//        // Getting each occupied tile and the players on them
//        for (Player player: players) {
//            Tile tile = player.getOccupiedTile();
//            if (!playerTiles.containsKey(tile)) { // If player's tile hasnt been added to playerTiles yet
//                ArrayList<Player> playerList = new ArrayList<>();
//                playerList.add(player);
//                playerTiles.put(tile, playerList);
//            } else {
//                playerTiles.get(player.getOccupiedTile()).add(player);
//            }
//        }
//
//        System.err.println("ENTER TILE NAME TO MOVE PLAYERS FROM:");
//
//        for (Tile key: playerTiles.keySet()) {
//            System.err.println(key + ": " + playerTiles.get(key));
//        }
//
//        String tileName = input.nextLine();
//        if (!playerTiles.containsKey(new Tile(tileName))) {
//            System.err.println("INVALID TILE");
//            return false;
//        }
//
//        ArrayList<Player> playersOfOriginalTile = playerTiles.get(new Tile(tileName));
//
//        HashSet<Tile> availableTiles = board.getAllNonSunkTiles();
//        System.err.println("ENTER TILE NAME TO MOVE PLAYERS TO:");
//        System.err.println(availableTiles);
//        String newTileName = input.nextLine();
//
//        if (!availableTiles.contains(new Tile(newTileName))) {
//            System.err.println("INVALID TILE");
//            return false;
//        }
//
//        // implement board.moveHelicopterLift
//        if (!board.moveHelicopterLift(newTileName, playersOfOriginalTile)) {
//            System.err.println("NAMES ARE CASE SENSITIVE");
//            return false;
//        }
//
//        System.err.println("MOVED " + playersOfOriginalTile + " FROM " + tileName + " TO " + newTileName);
//        return true;
//
//        // ---run win condition after this method is called---
//
//    }

    // Runs all lose conditions
    private boolean hasLost() {
        if (waterLevel.hasReachedMax()) {
            System.err.println("WATER LEVEL REACHED MAX LEVEL");
            return true;
        }

        if (board.getTile("Fools' Landing").getState() == TileState.SUNK) {
            System.err.println("FOOLS LANDING HAS SUNK");
            return true;
        }

        if (board.haveTreasureTilesSunk()) {
            System.err.println("TREASURE TILE HAS SUNK");
            return true;
        }

        /*
        Checks if each player's current island tile has sunk, and if it has,
        checks if the player has any available island tiles to move to.
         */
        for (Player player : players) {
            if (player.getOccupiedTile().getState() == TileState.SUNK && board.getAvailableMovementTiles(player).size() == 0) {
                System.err.println(player.getNumber());
                return true;
            }
        }

        return false;
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

    public void nextPlayerTurn() {
        actionsLeft = 3;

        if (currentPlayerIndex == players.length - 1)
            currentPlayerIndex = 0;
        else
            currentPlayerIndex++;

        currentPlayer = players[currentPlayerIndex];
    }

    public boolean decrementActionsLeft() {
        actionsLeft--;

        return actionsLeft > 0;
    }

    /*    //to access board in the BoardPanel class
    public Board getBoard() {
        return board;
    }

    public FloodDeck getFloodDeck() {
        return floodDeck;
    }

    public WaterLevel getWaterLevel() {
        return waterLevel;
    }


    public Player[] getPlayerArray() {
        return players;
    }

    public TreasureDeck getTreasureDeck() {
        return treasureDeck;
    }*/
}
