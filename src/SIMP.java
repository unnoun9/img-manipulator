// S's Image Manipulation Program (SIMP)
// Going for default access modifier on purpose

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;

class SIMP extends JFrame
{
    // Window related variables or constants
    final float SCALE_FACTOR = 0.75f;
    final int WINDOW_WIDTH = (int) (1920 * SCALE_FACTOR);
    final int WINDOW_HEIGHT = (int) (1080 * SCALE_FACTOR);
    float zoom_level = 1.0f;
    Color theme_color = new Color(45, 45, 45); // Not used yet
    
    // Image's Layer related variables
    ArrayList<Layer> layers;
    Layer active_layer;//, active_layer_copy;
    int bg_color = 0x00000000, fg_color = 0xFFFFFFFF;
    int new_img_color_combobox_index = 0;
    Graphics2D g;
    BufferedImage original; // This is added just for resetting purposes, and I'll remove it perhaps when I added undo/redo functionality

    // GUI elements
    Image_Panel img_panel;
    JScrollPane img_scroll_pane;
    JPanel img_canvas_panel, button_panel, toolbar_panel, utility_panel, color_picker_panel, layer_panel, layer_list_panel;
    JFileChooser file_chooser;

    JButton add_layer, delete_layer, move_layer_up, move_layer_down;
    JButton gray_scale, invert, sepia, blur, edge_detection, reflect_horizontally, reflect_vertically;
    JButton reset, zoom_in, zoom_out;
    JButton no_tool, brush_tool, eraser_tool, fill_tool;
    boolean brush_enabled = false, eraser_enabled = false, fill_enabled = false;

    JMenuBar menu_bar;
    JMenu file_menu, edit_menu, filters_menu;
    JMenuItem new_img_item, open_item, save_item;
    JMenuItem zoom_in_item, zoom_out_item, reset_item, reflect_hor_item, reflect_vert_item;
    JMenuItem grayscale_item, invert_item, sepia_item, blur_item, edge_detection_item;


    SIMP()
    {
        super("Image Manipulation Software");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("assets/icons/SIMP_icon.png").getImage());

        layers = new ArrayList<>();
        setup_image_canvas();
        setup_menu_bar();
        setup_buttons();
        setup_toolbar();
        setup_utility_panel();
    }


    // Set the image canvas up
    void setup_image_canvas()
    {
        img_panel = new Image_Panel(layers);
        img_canvas_panel = new JPanel(new GridBagLayout());
        img_canvas_panel.add(img_panel);
        
        img_scroll_pane = new JScrollPane(img_canvas_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        img_scroll_pane.setBorder(BorderFactory.createEmptyBorder());  
        img_scroll_pane.getVerticalScrollBar().setUnitIncrement(10);      
        img_scroll_pane.getHorizontalScrollBar().setUnitIncrement(10);      
        
        img_panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e)
            {
                Tools.last_mouse_pos = e.getPoint();
                call_tool_functions(e);
            }
        });

        img_panel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                call_tool_functions(e);
                Tools.last_mouse_pos = e.getPoint();
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {}
        });

        add(img_scroll_pane, BorderLayout.CENTER);
    }

    // Create menu bar
    void setup_menu_bar()
    {
        menu_bar = new JMenuBar();

        // File menu
        file_menu = new JMenu("File");
        new_img_item = new JMenuItem("New");
        open_item = new JMenuItem("Open");
        save_item = new JMenuItem("Save");

        new_img_item.addActionListener(e -> new_image());
        open_item.addActionListener(e -> open_image());
        save_item.addActionListener(e -> save_image());

        file_menu.add(new_img_item);
        file_menu.add(open_item);
        file_menu.add(save_item);
        
        // Edit menu
        edit_menu = new JMenu("Edit");
        zoom_in_item = new JMenuItem("Zoom In");
        zoom_out_item = new JMenuItem("Zoom Out");
        reset_item = new JMenuItem("Reset");
        reflect_hor_item = new JMenuItem("Reflect Horizontally");
        reflect_vert_item = new JMenuItem("Reflect Vertically");

        zoom_in_item.addActionListener(e -> zoom_in());
        zoom_out_item.addActionListener(e -> zoom_out());
        reset_item.addActionListener(e -> reset());
        reflect_hor_item.addActionListener(e -> reflect_horizontally());
        reflect_vert_item.addActionListener(e -> reflect_vertically());

        edit_menu.add(zoom_in_item);
        edit_menu.add(zoom_out_item);
        edit_menu.add(reset_item);
        edit_menu.add(reflect_hor_item);
        edit_menu.add(reflect_vert_item);

        // Filters menu
        filters_menu = new JMenu("Filters");
        grayscale_item = new JMenuItem("Gray Scale");
        invert_item = new JMenuItem("Invert");
        sepia_item = new JMenuItem("Sepia");
        blur_item = new JMenuItem("Box Blur");
        edge_detection_item = new JMenuItem("Edge Detection");

        grayscale_item.addActionListener(e -> gray_scale());
        invert_item.addActionListener(e -> invert());
        sepia_item.addActionListener(e -> sepia());
        blur_item.addActionListener(e -> box_blur());
        edge_detection_item.addActionListener(e -> edge_detection());

        filters_menu.add(grayscale_item);
        filters_menu.add(invert_item);
        filters_menu.add(sepia_item);
        filters_menu.add(blur_item);
        filters_menu.add(edge_detection_item);
        
        // Menubar
        menu_bar.add(file_menu);
        menu_bar.add(edit_menu);
        menu_bar.add(filters_menu);
        setJMenuBar(menu_bar);
    }

    // Set the buttons up
    void setup_buttons()
    {
        button_panel = new JPanel();
        add(button_panel, BorderLayout.NORTH);

        gray_scale = new JButton("Gray Scale");
        gray_scale.addActionListener(e -> gray_scale());
        button_panel.add(gray_scale);

        invert = new JButton("Invert");
        invert.addActionListener(e -> invert());
        button_panel.add(invert);

        sepia = new JButton("Sepia");
        sepia.addActionListener(e -> sepia());
        button_panel.add(sepia);

        blur = new JButton("Blur");
        blur.addActionListener(e -> box_blur());
        button_panel.add(blur);

        edge_detection = new JButton("Edge Detection");
        edge_detection.addActionListener(e -> edge_detection());
        button_panel.add(edge_detection);

        reflect_horizontally = new JButton("Reflect Horizontally");
        reflect_horizontally.addActionListener(e -> reflect_horizontally());
        button_panel.add(reflect_horizontally);

        reflect_vertically = new JButton("Reflect Vertically");
        reflect_vertically.addActionListener(e -> reflect_vertically());
        button_panel.add(reflect_vertically);
        
        reset = new JButton("Reset");
        reset.addActionListener(e -> reset());
        button_panel.add(reset);

        zoom_in = new JButton("Zoom In");
        zoom_in.addActionListener(e -> zoom_in());
        button_panel.add(zoom_in);

        zoom_out = new JButton("Zoom Out");
        zoom_out.addActionListener(e -> zoom_out());
        button_panel.add(zoom_out);
    }

    // Set the tool bar up
    void setup_toolbar()
    {
        toolbar_panel = new JPanel();
        toolbar_panel.setLayout(new BoxLayout(toolbar_panel, BoxLayout.Y_AXIS));

        Dimension button_size = new Dimension(32, 32);
        int icon_width = 16, icon_height = 16;
        
        ImageIcon icon = new ImageIcon("assets/icons/no_tool_icon.png");
        no_tool = new JButton();
        no_tool.setIcon(Helpers.resize_icon(icon, icon_width, icon_height));
        no_tool.addActionListener(e -> use_no_tool());
        no_tool.setPreferredSize(button_size);
        no_tool.setMaximumSize(button_size);
        no_tool.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        icon = new ImageIcon("assets/icons/brush_tool_icon.png");
        brush_tool = new JButton();
        brush_tool.setIcon(Helpers.resize_icon(icon, icon_width, icon_height));
        brush_tool.addActionListener(e -> {
            use_no_tool();
            brush_enabled = !brush_enabled;
        });
        brush_tool.setPreferredSize(button_size);
        brush_tool.setMaximumSize(button_size);
        brush_tool.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        icon = new ImageIcon("assets/icons/eraser_tool_icon.png");
        eraser_tool = new JButton();
        eraser_tool.setIcon(Helpers.resize_icon(icon, icon_width, icon_height));
        eraser_tool.addActionListener(e -> {
            use_no_tool();
            eraser_enabled = !eraser_enabled;
        });
        eraser_tool.setPreferredSize(button_size);
        eraser_tool.setMaximumSize(button_size);
        eraser_tool.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        icon = new ImageIcon("assets/icons/fill_tool_icon.png");
        fill_tool = new JButton();
        fill_tool.setIcon(Helpers.resize_icon(icon, icon_width, icon_height));
        fill_tool.addActionListener(e -> {
            use_no_tool();
            fill_enabled = !fill_enabled;
        });
        fill_tool.setPreferredSize(button_size);
        fill_tool.setMaximumSize(button_size);
        fill_tool.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        toolbar_panel.add(no_tool);
        toolbar_panel.add(brush_tool);
        toolbar_panel.add(eraser_tool);
        toolbar_panel.add(fill_tool);

        add(toolbar_panel, BorderLayout.WEST);
    }

    // Set the utility panel up
    // TODO - Maybe change the layout to box y-axis?
    void setup_utility_panel()
    {
        utility_panel = new JPanel(new BorderLayout());
        setup_color_picker();
        setup_layer_part();
        add(utility_panel, BorderLayout.EAST);
    }

    // Set the color picker up
    void setup_color_picker()
    {
        // Color picker Panel
        color_picker_panel = new JPanel();
        color_picker_panel.setLayout(new BoxLayout(color_picker_panel, BoxLayout.Y_AXIS));

        // Custom color chooser
        JColorChooser chooser = new JColorChooser(new Color(fg_color));
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
                            // Remove sliders and text fields
                            if (c2 instanceof JSlider || c2 instanceof JLabel || c2 instanceof JRadioButton || c2 instanceof JSpinner)
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

        chooser.getSelectionModel().addChangeListener(e -> {
            fg_color = chooser.getColor().getRGB();
        });
        chooser.setPreviewPanel(new JPanel());
        chooser.setPreferredSize(new Dimension(250, 200));

        // Add stuff
        color_picker_panel.add(chooser);
        utility_panel.add(color_picker_panel, BorderLayout.NORTH);
    }

    // Set the layer part up
    void setup_layer_part()
    {
        layer_panel = new JPanel();
        layer_panel.setLayout(new BoxLayout(layer_panel, BoxLayout.Y_AXIS));
        
        // Components of the panel
        add_layer = new JButton("Add Layer");
        delete_layer = new JButton("Delete Layer");
        move_layer_up = new JButton("Move Layer Up");
        move_layer_down = new JButton("Move Layer Down");

        add_layer.addActionListener(e -> add_layer());
        delete_layer.addActionListener(e -> delete_layer());
        move_layer_up.addActionListener(e -> move_layer_up());
        move_layer_down.addActionListener(e -> move_layer_down());

        layer_panel.add(add_layer);
        layer_panel.add(delete_layer);
        layer_panel.add(move_layer_up);
        layer_panel.add(move_layer_down);

        layer_list_panel = new JPanel();
        layer_list_panel.setLayout(new BoxLayout(layer_list_panel, BoxLayout.Y_AXIS));
        update_layer_list();
        layer_panel.add(layer_list_panel);
        
        utility_panel.add(layer_panel, BorderLayout.CENTER);
    }

    // Update the layer list
    void update_layer_list()
    {
        layer_list_panel.removeAll();
        if (layers.size() > 0) for (int i = layers.size() - 1; i >= 0; i--)
        {
            Layer layer = layers.get(i);

            JButton button = new JButton(layer.name);
            button.addActionListener(e -> {
                active_layer = layer;
                update_image_canvas();
            });
            layer_list_panel.add(button);
        }
        layer_list_panel.revalidate();
        layer_list_panel.repaint();
        layer_panel.revalidate();
        layer_panel.repaint();
    }

    // Add layer
    void add_layer()
    {
        if (layers.size() > 0)
        {
            int w = original.getWidth();
            int h = original.getHeight();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            active_layer = new Layer(img, "Layer " + (layers.size() + 1));
            layers.add(active_layer);
            update_image_canvas();
        }
    }

    // Delete layer
    void delete_layer()
    {
        if (layers.size() > 1)
        {
            layers.remove(active_layer);
            active_layer = layers.get(layers.size() - 1);
            update_image_canvas();
        }
    }
    
    // Move layer up
    void move_layer_up()
    {
        // swap the active layer with the one above it
        int index = layers.indexOf(active_layer);
        if (index < layers.size() - 1)
        {
            Layer temp = layers.get(index + 1);
            layers.set(index + 1, active_layer);
            layers.set(index, temp);
            update_image_canvas();
        }
    }

    // Move layer down
    void move_layer_down()
    {
        int index = layers.indexOf(active_layer);
        if (index > 0)
        {
            Layer temp = layers.get(index - 1);
            layers.set(index - 1, active_layer);
            layers.set(index, temp);
            update_image_canvas();
        }
    }

    // Opens a window to create a new Image
    void new_image()
    {
        New_Image_Dialog dialog = new New_Image_Dialog(this, "New Image", true);
        dialog.setVisible(true);
        BufferedImage img;
        if ((img = dialog.create_image()) != null)
        {
            original = Helpers.copy_image(img);
            layers.clear();
            layers.add(active_layer=new Layer(img, "Layer 1"));
            bg_color = dialog.bg_color;
            fg_color = dialog.get_fg_color();
            update_image_canvas();
        }
    }

    // Open Images
    void open_image()
    {
        file_chooser = new JFileChooser();
        file_chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));
        file_chooser.setCurrentDirectory(new File("D:\\Wallpapers"));

        int result = file_chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selected_file = file_chooser.getSelectedFile();
            try
            {
                BufferedImage img = ImageIO.read(selected_file);
                if (img == null)
                {
                    JOptionPane.showMessageDialog(this,  "Image could not be loaded", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            
                // Create a new BufferedImage of type TYPE_INT_ARGB, draw the original image onto the new BufferedImage
                BufferedImage argb_image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = argb_image.createGraphics();
                g2d.drawImage(img, 0, 0, null);
                g2d.dispose();

                // Put the image into layers
                original = Helpers.copy_image(argb_image);
                layers.clear();
                layers.add(active_layer=new Layer(argb_image, "Layer 1"));
                update_image_canvas();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,  "Something went wrong while opening the image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Save Images
    void save_image()
    {
        file_chooser = new JFileChooser();
        // file_chooser.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpg", "jpeg"));
        file_chooser.setCurrentDirectory(new File("D:\\"));

        int result = file_chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selected_file = file_chooser.getSelectedFile();
            try
            {
                Dimension size = img_panel.getPreferredSize();
                BufferedImage final_image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = final_image.createGraphics();
                for (Layer layer: layers)
                {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.opacity));
                    g2d.drawImage(layer.img, 0, 0, null);
                }
                g2d.dispose();
                ImageIO.write(final_image, "png", selected_file);    
            }
            catch (Exception e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,  "Something went wrong while saving the image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    // Zoom in on the canvas by 10%
    void zoom_in()
    {
        zoom_level *= 1.1f;
        update_image_canvas();
    }

    // Zoom out on the canvas by 10%
    void zoom_out()
    {
        zoom_level = Math.max(0.1f, zoom_level * 0.9f);
        update_image_canvas();
    }

    // Update the the canvas
    void update_image_canvas()
    {
        img_panel.layers = layers;
        img_panel.zoom_level = zoom_level;
        // img_panel.setPreferredSize(img_panel.getPreferredSize());
        img_panel.revalidate();
        img_panel.repaint();
        update_layer_list();
    }

    // Filters
    void gray_scale()
    {
        try
        {
            active_layer.img = Filters.gray_scale(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void invert()
    {
        try
        {
            active_layer.img = Filters.invert(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void sepia()
    {
        try
        {
            active_layer.img = Filters.sepia(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void box_blur()
    {
        try
        {
            active_layer.img = Filters.box_blur(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void edge_detection()
    {
        try
        {
            active_layer.img = Filters.edge_detection_v2(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void reflect_horizontally()
    {
        try
        {
            active_layer.img = Filters.reflect_horizontally(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void reflect_vertically()
    {
        try
        {
            active_layer.img = Filters.reflect_vertically(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void reset()
    {
        if (original == null) return;
        layers.clear();
        layers.add(active_layer=new Layer(Helpers.copy_image(original), "Layer 1"));
        update_image_canvas();
    }

    // Tools
    void call_tool_functions(MouseEvent e)
    {
        int x, y;
        if (zoom_level == 1)
        {
            x = e.getX();
            y = e.getY();
        }
        else
        {
            x = (int) (e.getX() / zoom_level);
            y = (int) (e.getY() / zoom_level);
        }


        if (brush_enabled)
        {
            Tools.use_brush(active_layer.img, zoom_level, fg_color, x, y);
            update_image_canvas();
        }
        else if (eraser_enabled)
        {
            Tools.use_eraser(active_layer.img, zoom_level, x, y);
            update_image_canvas();
        }
        else if (fill_enabled)
        {
            Tools.use_fill(active_layer.img, fg_color, x, y);
            update_image_canvas();
        }
    }
    
    void use_no_tool()
    {
        brush_enabled = false;
        eraser_enabled = false;
        fill_enabled = false;
    }
}