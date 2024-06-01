import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

class Tools
{
    static Point last_mouse_pos = null;

    // Brush tool
    static void use_brush(BufferedImage img, float zoom_level, int color, int x, int y)
    {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(color));
        g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        
        if (last_mouse_pos != null)
        {
            int last_x = (int) (last_mouse_pos.getX() / zoom_level);
            int last_y = (int) (last_mouse_pos.getY() / zoom_level);
            if (last_x >= 0 && last_x < img.getWidth() && last_y >= 0 && last_y < img.getHeight())
            {
                g.drawLine(last_x, last_y, x, y);
            }
        }
        g.dispose();
    }

    // Eraser tool
    static void use_eraser(BufferedImage img, float zoom_level, int x, int y)
    {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x00000000, true)); // Set color to background color
        g.setStroke(new BasicStroke(50, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setComposite(AlphaComposite.Src); // Use SRC rule

        if (last_mouse_pos != null)
        {
            int last_x = (int) (last_mouse_pos.getX() / zoom_level);
            int last_y = (int) (last_mouse_pos.getY() / zoom_level);
            if (last_x >= 0 && last_x < img.getWidth() && last_y >= 0 && last_y < img.getHeight())
            {
                g.drawLine(last_x, last_y, x, y);
            }
        }
        g.dispose();
    }

    // Fill tool
    static void use_fill(BufferedImage img, int new_color, int x, int y)
    {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return;

        int tolerance = 30;
        int src_color = img.getRGB(x, y);
        flood_fill(img, x, y, src_color, new_color, tolerance);
    }

    // Helper function that implements the flood fill algorithm
    static void flood_fill(BufferedImage img, int x, int y, int src_color, int new_color, int tolerance)
    {
        if (Helpers.are_colors_tolerant(src_color, new_color, tolerance)) return;

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty())
        {
            Point p = stack.pop();
            if (p.x < 0 || p.x >= img.getWidth() || p.y < 0 || p.y >= img.getHeight()) continue;
            if (!Helpers.are_colors_tolerant(img.getRGB(p.x, p.y), src_color, tolerance)) continue;

            img.setRGB(p.x, p.y, new_color);

            // Check each neighbor to avoid unnecessary stack operations
            if (p.x + 1 < img.getWidth() && Helpers.are_colors_tolerant(img.getRGB(p.x + 1, p.y), src_color, tolerance))
                stack.push(new Point(p.x + 1, p.y));
            if (p.x - 1 >= 0 && Helpers.are_colors_tolerant(img.getRGB(p.x - 1, p.y), src_color, tolerance))
                stack.push(new Point(p.x - 1, p.y));
            if (p.y + 1 < img.getHeight() && Helpers.are_colors_tolerant(img.getRGB(p.x, p.y + 1), src_color, tolerance))
                stack.push(new Point(p.x, p.y + 1));
            if (p.y - 1 >= 0 && Helpers.are_colors_tolerant(img.getRGB(p.x, p.y - 1), src_color, tolerance))
                stack.push(new Point(p.x, p.y - 1));
        }
    }

}