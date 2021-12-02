package graphics.util;

import graphics.Window;

import java.awt.*;

public class CenteredPosition {

    public static Dimension getPosition(int width, int height) {
        int x = (Window.width -width)/2;
        int y = ((Window.height-height)/2);

        return new Dimension(x, y);
    }

    // Center inside a rectangle
    public static Dimension getPosition(Rectangle rect, int width, int height) {
        int x = rect.x + (rect.width/2) - (width/2);
        int y = rect.y + (rect.height/2) - (height/2);

        return new Dimension(x, y);
    }
}
