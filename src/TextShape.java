
// FILE: TextShape.java
import java.awt.*;
import java.awt.geom.*;

public class TextShape extends ShapeBase {
    Point2D.Double pos;
    String text;

    public TextShape(Color color, float stroke, Point2D.Double pos, String text) {
        super(color, stroke, Tool.TEXT);
        this.pos = pos;
        this.text = text;
    }

    @Override
    public void moveTo(double x, double y) {
        pos.x = x;
        pos.y = y;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // small jitter to look hand-drawn
        double jitterX = (Math.random() - 0.5) * 0.8;
        double jitterY = (Math.random() - 0.5) * 0.8;
        g2.drawString(text, (float) (pos.x + jitterX), (float) (pos.y + jitterY));
    }

    @Override
    public boolean contains(Point2D.Double p) {
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(new Font("Segoe UI", Font.PLAIN, 16));
        Rectangle2D r = new Rectangle2D.Double(pos.x, pos.y - fm.getAscent(), fm.stringWidth(text), fm.getHeight());
        return r.contains(p);
    }

    @Override
    public Rectangle2D getBounds() {
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(new Font("Segoe UI", Font.PLAIN, 16));
        return new Rectangle2D.Double(pos.x, pos.y - fm.getAscent(), fm.stringWidth(text), fm.getHeight());
    }

    @Override
    public void setEnd(Point2D.Double p) {
        /* N/A */ }

    @Override
    public boolean isNear(Point2D.Double p, double radius) {
        return contains(p);
    }
}
