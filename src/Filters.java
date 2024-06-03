import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

class Filters
{ 
    static BufferedImage brighten(BufferedImage img, float factor)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (int) (((argb >> 16) & 0xFF) + 255 * factor);
                int g = (int) (((argb >> 8) & 0xFF) + 255 * factor);
                int b = (int) ((argb & 0xFF) + 255 * factor);

                if (r > 255) r = 255;
                if (g > 255) g = 255;
                if (b > 255) b = 255;
                if (r < 0) r = 0;
                if (g < 0) g = 0;
                if (b < 0) b = 0;

                int brightened_rgb = (a << 24) | (r << 16) | (g << 8) | b;
                copy.setRGB(x, y, brightened_rgb);
            }
        }
        return copy;
    }

    static BufferedImage gray_scale(BufferedImage img)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int gray = (int) (0.33 * r + 0.56 * g + 0.11 * b); // or  (r + g + b) / 3
                int gray_rgb = (a << 24) | (gray << 16) | (gray << 8) | gray;
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
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int inverted_rgb = (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
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
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int sepia_r = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int sepia_g = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int sepia_b = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                sepia_r = Math.min(sepia_r, 255);
                sepia_g = Math.min(sepia_g, 255);
                sepia_b = Math.min(sepia_b, 255);
                int sepia_rgb = (a << 24) | (sepia_r << 16) | (sepia_g << 8) | sepia_b;
                copy.setRGB(x, y, sepia_rgb);
            }
        }

        return copy;
    }

    static BufferedImage box_blur(BufferedImage img, int radius)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int sum_r = 0; int sum_g = 0; int sum_b = 0;
                int count = 0;
                int a = (img.getRGB(x, y) >> 24) & 0xFF;

                for (int i = -radius; i <= radius; i++)
                {
                    for (int j = -radius; j <= radius; j++)
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
                int blurred_rgb = (a << 24) | (avg_r << 16) | (avg_g << 8) | avg_b;
                copy.setRGB(x, y, blurred_rgb);
            }
        }
        return copy;
    }

    static BufferedImage fast_box_blur(BufferedImage img, int radius)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");
    
        BufferedImage copy = Helpers.copy_image(img);
        int width = img.getWidth();
        int height = img.getHeight();
    
        // Horizontal pass
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int sum_r = 0, sum_g = 0, sum_b = 0, count = 0;
                int a = (img.getRGB(x, y) >> 24) & 0xFF;
                for (int i = -radius; i <= radius; i++)
                {
                    int neighbor_x = x + i;
                    if (neighbor_x >= 0 && neighbor_x < width)
                    {
                        int rgb = img.getRGB(neighbor_x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        sum_r += r;
                        sum_g += g;
                        sum_b += b;
                        count++;
                    }
                }
                int avg_r = sum_r / count;
                int avg_g = sum_g / count;
                int avg_b = sum_b / count;
                int blurred_rgb = (a << 24) | (avg_r << 16) | (avg_g << 8) | avg_b;
                copy.setRGB(x, y, blurred_rgb);
            }
        }
    
        // Vertical pass
        BufferedImage final_img = Helpers.copy_image(copy);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int sum_r = 0, sum_g = 0, sum_b = 0, count = 0;
                int a = (copy.getRGB(x, y) >> 24) & 0xFF;
                for (int i = -radius; i <= radius; i++)
                {
                    int neighbor_y = y + i;
                    if (neighbor_y >= 0 && neighbor_y < height)
                    {
                        int rgb = copy.getRGB(x, neighbor_y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        sum_r += r;
                        sum_g += g;
                        sum_b += b;
                        count++;
                    }
                }
                int avg_r = sum_r / count;
                int avg_g = sum_g / count;
                int avg_b = sum_b / count;
                int blurred_rgb = (a << 24) | (avg_r << 16) | (avg_g << 8) | avg_b;
                final_img.setRGB(x, y, blurred_rgb);
            }
        }
    
        return final_img;
    }

    static BufferedImage gaussian_blur(BufferedImage img, int radius)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        float[][] kernal = generate_gaussian_kernel(radius);
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                float sum_r = 0.0f, sum_g = 0.0f, sum_b = 0.0f;
                int a = (img.getRGB(x, y) >> 24) & 0xFF;

                for (int j = -radius; j <= radius; j++)
                {
                    for (int i = -radius; i <= radius; i++)
                    {
                        int img_x = Math.min(Math.max(x + i, 0), width - 1);
                        int img_y = Math.min(Math.max(y + j, 0), height - 1);
                        int rgb = img.getRGB(img_x, img_y);

                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        sum_r += kernal[j + radius][i + radius] * r;
                        sum_g += kernal[j + radius][i + radius] * g;
                        sum_b += kernal[j + radius][i + radius] * b;
                    }
                }

                int blurred_rgb = (a << 24) | ((int) sum_r << 16) | ((int) sum_g << 8) | (int) sum_b;
                copy.setRGB(x, y, blurred_rgb);
            }
        }
        return copy;
    }

    static float[][] generate_gaussian_kernel(int radius)
    {
        int size = 2 * radius + 1;
        float[][] kernel = new float[size][size];
        float sigma = Math.max(1, radius/2.0f);
        float sum = 0.0f;

        for (int y = -radius; y <= radius; y++)
        {
            for (int x = -radius; x <= radius; x++)
            {
                float value = (float) (Math.exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma));
                kernel[y + radius][x + radius] = value;
                sum += value;
            }
        }

        // Normalize the kernel to ensure the sum is 1
        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                kernel[y][x] /= sum;
            }
        }

        return kernel;
    }

    static BufferedImage fast_gaussian_blur(BufferedImage img, int radius)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage copy = new BufferedImage(width, height, img.getType());

        float[] kernel = generate_1d_gaussian_kernel(radius);
        int kernel_size = kernel.length;
        int half_kernel = kernel_size / 2;

        int[] pixels = new int[width * height];
        int[] temp_pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);

        // Horizontal pass
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                float sum_r = 0, sum_g = 0, sum_b = 0;
                float sum_weights = 0;

                for (int k = -half_kernel; k <= half_kernel; k++)
                {
                    int pixel_x = Math.max(0, Math.min(width - 1, x + k));
                    int pixel = pixels[y * width + pixel_x];
                    float weight = kernel[k + half_kernel];

                    sum_r += weight * ((pixel >> 16) & 0xFF);
                    sum_g += weight * ((pixel >> 8) & 0xFF);
                    sum_b += weight * (pixel & 0xFF);
                    sum_weights += weight;
                }

                int r = Math.min(255, Math.max(0, Math.round(sum_r / sum_weights)));
                int g = Math.min(255, Math.max(0, Math.round(sum_g / sum_weights)));
                int b = Math.min(255, Math.max(0, Math.round(sum_b / sum_weights)));
                int a = (pixels[y * width + x] >> 24) & 0xFF;

                temp_pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }

        // Vertical pass
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                float sum_r = 0, sum_g = 0, sum_b = 0;
                float sum_weights = 0;

                for (int k = -half_kernel; k <= half_kernel; k++)
                {
                    int pixel_y = Math.max(0, Math.min(height - 1, y + k));
                    int pixel = temp_pixels[pixel_y * width + x];
                    float weight = kernel[k + half_kernel];

                    sum_r += weight * ((pixel >> 16) & 0xFF);
                    sum_g += weight * ((pixel >> 8) & 0xFF);
                    sum_b += weight * (pixel & 0xFF);
                    sum_weights += weight;
                }

                int r = Math.min(255, Math.max(0, Math.round(sum_r / sum_weights)));
                int g = Math.min(255, Math.max(0, Math.round(sum_g / sum_weights)));
                int b = Math.min(255, Math.max(0, Math.round(sum_b / sum_weights)));
                int a = (temp_pixels[y * width + x] >> 24) & 0xFF;

                copy.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return copy;
    }

    static float[] generate_1d_gaussian_kernel(int radius)
    {
        int size = 2 * radius + 1;
        float[] kernel = new float[size];
        float sigma = radius / 2.0f;
        float sum = 0.0f;

        for (int i = 0; i < size; i++)
        {
            int x = i - radius;
            kernel[i] = (float) Math.exp(-(x * x) / (2 * sigma * sigma));
            sum += kernel[i];
        }

        for (int i = 0; i < size; i++)
        {
            kernel[i] /= sum;
        }

        return kernel;
    }

    static BufferedImage pixelate(BufferedImage img, int size)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        BufferedImage copy = Helpers.copy_image(img);
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y += size)
        {
            for (int x = 0; x < width; x += size)
            {
                int sum_a = 0, sum_r = 0, sum_g = 0, sum_b = 0;
                int count = 0;

                // Calculate average color in the block of size 'size'
                for (int j = 0; j < size; j++)
                {
                    for (int i = 0; i < size; i++)
                    {
                        int img_x = x + i;
                        int img_y = y + j;
                        // Check bounds only once per pixel access
                        if (img_x >= width || img_y >= height) continue;

                        int rgb = img.getRGB(img_x, img_y);
                        int a = (rgb >> 24) & 0xFF;
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        sum_a += a;
                        sum_r += r;
                        sum_g += g;
                        sum_b += b;
                        count++;
                    }
                }

                int avg_a = sum_a / count;
                int avg_r = sum_r / count;
                int avg_g = sum_g / count;
                int avg_b = sum_b / count;
                int pixelated_rgb = (avg_a << 24) | (avg_r << 16) | (avg_g << 8) | avg_b;

                // Apply the average color of the block to the block
                for (int j = 0; j < size; j++)
                {
                    for (int i = 0; i < size; i++)
                    {
                        int img_x = x + i;
                        int img_y = y + j;
                        // Check bounds only once per pixel access
                        if (img_x >= width || img_y >= height) continue;

                        copy.setRGB(img_x, img_y, pixelated_rgb);
                    }
                }
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
                int a = (img.getRGB(x, y) >> 24) & 0xFF;

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

                int grgb = (a << 24) | ((int) gzr << 16) | ((int) gzg << 8) | (int) gzb;
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

    // TODO - Rotation currently either increases the canvas size, or cuts the image off. Fix this.
    // static BufferedImage rotate(BufferedImage img, float angle)
    // {
    //     if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

    //     int width = img.getWidth();
    //     int height = img.getHeight();
    //     float radians = (float) Math.toRadians(angle);

    //     // Calculate the dimensions of the new image
    //     int new_width = (int) Math.round(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
    //     int new_height = (int) Math.round(Math.abs(width * Math.sin(radians)) + Math.abs(height * Math.cos(radians)));

    //     BufferedImage rotated_image = new BufferedImage(new_width, new_height, img.getType());

    //     // Calculate the center of the image
    //     int cx = width / 2;
    //     int cy = height / 2;

    //     // Calculate the center of the new image
    //     int new_cx = new_width / 2;
    //     int new_cy = new_height / 2;

    //     for (int y = 0; y < new_height; y++)
    //     {
    //         for (int x = 0; x < new_width; x++)
    //         {
    //             // Calculate the coordinates in the original image
    //             int original_x = (int) ((x - new_cx) * Math.cos(radians) - (y - new_cy) * Math.sin(radians) + cx);
    //             int original_y = (int) ((x - new_cx) * Math.sin(radians) + (y - new_cy) * Math.cos(radians) + cy);

    //             if (original_x >= 0 && original_x < width && original_y >= 0 && original_y < height)
    //             {
    //                 int rgb = img.getRGB(original_x, original_y);
    //                 rotated_image.setRGB(x, y, rgb);
    //             }
    //             else
    //             {
    //                 // Set the pixel to a transparent color if it is out of bounds
    //                 rotated_image.setRGB(x, y, new Color(255, 255, 255, 0).getRGB());
    //             }
    //         }
    //     }

    //     return rotated_image;
    // }

    // static BufferedImage rotate(BufferedImage img, double angle)
    // {
    //     if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

    //     int width = img.getWidth();
    //     int height = img.getHeight();
    //     double radians = Math.toRadians(angle);

    //     BufferedImage rotated_image = new BufferedImage(width, height, img.getType());

    //     // Calculate the center of the image
    //     int cx = width / 2;
    //     int cy = height / 2;

    //     for (int y = 0; y < height; y++)
    //     {
    //         for (int x = 0; x < width; x++)
    //         {
    //             // Calculate the coordinates in the original image
    //             int original_x = (int) ((x - cx) * Math.cos(radians) + (y - cy) * Math.sin(radians) + cx);
    //             int original_y = (int) (-(x - cx) * Math.sin(radians) + (y - cy) * Math.cos(radians) + cy);

    //             if (original_x >= 0 && original_x < width && original_y >= 0 && original_y < height)
    //             {
    //                 int rgb = img.getRGB(original_x, original_y);
    //                 rotated_image.setRGB(x, y, rgb);
    //             }
    //             else
    //             {
    //                 // Set the pixel to a transparent color if it is out of bounds
    //                 rotated_image.setRGB(x, y, new Color(255, 255, 255, 0).getRGB());
    //             }
    //         }
    //     }

    //     return rotated_image;
    // }

    static BufferedImage rotate(BufferedImage img, double angle)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        int width = img.getWidth();
        int height = img.getHeight();
        double radians = -Math.toRadians(angle);

        // Create an off-screen image buffer that can hold the entire rotated image
        BufferedImage rotated_image = new BufferedImage(width, height, img.getType());

        // Create a Graphics2D object to draw the rotated image
        Graphics2D g2d = rotated_image.createGraphics();

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Calculate the transform for rotation around the center
        AffineTransform transform = new AffineTransform();
        transform.translate(width / 2, height / 2);
        transform.rotate(radians);
        transform.translate(-width / 2, -height / 2);

        // Draw the original image onto the rotated image using the transform
        g2d.drawImage(img, transform, null);
        g2d.dispose();

        return rotated_image;
    }

    static BufferedImage resize(BufferedImage img, float scale)
    {
        int new_width = (int) (img.getWidth() * scale);
        int new_height = (int) (img.getHeight() * scale);
        return resize(img, new_width, new_height);
    }

    static BufferedImage resize(BufferedImage img, int new_width, int new_height)
    {
        if (img == null) throw new IllegalArgumentException("Filter cannot be applied to a null image");

        int original_width = img.getWidth();
        int original_height = img.getHeight();
        BufferedImage resized_image = new BufferedImage(new_width, new_height, img.getType());

        float x_ratio = (float) original_width / new_width;
        float y_ratio = (float) original_height / new_height;

        for (int y = 0; y < new_height; y++)
        {
            for (int x = 0; x < new_width; x++)
            {
                int px = (int) (x * x_ratio);
                int py = (int) (y * y_ratio);

                int px1 = Math.min(px + 1, original_width - 1);
                int py1 = Math.min(py + 1, original_height - 1);

                float x_diff = (x * x_ratio) - px;
                float y_diff = (y * y_ratio) - py;

                int rgb_tl = img.getRGB(px, py); // Top-left
                int rgb_tr = img.getRGB(px1, py); // Top-right
                int rgb_bl = img.getRGB(px, py1); // Bottom-left
                int rgb_br = img.getRGB(px1, py1); // Bottom-right

                int a = bilinear_interpolate(
                    (rgb_tl >> 24) & 0xFF, (rgb_tr >> 24) & 0xFF,
                    (rgb_bl >> 24) & 0xFF, (rgb_br >> 24) & 0xFF,
                    x_diff, y_diff
                );

                int r = bilinear_interpolate(
                    (rgb_tl >> 16) & 0xFF, (rgb_tr >> 16) & 0xFF,
                    (rgb_bl >> 16) & 0xFF, (rgb_br >> 16) & 0xFF,
                    x_diff, y_diff
                );

                int g = bilinear_interpolate(
                    (rgb_tl >> 8) & 0xFF, (rgb_tr >> 8) & 0xFF,
                    (rgb_bl >> 8) & 0xFF, (rgb_br >> 8) & 0xFF,
                    x_diff, y_diff
                );

                int b = bilinear_interpolate(
                    rgb_tl & 0xFF, rgb_tr & 0xFF,
                    rgb_bl & 0xFF, rgb_br & 0xFF,
                    x_diff, y_diff
                );

                int rgb = (a << 24) | (r << 16) | (g << 8) | b;
                resized_image.setRGB(x, y, rgb);
            }
        }
        return resized_image;
    }

    static int bilinear_interpolate(int tl, int tr, int bl, int br, float x_diff, float y_diff)
    {
        // Interpolation between A and B is B*t + A(1-t) = A + (B-A)*t
        // Interpolate between the top two pixels
        float top = tl + x_diff * (tr - tl);
        // Interpolate between the bottom two pixels
        float bottom = bl + x_diff * (br - bl);
        // Interpolate between the top and bottom
        return (int) (top + y_diff * (bottom - top));
    }

    static BufferedImage resize_image(BufferedImage original_image, int type, int IMG_WIDTH, int IMG_HEIGHT)
    {
        BufferedImage resized_image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resized_image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original_image, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
    
        return resized_image;
    }
    
}