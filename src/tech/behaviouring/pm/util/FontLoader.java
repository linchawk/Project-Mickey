package tech.behaviouring.pm.util;

import java.awt.*;
import java.io.File;

/*
 * Created by Mohan on 12/12/2015
 */

public class FontLoader {

	private static final String tag = "Font Loader";

	public static boolean loadFont() {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			return ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("res/font/Lato-Reg.ttf")));
		} catch (Exception ex) {
			EventLog.e(tag, ex);
			return false;
		}
	}

	public static Font getFont(int style, int size) {
		Font font = new Font("Lato-Reg", style, size);
		return font;
	}

}
