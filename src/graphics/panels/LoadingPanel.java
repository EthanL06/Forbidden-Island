package graphics.panels;

import game.ForbiddenIsland;
import game.enums.Difficulty;
import graphics.Window;
import graphics.panels.RootPanel;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel {

    // https://www.youtube.com/watch?v=X5Q-Mecu_64
    // https://www.geeksforgeeks.org/swingworker-in-java/
    // SwingWorker allows display graphics and do background tasks on separate thread without blocking the EDT thread
    // which handles the graphics

    private final ForbiddenIsland game;

    public LoadingPanel(int seed, int players, Difficulty difficulty) {

        setLayout(null);
        game = new ForbiddenIsland(seed, players, difficulty);


        setLoadingGraphics();
        // Sets up the game in the background using SwingWorker
        gameSetup();
    }

    private void gameSetup() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Thread setUpThread = new Thread(game);
                setUpThread.setDaemon(true);
                setUpThread.start();
                setUpThread.join(); // Prevents return true until thread is finished
                return true;
            }

            @Override
            protected void done() {
                System.err.println("Done");
                startGame();
            }
        };

        worker.execute();
    }

    private void setLoadingGraphics() {

        JLabel loading = null;
        try {
            loading = new JLabel(ImageCreate.get("/images/loading.png"));
        } catch (Exception e) {
            System.out.println(e);
        }

        int width = loading.getPreferredSize().width;
        int height = loading.getPreferredSize().height;

        int x = CenteredPosition.getPosition(width, height).width;
        int y = CenteredPosition.getPosition(width, height).height;

        loading.setBounds(x, y - 200, width, height);
        add(loading);

        JLabel loadingDesc = null;
        try {
            loadingDesc = new JLabel(ImageCreate.get("/images/loading-desc.png"));
        } catch (Exception e) {
            System.out.println(e);
        }

        width = loading.getPreferredSize().width;
        height = loading.getPreferredSize().height;

        x = CenteredPosition.getPosition(width, height).width;
        y = CenteredPosition.getPosition(width, height).height;

        loadingDesc.setBounds(x, y + 200, width, height);
        add(loadingDesc);
    }

    private void startGame() {
        GamePanel gamePanel = new GamePanel(game);
        RootPanel.addPanel("game", gamePanel);
        RootPanel.switchPanel("game");
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ImageIcon icon = null;

        try {
            icon = ImageCreate.get("/images/background-blurred.png");
        } catch (Exception e) {
            System.out.println(e);
        }

        icon = ImageScaler.scale(icon, graphics.Window.width, Window.height);
        g.drawImage(icon.getImage(), 0, 0, null);
    }
}
