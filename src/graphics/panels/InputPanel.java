package graphics.panels;

import game.enums.Difficulty;
import graphics.Window;
import graphics.util.ButtonFactory;
import graphics.util.CenteredPosition;
import graphics.util.ImageCreate;
import graphics.util.ImageScaler;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class InputPanel extends JPanel {

    private JFormattedTextField seedInput;
    private JComboBox<String> playerInput;
    private JComboBox<String> difficultyInput;
    private JButton startButton;

    public InputPanel() {
        setLayout(null);
        RootPanel.addPanel("input", this);

        int y = 30;
        int inputGap = 100;

        CompoundBorder border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
        border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(171, 171, 171), 2), border);
        Font font = new Font("Roboto", Font.BOLD, 15);

        JLabel title = new JLabel(ImageCreate.get("/images/text/game-setup.png"));
        Dimension position = CenteredPosition.getPosition(title.getIcon().getIconWidth(), title.getIcon().getIconHeight());

        title.setBounds(position.width, y, title.getIcon().getIconWidth(), title.getIcon().getIconHeight());
        add(title);

        y += title.getHeight();

        JLabel seedLabel = new JLabel(ImageCreate.get("/images/text/game-seed.png"));
        position = CenteredPosition.getPosition(seedLabel.getIcon().getIconWidth(), seedLabel.getIcon().getIconHeight());
        seedLabel.setBounds(position.width, y, seedLabel.getIcon().getIconWidth(), seedLabel.getIcon().getIconHeight());
        add(seedLabel);

        y += seedLabel.getHeight();

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Long.class);
        formatter.setMinimum(Long.MIN_VALUE);
        formatter.setMaximum(Long.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);

        seedInput = new JFormattedTextField(formatter);
        seedInput.setFont(font);
        seedInput.setBounds(seedLabel.getX() + 10, y, seedLabel.getWidth()-19, 30);
        seedInput.setBorder(border);
        add(seedInput);

        y += inputGap;

        JLabel playersLabel = new JLabel(ImageCreate.get("/images/text/players.png"));
        position = CenteredPosition.getPosition(playersLabel.getIcon().getIconWidth(), playersLabel.getIcon().getIconHeight());
        playersLabel.setBounds(position.width, y, playersLabel.getIcon().getIconWidth(), playersLabel.getIcon().getIconHeight());
        add(playersLabel);

        y += playersLabel.getHeight();

        String[] choices = {"2 PLAYERS", "3 PLAYERS", "4 PLAYERS"};
        playerInput = new JComboBox<>(choices);
        playerInput.setBounds(CenteredPosition.getPosition(120, 50).width, y, 120, 50);
        playerInput.setBorder(border);
        playerInput.setFont(font);
        add(playerInput);

        y += inputGap;

        JLabel difficultyLabel = new JLabel(ImageCreate.get("/images/text/difficulty.png"));
        position = CenteredPosition.getPosition(difficultyLabel.getIcon().getIconWidth(), difficultyLabel.getIcon().getIconHeight());
        difficultyLabel.setBounds(position.width, y, difficultyLabel.getIcon().getIconWidth(), difficultyLabel.getIcon().getIconHeight());
        add(difficultyLabel);

        y += difficultyLabel.getHeight();

        choices = new String[]{"NOVICE", "NORMAL", "ELITE", "LEGENDARY"};
        difficultyInput = new JComboBox<>(choices);
        difficultyInput.setBounds(CenteredPosition.getPosition(120, 50).width, y, 120, 50);
        difficultyInput.setBorder(border);
        difficultyInput.setFont(font);
        add(difficultyInput);

        y += inputGap;

        startButton = ButtonFactory.create("/images/buttons/title/start.png", "/images/buttons/title/start-pressed.png", 200, 100);
        position = CenteredPosition.getPosition(startButton.getIcon().getIconWidth(), startButton.getIcon().getIconHeight());
        startButton.setBounds(position.width, y, 200, 100);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (seedInput.getText().isBlank()) {
                    JOptionPane.showMessageDialog(null, "Enter a seed!", "Seed Parameter Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                long seed = Long.parseLong(seedInput.getText().replaceAll(",", ""));
                int numOfPlayers = Integer.parseInt(("" + playerInput.getSelectedItem()).substring(0, 1));
                Difficulty difficulty = Difficulty.valueOf("" + difficultyInput.getSelectedItem());

                System.out.println("SEED: " + seed + "\nPLAYERS: " + numOfPlayers + "\nDIFFICULTY: " + difficulty.toString());

                RootPanel.addPanel("loading", new LoadingPanel(seed, numOfPlayers, difficulty)); // input here for debugging purposes
                RootPanel.switchPanel("loading");
            }
        });

        add(startButton);


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ImageIcon background = ImageCreate.get("/images/background-blurred.png");
        background = ImageScaler.scale(background, graphics.Window.width, Window.height);
        g.drawImage(background.getImage(), 0, 0, null);
    }
}
