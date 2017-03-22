package tech.behaviouring.pm.ui.widgets;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JTextField;
import tech.behaviouring.pm.util.FontLoader;

/*
 * Created by Mohan on 14/12/2015
 */

/*
 * Our custom TextField to make life easier at other places. For example
 * in GereralSetup.class window we need to use same property setters for 
 * each JTextField. Instead by using this TextField we can create with constructor itself
 */

public class PM_TextField extends JTextField {

	private static final long serialVersionUID = 1L;

	public PM_TextField() {
		super();
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

	public PM_TextField(String label) {
		super(label);
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

	public PM_TextField(String label, float alignment) {
		super(label);
		this.setAlignmentX(alignment);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

	public PM_TextField(String label, int fontWeight) {
		super(label);
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(fontWeight, 12));
	}

	public PM_TextField(String label, float alignment, int fontWeight, int fontSize) {
		super(label);
		this.setAlignmentX(alignment);
		this.setFont(FontLoader.getFont(fontWeight, fontSize));
	}

}
