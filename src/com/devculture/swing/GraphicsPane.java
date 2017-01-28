package com.devculture.swing;

import java.awt.Graphics;

import javax.swing.JPanel;

public abstract class GraphicsPane extends JPanel {

	private static final long serialVersionUID = 1L;

	/** constructor **/
	
	public GraphicsPane() {
		
	}
	
	/** window property **/
	
	protected int getCurrentWindowWidth() {
		return getBounds().width;
	}
	
	protected int getCurrentWindowHeight() {
		return getBounds().height;
	}
	
	/** draw **/
	
	public abstract void paintComponent(Graphics g);
	
}
