package graphics.panels;

import game.ForbiddenIsland;
import game.Player;
import game.WaterLevel;
import game.board.Board;
import game.board.Tile;
import game.cards.FloodCard;
import game.cards.SpecialCard;
import game.cards.TreasureCard;
import game.enums.*;
import game.enums.Action;
import graphics.Window;
import graphics.panels.helper.Actions;
import graphics.panels.helper.CardButton;
import graphics.panels.helper.Listeners;
import graphics.panels.helper.Setup;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
    TODO:
        - Exception where player's current tile sinks, player has to move
        - Run win and lose conditions (done; not 100% tested)
        - Complete HelpPanel and InputPanel
        -
        - Highlight pawn of the current player's turn *
        - When click on role icon, information on what role does shows up
        - Help button (redirects to HelpPanel)
        -
        - Allow players to look through discard piles?
 */

public class GamePanel extends JPanel {

    private final ForbiddenIsland game;
    private final WaterLevel waterLevel;

    // Tiles images
    private HashMap<Tile, JButton> tileButtons;
    private HashMap<Tile, JLayeredPane> layeredPanes;

    // Treasure images
    private JLabel oceansChalice, crystalOfFire, statueOfTheWind, earthStone;

    // Water meter images
    private JLabel waterLevelIcon, waterMarkerIcon;

    // Action buttons
    private HashMap<String, JButton> actionButtons; // Keys: Move, Shore, Special, Confirm, Give, Capture, Help, Cancel, End Turn
    private JLabel actionsLeftIndicator;

    // Decks
    private JLabel floodDeck, treasureDeck, floodDiscard, treasureDiscard;

    // Discard
    private JLabel floodDiscardCard, treasureDiscardCard;

    // Player info
    private HashMap<Player, ArrayList<CardButton>> playerCards;
    private HashMap<JButton, Player> playerPawns;
    private JLabel turnIndicator;
    private Player currentPlayer;

    // Action log
    private JTextArea actionLog;

    private JButton endTurnButton;

    private Action selectedAction = Action.NONE;
    private Tile selectedTile = null;
    private Tile secondShoreTile = null; // Specifically for Engineer
    private Tile landingTile = null; // Specifically for Helicopters Lift
    private boolean isGettingLandingSite = false; // Specifically for Helicopters Lift

    private boolean isDiscardingCard = false;
    private Player playerDiscarding = null;

    private Player selectedPlayer = null;
    private Object selectedCard = null;

    public GamePanel(ForbiddenIsland game) {
        this.game = game;
        this.waterLevel = game.getWaterLevel();

        game.setGamePanelReference(this);
        Setup.setGamePanelReference(this); // Graphics setup
        Listeners.setGamePanelReference(this); // Action listeners for buttons
        Actions.setGamePanelReference(this); // Methods for all the actions the player can take

        // Graphics setup
        setLayout(null);
        Setup.setGraphics();

        updateActionLog();
        disableTiles(); // Make all tiles disabled on start
    }

    // Called when player clicks confirm button
    public void confirmAction() {
        System.out.println(game.getTreasureDeck().toString());
        if (!isDiscardingCard && selectedAction == Action.NONE) {
            updateActionLogError("Select an action first before confirming!");
            return;
        }

        switch (selectedAction) {
            case MOVE:
                if (!Actions.move())
                    return;
                break;
            case SHORE:
                if (!Actions.shore())
                    return;
                break;
            case GIVE:
                if (!Actions.give())
                    return;
                break;
            case CAPTURE:
                if (!Actions.capture())
                    return;
                break;
            case SPECIAL:
                if (!Actions.special())
                    return;
                break;
        }

        if (game.hasWon()) {
            gameWin();
            return;
        }

        if (isDiscardingCard) {
            System.out.println("Discarding " + selectedCard);

            if (selectedCard.getClass().getSimpleName().equals("SpecialCard")) {
                if (!Actions.special())
                    return;
            }

            playerDiscarding.removeCard(selectedCard);
            ArrayList<CardButton> playerCardButtons = playerCards.get(playerDiscarding);

            for (CardButton cardButton : playerCardButtons) {
                if (cardButton.getCard().equals(selectedCard)) {
                    game.getTreasureDeck().addToDiscard(selectedCard);
                    playerCardButtons.remove(cardButton);
                    remove(cardButton);
                    break;
                }
            }

            updateDiscard();

            isDiscardingCard = false;
            System.out.println(game.getActionsLeft());
            updateHands();

            System.out.println("Cards drawn: " + game.getCardsDrawn());
            if (playerDiscarding == currentPlayer) {
                if (game.getCardsDrawn() >= 2) {
                    nextTurn();
                } else {
                    game.nextPlayerTurn();
                    //discardExcessCard(playerDiscarding);
                    return;
                }
            }



            if (game.getHasLost())
                return;
        }

        reset();

        // if player used up all actions
        if (game.getActionsLeft() <= 0)
            nextTurn();

        if (game.getHasLost())
            return;

        if (isDiscardingCard)
            return;

        try {
            ImageIcon indicator = ImageCreate.get("/images/misc/actions/actions_" + game.getActionsLeft() + ".png");
            actionsLeftIndicator.setIcon(indicator);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void reset() {
        if (game.getHasLost()) {
            gameLost();
            return;
        }

        enableActionButtons();
        disableCards();
        disableTiles();
        removeIcons();
        removeAllShoreIcons();
        disablePawns();

        resetHelicoptersLift();

        selectedTile = null;
        landingTile = null;
        secondShoreTile = null;
        selectedPlayer = null;
        selectedCard = null;
        isDiscardingCard = false;
        playerDiscarding = null;
        setGettingLandingSite(false);
        game.setCardsDrawn(0);
        selectedAction = Action.NONE;
    }

    public void resetHelicoptersLift() {
        if (isGettingLandingSite || (selectedCard != null && selectedCard.getClass().getSimpleName().equals("SpecialCard"))) {
            for (Tile tile : tileButtons.keySet()) {
                if (tile.equals(selectedTile)) {
                    JButton tileButton = tileButtons.get(tile);
                    tileButton.setDisabledIcon(selectedTile.getImage());
                    break;
                }
            }
        }
    }

    // Called when player clicks cancel button
    public void cancelAction() {
        if (isDiscardingCard && selectedCard.getClass().getSimpleName().equals("SpecialCard")) {
            removeIcons(Action.SPECIAL);
            disableTiles();
            resetHelicoptersLift();
            removeAllShoreIcons();
            disableCards();
            enablePlayerCards(playerDiscarding);

            selectedCard = null;
            selectedTile = null;
            landingTile = null;
            setGettingLandingSite(false);
        } else if (selectedAction != Action.NONE) {
            reset();
        }
    }

    // Sets the pawn's position on a tile; takes into account if there are other pawns on the same tile
    public void setPawnPosition(JButton pawn, Player player, JButton tileButton) {
        HashSet<Player> sameTilePlayers = game.getBoard().getSameTilePlayers(player);

        if (sameTilePlayers == null || sameTilePlayers.size() == 0) {
            // Get the center position of the tile
            Dimension position = CenteredPosition.getPosition(tileButton.getBounds(), pawn.getIcon().getIconWidth(), pawn.getIcon().getIconHeight());

            int xVariation = (int) (Math.random() * 3 * (Math.random() > 0.5 ? 1 : -1));
            int yVariation = (int) (Math.random() * 5);

            pawn.setBounds(position.width + xVariation, position.height - yVariation, pawn.getIcon().getIconWidth(), pawn.getIcon().getIconHeight());
            return;
        }

        Component[] components = layeredPanes.get(player.getOccupiedTile()).getComponents();

        int x = 5;
        int y = 5;

        int times = 0;

        for (Component c : components) {
            for (Player sameTilePlayer : sameTilePlayers) {
                if (c.getName().equalsIgnoreCase(sameTilePlayer.getRole().name())) {
                    c.setBounds(x, y, c.getWidth(), c.getHeight());
                    times++;
                    if (times == 1)
                        x += 100 - c.getWidth();
                    else {
                        x = CenteredPosition.getPosition(tileButton.getBounds(), c.getWidth(), c.getHeight()).width;
                        y = 0;
                    }
                    break;
                }
            }
        }

        x = CenteredPosition.getPosition(tileButton.getBounds(), pawn.getWidth(), pawn.getHeight()).width;
        y = CenteredPosition.getPosition(tileButton.getBounds(), pawn.getWidth(), pawn.getHeight()).height + 10;

        pawn.setBounds(x, y, pawn.getWidth(), pawn.getHeight());
    }

    // Updates the inputted tile's images
    public void updateTile(Tile tile) {
        JButton tileBtn = tileButtons.get(tile);
        JLayeredPane layeredPane = layeredPanes.get(tile);

        if (tile.getState() == TileState.SUNK) {
            removeComponent(layeredPane, tileBtn.getName());
            return;
        }

        tileBtn.setIcon(tile.getImage());
        tileBtn.setPressedIcon(tile.getImagePressed());
        tileBtn.setSelectedIcon(tile.getImageSelected());
        tileBtn.setDisabledIcon(tile.getImage()); // <-- REMEMBER
    }

    public JButton removeAndGetPawn(Player player) {
        return (JButton) removeComponent(layeredPanes.get(player.getOccupiedTile()), player.getRole().name());
    }

    public void updatePawn(Player player, JButton pawn) {
        // Get and remove pawn from old tile and move it to new tile
//        JButton pawn = (JButton) removeComponent(layeredPanes.get(player.getOccupiedTile()), player.getRole().name());
//        game.move(player, selectedTile);

        // Move pawn to new tile graphically
        JLayeredPane pane = layeredPanes.get(player.getOccupiedTile());
        JButton tileBtn = null;

        for (Component component : pane.getComponents()) {
            if (component.getName().equals(player.getOccupiedTile().getName())) {
                tileBtn = (JButton) component;
                break;
            }
        }

        setPawnPosition(pawn, player, tileBtn);
        pane.add(pawn);
        pane.setLayer(pawn, pane.highestLayer()); // Use setLayer to set the z-index

        pane.revalidate();
        pane.repaint();
    }

    public void updateHands() {
        int x = 55;
        int y = 55 + 64;

        for (Player player : game.getPlayers()) {
            // System.out.println(player.getHand());
            ArrayList<CardButton> cardButtonList = playerCards.get(player);
            // System.out.println(cardButtonList);

            for (CardButton cardButton : cardButtonList) {
                remove(cardButton);
                revalidate();
                repaint();

                cardButton.setBounds(x, y, cardButton.getWidth(), cardButton.getHeight());

                add(cardButton);
                revalidate();
                repaint();

                x += 77;
            }

            x = 55;
            y += 180;
        }
    }

    public void updateDiscard() {
        //System.out.println(game.getTreasureDeck().size());

        if (game.getTreasureDeck().discardSize() == 0) {
            treasureDiscardCard.setIcon(null);
            treasureDiscardCard.setVisible(false);
        } else {
            Object treasureCard = game.getTreasureDeck().topOfDiscard();
            ImageIcon treasureIcon;

            if (treasureCard.getClass().getSimpleName().equals("TreasureCard"))
                treasureIcon = ((TreasureCard) treasureCard).getImage();
            else
                treasureIcon = ((SpecialCard) treasureCard).getImage();

            treasureDiscardCard.setIcon(ImageScaler.scale(treasureIcon, treasureDiscardCard.getWidth(), treasureDiscardCard.getHeight()));
            treasureDiscardCard.setVisible(true);
        }

        if (game.getFloodDeck().discardSize() == 0) {
            floodDiscardCard.setIcon(null);
            floodDiscardCard.setVisible(false);
        } else {
            FloodCard floodCard = game.getFloodDeck().topOfDiscard();
            floodDiscardCard.setIcon(ImageScaler.scale(floodCard.getImage(), floodDiscardCard.getWidth(), floodDiscardCard.getHeight()));
            floodDiscardCard.setVisible(true);
        }
    }

    // Called whenever a player captures a Treasure and moves the figurine next to the player
    public void updateFigurine(Player player) {
        int x = 447;
        int y = 55 + (180 * (player.getNumber() - 1));
        ImageIcon icon = null;

        int iterations = 0;
        for (Treasure treasure : player.getCapturedTreasure()) {
            switch (treasure) {
                case OCEANS_CHALICE:
                    try {
                        icon = ImageCreate.get("/images/figurines/Oceans_Chalice.png");
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    oceansChalice.setIcon(ImageScaler.scale(icon,
                            oceansChalice.getWidth() / 2, oceansChalice.getHeight() / 2));
                    oceansChalice.setBounds(x, y, oceansChalice.getIcon().getIconWidth(), oceansChalice.getIcon().getIconHeight());
                    break;
                case CRYSTAL_OF_FIRE:
                    try {
                        icon = ImageCreate.get("/images/figurines/Crystal_of_Fire.png");
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    crystalOfFire.setIcon(ImageScaler.scale(icon,
                            crystalOfFire.getWidth() / 2, crystalOfFire.getHeight() / 2));
                    crystalOfFire.setBounds(x, y, crystalOfFire.getIcon().getIconWidth(), crystalOfFire.getIcon().getIconHeight());
                    break;
                case STATUE_OF_THE_WIND:
                    try {
                        icon = ImageCreate.get("/images/figurines/Statue_of_the_Wind.png");
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    statueOfTheWind.setIcon(ImageScaler.scale(icon,
                            statueOfTheWind.getWidth() / 2, statueOfTheWind.getHeight() / 2));
                    statueOfTheWind.setBounds(x, y, statueOfTheWind.getIcon().getIconWidth(), statueOfTheWind.getIcon().getIconHeight());
                    break;
                case EARTH_STONE:
                    try {
                        icon = ImageCreate.get("/images/figurines/Earth_Stone.png");
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    earthStone.setIcon(ImageScaler.scale(icon,
                            earthStone.getWidth() / 2, earthStone.getHeight() / 2));
                    earthStone.setBounds(x, y, earthStone.getIcon().getIconWidth(), earthStone.getIcon().getIconHeight());
                    break;
            }

            iterations++;

            if (iterations == 3) {
                x = 447;
                y += 125;
            } else {
                x += 125 + 20;
            }
        }
    }

    public void updateWaterLevel() {
        int x = waterMarkerIcon.getX();
        waterMarkerIcon.setBounds(x, waterLevel.getHeight(), waterMarkerIcon.getWidth(), waterMarkerIcon.getHeight());

        System.out.println("Updated to " + waterLevel.getHeight());
        revalidate();
        repaint();
    }

    public void endTurnPress() {
        updateActionLog("has ended their turn");
        reset();

        nextTurn();
    }

    private void nextTurn() {
        System.out.println("Called next turn");
        System.out.println(isDiscardingCard);
        selectedTile = null;
        selectedAction = Action.NONE;
        updateWaterLevel();

        game.nextPlayerTurn();

        if (game.getHasLost()) {
            gameLost();
            return;
        }

        updateHands();
        updateDiscard();

        System.out.println(isDiscardingCard);
        if (isDiscardingCard) {
            return;
        }

        // false if player is discarding excess cards
//        if (!game.nextPlayerTurn()) {
//            return;
//        }


        currentPlayer = game.getCurrentPlayer();

        ImageIcon indicator = null;

        try {
            indicator = ImageCreate.get("/images/misc/actions/actions_" + game.getActionsLeft() + ".png");
        } catch (Exception e) {
            System.out.println(e);
        }

        actionsLeftIndicator.setIcon(indicator);
        updateActionLog();

        if (currentPlayer.getNumber() == 1) {
            turnIndicator.setBounds(turnIndicator.getX(), 55, turnIndicator.getWidth(), turnIndicator.getHeight());
        } else {
            turnIndicator.setBounds(turnIndicator.getX(), turnIndicator.getY() + 180, turnIndicator.getWidth(), turnIndicator.getHeight());
        }
    }

    private void gameEnd() {
        disableTiles();
        disableActionButtons();
        endTurnButton.setEnabled(false);
        disableCards();
    }

    public void gameLost() {
        System.out.println("Game lost!");
        gameEnd();

        updateActionLogCustom("Press the confirm button to play again");
    }

    public void gameWin() {
        System.out.println("Game win!");

        updateActionLogCustom("Congratulations! You have successfully escaped Forbidden Island\n\twith all four treasures!");
        updateActionLogCustom("Press the confirm button to play again");
        gameEnd();
    }

    public void discardExcessCard(Player player) {
        System.out.println("Called discard excess cards!");
        selectedAction = Action.NONE;
        playerDiscarding = player;
        updateActionLogCustom(player.getName() + "'s hand exceeded card limit. Select a card to discard");

        disableActionButtons();
        endTurnButton.setEnabled(false);

        disableTiles();
        enablePlayerCards(player);
    }

    public void updateActionLog() {
        actionLog.append("\n\n\t" + currentPlayer.getName() + "'s turn.");
        actionLog.getCaret().setDot(Integer.MAX_VALUE);
    }

    public void updateActionLogActionsLeft() {
        if (game.getActionsLeft() > 0)
            actionLog.append("\n\t" + currentPlayer.getName() + " has (" + game.getActionsLeft() + "/3) actions left.");
        actionLog.getCaret().setDot(Integer.MAX_VALUE);
    }

    public void updateActionLog(String msg) {
        actionLog.append("\n\t" + currentPlayer.getName() + " " + msg + ".");
        actionLog.getCaret().setDot(Integer.MAX_VALUE);
    }

    public void updateActionLogCustom(String msg) {
        actionLog.append("\n\t" + msg + ".");
        actionLog.getCaret().setDot(Integer.MAX_VALUE);
    }

    public void updateActionLogError(String msg) {
        actionLog.append("\n\t" + msg);
        actionLog.getCaret().setDot(Integer.MAX_VALUE);
    }

    public boolean isActionValid() {
        Board board = game.getBoard();

        switch (selectedAction) {
            case MOVE:
                if (board.getAvailableMovementTiles(currentPlayer).size() == 0) {
                    updateActionLogError("No available tiles to move to!");
                    return false;
                }
                break;

            case SHORE:
                if (board.getAvailableShoreTiles(currentPlayer, false).size() == 0) {
                    updateActionLogError("No available tiles to shore!");
                    return false;
                }
                break;

            case GIVE:
                if (currentPlayer.getHand().size() == 0) {
                    updateActionLogError("No available cards to trade!");
                }

                if (board.getPlayersToTradeWith(currentPlayer).size() == 0) {
                    updateActionLogError("No available players to trade with!");
                    return false;
                }
                break;

            case CAPTURE:
                Player currentPlayer = game.getCurrentPlayer();
                Tile currentTile = currentPlayer.getOccupiedTile();
                Treasure treasure;

                // if current tile isn't a treasure tile
                if (currentTile.getTreasureState() == null) {
                    updateActionLogError("No treasure on current tile!");
                    return false;
                }

                treasure = currentTile.getTreasure();

                // if tile's treasure is already captured
                if (currentTile.getTreasureState() == TreasureState.CAPTURED || game.getCapturedTreasure().contains(treasure)) {
                    updateActionLogError("Treasure has already been captured!");
                    return false;
                }

                if (currentPlayer.getHandSize() < 4) {
                    updateActionLogError("Not enough cards to capture!");
                    return false;
                }
                break;

            case SPECIAL:
                // TODO: To be implemented.
                for (Player player : game.getPlayers()) {
                    for (Object card : player.getHand()) {
                        if (card.getClass().getSimpleName().equals("SpecialCard"))
                            return true;
                    }
                }
                return false;
        }

        return true;
    }

    // ------------------------ ENABLE/DISABLE BUTTONS ------------------------

    private void enableActionButtons() {
        for (JButton button : actionButtons.values()) {
            button.setSelected(false);
            button.setEnabled(true);
        }

        endTurnButton.setEnabled(true);
    }

    // Disables all other buttons when an action button is selected excluding confirm, cancel, help, and end turn
    public void disableActionButtons(JButton selectedButton) {
        for (JButton button : actionButtons.values()) {
            String name = button.getName();

            if (button.equals(selectedButton))
                continue;

            if (name.equals("Confirm") || name.equals("Cancel") || name.equals("Help") || name.equals("End Turn"))
                continue;

            button.setEnabled(false);
        }
    }

    public void disableActionButtons() {
        for (JButton button : actionButtons.values()) {
            String name = button.getName();

            if (name.equals("Confirm") || name.equals("Cancel") || name.equals("Help") || name.equals("End Turn"))
                continue;

            button.setEnabled(false);
        }
    }

    public HashSet<Tile> enableLandingTiles() {
        HashSet<Tile> tiles = new HashSet<>();

        for (Tile tile : tileButtons.keySet()) {
            if (!tile.equals(selectedTile)) {
                tileButtons.get(tile).setEnabled(true);
                tileButtons.get(tile).setSelectedIcon(tile.getHelicopterSelected());
                tiles.add(tile);
            } else {
                JButton tileButton = tileButtons.get(tile);
                tileButton.setDisabledIcon(tile.getHelicopterSelected());
                tileButton.setEnabled(false);
                tileButton.setSelected(false);
            }
        }

        return tiles;
//        showHelicopterLiftIcons(tiles);
    }

    private void enableTiles(HashSet<Tile> tiles) {
        for (Tile tile : tileButtons.keySet()) {
            if (tiles.contains(tile)) {
                tileButtons.get(tile).setEnabled(true);
                tileButtons.get(tile).setSelectedIcon(tile.getImageSelected());
            }
        }
    }

    // Disables all tiles except the inputted tiles
    private void disableTiles(HashSet<Tile> remainingTiles) {
        for (Tile tile : tileButtons.keySet()) {
            if (!remainingTiles.contains(tile)) {
                tileButtons.get(tile).setEnabled(false);
            }
        }
    }

    private void disableTiles(Tile remainingTile) {
        for (Tile tile : tileButtons.keySet()) {
            if (!tile.equals(remainingTile)) {
                tileButtons.get(tile).setEnabled(false);
            }
        }
    }

    private void disableTiles(Tile remainingTile, Tile secondRemainingTile) {
        for (Tile tile : tileButtons.keySet()) {
            if (!tile.equals(remainingTile) && !tile.equals(secondRemainingTile)) {
                tileButtons.get(tile).setEnabled(false);
            }
        }
    }

    public void disableTiles() {
        for (Tile tile : tileButtons.keySet()) {
            JButton button = tileButtons.get(tile);
            button.setSelected(false);
            button.setEnabled(false);
        }
    }

    public void enablePlayerCards(Player player) {
        for (CardButton card : playerCards.get(player)) {
            //System.out.println("Enabled " + card.getCard());
            card.setSelected(false);
            card.setEnabled(true);
        }
    }

    public void enableTradeCards(Player player) {
        for (CardButton card : playerCards.get(player)) {
            String cardClass = card.getCard().getClass().getSimpleName();

            if (!cardClass.equals("SpecialCard"))
                card.setEnabled(true);
        }
    }


    public void enableSpecialCards() {
        for (ArrayList<CardButton> cardButtonList : playerCards.values()) {
            for (CardButton cardButton : cardButtonList) {
                if (cardButton.getCard().getClass().getSimpleName().equals("SpecialCard")) {
                    cardButton.setEnabled(true);
                }
            }
        }
    }

    // Disables all other cards except inputted card button
    public void disableCards(CardButton cardButton) {
        for (ArrayList<CardButton> cardButtons : playerCards.values()) {
            for (JButton card : cardButtons) {
                if (!card.equals(cardButton)) {
                    card.setSelected(false);
                    card.setEnabled(false);
                }
            }
        }
    }

    private void disableCards() {
        for (ArrayList<CardButton> cardButtons : playerCards.values()) {
            for (JButton card : cardButtons) {
                card.setSelected(false);
                card.setEnabled(false);
            }
        }
    }

    // ------------------------ ADD/REMOVE ACTION ICONS ------------------------
    public void showIcons() {
        displayIcons(selectedAction);
    }

    public void showIcons(Action action) {
        displayIcons(action);
    }

    private void displayIcons(Action action) {
        switch (action) {
            case MOVE:
                if (currentPlayer.getRole() == Role.NAVIGATOR) { // If navigator hasn't selected a pawn
                    if (selectedPlayer == null)
                        enablePawns();
                    else
                        showMovementTiles(selectedPlayer);
                } else {
                    showMovementTiles();
                }
                break;
            case SHORE:
                showShoreTiles();
                break;
            case GIVE:
                showAvailableTrades();
                break;
            case SPECIAL:
                if (selectedCard == null) {
                    return;
                }

                switch (((SpecialCard) selectedCard).getType()) {
                    case SANDBAGS:
                        showShoreTiles();
                        break;
                    case HELICOPTER_LIFT:
                        if (!isGettingLandingSite) {
                            showHelicopterLiftIcons(getGame().getBoard().getAllPlayerTiles());
                        } else {
                            showHelicopterLiftIcons(getGame().getBoard().getAllTiles());
                        }
                        break;
                }

                break;
        }
    }

    public void removeIcons(Action action) {
        removeActionIcons(action);
    }

    public void removeIcons() {
        removeActionIcons(selectedAction);
    }

    private void removeActionIcons(Action action) {
        switch (action) {
            case MOVE:
                removeMovementTileIcons();
                break;
            case SHORE:
                removeShoreTileIcons();
                break;
            case GIVE:
                disablePawns();
                break;
            case SPECIAL:
                if (selectedCard == null)
                    return;

                SpecialCard specialCard = (SpecialCard) selectedCard;

                switch (specialCard.getType()) {
                    case SANDBAGS:
                        removeShoreTileIcons();
                        break;
                    case HELICOPTER_LIFT:
                        removeHelicopterLiftIcons();
                        break;
                }

                break;
        }
    }




    private void showMovementTiles() {
        showMovementTiles(currentPlayer);
    }

    private void showMovementTiles(Player player) {
        HashSet<Tile> movementTiles;

        if (player.getRole() == Role.NAVIGATOR) {
            movementTiles = game.getBoard().getNavigatorMovementTiles(player);
        } else {
            movementTiles = game.getBoard().getAvailableMovementTiles(player);
        }

        ImageIcon moveIcon = null;

        try {
            moveIcon = ImageCreate.get("/images/action_icons/move.png");
        } catch (Exception e) {
            System.out.println(e);
        }

        moveIcon = ImageScaler.scale(moveIcon, Tile.IMAGE_SIZE / 2, Tile.IMAGE_SIZE / 2);

        for (Tile tile : movementTiles) {
            JButton tileBtn = tileButtons.get(tile);
            Rectangle bounds = tileBtn.getBounds();
            bounds.width -= 10;
            bounds.height -= 10;
            Dimension position = CenteredPosition.getPosition(bounds, moveIcon.getIconWidth(), moveIcon.getIconHeight());

            JLayeredPane layeredPane = layeredPanes.get(tile);
//            JLayeredPane layeredPane = (JLayeredPane) tileButtons.get(tile).getParent();
            JLabel move = new JLabel(moveIcon);
            move.setName("Move");
            move.setBounds(position.width, position.height, moveIcon.getIconWidth(), moveIcon.getIconHeight());

            layeredPane.setLayer(move, layeredPane.highestLayer());
            layeredPane.add(move, 0);

        }

        enableTiles(movementTiles);
    }

    private void removeMovementTileIcons() {
        HashSet<Tile> movementTiles = game.getBoard().getAllTiles();

//        if (game.getCurrentPlayer().getRole() == Role.NAVIGATOR) {
//            movementTiles = game.getBoard().getAvailableMovementTiles(selectedPlayer);
//        } else {
//            movementTiles = game.getBoard().getAvailableMovementTiles(currentPlayer);
//        }

        for (Tile tile : movementTiles) {
            JLayeredPane layeredPane = layeredPanes.get(tile);
            removeComponent(layeredPane, "Move");
        }

        if (selectedTile != null)
            disableTiles(selectedTile);
        else
            disableTiles();
    }

    public void showHelicopterLiftIcons(HashSet<Tile> tiles) {
//        HashSet<Tile> tiles = game.getBoard().getAllPlayerTiles();

        ImageIcon helicopterIcon = null;
        try {
            helicopterIcon = ImageCreate.get("/images/action_icons/helicopter_lift.png");
        } catch (Exception e) {
            System.out.println(e);
        }

        helicopterIcon = ImageScaler.scale(helicopterIcon, (int) (helicopterIcon.getIconWidth() * 0.60), (int) (helicopterIcon.getIconHeight() * 0.60));

        if (isGettingLandingSite) {
            tiles.remove(selectedTile);
        }

        for (Tile tile : tiles) {
            if (tile.getState() == TileState.SUNK)
                continue;

            JButton tileBtn = tileButtons.get(tile);
            Rectangle bounds = tileBtn.getBounds();
            bounds.width -= 10;
            bounds.height -= 10;
            Dimension position = CenteredPosition.getPosition(bounds, helicopterIcon.getIconWidth(), helicopterIcon.getIconHeight());

            JLayeredPane layeredPane = layeredPanes.get(tile);
//            JLayeredPane layeredPane = (JLayeredPane) tileButtons.get(tile).getParent();
            JLabel move = new JLabel(helicopterIcon);
            move.setName("Helicopter");
            move.setBounds(position.width, position.height, helicopterIcon.getIconWidth(), helicopterIcon.getIconHeight());

            layeredPane.setLayer(move, layeredPane.highestLayer());
            layeredPane.add(move, 0);
        }

        enableTiles(tiles);
    }

    private void removeHelicopterLiftIcons() {
        HashSet<Tile> tiles = game.getBoard().getAllTiles();

        for (Tile tile : tiles) {
            JLayeredPane layeredPane = layeredPanes.get(tile);
            removeComponent(layeredPane, "Helicopter");
        }

        if (isGettingLandingSite && landingTile != null)
            disableTiles(landingTile);
        else if (selectedTile != null)
            disableTiles(selectedTile);
        else
            disableTiles();
    }

    private void showShoreTiles() {
        HashSet<Tile> shoreTiles;

        if (selectedAction == Action.SPECIAL || (isDiscardingCard && selectedCard.getClass().getSimpleName().equals("SpecialCard"))) {
            shoreTiles = game.getBoard().getAvailableShoreTiles(playerDiscarding, true);
        } else {
            shoreTiles = game.getBoard().getAvailableShoreTiles(currentPlayer, false);
        }

        System.out.println(selectedTile + " " + secondShoreTile);
        if (selectedTile != null)
            shoreTiles.remove(selectedTile);

        if (secondShoreTile != null)
            shoreTiles.remove(secondShoreTile);

        ImageIcon shoreIcon = null;
        try {
            shoreIcon = ImageCreate.get("/images/action_icons/shore.png");
        } catch (Exception e) {
            System.out.println(e);
        }

        shoreIcon = ImageScaler.scale(shoreIcon, (int) (Tile.IMAGE_SIZE * 0.45), (int) (Tile.IMAGE_SIZE * 0.45));

        for (Tile tile : shoreTiles) {
            JButton tileBtn = tileButtons.get(tile);
            Rectangle bounds = tileBtn.getBounds();
            bounds.width -= 10;
            bounds.height -= 10;
            Dimension position = CenteredPosition.getPosition(bounds, shoreIcon.getIconWidth(), shoreIcon.getIconHeight());

            JLayeredPane layeredPane = layeredPanes.get(tile);
            JLabel shore = new JLabel(shoreIcon);
            shore.setName("Shore");
            shore.setBounds(position.width, position.height, shoreIcon.getIconWidth(), shoreIcon.getIconHeight());

            layeredPane.setLayer(shore, layeredPane.highestLayer() + 1);
            layeredPane.add(shore, 0);
        }

        enableTiles(shoreTiles);
    }

    private void removeShoreTileIcons() {
        HashSet<Tile> shoreTiles;
        System.out.println(selectedTile + " " + secondShoreTile);
        if (game.getCurrentPlayer().getRole() == Role.ENGINEER && selectedAction != Action.SPECIAL && !isDiscardingCard) {
            if (selectedTile != null && secondShoreTile == null) {
                JLayeredPane layeredPane = layeredPanes.get(selectedTile);
                removeComponent(layeredPane, "Shore");
                return;
            } else if (selectedTile == null && secondShoreTile != null) {
                JLayeredPane layeredPane = layeredPanes.get(secondShoreTile);
                removeComponent(layeredPane, "Shore");
            }
        }

        if (selectedAction == Action.SPECIAL || (isDiscardingCard && selectedCard.getClass().getSimpleName().equals("SpecialCard"))) {
            shoreTiles = game.getBoard().getAvailableShoreTiles(playerDiscarding, true);
        } else {
            shoreTiles = game.getBoard().getAvailableShoreTiles(currentPlayer, false);
        }

        for (Tile tile : shoreTiles) {
            JLayeredPane layeredPane = layeredPanes.get(tile);
            removeComponent(layeredPane, "Shore");
        }

        if (selectedTile != null && secondShoreTile != null)
            disableTiles(selectedTile, secondShoreTile);
        else if (selectedTile != null)
            disableTiles(selectedTile);
        else
            disableTiles();
    }

    private void removeAllShoreIcons() {
        HashSet<Tile> shoreTiles = game.getBoard().getAvailableShoreTiles(currentPlayer, true);

        for (Tile tile : shoreTiles) {
            JLayeredPane layeredPane = layeredPanes.get(tile);
            removeComponent(layeredPane, "Shore");
        }

        disableTiles();
    }

    // Shows the highlight around pawns that can be traded with
    private void showAvailableTrades() {
        HashSet<Player> playersToTrade = game.getBoard().getPlayersToTradeWith(currentPlayer);

        for (Player player : playersToTrade) {
            Tile tile = player.getOccupiedTile();

            for (Component component : layeredPanes.get(tile).getComponents()) {
                if (component.getName() != null && component.getName().equals(player.getRole().name())) {
                    component.setEnabled(true);
                }
            }
        }
    }

    // Removes the highlight around all pawns
    public void disablePawns() {
        for (JButton pawn : playerPawns.keySet()) {
            pawn.setEnabled(false);
            pawn.setSelected(false);
        }
    }

    // Adds highlight around all pawns (specifically for Navigator)
    public void enablePawns() {
        for (JButton pawn : playerPawns.keySet()) {
            pawn.setEnabled(true);
        }
    }

    // Removes the highlight around pawns that can be traded with except the selected player
    public void showSelectedPlayer(JButton pawn) {
        for (JButton otherPlayerPawn : playerPawns.keySet()) {
            if (!otherPlayerPawn.equals(pawn)) {
                otherPlayerPawn.setEnabled(false);
            }
        }
    }


    // https://stackoverflow.com/questions/11438512/fully-remove-jlabel-from-jpanel-not-setvisiblefalse/11438626
    public Component removeComponent(JLayeredPane pane, String componentName) {
        Component removedComponent = null;

        for (Component component : pane.getComponents()) {
            if (component.getName() != null && component.getName().equals(componentName)) {
                removedComponent = component;
                pane.remove(component);
                pane.revalidate();
                pane.repaint();
                //System.err.println("Removed " + component.getName());
            }
        }

        return removedComponent;
    }


    public ForbiddenIsland getGame() {
        return game;
    }

    public HashMap<Tile, JLayeredPane> getLayeredPanes() {
        return layeredPanes;
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    public void setSelectedTile(Tile selectedTile) {
        this.selectedTile = selectedTile;
    }

    public HashMap<String, JButton> getActionButtons() {
        return actionButtons;
    }

    public void setSelectedAction(Action selectedAction) {
        this.selectedAction = selectedAction;
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public HashMap<JButton, Player> getPlayerPawns() {
        return playerPawns;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public void setSelectedPlayer(Player selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    public Object getSelectedCard() {
        return selectedCard;
    }

    public void setLandingTile(Tile landingTile) {
        this.landingTile = landingTile;
    }

    public Tile getLandingTile() {
        return landingTile;
    }

    public void setSecondShoreTile(Tile secondShoreTile) {
        this.secondShoreTile = secondShoreTile;
    }

    public Tile getSecondShoreTile() {
        return secondShoreTile;
    }

    public Player getPlayerDiscarding() {
        return playerDiscarding;
    }

    public void setPlayerDiscarding(Player playerDiscarding) {
        this.playerDiscarding = playerDiscarding;
    }

    public void setSelectedCard(Object selectedCard) {
        this.selectedCard = selectedCard;
    }

    public HashMap<Player, ArrayList<CardButton>> getPlayerCards() {
        return playerCards;
    }

    public void setPlayerCards(HashMap<Player, ArrayList<CardButton>> playerCards) {
        this.playerCards = playerCards;
    }

    public boolean isGettingLandingSite() {
        return isGettingLandingSite;
    }

    public void setGettingLandingSite(boolean gettingLandingSite) {
        isGettingLandingSite = gettingLandingSite;
    }

    public boolean isDiscardingCard() {
        return isDiscardingCard;
    }

    public void setDiscardingCard(boolean discardingCard) {
        isDiscardingCard = discardingCard;
    }

    public void setEndTurnButton(JButton button) {
        endTurnButton = button;
    }

    public void setTilesAndPawns(HashMap<Tile, JButton> tileButtons, HashMap<Tile, JLayeredPane> panes, HashMap<JButton, Player> pawns) {
        this.tileButtons = tileButtons;
        layeredPanes = panes;
        playerPawns = pawns;
    }

    public void setTreasureLabels(JLabel ocean, JLabel crystal, JLabel wind, JLabel earth) {
        oceansChalice = ocean;
        crystalOfFire = crystal;
        statueOfTheWind = wind;
        earthStone = earth;
    }

    public void setWaterMeter(JLabel level, JLabel marker) {
        waterLevelIcon = level;
        waterMarkerIcon = marker;
    }

    public void setAction(HashMap<String, JButton> buttons, JLabel indicator) {
        actionButtons = buttons;
        actionsLeftIndicator = indicator;
    }

    public void setDecks(JLabel floodDeck, JLabel floodDiscard, JLabel treasureDeck, JLabel treasureDiscard, JLabel floodDiscardCard, JLabel treasureDiscardCard) {
        this.floodDeck = floodDeck;
        this.floodDiscard = floodDiscard;
        this.treasureDeck = treasureDeck;
        this.treasureDiscard = treasureDiscard;
        this.floodDiscardCard = floodDiscardCard;
        this.treasureDiscardCard = treasureDiscardCard;
    }

    public void setPlayers(HashMap<Player, ArrayList<CardButton>> playerCards, JLabel turnIndicator, Player currentPlayer) {
        this.playerCards = playerCards;
        this.turnIndicator = turnIndicator;
        this.currentPlayer = currentPlayer;
    }

    public void setActionLog(JTextArea actionLog) {
        this.actionLog = actionLog;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ImageIcon icon = null;

        try {
            icon = ImageCreate.get("/images/game_bg.jpg");
        } catch (Exception e) {
            System.out.println(e);
        }

        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        for (int y = 0; y < Window.height; y += height) {
            for (int x = 0; x < Window.width; x += width) {
                g.drawImage(icon.getImage(), x, y, null);
            }
        }
    }
}

