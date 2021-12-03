package graphics.panels.helper;

import game.ForbiddenIsland;
import game.Player;
import game.WaterLevel;
import game.board.Board;
import game.board.Tile;
import game.cards.SpecialCard;
import game.cards.TreasureCard;
import game.enums.TileState;
import graphics.panels.GamePanel;
import graphics.util.ButtonFactory;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Setup {

    private static GamePanel gp;
    private static ForbiddenIsland game;

    // Every image and object will be positioned relative to this point
    public static final int boardSize = 720;
    public static final int initialX = CenteredPosition.getPosition(boardSize, boardSize).width;
    public static final int initialY = CenteredPosition.getPosition(boardSize, boardSize).height - 125;

    private Setup() {

    }

    public static void setGamePanelReference(GamePanel gamePanel) {
        gp = gamePanel;
        game = gp.getGame();
    }

    public static void setGraphics() {
        setTilesAndPawns();
        setTreasures();
        setWaterMeter();
        setActionButtons();
        setDecks();
        setPlayerCards();
        setActionLog();
    }

    private static void setTilesAndPawns() {
        HashMap<Tile, JButton> tileButtons = new HashMap<>();
        HashMap<Tile, JLayeredPane> layeredPanes = new HashMap<>();
        HashMap<JButton, Player> playerPawns = new HashMap<>();

        int x = initialX;
        int y = initialY;

        Board board = game.getBoard();
        Player[] players = game.getPlayers();
        Tile[][] boardArr = board.getBoard();

        int size = Tile.IMAGE_SIZE;
        int gap = 10;
        for (int r = 0; r < boardArr.length; r++) {
            for (int c = 0; c < boardArr[r].length; c++) {
                Tile tile = boardArr[r][c];
                if (tile.getState() != TileState.SUNK && tile.getImage() != null && !tileButtons.containsKey(tile)) {
                    JLayeredPane layeredPane = new JLayeredPane();
                    layeredPane.setName(tile.getName());
                    layeredPane.setBounds(x, y, Tile.IMAGE_SIZE, Tile.IMAGE_SIZE);

                    JButton tileBtn = ButtonFactory.create(tile.getImage(), tile.getImagePressed(), tile.getImageSelected());
                    tileBtn.setBounds(0, 0, Tile.IMAGE_SIZE, Tile.IMAGE_SIZE);
                    tileBtn.setName(tile.getName());
                    Listeners.setTileListener(tileBtn);

                    // Set pawns
                    for (Player player: players) {
                        if (player.getOccupiedTile().equals(tile)) {
                            JButton pawn = ButtonFactory.create(player.getHighlightImage(), player.getImage(), player.getSelectedImage());
                            pawn.setDisabledIcon(player.getImage());

                            gp.setPawnPosition(pawn, player, tileBtn);
                            pawn.setName(player.getRole().name());

                            pawn.setEnabled(false);

                            Listeners.setPawnListener(pawn);

                            layeredPane.setLayer(pawn, layeredPane.highestLayer()+1);
                            layeredPane.add(pawn);
                            playerPawns.put(pawn, player);
                        }
                    }

                    layeredPane.setLayer(tileBtn, layeredPane.highestLayer()-1);
                    layeredPane.add(tileBtn);
                    gp.add(layeredPane);

                    tileButtons.put(tile, tileBtn);
                    layeredPanes.put(tile, layeredPane);
                }

                x += size + gap;
            }
            x = initialX;
            y += size + gap;
        }

        gp.setTilesAndPawns(tileButtons, layeredPanes, playerPawns);
    }

    private static void setTreasures() {

        int x = initialX;
        int y = initialY;

        ImageIcon oceansChaliceIcon = null;
        ImageIcon crystalOfFireIcon = null;
        ImageIcon statueOfWindIcon = null;
        ImageIcon earthStoneIcon = null;

        try {
            oceansChaliceIcon = ImageCreate.get("/images/figurines/Oceans_Chalice.png");
            oceansChaliceIcon = ImageScaler.scale(oceansChaliceIcon, oceansChaliceIcon.getIconWidth()/6, oceansChaliceIcon.getIconHeight()/6);

            crystalOfFireIcon = ImageCreate.get("/images/figurines/Crystal_of_Fire.png");
            crystalOfFireIcon = ImageScaler.scale(crystalOfFireIcon, crystalOfFireIcon.getIconWidth()/6, crystalOfFireIcon.getIconHeight()/6);

            statueOfWindIcon = ImageCreate.get("/images/figurines/Statue_of_the_Wind.png");
            statueOfWindIcon = ImageScaler.scale(statueOfWindIcon, statueOfWindIcon.getIconWidth()/6, statueOfWindIcon.getIconHeight()/6);

            earthStoneIcon = ImageCreate.get("/images/figurines/Earth_Stone.png");
            earthStoneIcon = ImageScaler.scale(earthStoneIcon, earthStoneIcon.getIconWidth()/6, earthStoneIcon.getIconHeight()/6);
        } catch (Exception e) {
            System.out.println(e);
        }

        JLabel oceansChalice = new JLabel(oceansChaliceIcon);
        JLabel crystalOfFire = new JLabel(crystalOfFireIcon);
        JLabel statueOfTheWind = new JLabel(statueOfWindIcon);
        JLabel earthStone = new JLabel(earthStoneIcon);

        oceansChalice.setBounds(x-10, y, oceansChalice.getIcon().getIconWidth(), oceansChalice.getIcon().getIconHeight());
        crystalOfFire.setBounds((x-10) + (120*5), y, crystalOfFire.getIcon().getIconWidth(), crystalOfFire.getIcon().getIconHeight());
        statueOfTheWind.setBounds((x-10), y + (120*5), statueOfTheWind.getIcon().getIconWidth(), statueOfTheWind.getIcon().getIconHeight());
        earthStone.setBounds((x-10) + (120*5), y + (120*5), earthStone.getIcon().getIconWidth(), earthStone.getIcon().getIconHeight());

        gp.setTreasureLabels(oceansChalice, crystalOfFire, statueOfTheWind, earthStone);

        gp.add(oceansChalice);
        gp.add(crystalOfFire);
        gp.add(statueOfTheWind);
        gp.add(earthStone);
    }

    private static void setWaterMeter() {
        int x = initialX;
        int y = initialY;

        WaterLevel waterLevel = game.getWaterLevel();

        ImageIcon levelIcon = waterLevel.getImage();
        ImageIcon markerIcon = waterLevel.getMarkerImage();
        JLabel waterLevelIcon = new JLabel(levelIcon);
        JLabel waterMarkerIcon = new JLabel(markerIcon);

        x += (850 + 203) - levelIcon.getIconWidth()/2;

        waterLevelIcon.setBounds(x, y, levelIcon.getIconWidth(), levelIcon.getIconHeight());
        waterMarkerIcon.setBounds(x - markerIcon.getIconWidth() - 5, waterLevel.getHeight(),
                markerIcon.getIconWidth(), markerIcon.getIconHeight());

        System.out.println(waterMarkerIcon.getY());
        System.out.println(waterLevelIcon.getHeight());

        gp.setWaterMeter(waterLevelIcon, waterMarkerIcon);

        gp.add(waterLevelIcon);
        gp.add(waterMarkerIcon);
        gp.revalidate();
        gp.repaint();
    }

    private static void setActionButtons() {
        int x = initialX;
        int y = initialY;

        x += 850;
        y += boardSize - 104 - 16 - 40 + 69;
        int gap = 20;

        HashMap<String, JButton> actionButtons = new HashMap<>();

        JLabel actionIndicator = new JLabel(ImageCreate.get("/images/misc/actions/actions_3.png"));

        actionIndicator.setBounds((x + 203) - actionIndicator.getIcon().getIconWidth()/2, y - 110, actionIndicator.getIcon().getIconWidth(), actionIndicator.getIcon().getIconHeight());
        gp.add(actionIndicator);



        // Move
        JButton moveBtn = ButtonFactory.create("/images/buttons/game/move.png", "/images/buttons/game/pressed/move.png", "/images/buttons/game/selected/move.png", 89, 90);
        moveBtn.setBounds(x, y, moveBtn.getIcon().getIconWidth(), moveBtn.getIcon().getIconHeight());
        moveBtn.setName("Move");
        moveBtn.setToolTipText("Move");

        Listeners.setActionButtonListener(moveBtn);
        actionButtons.put("Move", moveBtn);
        gp.add(moveBtn);



        // Shore
        JButton shoreBtn = ButtonFactory.create("/images/buttons/game/shore.png", "/images/buttons/game/pressed/shore.png", "/images/buttons/game/selected/shore.png", 89, 90);
        x += shoreBtn.getIcon().getIconWidth() + gap;
        shoreBtn.setBounds(x, y, shoreBtn.getIcon().getIconWidth(), shoreBtn.getIcon().getIconHeight());
        shoreBtn.setName("Shore");
        shoreBtn.setToolTipText("Shore");

        Listeners.setActionButtonListener(shoreBtn);
        actionButtons.put("Shore", shoreBtn);
        gp.add(shoreBtn);



        // Special
        JButton specialBtn = ButtonFactory.create("/images/buttons/game/special.png", "/images/buttons/game/pressed/special.png", "/images/buttons/game/selected/special.png", 89, 90);
        x += specialBtn.getIcon().getIconWidth() + gap;
        specialBtn.setBounds(x, y, specialBtn.getIcon().getIconWidth(), shoreBtn.getIcon().getIconHeight());
        specialBtn.setName("Special");
        specialBtn.setToolTipText("Special");

        Listeners.setActionButtonListener(specialBtn);
        actionButtons.put("Special", specialBtn);
        gp.add(specialBtn);



        // Confirm
        JButton confirmActionBtn = ButtonFactory.create("/images/buttons/game/confirm.png", "/images/buttons/game/pressed/confirm.png", 89, 90);
        x += confirmActionBtn.getIcon().getIconWidth() + gap;
        confirmActionBtn.setBounds(x, y, confirmActionBtn.getIcon().getIconWidth(), confirmActionBtn.getIcon().getIconHeight());
        confirmActionBtn.setName("Confirm");
        confirmActionBtn.setToolTipText("Confirm Action");

        Listeners.setMiscButtonListener(confirmActionBtn);
        actionButtons.put("Confirm", confirmActionBtn);
        gp.add(confirmActionBtn);

        x = moveBtn.getX();
        y += gap;



        // Give
        JButton giveBtn = ButtonFactory.create("/images/buttons/game/give.png", "/images/buttons/game/pressed/give.png", "/images/buttons/game/selected/give.png", 89, 90);
        y += giveBtn.getIcon().getIconHeight();
        giveBtn.setBounds(x, y, giveBtn.getIcon().getIconWidth(), giveBtn.getIcon().getIconHeight());
        giveBtn.setName("Give");
        giveBtn.setToolTipText("Trade Cards");

        Listeners.setActionButtonListener(giveBtn);
        actionButtons.put("Give", giveBtn);
        gp.add(giveBtn);



        // Capture
        JButton captureBtn = ButtonFactory.create("/images/buttons/game/capture.png", "/images/buttons/game/pressed/capture.png", "/images/buttons/game/selected/capture.png", 89, 90);
        x += captureBtn.getIcon().getIconWidth() + gap;
        captureBtn.setBounds(x, y, captureBtn.getIcon().getIconWidth(), captureBtn.getIcon().getIconHeight());
        captureBtn.setName("Capture");
        captureBtn.setToolTipText("Capture Treasure");

        Listeners.setActionButtonListener(captureBtn);
        actionButtons.put("Capture", captureBtn);
        gp.add(captureBtn);



        // Help
        JButton helpBtn = ButtonFactory.create("/images/buttons/game/help.png", "/images/buttons/game/pressed/help.png", 89, 90);
        x += helpBtn.getIcon().getIconWidth() + gap;
        helpBtn.setBounds(x, y, helpBtn.getIcon().getIconWidth(), helpBtn.getIcon().getIconHeight());
        helpBtn.setName("Help");
        helpBtn.setToolTipText("Help");

        //setActionButtonListener(helpBtn);
        Listeners.setMiscButtonListener(helpBtn);
        actionButtons.put("Help", helpBtn);
        gp.add(helpBtn);



        // Cancel
        JButton cancelActionBtn = ButtonFactory.create("/images/buttons/game/cancel.png", "/images/buttons/game/pressed/cancel.png", 89, 90);
        x += cancelActionBtn.getIcon().getIconWidth() + gap;
        cancelActionBtn.setBounds(x, y, cancelActionBtn.getIcon().getIconWidth(), cancelActionBtn.getIcon().getIconHeight());
        cancelActionBtn.setName("Cancel");
        cancelActionBtn.setToolTipText("Cancel Action");

        Listeners.setMiscButtonListener(cancelActionBtn);
        actionButtons.put("Cancel", cancelActionBtn);
        gp.add(cancelActionBtn);

        x = moveBtn.getX();
        y += gap;



        // End turn
        JButton endTurnBtn = ButtonFactory.create("/images/buttons/game/end_turn.png", "/images/buttons/game/pressed/end_turn.png", (89*4) + (gap*3), 90);
        y += endTurnBtn.getIcon().getIconHeight();
        endTurnBtn.setBounds(x, y, endTurnBtn.getIcon().getIconWidth(), endTurnBtn.getIcon().getIconHeight());
        endTurnBtn.setName("End Turn");
        endTurnBtn.setToolTipText("End Turn");

        Listeners.setMiscButtonListener(endTurnBtn);
        actionButtons.put("End Turn", endTurnBtn);
        gp.add(endTurnBtn);

        gp.setAction(actionButtons, actionIndicator);
        gp.setEndTurnButton(endTurnBtn);
    }

    private static void setDecks() {
        JLabel floodDeck = null, floodDiscard = null, treasureDeck = null, treasureDiscard = null;

        int x = initialX;
        int y = initialY;
        int topMargin = 60;
        int horizontalMargin = 10;

        x += boardSize/2;
        y += boardSize + topMargin;


        floodDeck = new JLabel(ImageCreate.get("/images/cards/flood/Flood_Back.png"));
        floodDeck.setBounds(x, y, floodDeck.getIcon().getIconWidth(), floodDeck.getIcon().getIconHeight());
        gp.add(floodDeck);

        floodDiscard = new JLabel(ImageCreate.get("/images/cards/flood/Flood_Discard.png"));
        floodDiscard.setBounds(x + floodDiscard.getIcon().getIconWidth() + horizontalMargin, y, floodDiscard.getIcon().getIconWidth(), floodDiscard.getIcon().getIconHeight());
        gp.add(floodDiscard);

        JLabel floodDiscardCard = new JLabel();
        floodDiscardCard.setBounds(floodDiscard.getX() + 4, floodDiscard.getY() + 5, 105, 138);
        gp.add(floodDiscardCard);

        treasureDeck = new JLabel(ImageCreate.get("/images/cards/treasure/treasure_deck_back.jpg"));

        x = initialX + boardSize/2 - horizontalMargin - treasureDeck.getIcon().getIconWidth();
        treasureDeck.setBounds(x, y, treasureDeck.getIcon().getIconWidth(), treasureDeck.getIcon().getIconHeight());
        gp.add(treasureDeck);

        treasureDiscard = new JLabel(ImageCreate.get("/images/cards/treasure/Treasure_Discard.png"));
        treasureDiscard.setBounds(x - treasureDiscard.getIcon().getIconWidth() - horizontalMargin*2, y, treasureDiscard.getIcon().getIconWidth(), treasureDiscard.getIcon().getIconHeight());
        gp.add(treasureDiscard);

        JLabel treasureDiscardCard = new JLabel();
        treasureDiscardCard.setBounds(treasureDiscard.getX() + 4, treasureDiscard.getY() + 5, 105, 138);
        gp.add(treasureDiscardCard);

        gp.setDecks(floodDeck, floodDiscard, treasureDeck, treasureDiscard, floodDiscardCard, treasureDiscardCard);
    }

    private static void setPlayerCards() {
        int x = initialX;
        int y = initialY;

        int defaultX = x - 545;
        x -= 545;

        HashMap<Player, ArrayList<CardButton>> playerCards = new HashMap<>();
        JLabel turnIndicator = null;
        Player currentPlayer = game.getCurrentPlayer();

        for (int i = 0; i < game.getPlayers().length; i++) {
            Player player = game.getPlayers()[i];
            String role = player.getRole().toString().toLowerCase(Locale.ROOT);

            playerCards.put(player, new ArrayList<>());

            ImageIcon playerText = ImageCreate.get("/images/players/text/" + role + ".png");

            JLabel playerLabel = new JLabel(playerText);
            playerLabel.setBounds(x, y, playerLabel.getIcon().getIconWidth(), playerLabel.getIcon().getIconHeight());
            gp.add(playerLabel);

            if (i == game.getCurrentPlayerIndex()) {
                turnIndicator = new JLabel(ImageCreate.get("/images/misc/player_indicator2.png"));

                turnIndicator.setBounds(x - turnIndicator.getIcon().getIconWidth(), y, turnIndicator.getIcon().getIconWidth(), turnIndicator.getIcon().getIconHeight());
                //System.out.println(turnIndicator.getY());
                gp.add(turnIndicator);
            }

            x = 332; // roleIcon will always have constant x position

            ImageIcon roleIcon = roleIcon = ImageCreate.get("/images/players/role_icons/" + role + ".png");

            roleIcon = ImageScaler.scale(roleIcon, roleIcon.getIconWidth()/2, roleIcon.getIconHeight()/2);
            JLabel roleLabel = new JLabel(roleIcon);
            roleLabel.setBounds(x, y, roleLabel.getIcon().getIconWidth(), roleLabel.getIcon().getIconHeight());
            gp.add(roleLabel);

            x += roleLabel.getIcon().getIconWidth() + 20;

            JLabel pawnLabel = new JLabel(player.getImage());
            pawnLabel.setBounds(x, y, pawnLabel.getIcon().getIconWidth(), pawnLabel.getIcon().getIconHeight());
            gp.add(pawnLabel);

//            System.out.println(x);
//            System.out.println(y);

            x = defaultX;

            // Card graphics
            ArrayList<Object> hand = player.getHand();
            for (Object card : hand) {
                ImageIcon cardImage;
                ImageIcon selectedImage;

                if (card.getClass().getSimpleName().equals("TreasureCard")) {
                    cardImage = ((TreasureCard) card).getImage();
                    selectedImage = ((TreasureCard) card).getSelectedImage();
                } else {
                    cardImage = ((SpecialCard) card).getImage();
                    selectedImage = ((SpecialCard) card).getSelectedImage();
                }

                cardImage = ImageScaler.scale(cardImage, 72, 102);
                selectedImage = ImageScaler.scale(selectedImage, 72, 102);

                CardButton cardBtn = ButtonFactory.createCardButton(cardImage, cardImage, selectedImage);
                cardBtn.setCard(card);
                cardBtn.setBounds(x, y + playerLabel.getIcon().getIconHeight() + 20, cardBtn.getIcon().getIconWidth(), cardBtn.getIcon().getIconHeight());
                Listeners.setCardListener(cardBtn);
                cardBtn.setEnabled(false);

                gp.add(cardBtn);
                x += cardBtn.getIcon().getIconWidth() + 5;
                playerCards.get(player).add(cardBtn);
            }

            x = defaultX;
            y += 180;
        }

        gp.setPlayers(playerCards, turnIndicator, currentPlayer);
    }

    private static void setActionLog() {
        int x = initialX;
        int y = 775 + 30 + 28;

        x -= 535;

        ImageIcon actionLogImg = actionLogImg = ImageCreate.get("/images/misc/action_log.png");


        actionLogImg = ImageScaler.scale(actionLogImg, (int) (actionLogImg.getIconWidth() * (3.0/4)), (int) (actionLogImg.getIconHeight() * (3.0/4)));
        JLabel actionLogText = new JLabel(actionLogImg);
        actionLogText.setBounds(x, y - actionLogText.getIcon().getIconHeight() - 5, actionLogText.getIcon().getIconWidth(), actionLogText.getIcon().getIconHeight());
        gp.add(actionLogText);

        JTextArea actionLog = new JTextArea();
        actionLog.setFont(new Font("Roboto", Font.BOLD, 13));
        actionLog.setText("\tWelcome to Forbidden Island! Escape before the island sinks!\n\tPress the ? icon for help.");
        actionLog.setTabSize(1);
        actionLog.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(actionLog);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(x, y, 500, 150);

        CompoundBorder border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(171, 171, 171), 5), border));
        gp.add(scrollPane);

        gp.setActionLog(actionLog);
    }
}
