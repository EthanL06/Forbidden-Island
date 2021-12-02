package game.board;

public class Location {

    private int row;
    private int column;

    public Location(int row, int col) {
        this.row = row;
        this.column = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean up() {
        if (!isLegalIndex(row+1, column))
            return false;

        row++;
        return true;
    }

    public boolean down() {
        if (!isLegalIndex(row-1, column))
            return false;

        row--;
        return true;
    }

    public boolean right() {
        if (!isLegalIndex(row, column+1))
            return false;

        column++;
        return true;
    }

    public boolean left() {
        if (!isLegalIndex(row, column-1))
            return false;

        column--;
        return true;
    }

    // Returns true if location set to legal index
    public boolean setLocation(int row, int col) {
        if (!isLegalIndex(row, col))
            return false;

        this.row = row;
        this.column = col;
        return true;
    }

    public static boolean isLegalIndex(int r, int c) {
        if (r < 0 || r > 5 || c < 0 || c > 5) // Checks if index out of bounds
            return false;

        // Checks if the specified index is on an Empty tile
        if (r == 0 || r == 5) {
            return c != 0 && c != 1 && c != 4 && c != 5;
        } else if (r == 1 || r == 4) {
            return c != 0 && c != 5;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Location o = (Location) obj;
        return row == o.getRow() && column == o.getColumn();
    }


}
