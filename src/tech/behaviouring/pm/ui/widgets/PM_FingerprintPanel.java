package tech.behaviouring.pm.ui.widgets;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.FontLoader;

/*
 * Created by Mohan on 23/1/2015
 */

public class PM_FingerprintPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String tag = "PM_FingerPrintPanel";

	private int BORDER_WIDTH = 50;

	private String imgPath = "res\\img\\blank.JPG";
	private String imgText;
	private JLabel imgContainer;
	private JLabel imgLabel;
	private JButton capture;
	private ActionListener listner;

	public PM_FingerprintPanel(String label, ActionListener listener) {
		imgText = label;
		this.listner = listener;
		init();
	}

	private void init() {

		try {
			Image blankImage = ImageIO.read(new File(imgPath));
			imgContainer = new JLabel(new ImageIcon(blankImage.getScaledInstance(
					(int) (blankImage.getWidth(this) / ((double) blankImage.getHeight(this) / 240)), 240,
					Image.SCALE_DEFAULT)));
			imgContainer.setBorder(LineBorder.createGrayLineBorder());
			imgContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
			imgLabel = new JLabel(imgText);
			imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			imgLabel.setFont(FontLoader.getFont(Font.BOLD, 14));
			capture = new JButton("Capture");
			capture.setAlignmentX(Component.CENTER_ALIGNMENT);
			capture.addActionListener(listner);
			BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
			super.setLayout(boxLayout);
			super.add(imgLabel);
			super.add(new PM_EmptyLabel(5));
			super.add(imgContainer);
			super.add(new PM_EmptyLabel(10));
			super.add(capture);
			super.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		} catch (IOException e) {
			EventLog.e(tag, e);
			e.printStackTrace();
		}
	}

	public void setImage(Image newImage) {
		imgContainer.setIcon(new ImageIcon(newImage));
	}

	public String getImagePath() {
		return imgContainer.getIcon().toString();
	}

	public void setCaptureEnabled(boolean flag) {
		capture.setEnabled(flag);
	}
}
