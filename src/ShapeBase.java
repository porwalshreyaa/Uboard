// FILE: ShapeBase.java
import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;

public abstract class ShapeBase implements Serializable {
    Color color;
    float stroke;
    Tool type;

    public ShapeBase(Color color, float stroke, Tool type) {
        this.color = color;
        this.stroke = stroke;
        this.type = type;
    }

    public abstract void draw(Graphics2D g2);
    public abstract boolean contains(Point2D.Double p);
    public abstract Rectangle2D getBounds();
    public abstract void setEnd(Point2D.Double p); // for shapes with end point
    public abstract boolean isNear(Point2D.Double p, double radius);
    public abstract void moveTo(double dx, double dy);
}