package io.anisthesie.ui.panels.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int arcWidth;
    private final int arcHeight;
    private final Color backgroundColor;

    public RoundedPanel(LayoutManager layout, int arcWidth, int arcHeight, Color bgColor) {
        super(layout);
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.backgroundColor = bgColor;
        setOpaque(false); 
    }

    public RoundedPanel(int arc, Color bgColor) {
        this(new BorderLayout(), arc, arc, bgColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
        g2.dispose();
        super.paintComponent(g);
    }
}
