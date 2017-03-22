package tech.behaviouring.pm.ui.widgets;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.util.FontLoader;

/*
 * Created by Mohan on 6/2/2016
 */

public class PM_BigNumber extends JPanel {

	private static final long serialVersionUID = 1L;

	private int BORDER_WIDTH = 25;

	private String number;
	private String label;
	private PM_Label numberContainer;
	private PM_Label numberLabel;
	private Color color;

	public PM_BigNumber(int number, String label, Color color) {
		this.number = number + "";
		this.label = label;
		this.color = color;
		init();
	}

	private void init() {
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		super.setLayout(boxLayout);
		numberContainer = new PM_Label(number);
		numberContainer.setAlignmentY(Component.CENTER_ALIGNMENT);
		numberContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
		numberContainer.setFont(FontLoader.getFont(Font.PLAIN, 72));
		if (color != null)
			numberContainer.setForeground(color);
		numberLabel = new PM_Label(label);
		numberLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		numberLabel.setFont(FontLoader.getFont(Font.PLAIN, 14));
		numberLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
		super.add(numberContainer);
		super.add(numberLabel);
		super.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
	}
}
