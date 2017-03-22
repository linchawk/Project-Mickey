package tech.behaviouring.pm.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/*
 * Created by Mohan on 6/2/2016
 */

/*
 * This widget can be used for representing a single row with four columns
 */

public class PM_ListSingleRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private String label;
	private String id;
	private Color bgColor;
	private MouseListener listener;
	private int BORDER_WIDTH = 25;

	public PM_ListSingleRow(String label, String id, Color bgColor, MouseListener listener) {
		this.label = label;
		this.id = id;
		this.bgColor = bgColor;
		this.listener = listener;
		init();
	}

	private void init() {
		setLayout(new GridLayout(0, 1));
		setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		if (bgColor != null)
			setBackground(bgColor);

		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		if (bgColor != null)
			rowPanel.setBackground(bgColor);

		JLabel col1 = new JLabel(label);
		JLabel col2 = new JLabel("View");
		JLabel col3 = new JLabel("Edit");
		JLabel col4 = new JLabel("Delete");

		Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

		Font font = col2.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		col2.setFont(font.deriveFont(attributes));
		col2.setBorder(new EmptyBorder(0, BORDER_WIDTH, 0, 0));
		col2.setCursor(cursor);
		col2.setName(id);
		col2.addMouseListener(listener);

		font = col3.getFont();
		attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		col3.setFont(font.deriveFont(attributes));
		col3.setBorder(new EmptyBorder(0, BORDER_WIDTH / 2, 0, 0));
		col3.setCursor(cursor);
		col3.setName(id);
		col3.addMouseListener(listener);

		font = col4.getFont();
		attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		col4.setFont(font.deriveFont(attributes));
		col4.setBorder(new EmptyBorder(0, BORDER_WIDTH / 2, 0, 0));
		col4.setCursor(cursor);
		col4.setName(id);
		col4.addMouseListener(listener);

		rowPanel.add(col1);
		rowPanel.add(Box.createHorizontalGlue());
		rowPanel.add(col2);
		rowPanel.add(col3);
		rowPanel.add(col4);

		add(rowPanel);
	}
}
