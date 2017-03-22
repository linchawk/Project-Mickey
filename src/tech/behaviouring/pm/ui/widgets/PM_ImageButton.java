package tech.behaviouring.pm.ui.widgets;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.util.FontLoader;

/*
 * Created by Mohan on 3/12/2015
 */

public class PM_ImageButton extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;

	private int BORDER_WIDTH = 50;

	private String imgPath;
	private String imgText;
	private Runnable targetRunnable;
	private JLabel imgContainer;
	private JLabel imgLabel;
	private static Color bgColorNormal; // Normal bg color of the image button
	private static Color bgColorFocused; // Normal bg color of the image button

	public PM_ImageButton(String imgPath, String imgText, Runnable onClickLaunchRunnable) {
		this.imgPath = imgPath;
		this.imgText = imgText;
		targetRunnable = onClickLaunchRunnable;
		init();
	}

	private void init() {
		imgContainer = new JLabel(new ImageIcon(imgPath));
		imgContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
		imgContainer.addMouseListener(this);
		imgLabel = new JLabel(imgText);
		imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		imgLabel.setFont(FontLoader.getFont(Font.BOLD, 14));
		imgLabel.addMouseListener(this);
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		super.setLayout(boxLayout);
		super.add(imgContainer);
		super.add(imgLabel);
		super.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		bgColorNormal = new Color(0xF5, 0xF5, 0xF5); // Same LandingScreen frame
														// bg color
		bgColorFocused = new Color(0xCF, 0xCF, 0xCF); // A little darker hint
		super.setBackground(bgColorNormal);
		super.addMouseListener(this);
	}

	public void mouseEntered(MouseEvent event) {
		super.setBackground(bgColorFocused);
	}

	public void mouseExited(MouseEvent event) {
		super.setBackground(bgColorNormal);
	}

	public void mouseClicked(MouseEvent event) {
		System.out.println("Mouse click on " + event.getSource());
		if (targetRunnable != null)
			new Thread(targetRunnable).start(); // Launch this runnable when
												// mouse is clicked on this
												// image button
	}

	public void mousePressed(MouseEvent event) {

	}

	public void mouseReleased(MouseEvent event) {

	}
}
