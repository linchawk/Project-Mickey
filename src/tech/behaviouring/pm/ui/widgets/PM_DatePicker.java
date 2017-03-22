package tech.behaviouring.pm.ui.widgets;

import java.awt.Component;
import java.awt.Font;
import java.util.Date;

/*
 * Created by Mohan on 23/12/2015
 */

/*
 * Our custom DatePicker component extended from JCalendar library
 */

import com.toedter.calendar.*;

import tech.behaviouring.pm.util.FontLoader;

public class PM_DatePicker extends JDateChooser {

	private static final long serialVersionUID = 1L;

	public PM_DatePicker() {
		super();
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

	public PM_DatePicker(Date date) {
		super(date);
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

	public PM_DatePicker(String dateFromat, Date date) {
		super(date, dateFromat);
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setFont(FontLoader.getFont(Font.TRUETYPE_FONT, 12));
	}

}
