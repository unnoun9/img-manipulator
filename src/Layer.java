import java.awt.image.BufferedImage;

class Layer
{
    BufferedImage img;
    float opacity;
    String name = "Layer";
    int x = 0, y = 0; // Position

    Layer(BufferedImage img, String name)
    {
        opacity = 1.0f;
        this.img = img;
        this.name = name;
    }
}