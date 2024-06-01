import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.text.NumberFormat;
import java.util.ArrayList;

class New_Image_Dialog extends JDialog
{
    int image_width;
    int image_height;
    int image_type;
    int bg_color;
    int new_img_color_combobox_index = 0;

    JPanel configurations_panel;
    JFormattedTextField width_field;
    JFormattedTextField height_field;
    JComboBox<String> type_box;
    JComboBox<String> bg_color_box;

    JPanel button_panel;
    JButton create_button, cancel_button;
    boolean create_new_image = false;

    String[] bg_colors = {"Black", "White", "Transparent", "Custom"};
    String[] types = {"RGB", "Grayscale"};

    New_Image_Dialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        setSize(300, 210);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        create_UI();
    }

    void create_UI()
    {   
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        
        // Configurations Panel
        configurations_panel = new JPanel(new GridLayout(4, 2, 20, 15));

        // Width
        width_field = new JFormattedTextField(format);
        width_field.setValue(800);

        // Height
        height_field = new JFormattedTextField(format);
        height_field.setValue(600);

        // Don't let the user type in crazy things
        InputVerifier size_input_verifier = new InputVerifier() {
            @Override
            public boolean verify(JComponent input)
            {
                JFormattedTextField field = (JFormattedTextField) input;
                try
                {
                    double value = Double.parseDouble(field.getText());
                    if (value < 1)
                        throw new NumberFormatException("Value must be positive");
                    if (value > 4096)
                        throw new Exception("Value must be less than 30000");
                    field.setValue((int) Math.round(value));
                    return true;
                }
                catch (NumberFormatException e)
                {
                    field.setValue(1);
                    return false;
                }
                catch (Exception e)
                {
                    field.setValue(4096);
                    return false;
                }
            }
        };
        width_field.setInputVerifier(size_input_verifier);
        height_field.setInputVerifier(size_input_verifier);

        // Type
        type_box = new JComboBox<>(types);
        type_box.setSelectedIndex(0);
        type_box.setEditable(false);
        
        // Background Color
        bg_color_box = new JComboBox<>(bg_colors);
        bg_color_box.setSelectedIndex(0);
        bg_color_box.setEditable(false);
        bg_color_box.addActionListener(e -> {
            switch(new_img_color_combobox_index = bg_color_box.getSelectedIndex())
            {
                case 0:
                    bg_color = 0xFF000000;
                    break;
                case 1:
                    bg_color = 0xFFFFFFFF;
                    break;
                case 2:
                    bg_color = 0x00000000;
                    break;
                case 3:
                    JColorChooser chooser = new JColorChooser(Color.BLACK);
                    AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
                    for (AbstractColorChooserPanel panel : panels)
                    {
                        if (!panel.getDisplayName().equals("HSV"))
                        {
                            chooser.removeChooserPanel(panel);
                        }
                        else
                        {
                            // Traverse the component hierarchy and remove the sliders
                            for (Component c1 : panel.getComponents())
                            {
                                if (c1 instanceof JPanel)
                                {
                                    JPanel color_panel = (JPanel) c1;
                                    ArrayList<Component> components_to_remove = new ArrayList<>();
                                    for (Component c2 : color_panel.getComponents())
                                    {
                                        if (c2 instanceof JSlider)
                                        {
                                            components_to_remove.add(c2);
                                        }
                                    }
                                    for (Component c : components_to_remove)
                                    {
                                        color_panel.remove(c);
                                    }
                                    color_panel.revalidate();
                                    color_panel.repaint();
                                }
                            }
                        }
                    }

                    // Create a custom preview panel
                    chooser.setPreviewPanel(new JPanel());
                    
                    JOptionPane.showMessageDialog(null, chooser, "Choose a color", JOptionPane.PLAIN_MESSAGE);
                    Color color = chooser.getColor();
                    if (color != null)
                    {
                        bg_color = color.getRGB();
                    }
                    break;
            }
        });
        bg_color_box.setSelectedIndex(new_img_color_combobox_index);
        
        // Button Panel
        button_panel = new JPanel();

        // Create button
        create_button = new JButton("Create");
        create_button.addActionListener(e -> {
            create_new_image = true;
            create_image();
        });
        

        // Cancel button
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(e -> {
            create_new_image = false;
            dispose();
        });

        // Add stuff
        configurations_panel.add(new JLabel("Width", JLabel.CENTER));
        configurations_panel.add(width_field);
        configurations_panel.add(new JLabel("Height", JLabel.CENTER));
        configurations_panel.add(height_field);
        configurations_panel.add(new JLabel("Type", JLabel.CENTER));
        configurations_panel.add(type_box);
        configurations_panel.add(new JLabel("Background Color", JLabel.CENTER));
        configurations_panel.add(bg_color_box);
        button_panel.add(create_button);
        button_panel.add(cancel_button);
        add(configurations_panel, BorderLayout.CENTER);
        add(button_panel, BorderLayout.SOUTH);
    }

    BufferedImage create_image()
    {
        if (!create_new_image) return null;
        image_width = Integer.parseInt(width_field.getText());
        image_height = Integer.parseInt(height_field.getText());
        image_type = type_box.getSelectedIndex() == 0 ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_BYTE_GRAY;
        dispose();

        BufferedImage new_img = new BufferedImage(image_width, image_height, image_type);
        for (int y = 0; y < new_img.getHeight(); y++)
        {
            for (int x = 0; x < new_img.getWidth(); x++)
            {
                new_img.setRGB(x, y, bg_color);
            }
        }
        return new_img;
    }

    int get_fg_color()
    {
        int fg_color = 0xFF000000;
        int bg_r = (bg_color >> 16) & 0xFF;
        int bg_g = (bg_color >> 8) & 0xFF;
        int bg_b = bg_color & 0xFF;
        int bg_avg = (bg_r + bg_g + bg_b) / 3;

        if (bg_color == 0xFF000000)
            fg_color = 0xFFFFFFFF;
        else if (bg_color == 0xFFFFFFFF)
            fg_color = 0xFF000000;
        else if (bg_avg < 128)
            fg_color = 0xFFFFFFFF;
        else if (bg_avg >= 128)
            fg_color = 0xFF000000;

        return fg_color;
    }
}