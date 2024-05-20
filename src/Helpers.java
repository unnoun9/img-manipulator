import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

class Helpers
{
    // Method to deep copy an image
    static BufferedImage copy_image(BufferedImage image) 
    {
        ColorModel cm = image.getColorModel();
        boolean is_alpha_prm = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, is_alpha_prm, null);
    }
    
    // Converts BufferedImage to Mat
    static Mat buffered_image_to_mat(BufferedImage image)
    {
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
    
    // Converts Mat to BufferedImage
    static BufferedImage mat_to_buffered_image(Mat mat)
    {
        byte[] data = new byte[mat.width() * mat.height() * (int) mat.elemSize()];
        mat.get(0, 0, data);
        int type;
        // Check number of channels
        switch (mat.channels())
        {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                // OpenCV uses BGR instead of RGB, so convert BGR to RGB
                type = BufferedImage.TYPE_3BYTE_BGR;
                byte temp;
                for (int i = 0; i < data.length; i += 3)
                {
                    temp = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = temp;
                }
            default:
                return null;
        }
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        image.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        return image;
    }

    // Resizes image icons
    static ImageIcon resize_icon(ImageIcon icon, int width, int height)
    {
        Image img = icon.getImage();
        Image resized_image = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resized_image);
    }

    // Print the structure of a component
    static void printComponentStructure(Component comp, int indent)
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print("  ");
        }
        System.out.println(comp.getClass().getName());
        if (comp instanceof Container)
        {
            for (Component child : ((Container) comp).getComponents())
            {
                printComponentStructure(child, indent + 1);
            }
        }
    }

    // Checks if two colors are close
    static boolean are_colors_tolerant(int color1, int color2, int tolerance)
    {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
    
        int r_diff = Math.abs(r1 - r2);
        int g_diff = Math.abs(g1 - g2);
        int b_diff = Math.abs(b1 - b2);
    
        // Calculate the square of the distance and compare it with the square of the tolerance
        int distance_squared = r_diff * r_diff + g_diff * g_diff + b_diff * b_diff;
        return distance_squared <= tolerance * tolerance;
    }
}

