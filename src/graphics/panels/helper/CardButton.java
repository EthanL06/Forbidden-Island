package graphics.panels.helper;

import javax.swing.*;

// Associates each card JButton with a Card object

public class CardButton extends JButton {

    private Object card;

    public CardButton() {
        card = null;
    }

    public CardButton(Object card) {
        this.card = card;
    }

    public void setCard(Object card) {
        this.card = card;
    }

    public Object getCard() {
        return card;
    }

    public String toString() {
        return card.toString();
    }

}
