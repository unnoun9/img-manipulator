import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

class Icon_Button extends JButton
{
    Dimension button_size;
    int icon_width, icon_height;

    Icon_Button(String icon_path)
    {
        super();
        button_size = new Dimension(32, 32);
        icon_width = icon_height = 16;
        setIcon(Helpers.resize_icon(new ImageIcon(icon_path), icon_width, icon_height));
        setPreferredSize(button_size);
        setMaximumSize(button_size);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}

class Text_Button extends JButton
{
    Dimension button_size;

    Text_Button(String text)
    {
        super(text);//String.format("%-8s", text).replace(' ', '\u00A0'));
        button_size = new Dimension(getPreferredSize().width, 25);
        if (text.contains("Layer"))
        {
            button_size = new Dimension(204, 28);
            setPreferredSize(button_size);
            setMaximumSize(button_size);
        }
        setPreferredSize(button_size);
        setMaximumSize(button_size);
    }
}