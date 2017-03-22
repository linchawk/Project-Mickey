package tech.behaviouring.pm.ui.widgets;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class PM_EmptyLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	public PM_EmptyLabel(int borderWidth) {
		super();
		this.setBorder(new EmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth));
	}

}
