import javax.swing.*;
import org.opencv.core.Core;
import java.awt.Color;

public class Main
{
    public static void main(String[] args)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.out.println("Caught From main:" + e);
        }
        SIMP app = new SIMP();
        app.setVisible(true);
        app.menu_bar.setBorder(null);
        app.menu_bar.setBackground(new Color(0x0e0e0e));
    }
}
