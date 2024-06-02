// S's Image Manipulation Program (SIMP)
// Going for default access modifier on purpose

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

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
    BufferedImage original; // For resetting the image

    // Undo and Redo related variables
    Stack<ArrayList<Layer>> undo_stack = new Stack<>();
    Stack<ArrayList<Layer>> redo_stack = new Stack<>();
    boolean toggle_tools_state_save = false;
    
    // GUI elements
    Image_Panel img_panel;
    JScrollPane img_scroll_pane;
    JPanel img_canvas_panel, button_panel, toolbar_panel, utility_panel, color_picker_panel, layer_panel, layer_list_panel;
    JFileChooser file_chooser;

    Icon_Button add_layer, delete_layer, move_layer_up, move_layer_down;
    JScrollPane layer_scroll_pane;
    Text_Button brighten, gray_scale, invert, sepia, box_blur, gaussian_blur, pixelate, edge_detection, reflect_horizontally, reflect_vertically, rotate, resize;
    Text_Button reset, zoom_in, zoom_out, undo_button, redo_button;
    Icon_Button no_tool, move_tool, brush_tool, eraser_tool, fill_tool;
    boolean brush_enabled = false, move_enabled = false, eraser_enabled = false, fill_enabled = false;

    JMenuBar menu_bar;
    JMenu file_menu, edit_menu, filters_menu;
    JMenuItem new_img_item, open_item, save_item;
    JMenuItem undo_item, redo_item, zoom_in_item, zoom_out_item, reset_item, reflect_hor_item, reflect_vert_item;
    JMenuItem grayscale_item, invert_item, sepia_item, box_blur_item, edge_detection_item;


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
        add_keyboard_shorcuts();
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
                toggle_tools_state_save = true;
                Point p = e.getPoint();
                if (move_enabled)
                {
                    if (p.x >= 0 + active_layer.x && p.x < active_layer.img.getWidth() + active_layer.x
                            && p.y >= 0 + active_layer.y && p.y < active_layer.img.getHeight() + active_layer.y)
                    {
                        if (toggle_tools_state_save)
                        {
                            save_state();
                            toggle_tools_state_save = false;
                        }
                        img_panel.drag_start = p;
                        img_panel.initial_layer_x = active_layer.x;
                        img_panel.initial_layer_y = active_layer.y;
                        img_panel.is_dragging = true;
                    }
                }
                else
                {
                    Tools.last_mouse_pos = p;
                    call_tool_functions(e);
                }

            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (move_enabled)
                {
                    img_panel.is_dragging = false;
                }
            }
        });

        img_panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                Point p = e.getPoint();
                if (move_enabled && img_panel.is_dragging)
                {
                    Point current_pos = p;
                    int dx = (int) ((current_pos.x - img_panel.drag_start.x) / zoom_level);
                    int dy = (int) ((current_pos.y - img_panel.drag_start.y) / zoom_level);
                    active_layer.x = img_panel.initial_layer_x + dx;
                    active_layer.y = img_panel.initial_layer_y + dy;
                    img_panel.repaint();
                }
                else
                {
                    call_tool_functions(e);
                    Tools.last_mouse_pos = p;
                }
            }
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
        undo_item = new JMenuItem("Undo");
        redo_item = new JMenuItem("Redo");
        zoom_in_item = new JMenuItem("Zoom In");
        zoom_out_item = new JMenuItem("Zoom Out");
        reset_item = new JMenuItem("Reset");
        reflect_hor_item = new JMenuItem("Reflect Horizontally");
        reflect_vert_item = new JMenuItem("Reflect Vertically");

        undo_item.addActionListener(e -> undo());
        redo_item.addActionListener(e -> redo());
        zoom_in_item.addActionListener(e -> zoom_in());
        zoom_out_item.addActionListener(e -> zoom_out());
        reset_item.addActionListener(e -> reset());
        reflect_hor_item.addActionListener(e -> reflect_horizontally());
        reflect_vert_item.addActionListener(e -> reflect_vertically());

        edit_menu.add(undo_item);
        edit_menu.add(redo_item);
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
        box_blur_item = new JMenuItem("Box Blur");
        edge_detection_item = new JMenuItem("Edge Detection");

        grayscale_item.addActionListener(e -> gray_scale());
        invert_item.addActionListener(e -> invert());
        sepia_item.addActionListener(e -> sepia());
        box_blur_item.addActionListener(e -> box_blur());
        edge_detection_item.addActionListener(e -> edge_detection());

        filters_menu.add(grayscale_item);
        filters_menu.add(invert_item);
        filters_menu.add(sepia_item);
        filters_menu.add(box_blur_item);
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
        button_panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));

        brighten = new Text_Button("Brighten");
        brighten.addActionListener(e -> brighten());
        button_panel.add(brighten);

        gray_scale = new Text_Button("Gray Scale");
        gray_scale.addActionListener(e -> gray_scale());
        button_panel.add(gray_scale);

        invert = new Text_Button("Invert");
        invert.addActionListener(e -> invert());
        button_panel.add(invert);

        sepia = new Text_Button("Sepia");
        sepia.addActionListener(e -> sepia());
        button_panel.add(sepia);

        box_blur = new Text_Button("Blur");
        box_blur.addActionListener(e -> box_blur());
        button_panel.add(box_blur);

        gaussian_blur = new Text_Button("Gaussian Blur");
        gaussian_blur.addActionListener(e -> gaussian_blur());
        button_panel.add(gaussian_blur);

        pixelate = new Text_Button("Pixelate");
        pixelate.addActionListener(e -> pixelate());
        button_panel.add(pixelate);

        edge_detection = new Text_Button("Edge Detection");
        edge_detection.addActionListener(e -> edge_detection());
        button_panel.add(edge_detection);

        reflect_horizontally = new Text_Button("Reflect Horizontally");
        reflect_horizontally.addActionListener(e -> reflect_horizontally());
        button_panel.add(reflect_horizontally);

        reflect_vertically = new Text_Button("Reflect Vertically");
        reflect_vertically.addActionListener(e -> reflect_vertically());
        button_panel.add(reflect_vertically);

        rotate = new Text_Button("Rotate");
        rotate.addActionListener(e -> rotate());
        button_panel.add(rotate);

        resize = new Text_Button("Resize");
        resize.addActionListener(e -> resize());
        button_panel.add(resize);
        
        reset = new Text_Button("Reset");
        reset.addActionListener(e -> reset());
        button_panel.add(reset);

        zoom_in = new Text_Button("Zoom In");
        zoom_in.addActionListener(e -> zoom_in());
        button_panel.add(zoom_in);

        zoom_out = new Text_Button("Zoom Out");
        zoom_out.addActionListener(e -> zoom_out());
        button_panel.add(zoom_out);

        undo_button = new Text_Button("Undo");
        undo_button.addActionListener(e -> undo());
        button_panel.add(undo_button);

        redo_button = new Text_Button("Redo");
        redo_button.addActionListener(e -> redo());
        button_panel.add(redo_button);
    }

    // Set the tool bar up
    void setup_toolbar()
    {
        toolbar_panel = new JPanel();
        toolbar_panel.setLayout(new BoxLayout(toolbar_panel, BoxLayout.Y_AXIS));

        no_tool = new Icon_Button("assets/icons/no_tool_icon.png");
        no_tool.addActionListener(e -> use_no_tool());

        move_tool = new Icon_Button("assets/icons/move_tool_icon.png");
        move_tool.addActionListener(e -> {
            use_no_tool();
            move_enabled = !move_enabled;
        });

        brush_tool = new Icon_Button("assets/icons/brush_tool_icon.png");
        brush_tool.addActionListener(e -> {
            use_no_tool();
            brush_enabled = !brush_enabled;
        });

        eraser_tool = new Icon_Button("assets/icons/eraser_tool_icon.png");
        eraser_tool.addActionListener(e -> {
            use_no_tool();
            eraser_enabled = !eraser_enabled;
        });

        fill_tool = new Icon_Button("assets/icons/fill_tool_icon.png");
        fill_tool.addActionListener(e -> {
            use_no_tool();
            fill_enabled = !fill_enabled;
        });

        toolbar_panel.add(no_tool);
        toolbar_panel.add(move_tool);
        toolbar_panel.add(brush_tool);
        toolbar_panel.add(eraser_tool);
        toolbar_panel.add(fill_tool);

        add(toolbar_panel, BorderLayout.WEST);
    }

    // Set the utility panel up
    void setup_utility_panel()
    {
        utility_panel = new JPanel(new BorderLayout(0, 20));
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
            if (!(panel.getDisplayName().equals("HSV")))
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
        chooser.setPreferredSize(new Dimension(240, 240));

        // Add stuff
        color_picker_panel.add(chooser);
        utility_panel.add(color_picker_panel, BorderLayout.NORTH);
    }

    // Set the layer part up
    // TODO - Add a way to change the opacity of layers (probably through sliders)
    void setup_layer_part()
    {
        layer_panel = new JPanel();
        layer_panel.setLayout(new BoxLayout(layer_panel, BoxLayout.Y_AXIS));
        
        // Components of the panel
        add_layer = new Icon_Button("assets/icons/add_layer.png");
        delete_layer = new Icon_Button("assets/icons/delete_layer.png");
        move_layer_up = new Icon_Button("assets/icons/move_layer_up.png");
        move_layer_down = new Icon_Button("assets/icons/move_layer_down.png");

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

        utility_panel.add(layer_panel, BorderLayout.EAST);
        utility_panel.add(layer_list_panel, BorderLayout.CENTER);
    }

    // Update the layer list
    void update_layer_list()
    {
        layer_list_panel.removeAll();
        if (layers.size() > 0) for (int i = layers.size() - 1; i >= 0; i--)
        {
            Layer layer = layers.get(i);

            Text_Button layer_b = new Text_Button(layer.name);
            layer_b.addActionListener(e -> {
                active_layer = layer;
                update_image_canvas();
            });
            layer_list_panel.add(layer_b);
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
            int w = layers.get(0).img.getWidth();
            int h = layers.get(0).img.getHeight();
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
        img_panel.revalidate();
        img_panel.repaint();
        update_layer_list();
    }

    // Filters
    // TODO - Add dialog boxes so that user may customize the use of filters
    void brighten()
    {
        save_state();
        try
        {
            active_layer.img = Filters.brighten(active_layer.img, 1.5f);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void gray_scale()
    {
        save_state();
        try
        {
            active_layer.img = Filters.gray_scale(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void invert()
    {
        save_state();
        try
        {
            active_layer.img = Filters.invert(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void sepia()
    {
        save_state();
        try
        {
            active_layer.img = Filters.sepia(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void box_blur()
    {
        save_state();
        try
        {
            active_layer.img = Filters.box_blur(active_layer.img, 9);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void gaussian_blur()
    {
        save_state();
        try
        {
            active_layer.img = Filters.gaussian_blur(active_layer.img, 9);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void pixelate()
    {
        save_state();
        try
        {
            active_layer.img = Filters.pixelate(active_layer.img, 10);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void edge_detection()
    {
        save_state();
        try
        {
            active_layer.img = Filters.edge_detection_v2(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void reflect_horizontally()
    {
        save_state();
        try
        {
            active_layer.img = Filters.reflect_horizontally(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void reflect_vertically()
    {
        save_state();
        try
        {
            active_layer.img = Filters.reflect_vertically(active_layer.img);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    // TODO - Deal with rotation
    void rotate()
    {
        save_state();
        try
        {
            active_layer.img = Filters.rotate(active_layer.img, 30f);
        }
        catch (Exception e) { return; }
        update_image_canvas();
    }

    void resize()
    {
        save_state();
        try
        {
            active_layer.img = Filters.resize(active_layer.img, 3.0f);
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
        undo_stack.clear();
        redo_stack.clear();
    }

    // Tools
    void call_tool_functions(MouseEvent e)
    {
        int x = (int) (e.getX() / zoom_level);
        int y = (int) (e.getY() / zoom_level);

        if (brush_enabled)
        {
            if (toggle_tools_state_save)
            {
                save_state();
                toggle_tools_state_save = false;
            }
            Tools.use_brush(active_layer.img, zoom_level, fg_color, x, y);
            update_image_canvas();
        }
        else if (eraser_enabled)
        {
            if (toggle_tools_state_save)
            {
                save_state();
                toggle_tools_state_save = false;
            }
            Tools.use_eraser(active_layer.img, zoom_level, x, y);
            update_image_canvas();
        }
        else if (fill_enabled)
        {
            if (toggle_tools_state_save)
            {
                save_state();
                toggle_tools_state_save = false;
            }
            Tools.use_fill(active_layer.img, fg_color, x, y);
            update_image_canvas();
        }
    }
    
    void use_no_tool()
    {
        move_enabled = false;
        brush_enabled = false;
        eraser_enabled = false;
        fill_enabled = false;
    }

    // Undo and Redo
    void save_state()
    {
        try
        {
            // Deep copy the layers' current state, push it to undo stack, and clear the redo stack
            ArrayList<Layer> current_state = new ArrayList<>();
            for (Layer layer: layers)
            {
                BufferedImage img_copy = Helpers.copy_image(layer.img);
                current_state.add(new Layer(img_copy, layer.name));
            }

            undo_stack.push(current_state);
            redo_stack.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    
    void undo()
    {
        if (!undo_stack.isEmpty())
        {
            int active_index = layers.indexOf(active_layer);
            redo_stack.push(layers);
            layers = undo_stack.pop();
            if (!layers.isEmpty() && active_index >= 0 && active_index <= layers.size() - 1) active_layer = layers.get(active_index);
            else if (!layers.isEmpty()) active_layer = layers.get(layers.size() - 1);
            update_image_canvas();
        }
    }

    void redo()
    {
        if (!redo_stack.isEmpty())
        {
            int active_index = layers.indexOf(active_layer);
            undo_stack.push(layers);
            layers = redo_stack.pop();
            if (!layers.isEmpty() && active_index >= 0 && active_index <= layers.size() - 1) active_layer = layers.get(active_index);
            else if (!layers.isEmpty()) active_layer = layers.get(layers.size() - 1);
            update_image_canvas();
        }
    }

    // Keyboard shortcuts
    void add_keyboard_shorcuts()
    {
        InputMap input_map = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap action_map = getRootPane().getActionMap();

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        action_map.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                undo();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        action_map.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                redo();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "save");
        action_map.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                save_image();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
        action_map.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                open_image();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "new");
        action_map.put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new_image();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoom_in");
        action_map.put("zoom_in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                zoom_in();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "zoom_out");
        action_map.put("zoom_out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                zoom_out();
            }
        });

        input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "reset");
        action_map.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reset();
            }
        });

        file_menu.setMnemonic(KeyEvent.VK_F);
        new_img_item.setMnemonic(KeyEvent.VK_N);
        open_item.setMnemonic(KeyEvent.VK_O);
        save_item.setMnemonic(KeyEvent.VK_S);

        edit_menu.setMnemonic(KeyEvent.VK_E);
        undo_item.setMnemonic(KeyEvent.VK_U);
        redo_item.setMnemonic(KeyEvent.VK_R);
        zoom_in_item.setMnemonic(KeyEvent.VK_I);
        zoom_out_item.setMnemonic(KeyEvent.VK_O);

        filters_menu.setMnemonic(KeyEvent.VK_I);
    }
}
