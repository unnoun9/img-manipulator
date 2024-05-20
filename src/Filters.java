import java.awt.*;
import java.awt.image.BufferedImage;

class Filters
{
    static BufferedImage gray_scale(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.33 * r + 0.56 * g + 0.11 * b); // or  (r + g + b) / 3
                int gray_rgb = (gray << 16) | (gray << 8) | gray;
                copy.setRGB(x, y, gray_rgb);
            }
        }

        return copy;
    }

    static BufferedImage invert(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int inverted_rgb = ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
                copy.setRGB(x, y, inverted_rgb);
            }
        }

        return copy;
    }

    static BufferedImage sepia(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int sepia_r = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int sepia_g = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int sepia_b = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                sepia_r = Math.min(sepia_r, 255);
                sepia_g = Math.min(sepia_g, 255);
                sepia_b = Math.min(sepia_b, 255);
                int sepia_rgb = (sepia_r << 16) | (sepia_g << 8) | sepia_b;
                copy.setRGB(x, y, sepia_rgb);
            }
        }

        return copy;
    }

    static BufferedImage box_blur(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int sum_r = 0; int sum_g = 0; int sum_b = 0;
                int count = 0;

                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        int neighbor_x = x + i;
                        int neighbor_y = y + j;
                        if (neighbor_x >= 0 && neighbor_x < copy.getWidth() && neighbor_y >= 0 && neighbor_y < copy.getHeight())
                        {
                            int rgb = img.getRGB(neighbor_x, neighbor_y);
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;
                            sum_r += r;
                            sum_g += g;
                            sum_b += b;
                            count++;
                        }
                    }
                }

                int avg_r = sum_r / count;
                int avg_g = sum_g / count;
                int avg_b = sum_b / count;
                int blurred_rgb = (avg_r << 16) | (avg_g << 8) | avg_b;
                copy.setRGB(x, y, blurred_rgb);
            }
        }
        return copy;
    }

    static BufferedImage edge_detection(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int current_rgb = img.getRGB(x, y);
                int current_r = (current_rgb >> 16) & 0xFF;
                int current_g = (current_rgb >> 8) & 0xFF;
                int current_b = current_rgb & 0xFF;

                int neighbor_rgb = 0;
                int neighbor_r = 0;
                int neighbor_g = 0;
                int neighbor_b = 0;

                // Calculate the average difference between the current pixel and its neighbors
                int difference_sum = 0;
                int neighbor_count = 0;

                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        int neighbor_x = x + i;
                        int neighbor_y = y + j;
                        if (neighbor_x >= 0 && neighbor_x < img.getWidth() && neighbor_y >= 0 && neighbor_y < img.getHeight())
                        {
                            neighbor_rgb = img.getRGB(neighbor_x, neighbor_y);
                            neighbor_r = (neighbor_rgb >> 16) & 0xFF;
                            neighbor_g = (neighbor_rgb >> 8) & 0xFF;
                            neighbor_b = neighbor_rgb & 0xFF;

                            int difference_r = Math.abs(current_r - neighbor_r);
                            int difference_g = Math.abs(current_g - neighbor_g);
                            int difference_b = Math.abs(current_b - neighbor_b);

                            int difference = (difference_r + difference_g + difference_b) / 3;
                            difference_sum += difference;
                            neighbor_count++;
                        }
                    }
                }

                // Calculate the average difference
                int averageDifference = difference_sum / neighbor_count;

                // Set the pixel to black if the average difference is above a threshold, otherwise set it to white
                int edgeRGB = averageDifference > 30 ? 0x000000 : 0xFFFFFF;
                copy.setRGB(x, y, edgeRGB);
            }
        }
        return copy;
    }

    static BufferedImage edge_detection_v2(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);

        int[][] Gx = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };

        int[][] Gy = {
            {-1, -2, -1},
            { 0,  0,  0},
            { 1,  2,  1}
        };

        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                float gxr = 0.0f, gxg = 0.0f, gxb = 0.0f;
                float gyr = 0.0f, gyg = 0.0f, gyb = 0.0f;

                for (int i = y - 1; i <= y + 1; i++)
                {
                    for (int j = x - 1; j <= x + 1; j++)
                    {
                        if (i >= 0 && i < img.getHeight() && j >= 0 && j < img.getWidth())
                        {
                            int rgb = img.getRGB(j, i);
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;
                            
                            gxr += Gx[i - y + 1][j - x + 1] * r;
                            gxg += Gx[i - y + 1][j - x + 1] * g;
                            gxb += Gx[i - y + 1][j - x + 1] * b;

                            gyr += Gy[i - y + 1][j - x + 1] * r;
                            gxg += Gy[i - y + 1][j - x + 1] * g;
                            gyb += Gy[i - y + 1][j - x + 1] * b;
                        }
                    }
                }

                float gzr = Math.min((float) Math.sqrt(gxr * gxr + gyr * gyr), 255);
                float gzg = Math.min((float) Math.sqrt(gxg * gxg + gyg * gyg), 255);
                float gzb = Math.min((float) Math.sqrt(gxb * gxb + gyb * gyb), 255);

                int grgb = ((int) gzr << 16) | ((int) gzg << 8) | (int) gzb;
                copy.setRGB(x, y, grgb);
            }
        }
        return copy;
    }

    static BufferedImage reflect_horizontally(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                copy.setRGB(copy.getWidth() - x - 1, y, rgb);
            }
        }
        return copy;
    }

    static BufferedImage reflect_vertically(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                copy.setRGB(x, copy.getHeight() - y - 1, rgb);
            }
        }
        return copy;
    }

    // Resizes the image
    static BufferedImage resize_image(BufferedImage original_image, int type, int IMG_WIDTH, int IMG_HEIGHT)
    {
        BufferedImage resized_image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resized_image.createGraphics();
        g.drawImage(original_image, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
    
        return resized_image;
    }
    
}
