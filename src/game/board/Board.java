package game.board;

import game.ForbiddenIsland;
import game.Player;
import game.Randomizer;
import game.cards.FloodCard;
import game.enums.Role;
import game.enums.TileState;
import game.enums.Treasure;
import game.enums.TreasureState;
import graphics.panels.GamePanel;

import javax.swing.*;
import java.io.InputStream;
import java.util.*;

public class Board {

    private final Tile[][] board;
    // Key: Tile name; Value: Tile object
    private final HashMap<String, Tile> tiles;
    private final HashMap<Treasure, Tile[]> treasureTiles;
    private final HashMap<Tile, Location> tileLocations;
    private final HashMap<Player, Location> playerLocations;

    private final ForbiddenIsland game;

    public Board(ForbiddenIsland game) {
        System.err.println("Board initialized.");
        board = new Tile[6][6];
        this.game = game;

        // Sets all empty tiles in each row
        Tile emptyTile = new Tile();
        board[0][0] = board[0][1] = board[0][4] = board[0][5] = emptyTile;
        board[1][0] = board[1][5] = emptyTile;
        board[4][0] = board[4][5] = emptyTile;
        board[5][0] = board[5][1] = board[5][4] = board[5][5] = emptyTile;

        tiles = new HashMap<>();
        treasureTiles = new HashMap<>();
        tileLocations = new HashMap<>();
        playerLocations = new HashMap<>();

        InputStream is = Board.class.getResourceAsStream("/game/txt/island_names.txt");
        Scanner islandNames = new Scanner(is);
        ArrayList<Tile> tileList = new ArrayList<>();

        // Creates all Island tiles
        while (islandNames.hasNextLine()) {
            String name = islandNames.nextLine();
            Tile tile = new Tile(name);
            tile.setImages();

            tiles.put(tile.getName(), tile);
            tileList.add(tile);

            Treasure treasure = tile.getTreasure();
            if (treasure != null) {
                if (treasureTiles.containsKey(treasure)) {
                    treasureTiles.get(treasure)[1] = tile;
                } else {
                    treasureTiles.put(treasure, new Tile[]{tile, null});
                }
            }
        }

        setBoard(tileList);
        //Arrays.stream(board).map(Arrays::toString).forEach(System.out::println);
    }

    public void floodTile(FloodCard card) {
        if (tiles.containsKey(card.getName())) {
            tiles.get(card.getName()).flood();
            System.err.println("FLOODED " + tiles.get(card.getName()) + " (" + tiles.get(card.getName()).getState() + ")");
        }
    }

    public Tile floodTile(FloodCard card, GamePanel gamePanel) {
        if (tiles.containsKey(card.getName())) {
            Tile tile = tiles.get(card.getName());
            tile.flood();
            gamePanel.updateTile(tile);

            System.err.println("FLOODED " + tiles.get(card.getName()) + " (" + tiles.get(card.getName()).getState() + ")");
            return tile;
        }

        return null;
    }

    public void shoreTile(Tile tile) {
        tile.shore();
    }

    public boolean movePlayer(Player player, Tile tile) {
        Location location = tileLocations.get(tile);


        if (player.getRole() == Role.PILOT && !getAdjacentUnSunkTiles(location).contains(tile)) {
            System.out.println("Pilot used ability");
            game.setPilotUsedAbility(true);
        }

        player.setOccupiedTile(tile);
        playerLocations.replace(player, location);

        return true;
    }

    // Moves players from the originalTile to the newTile
    public void moveHelicopterLift(Tile oldTile, Tile newTile, GamePanel gp) {
        HashSet<Player> playersToMove = new HashSet<>();

        for (Player player: playerLocations.keySet()) {
            if (player.getOccupiedTile().equals(oldTile)) {
                playersToMove.add(player);
            }
        }

        Location location = tileLocations.get(newTile);
        gp.updateActionLogError("");

        for (Player player: playersToMove) {
            JButton pawn = gp.removeAndGetPawn(player);

            player.setOccupiedTile(newTile);
            playerLocations.replace(player, location);

            gp.updatePawn(player, pawn);

            gp.updateActionLogCustom(player.getName() + " lifted from " + oldTile + " to " + newTile);
        }
    }

    public void give(Player trader, Player recipient, Object card) {
        trader.removeCard(card);
        recipient.addCard(card);
    }

    // Calculates and returns all the Tiles the player is able to move to
    public HashSet<Tile> getAvailableMovementTiles(Player player) {
        Location location = playerLocations.get(player);
        //printBoardStatus(player);

        // Any available adjacent tiles
        HashSet<Tile> availableTiles = new HashSet<>(getAdjacentUnSunkTiles(location));

        // Role specific
        switch(player.getRole()) {
            case EXPLORER:
                availableTiles.addAll(getDiagonalUnSunkTiles(location));
                break;
            case PILOT:
                // if pilot hasnt used their ability or the pilot's tile sunk and is moving to another tile
                if (!game.isPilotUsedAbility() || (game.getGamePanel().getPlayerSunkTile() != null && game.getGamePanel().getPlayerSunkTile().equals(player))) {
                    availableTiles.addAll(getAllNonSunkTiles());
                }

                break;
            case DIVER:
                availableTiles.addAll(getDiverTiles(location.getRow(), location.getColumn(), location.getRow(), location.getColumn(), new HashSet<>()));
                break;
        }

        availableTiles.remove(player.getOccupiedTile());
        return availableTiles;
    }

    public HashSet<Tile> getAvailableShoreTiles(Player player, boolean special) {

        if (special) {
            return getAllFloodedTiles();
        }

        Location location = playerLocations.get(player);

        HashSet<Tile> availableTiles = new HashSet<>(getAdjacentFloodedTiles(location));

        if (player.getOccupiedTile().getState() == TileState.FLOODED)
            availableTiles.add(player.getOccupiedTile());

        // Role specific
        if (player.getRole() == Role.EXPLORER)
            availableTiles.addAll(getDiagonalFloodedTiles(location));

        return availableTiles;
    }

    // Calculates and returns all the available tiles of the player the Navigator is moving
    // Navigator can move player up to 2 adjacent tiles for 1 action
    public HashSet<Tile> getNavigatorMovementTiles(Player player) {
        Location location = playerLocations.get(player);
        //(player);

        // Adds all legal adjacent tiles
        HashSet<Tile> availableTiles = new HashSet<>(getAdjacentUnSunkTiles(location));
        HashSet<Tile> temp = new HashSet<>();

        // Adds all legal adjacent tiles of the previously added adjacent tiles
        for (Tile tile: availableTiles) {
            Location tileLocation = tileLocations.get(tile);
            temp.addAll(getAdjacentUnSunkTiles(tileLocation));
        }

        availableTiles.addAll(temp);

        availableTiles.remove(player.getOccupiedTile());
        return availableTiles;
    }

    // Returns all the players that are on the same tile as the inputted player
    public HashSet<Player> getPlayersToTradeWith(Player player) {
        HashSet<Player> players = new HashSet<>(getSameTilePlayers(player));

        if (player.getRole() == Role.MESSENGER) { // Can trade any with player regardless of location
            players.addAll(playerLocations.keySet());
            players.remove(player); // Remove itself from available list of players to trade with
            return players;
        }


        return players;
    }

    public HashSet<Player> getSameTilePlayers(Player player) {
        Location playerLocation = playerLocations.get(player);

        if (playerLocation == null)
            return null;

        HashSet<Player> players = new HashSet<>();

        for (Player key: playerLocations.keySet()) {
            if (key.equals(player)) // If both players are the same
                continue;

            Location teammateLocation = playerLocations.get(key);
            if (playerLocation.equals(teammateLocation))
                players.add(key);
        }

        return players;
    }

    private HashSet<Tile> getAdjacentTiles(Location location) {
        int row = location.getRow();
        int col = location.getColumn();
        HashSet<Tile> adjacentTiles = new HashSet<>();

        if (Location.isLegalIndex(row-1, col)) // up
            adjacentTiles.add(board[row-1][col]);

        if (Location.isLegalIndex(row+1, col)) // down
            adjacentTiles.add(board[row+1][col]);

        if (Location.isLegalIndex(row, col-1)) // left
            adjacentTiles.add(board[row][col-1]);

        if (Location.isLegalIndex(row, col+1)) // right
            adjacentTiles.add(board[row][col+1]);

        return adjacentTiles;
    }

    private HashSet<Tile> getDiagonalTiles(Location location) {
        int row = location.getRow();
        int col = location.getColumn();
        HashSet<Tile> diagonalTiles = new HashSet<>();

        if (Location.isLegalIndex(row-1, col-1)) // top-left
            diagonalTiles.add(board[row-1][col-1]);

        if (Location.isLegalIndex(row-1, col+1)) // top-right
            diagonalTiles.add(board[row-1][col+1]);

        if (Location.isLegalIndex(row+1, col-1)) // bottom-left
            diagonalTiles.add(board[row+1][col-1]);

        if (Location.isLegalIndex(row+1, col+1)) // bottom-right
            diagonalTiles.add(board[row+1][col+1]);

        return diagonalTiles;
    }

    // Returns all adjacent NON-SUNK tiles given a player's location
    private HashSet<Tile> getAdjacentUnSunkTiles(Location location) {
        HashSet<Tile> adjacentTiles = getAdjacentTiles(location);
        HashSet<Tile> unSunkAdjacent = new HashSet<>();

        for (Tile tile: adjacentTiles) {
            if (tile.getState() != TileState.SUNK)
                unSunkAdjacent.add(tile);
        }

        return unSunkAdjacent;
    }

    // Returns all diagonal NON-SUNK tiles given a player's location
    private HashSet<Tile> getDiagonalUnSunkTiles(Location location) {
        HashSet<Tile> diagonalTiles = getDiagonalTiles(location);
        HashSet<Tile> unSunkDiagonal = new HashSet<>();

        for (Tile tile: diagonalTiles) {
            if (tile.getState() != TileState.SUNK)
                unSunkDiagonal.add(tile);
        }

        return unSunkDiagonal;
    }

    public HashSet<Tile> getAllNonSunkTiles() {
        HashSet<Tile> unSunkTiles = new HashSet<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].getState() != TileState.SUNK && board[row][col].getState() != TileState.EMPTY)
                    unSunkTiles.add(board[row][col]);
            }
        }

        return unSunkTiles;
    }

    public HashSet<Tile> getAllFloodedTiles() {
        HashSet<Tile> floodedTiles = new HashSet<>();

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].getState() == TileState.FLOODED)
                    floodedTiles.add(board[row][col]);
            }
        }

        return floodedTiles;
    }

    private HashSet<Tile> getAdjacentFloodedTiles(Location location) {
        HashSet<Tile> adjacentTiles = getAdjacentTiles(location);
        HashSet<Tile> floodedTiles = new HashSet<>();

        for (Tile tile: adjacentTiles) {
            if (tile.getState() == TileState.FLOODED)
                floodedTiles.add(tile);
        }

        return floodedTiles;
    }

    private HashSet<Tile> getDiagonalFloodedTiles(Location location) {
        HashSet<Tile> diagonalTiles = getDiagonalTiles(location);
        HashSet<Tile> floodedTiles = new HashSet<>();

        for (Tile tile: diagonalTiles) {
            if (tile.getState() == TileState.FLOODED)
                floodedTiles.add(tile);
        }

        return floodedTiles;
    }

    public HashMap<Player, Location> getPlayerLocations() {

        return playerLocations;
    }

    public HashSet<Tile> getAllPlayerTiles() {
        HashSet<Tile> tiles = new HashSet<>();

        for (Player player: playerLocations.keySet()) {
            tiles.add(player.getOccupiedTile());
        }

        return tiles;
    }

    /*
     Recursively finds all flooded/sunken adjacent Island tiles given the Diver's current location
     and returns all the available tiles the Diver can move to

     NOTE: Diver can move through flooded and sunken tiles
     */
    private HashSet<Tile> getDiverTiles(int row, int col, int startingRow, int startingCol, HashSet<Location> checkedLocations) {
        // HashSet of all Tiles the Diver can move to is initialized with the Diver's available adjacent tiles
        HashSet<Tile> diverTiles = new HashSet<>(getAdjacentUnSunkTiles(new Location(row, col)));

        // Checks if the location has not been checked yet AND is a legal index (to prevent out of bounds error)
        if (!checkedLocations.contains(new Location(row-1, col)) && Location.isLegalIndex(row-1, col)) { // up
            TileState tileState = board[row-1][col].getState();

            switch(tileState) {
                case FLOODED: // If the Tile the algorithm checking is flooded, the Tile is added to the HashSet of all the tiles Diver can move to
                    diverTiles.add(board[row-1][col]);
                case SUNK:  // If the Tile is flooded or sunk, the Tile is added to the HashSet of checkedLocations and calls the recursive function with the new location
                    checkedLocations.add(new Location(row-1, col));
                    diverTiles.addAll(getDiverTiles(row-1, col, startingRow, startingCol, checkedLocations));
                    break;
            }
        }

        if (!checkedLocations.contains(new Location(row+1, col)) && Location.isLegalIndex(row+1, col)) { // down
            TileState tileState = board[row+1][col].getState();

            switch(tileState) {
                case FLOODED:
                    diverTiles.add(board[row+1][col]);
                case SUNK:
                    checkedLocations.add(new Location(row+1, col));
                    diverTiles.addAll(getDiverTiles(row+1, col, startingRow, startingCol, checkedLocations));
                    break;
            }
        }

        if (!checkedLocations.contains(new Location(row, col-1)) && Location.isLegalIndex(row, col-1)) { // left
            TileState tileState = board[row][col-1].getState();

            switch(tileState) {
                case FLOODED:
                    diverTiles.add(board[row][col-1]);
                case SUNK:
                    checkedLocations.add(new Location(row, col-1));
                    diverTiles.addAll(getDiverTiles(row, col-1, startingRow, startingCol, checkedLocations));
                    break;
            }
        }

        if (!checkedLocations.contains(new Location(row, col+1)) && Location.isLegalIndex(row, col+1)) { // right
            TileState tileState = board[row][col+1].getState();

            switch(tileState) {
                case FLOODED:
                    diverTiles.add(board[row][col+1]);
                case SUNK:
                    checkedLocations.add(new Location(row, col+1));
                    diverTiles.addAll(getDiverTiles(row, col+1, startingRow, startingCol, checkedLocations));
                    break;
            }
        }

        diverTiles.remove(board[startingRow][startingCol]); // Removes the Tile the player is currently on
        return diverTiles;
    }

    public void setStartingPositions(Player[] players) {
        for (Player player: players) {
            Tile tile = null;
            Location location = null;

            switch(player.getRole()) {
                case DIVER:
                    tile = tiles.get("Iron Gate");
                    location = tileLocations.get(tiles.get("Iron Gate"));
                    break;
                case ENGINEER:
                    tile = tiles.get("Bronze Gate");
                    location = tileLocations.get(tiles.get("Bronze Gate"));
                    break;
                case EXPLORER:
                    tile = tiles.get("Copper Gate");
                    location = tileLocations.get(tiles.get("Copper Gate"));
                    break;
                case MESSENGER:
                    tile = tiles.get("Silver Gate");
                    location = tileLocations.get(tiles.get("Silver Gate"));
                    break;
                case NAVIGATOR:
                    tile = tiles.get("Gold Gate");
                    location = tileLocations.get(tiles.get("Gold Gate"));
                    break;
                case PILOT:
                    tile = tiles.get("Fools' Landing");
                    location = tileLocations.get(tiles.get("Fools' Landing"));
                    break;
            }

            player.setOccupiedTile(tile);
            playerLocations.put(player, location);
        }
    }

    public boolean haveTreasureTilesSunk(GamePanel gamePanel) {
        for (Treasure key: treasureTiles.keySet()) {
            Tile[] tiles = treasureTiles.get(key);

            // If either treasure tile has been captured, doesn't matter if they're sunk
            if (tiles[0].getTreasureState() == TreasureState.CAPTURED || tiles[1].getTreasureState() == TreasureState.CAPTURED)
                continue;

            if (tiles[0].getState() == TileState.SUNK && tiles[1].getState() == TileState.SUNK) {
                gamePanel.updateActionLogError("Game over! Both tiles containing " + tiles[0].getTreasure() + " have sunk!");
                return true;
            }
        }

        return false;
    }

    public Tile[][] getBoard(){
        return board;
    }

    public Tile getTile(String tile) {
        return tiles.get(tile);
    }

    public HashSet<Tile> getAllTiles() {
        HashSet<Tile> tiles = new HashSet<>();

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                Tile tile = board[r][c];
                if (tile.getState() != TileState.EMPTY && tile.getState() != TileState.SUNK) {
                    tiles.add(tile);
                }
            }
        }

        return tiles;
    }

    public void printBoardStatus(Player player) {
        int playerRow = playerLocations.get(player).getRow();
        int playerColumn = playerLocations.get(player).getColumn();

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                String output = String.format("%-10s", "");

                if (r == playerRow && c == playerColumn) {
                    output = String.format("%-10s", player.getRole().name());
                } else if (board[r][c].getState() != TileState.EMPTY) {
                    output = String.format("%-10s", board[r][c].getState().toString());
                }

                System.err.print(output + " ");
            }

            System.err.println();
        }
        System.err.println();
    }

    public void printBoard() {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                String name = board[r][c].getName();
                String outputText;

                // https://stackoverflow.com/questions/25750641/t-not-consistent-with-spacing-in-java
                if (name.equals("Empty"))
                    outputText = String.format("%-20s", "");
                else
                    outputText = String.format("%-20s", name);

                System.err.print(outputText + " ");

            }
            System.err.println();
        }
        System.err.println();
    }

    private void setBoard(ArrayList<Tile> tileList) {
        Collections.shuffle(tileList, Randomizer.getRandom());

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == null) {
                    Tile tile = tileList.remove(0);
                    board[row][col] = tile;
                    tileLocations.put(tile, new Location(row, col));
                }
            }
        }
    }
}
