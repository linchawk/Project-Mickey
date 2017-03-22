package tech.behaviouring.pm.ui;

import java.awt.Image;
import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * Created by Mohan on 24/2/2016
 * All Windows aka Frames in our app will inherit this class. 
 * This class provides a basic skeleton for running a window.
 */
public class PM_Activity implements Runnable, MouseListener, ActionListener, WindowListener {

	protected JFrame mainWindow;

	public PM_Activity() {
		mainWindow = new JFrame();
		Image image = new ImageIcon("res/img/appicon_64.png").getImage();
		mainWindow.setIconImage(image);
	}

	public void closeActivity() {
		mainWindow.dispose();
	}

	// Runnable interface

	@Override
	public void run() {

	}

	// Mouse Listener

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	// Window Listener

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	// Action Listener

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
