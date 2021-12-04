package graphics.panels.helper;

import game.ForbiddenIsland;
import game.Player;
import game.board.Tile;
import game.cards.SpecialCard;
import game.enums.Action;
import game.enums.Role;
import game.enums.Treasure;
import game.enums.TreasureState;
import graphics.panels.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Actions {

    private static GamePanel gp;
    private static ForbiddenIsland game;

    public static void setGamePanelReference(GamePanel gamePanel) {
        gp = gamePanel;
        game = gp.getGame();
    }

    public static boolean move() {
        Player selectedPlayer;

        if (game.getCurrentPlayer().getRole() == Role.NAVIGATOR && gp.getPlayerSunkTile() == null) {
            selectedPlayer = gp.getSelectedPlayer();
            if (selectedPlayer == null) {
                gp.updateActionLogError("Select a pawn to move!");
                return false;
            }
        } else if (gp.getPlayerSunkTile() != null) {
            selectedPlayer = gp.getPlayerSunkTile();
        } else {
            selectedPlayer = game.getCurrentPlayer();
        }

        Tile selectedTile = gp.getSelectedTile();

        if (selectedTile == null) {
            gp.updateActionLogError("Select a tile before moving!");
            return false;
        }

        JButton pawn = gp.removeAndGetPawn(selectedPlayer);
        game.move(selectedPlayer, selectedTile);
        gp.updatePawn(selectedPlayer, pawn);


        return true;
    }

    public static boolean shore() {
        Tile selectedTile = gp.getSelectedTile();
        Tile secondSelectedTile = gp.getSecondShoreTile();

        if (selectedTile == null && secondSelectedTile == null) {
            gp.updateActionLogError("Select a tile before shoring!");
            return false;
        }

        if (selectedTile == null && game.getCurrentPlayer().getRole() != Role.ENGINEER) {
            gp.updateActionLogError("Select a tile before shoring!");
            return false;
        }

        if (gp.getGame().getCurrentPlayer().getRole() == Role.ENGINEER && secondSelectedTile != null) {
            game.shore(secondSelectedTile);
            gp.updateTile(secondSelectedTile);
        }

        if (selectedTile != null) {
            game.shore(selectedTile);
            gp.updateTile(selectedTile);
        }

        gp.updateActionLogActionsLeft();

        if (game.getCurrentPlayer().getRole() == Role.ENGINEER && selectedTile != null && secondSelectedTile != null) {
            game.setActionsLeft(game.getActionsLeft()+1);
        }

        return true;
    }

    public static boolean give() {
        Player selectedPlayer = gp.getSelectedPlayer();
        Object cardToTrade = gp.getSelectedCard();

        if (selectedPlayer == null && cardToTrade == null) {
            gp.updateActionLogError("Select a player's pawn and a Treasure Card from your hand before trading!");
            return false;
        } else if (selectedPlayer == null) {
            gp.updateActionLogError("Select a player's pawn before trading!");
            return false;
        } else if (cardToTrade == null) {
            gp.updateActionLogError("Select a Treasure Card from your hand before trading!");
            return false;
        }

        ArrayList<CardButton> traderCards = gp.getPlayerCards().get(game.getCurrentPlayer());
        ArrayList<CardButton> recipientCards = gp.getPlayerCards().get(selectedPlayer);

        CardButton tradingCardButton = null;

        // Remove CardButton from the trader into the recipient's hand
        for (CardButton cardButton : traderCards) {
            if (cardButton.getCard().equals(cardToTrade)) {
                tradingCardButton = cardButton;
                traderCards.remove(cardButton);
                break;
            }
        }

        recipientCards.add(tradingCardButton);
        game.give(selectedPlayer, cardToTrade);

        gp.updateHands();

        if (selectedPlayer.getHand().size() > 5) {
            gp.setDiscardingCard(true);
            gp.discardExcessCard(selectedPlayer);
            gp.disablePawns();
            return false;
        }

        return true;

    }

    public static boolean capture() {

        if (!game.capture()) {
            return false;
        }

        gp.updateHands();
        gp.updateFigurine(game.getCurrentPlayer());
        return true;
    }

    public static boolean special() {
        if (gp.getSelectedCard() == null) {
            gp.updateActionLogError("Select a Special Card before confirming!");
            return false;
        }

        SpecialCard specialCard = (SpecialCard) gp.getSelectedCard();

        switch (specialCard.getType()) {
            case SANDBAGS:
                if (gp.getSelectedTile() == null) {
                    gp.updateActionLogError("Select a flooded tile before using Sandbags!");
                    return false;
                }
                break;

            case HELICOPTER_LIFT:
                if (!gp.isGettingLandingSite()) {
                    if (gp.getSelectedTile() == null) {
                        gp.updateActionLogError("Select a tile to lift off from and confirm!");
                        return false;
                    }

                    if (gp.getSelectedTile().getName().equals("Fools' Landing") && game.hasWon()) {

                        gp.removeIcons(Action.SPECIAL);
                        return true;
                    }

                    gp.setGettingLandingSite(true);
                    gp.updateActionLogError("Select a tile to land on!");
                    gp.showHelicopterLiftIcons(gp.enableLandingTiles());
                    return false;
                }

                if (gp.getLandingTile() == null) {
                    gp.updateActionLogError("Select a landing site!");
                    gp.enableLandingTiles();
                    return false;
                }

                break;
        }

        game.special(specialCard);
        gp.updateHands();
        gp.resetHelicoptersLift();

        return true;
    }
}
