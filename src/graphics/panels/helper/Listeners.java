package graphics.panels.helper;

import game.Player;
import game.board.Tile;
import game.cards.SpecialCard;
import game.enums.Action;
import game.enums.Role;
import game.enums.Special;
import graphics.panels.GamePanel;
import graphics.panels.HelpPanel;
import graphics.panels.RootPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

// All button listeners for GamePanel

public class Listeners {

    private static GamePanel gp;

    private Listeners() {

    }

    public static void setGamePanelReference(GamePanel gamePanel) {
        gp = gamePanel;
    }

    public static void setTileListener(JButton tile) {
        tile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tile.setSelected(!tile.isSelected());

                if (tile.isSelected()) {
                    tileSelect(tile);
                } else {
                    tileUnSelect(tile);
                }
            }
        });
    }

    private static void tileSelect(JButton tile) {
        gp.updateActionLog("selected " + tile.getName());

        if (!gp.isGettingLandingSite()) {
            if (gp.getGame().getCurrentPlayer().getRole() == Role.ENGINEER && gp.getSelectedTile() != null) {
                gp.setSecondShoreTile(gp.getGame().getBoard().getTile(tile.getName()));
            } else{
                gp.setSelectedTile(gp.getGame().getBoard().getTile(tile.getName()));
            }
        } else {
            // if is getting landing site
            gp.setLandingTile(gp.getGame().getBoard().getTile(tile.getName()));
        }

        if (gp.isDiscardingCard() && gp.getSelectedCard() != null && gp.getSelectedCard().getClass().getSimpleName().equals("SpecialCard")) {
            gp.removeIcons(Action.SPECIAL);
            return;
        }

        if (gp.getPlayerSunkTile() != null) {
            gp.removeIcons(Action.MOVE);
        }

        gp.removeIcons();
    }

    private static void tileUnSelect(JButton tile) {
        gp.updateActionLog("unselected " + tile.getName());

        if (!gp.isGettingLandingSite()) {
            if (gp.getGame().getBoard().getTile(tile.getName()).equals(gp.getSelectedTile()))
                gp.setSelectedTile(null);
            else
                gp.setSecondShoreTile(null);

        } else {
            gp.setLandingTile(null);
        }

        if (gp.isDiscardingCard() && gp.getSelectedCard().getClass().getSimpleName().equals("SpecialCard")) {
            gp.showIcons(Action.SPECIAL);
            return;
        }

        if (gp.getPlayerSunkTile() != null) {
            gp.showMovementTiles(gp.getPlayerSunkTile());
            return;
        }

        gp.showIcons();
    }

    public static void setPawnListener(JButton pawn) {
        pawn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pawn.setSelected(!pawn.isSelected());

                if (pawn.isSelected()) {
                    pawnSelect(pawn);
                } else {
                    pawnUnSelect(pawn);
                }
            }
        });
    }

    private static void pawnSelect(JButton pawn) {
        HashMap<JButton, Player> pawns = gp.getPlayerPawns();
        gp.setSelectedPlayer(pawns.get(pawn));
        gp.showSelectedPlayer(pawn);
        gp.updateActionLog("selected " + gp.getPlayerPawns().get(pawn).getName());

        if (gp.getGame().getCurrentPlayer().getRole() == Role.NAVIGATOR)
            gp.showIcons();
    }

    private static void pawnUnSelect(JButton pawn) {
        gp.updateActionLog("unselected " + gp.getPlayerPawns().get(pawn).getName());

        if (gp.getSelectedAction() == Action.GIVE) {
            gp.showIcons();
        }
        else if (gp.getGame().getCurrentPlayer().getRole() == Role.NAVIGATOR) {
            gp.removeIcons();
            gp.enablePawns();
            gp.disableTiles();
            gp.setSelectedTile(null);
        }

        gp.setSelectedPlayer(null);
    }

    public static void setCardListener(CardButton card) {
        card.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.setSelected(!card.isSelected());

                if (card.isSelected()) {
                    cardSelect(card);
                } else {
                    cardUnSelect(card);
                }
            }
        });
    }

    private static void cardSelect(CardButton card) {
        gp.setSelectedCard(card.getCard());
        gp.disableCards(card);
        gp.updateActionLog("selected " + card.getCard().toString());

        if (gp.getSelectedAction() == Action.SPECIAL || (gp.isDiscardingCard() && gp.getSelectedCard().getClass().getSimpleName().equals("SpecialCard"))) {
            SpecialCard specialCard = (SpecialCard) gp.getSelectedCard();

            if (specialCard.getType() == Special.HELICOPTER_LIFT) {
                if (!gp.isGettingLandingSite() && gp.getSelectedTile() == null) {
                    gp.updateActionLogError("Select a tile to lift off from and confirm!");
                }
            }

            if (gp.isDiscardingCard()) {
//                if (specialCard.getType() == Special.SANDBAGS)
//                    gp.showIcons(Action.SHORE);
//                else
//                    gp.showIcons(Action.SPECIAL);

                gp.showIcons(Action.SPECIAL);

                return;
            }

            gp.showIcons();
        }


    }

    private static void cardUnSelect(CardButton card) {
        gp.updateActionLog("unselected " + card.getCard().toString());

        if (gp.isDiscardingCard()) {
            gp.enablePlayerCards(gp.getPlayerDiscarding());

            if (card.getCard().getClass().getSimpleName().equals("SpecialCard")) {
                gp.removeIcons(Action.SPECIAL);
                gp.disableTiles();
                gp.resetHelicoptersLift();
            }

        } else if (gp.getSelectedAction() == Action.GIVE) {
            gp.enableTradeCards(gp.getGame().getCurrentPlayer());
        } else if (gp.getSelectedAction() == Action.SPECIAL) {
            gp.enableSpecialCards();
            gp.removeIcons();
            gp.disableTiles();

            if (gp.getSelectedTile() != null)
                gp.resetHelicoptersLift();
        }

        gp.setSelectedCard(null);
        gp.setSelectedTile(null);
        gp.setLandingTile(null);
        gp.setGettingLandingSite(false);
    }

    public static void setActionButtonListener(JButton button) {
        button.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.setSelected(!button.isSelected());

                if (!button.isSelected()) { // If button is unselected
                    actionButtonUnSelect();
                } else {
                    actionButtonSelect(button);
                }
            }
        });
    }

    private static void actionButtonSelect(JButton button) {
        String name = button.getName();
        gp.setSelectedAction(Action.valueOf(name.toUpperCase())); // Sets the selected action instance variable

        // Checks if the player can play the selected action
        if (!gp.isActionValid()) {
            gp.cancelAction();
            return;
        }

        gp.disableActionButtons(gp.getActionButtons().get(name)); // Disables all other action buttons except itself
        gp.updateActionLog("selected " + gp.getSelectedAction() + " action");

        gp.showIcons(); // Shows the action icons on the tiles

        if (gp.getSelectedAction() == Action.GIVE)
            gp.enableTradeCards(gp.getGame().getCurrentPlayer());

        if (gp.getSelectedAction() == Action.SPECIAL) {
            gp.enableSpecialCards();
        }
    }

    private static void actionButtonUnSelect() {
        gp.updateActionLog("cancelled " + gp.getSelectedAction() + " action");
        gp.removeIcons();
        gp.cancelAction();
    }

    // Confirm, cancel, and end turn
    public static void setMiscButtonListener(JButton button) {
        button.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (button.getName()) {
                    case "Confirm":
                        if (gp.hasGameEnd()) {
                            RootPanel.switchPanel("input");
                            return;
                        }

                        if (gp.isDiscardingCard() && gp.getSelectedCard() == null) {
                            gp.updateActionLogError("Select a card to discard!");
                            break;
                        }

                        if (gp.getPlayerSunkTile() != null && gp.getSelectedTile() == null) {
                            gp.updateActionLogError("Select a tile before moving!");
                            break;
                        }

                        gp.confirmAction();
                        break;

                    case "Cancel":
                        if (gp.getSelectedAction() != Action.NONE)
                            gp.updateActionLog("cancelled " + gp.getSelectedAction() + " action");

                        gp.cancelAction();
                        break;

                    case "End Turn":
                        gp.endTurnPress();
                        break;

                    case "Help":
                        if (!HelpPanel.isInstantiated()) {
                            new HelpPanel();
                        }

                        HelpPanel.setPreviousPanel("game");
                        RootPanel.switchPanel("help");
                        break;
                }
            }
        });
    }
}
