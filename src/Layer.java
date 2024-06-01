import java.awt.image.BufferedImage;

class Layer
{
    BufferedImage img;
    float opacity;
    String name = "Layer";

    Layer(BufferedImage img, String name)
    {
        opacity = 1.0f;
        this.img = img;
        this.name = name;
    }
}