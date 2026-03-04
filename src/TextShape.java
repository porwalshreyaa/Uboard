
// FILE: TextShape.java
import java.awt.*;
import java.awt.geom.*;

public class TextShape extends ShapeBase {
    private static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final FontMetrics TEXT_METRICS =
            Toolkit.getDefaultToolkit().getFontMetrics(TEXT_FONT);

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
        g2.setFont(TEXT_FONT);
        g2.drawString(text, (float) pos.x, (float) pos.y);
    }

    @Override
    public boolean contains(Point2D.Double p) {
        Rectangle2D r = new Rectangle2D.Double(
                pos.x,
                pos.y - TEXT_METRICS.getAscent(),
                TEXT_METRICS.stringWidth(text),
                TEXT_METRICS.getHeight());
        return r.contains(p);
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(
                pos.x,
                pos.y - TEXT_METRICS.getAscent(),
                TEXT_METRICS.stringWidth(text),
                TEXT_METRICS.getHeight());
    }

    @Override
    public void setEnd(Point2D.Double p) {
        /* N/A */ }

    @Override
    public boolean isNear(Point2D.Double p, double radius) {
        return contains(p);
    }
}
