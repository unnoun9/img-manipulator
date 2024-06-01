import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

class Image_Panel extends JPanel
{
    BufferedImage image;
    float zoom_level = 1.0f;

    Image_Panel(BufferedImage img)
    {
        image = img;
        setDoubleBuffered(true);
        setBackground(Color.DARK_GRAY);
    }

    void set_image(BufferedImage img)
    {
        image = img;
        revalidate();
        repaint();
    }

    void set_zoom_level(float zoom_lvl)
    {
        zoom_level = zoom_lvl;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Checker pattern
        // TODO - fix lag or stutter
        // int square_size = Math.max(1, (int)(16 * zoom_level));
        int width = getWidth(), height = getHeight();
        int size = 8;//Math.max(1, Math.min(8, (int)(8 / zoom_level)));

        for(int i = 0; i < height/size + 1; i++)
        {
            for(int j = 0; j < width/size + 1; j++)
            {
                if ((i + j) % 2 == 0) g.setColor(new Color(0xffffff));
                else g.setColor(new Color(125, 125, 125));

                g.fillRect(j * size, i * size, size, size);
            }
        }

        // Draw the image
        if (image != null)
        {
            int image_width = image.getWidth();
            int image_height = image.getHeight();

            // Calculate the center of the panel and the center of the image
            int panel_center_x = getWidth() / 2;
            int panel_center_y = getHeight() / 2;
            int image_center_x = (int) (image_width * zoom_level / 2);
            int image_center_y = (int) (image_height * zoom_level / 2);

            // Create an AffineTransform for the scaling
            AffineTransform at = new AffineTransform();
            
            // Translate the image to the center of the panel
            at.translate(panel_center_x - image_center_x, panel_center_y - image_center_y);
            at.scale(zoom_level, zoom_level);

            // Apply the transformation and draw the image
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.transform(at);
            g2d.drawImage(image, 0, 0, this);
        }
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize()
    {
        if (image == null)
        {
            return new Dimension(0, 0); // Default size
        } 
        else
        {
            int width = (int) (image.getWidth() * zoom_level);
            int height = (int) (image.getHeight() * zoom_level);
            return new Dimension(width, height);
        }
    }
}
