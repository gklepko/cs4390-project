import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Utilities {

    public static final String DEFAULT_FONT_FAMILY = "Inter";

    public static final Color TRANSPARENT_COLOR = new Color(0,0,0,0);
    public static final Color PRIMARY_COLOR = Color.decode("#5F4B66");
    public static final Color SECONDARY_COLOR = Color.decode("#2C1320");
    public static final Color TERTIARY_COLOR = Color.decode("#A7ADC6");
    public static final Color ACCENT_COLOR_PRIMARY = Color.decode("#56667A");
    public static final Color ACCENT_COLOR_SECONDARY = Color.decode("#8797AF");

    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Font CONNECTED_USERS_FONT = new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 16);
    public static final Font CONNECTED_USERS_LIST_FONT = new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 14);
    public static final Font MESSAGE_FONT = new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 14);
    public static final Font USERNAME_FONT = new Font (DEFAULT_FONT_FAMILY, Font.BOLD, 16);
    public static final Font CONSOLE_FONT = new Font(DEFAULT_FONT_FAMILY, Font.ITALIC, 14);


    public static EmptyBorder addPadding(int top, int left, int bottom, int right){
        return new EmptyBorder(top, left, bottom, right);
    }
}
