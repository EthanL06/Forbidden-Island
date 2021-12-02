package graphics.util;

import graphics.panels.helper.CardButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ButtonFactory {


    private static void buttonConfig(JButton button) {
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(null);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton create(String imagePath, String pressedImagePath, int newWidth, int newHeight) {
        JButton button = new JButton();
        ImageIcon buttonImg = ImageCreate.get(imagePath);
        ImageIcon pressedImg = ImageCreate.get(pressedImagePath);

        buttonImg = ImageScaler.scale(buttonImg, newWidth, newHeight);
        pressedImg = ImageScaler.scale(pressedImg, newWidth, newHeight);

        button.setIcon(buttonImg);
        button.setPressedIcon(pressedImg);
//        button.setOpaque(false);
//        button.setFocusPainted(false);
//        button.setContentAreaFilled(false);
//        button.setBorderPainted(false);
//        button.setBorder(null);

        buttonConfig(button);

        return button;
    }

    public static JButton create(String imagePath, String pressedImagePath, String selectedImagePath, int newWidth, int newHeight) {
        JButton button = new JButton();

        ImageIcon buttonImg = ImageCreate.get(imagePath);
        ImageIcon pressedImg = ImageCreate.get(pressedImagePath);
        ImageIcon selectedImg = ImageCreate.get(selectedImagePath);


        buttonImg = ImageScaler.scale(buttonImg, newWidth, newHeight);
        pressedImg = ImageScaler.scale(pressedImg, newWidth, newHeight);
        selectedImg = ImageScaler.scale(selectedImg, newWidth, newHeight);

        button.setIcon(buttonImg);
        button.setPressedIcon(pressedImg);
        button.setSelectedIcon(selectedImg);

        buttonConfig(button);

        return button;
    }

    public static JButton create(ImageIcon normalImage, ImageIcon pressedImage, ImageIcon selectedImage) {
        JButton button = new JButton();
        button.setIcon(normalImage);
        button.setPressedIcon(pressedImage);
        button.setSelectedIcon(selectedImage);
        // Use setEnabled if a button isn't supposed to be clicked
        //button.setEnabled(false);
        button.setDisabledIcon(normalImage);

        buttonConfig(button);

        return button;
    }

    public static CardButton createCardButton(ImageIcon normalImage, ImageIcon pressedImage, ImageIcon selectedImage) {
        CardButton button = new CardButton();
        button.setIcon(normalImage);
        button.setPressedIcon(pressedImage);
        button.setSelectedIcon(selectedImage);
        // Use setEnabled if a button isn't supposed to be clicked
        //button.setEnabled(false);
        button.setDisabledIcon(normalImage);

        buttonConfig(button);

        return button;
    }

    public static ImageIcon getImage(String path) {
        return ImageCreate.get(path);
    }
}
