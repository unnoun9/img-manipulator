import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;

class Image_Panel extends JPanel
{
    ArrayList<Layer> layers;
    float zoom_level = 1.0f;
    Point drag_start;
    int initial_layer_x, initial_layer_y;
    boolean is_dragging = false;

    Image_Panel(ArrayList<Layer> layers)
    {
        this.layers = layers;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (layers != null && !layers.isEmpty())
        {
            // Checker pattern
            int width = getWidth(), height = getHeight();
            int size = 8;
            for(int i = 0; i < height/size + 1; i++)
            {
                for(int j = 0; j < width/size + 1; j++)
                {
                    if ((i + j) % 2 == 0) g2d.setColor(new Color(0xffffff));
                    else g2d.setColor(new Color(125, 125, 125));

                    g2d.fillRect(j * size, i * size, size, size);
                }
            }

            // Draw the all image layers
            for (Layer layer: layers)
            {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.opacity));
                int w = (int) (layer.img.getWidth() * zoom_level);
                int h = (int) (layer.img.getHeight() * zoom_level);
                int x = (int) (layer.x * zoom_level);
                int y = (int) (layer.y * zoom_level);
                g2d.drawImage(layer.img, x, y, w, h, null);
            }
        }
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize()
    {
        if (layers != null && !layers.isEmpty())
        {
            int w = (int) (layers.get(0).img.getWidth() * zoom_level);
            int h = (int) (layers.get(0).img.getHeight() * zoom_level);
            return new Dimension(w, h);
        }
        return super.getPreferredSize();
    }
}