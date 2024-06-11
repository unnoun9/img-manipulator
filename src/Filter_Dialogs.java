import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

// Custom slider
class Custom_Slider extends JSlider
{
    Custom_Slider(int orientation, int min, int max, int value, int major_tick_spacing, int minor_tick_spacing)
    {
        super(orientation, min, max, value);
        setMajorTickSpacing(major_tick_spacing);
        setMinorTickSpacing(minor_tick_spacing);
        setPaintTicks(true);
        setPaintLabels(true);
        setFocusable(false);
    }
}

// General Dialog
class Filter_Dialog extends JDialog
{
    Text_Button ok_button = new Text_Button("OK"), cancel_button = new Text_Button("Cancel");
    JPanel main_panel = new JPanel(), button_panel = new JPanel();

    Filter_Dialog(Frame owner, boolean modal, String title)
    {
        super(owner, title, modal);
        setSize(240, 120);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        button_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    }

    void init()
    {
        main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
        button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));

        button_panel.add(ok_button);
        button_panel.add(cancel_button);
        main_panel.add(button_panel);
        add(main_panel);
    }
}

// Specific Dialogs
class Brighteness_Dialog extends Filter_Dialog
{
    float factor = 0.0f;
    Custom_Slider brighteness_slider;

    Brighteness_Dialog(Frame owner, boolean modal)
    {
        super(owner, modal, "Set Brightness Percentage");
        init();        
        setVisible(true);
    }

    void init()
    {
        brighteness_slider = new Custom_Slider(JSlider.HORIZONTAL, -100, 100, 0, 50, 10);

        ok_button.addActionListener(e -> {
            factor = brighteness_slider.getValue() / 100.0f;
            dispose();
        });

        cancel_button.addActionListener(e -> {
            factor = 0.0f;
            dispose();
        });

        main_panel.add(brighteness_slider);
        super.init();
    }
}

class Blur_Or_Pixelate_Dialog extends Filter_Dialog
{
    int strength = 1;
    JFormattedTextField strength_field;
    
    Blur_Or_Pixelate_Dialog(Frame owner, boolean modal, String title)
    {
        super(owner, modal, title);
        setSize(220, 95);
        init();
        setVisible(true);
    }

    void init()
    {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);

        strength_field = new JFormattedTextField(format);
        strength_field.setValue(1);
        strength_field.setPreferredSize(new Dimension(90, 22));
        strength_field.setMaximumSize(new Dimension(90, 22));
        // Don't let the user type in crazy things
        InputVerifier strength_input_verifier = new InputVerifier() {
            @Override
            public boolean verify(JComponent input)
            {
                JFormattedTextField field = (JFormattedTextField) input;
                try
                {
                    double value = Double.parseDouble(field.getText());
                    if (value < 1)
                        throw new NumberFormatException("Value must be positive");
                    if (value > 300)
                        throw new Exception("Value must not be more than 300");
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
                    field.setValue(300);
                    return false;
                }
            }
        };
        strength_field.setInputVerifier(strength_input_verifier);

        ok_button.addActionListener(e -> {
            strength = (int) strength_field.getValue();
            dispose();
        });

        cancel_button.addActionListener(e -> {
            strength = 1;
            dispose();
        });

        main_panel.add(strength_field);
        super.init();
    }
}

class Rotate_Dialog extends Filter_Dialog
{
    int angle = 0;
    JFormattedTextField angle_field;
    
    Rotate_Dialog(Frame owner, boolean modal, String title)
    {
        super(owner, modal, title);
        setSize(220, 95);
        init();
        setVisible(true);
    }

    void init()
    {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);

        angle_field = new JFormattedTextField(format);
        angle_field.setValue(0);
        angle_field.setPreferredSize(new Dimension(90, 22));
        angle_field.setMaximumSize(new Dimension(90, 22));
        // Don't let the user type in crazy things
        InputVerifier angle_input_verifier = new InputVerifier() {
            @Override
            public boolean verify(JComponent input)
            {
                JFormattedTextField field = (JFormattedTextField) input;
                try
                {
                    float value = Float.parseFloat(field.getText());
                    if (value < -360)
                        throw new NumberFormatException("Value need not be less than -360");
                    if (value > 360)
                        throw new Exception("Value need not be more than 360");
                    field.setValue((int) Math.round(value));
                    return true;
                }
                catch (NumberFormatException e)
                {
                    field.setValue(-360.0f);
                    return false;
                }
                catch (Exception e)
                {
                    field.setValue(360.0f);
                    return false;
                }
            }
        };
        angle_field.setInputVerifier(angle_input_verifier);

        ok_button.addActionListener(e -> {
            angle = (int) angle_field.getValue();
            dispose();
        });

        cancel_button.addActionListener(e -> {
            angle = 0;
            dispose();
        });

        main_panel.add(angle_field);
        super.init();
    }
}

class Resize_Dialog extends JDialog
{
    int width = 1, height = 1;
    final int ORIGINAL_WIDTH, ORIGINAL_HEIGHT;

    JPanel configurations_panel;
    JFormattedTextField width_field;
    JFormattedTextField height_field;

    JPanel button_panel;
    JButton ok_button, cancel_button;

    Resize_Dialog(Frame owner, boolean modal, String title, int original_width, int original_height)
    {
        super(owner, title, modal);
        ORIGINAL_WIDTH = original_width;
        ORIGINAL_HEIGHT = original_height;
        width = ORIGINAL_WIDTH;
        height = ORIGINAL_HEIGHT;
        setSize(240, 135);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        init();
        setVisible(true);
    }

    void init()
    {   
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        
        // Configurations Panel
        configurations_panel = new JPanel(new GridLayout(2, 2, 20, 15));

        // Width
        width_field = new JFormattedTextField(format);
        width_field.setValue(ORIGINAL_WIDTH);

        // Height
        height_field = new JFormattedTextField(format);
        height_field.setValue(ORIGINAL_HEIGHT);

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
                        throw new Exception("Value must not be more than 4096");
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
    
        // Button Panel
        button_panel = new JPanel();
        button_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create button
        ok_button = new JButton("OK");
        ok_button.addActionListener(e -> {
            width = (int) width_field.getValue();
            height = (int) height_field.getValue();
            dispose();
        });

        // Cancel button
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(e -> {
            width = ORIGINAL_WIDTH;
            height = ORIGINAL_HEIGHT;
            dispose();
        });

        // Add stuff
        configurations_panel.add(new JLabel("Width", JLabel.CENTER));
        configurations_panel.add(width_field);
        configurations_panel.add(new JLabel("Height", JLabel.CENTER));
        configurations_panel.add(height_field);
        button_panel.add(ok_button);
        button_panel.add(cancel_button);
        add(configurations_panel, BorderLayout.CENTER);
        add(button_panel, BorderLayout.SOUTH);
    }
}