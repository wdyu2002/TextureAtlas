package com.devculture.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

public class ThinBorder implements Border {
	
	/** variables **/
	
	private final static int THICKNESS_OF_THIN_BORDER = 1;
	public final static int THIN_BORDER_DIRECTION_TOP = 1;
	public final static int THIN_BORDER_DIRECTION_LEFT = 2;
	public final static int THIN_BORDER_DIRECTION_RIGHT = 4;
	public final static int THIN_BORDER_DIRECTION_BOTTOM = 8;
	
	private Color color = Color.black;
	private int directions = 0;
	
	/** constructor **/
	
	public ThinBorder(Color color, int directions) {
		this.color = color;
		this.directions = directions;
	}
	
	public Insets getBorderInsets(Component c) {
		return new Insets(THICKNESS_OF_THIN_BORDER, THICKNESS_OF_THIN_BORDER, THICKNESS_OF_THIN_BORDER, THICKNESS_OF_THIN_BORDER);
	}

	public boolean isBorderOpaque() {
		return true;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		g.setColor(color);

		if((directions & THIN_BORDER_DIRECTION_TOP) > 0) {
			g.drawLine(0, 0, width-1, 0);
		}
		
		if((directions & THIN_BORDER_DIRECTION_LEFT) > 0) {
			g.drawLine(0, 0, 0, height-1);
		}
		
		if((directions & THIN_BORDER_DIRECTION_RIGHT) > 0) {
			g.drawLine(width-1, 0, width-1, height-1);
		}
		
		if((directions & THIN_BORDER_DIRECTION_BOTTOM) > 0) {
			g.drawLine(0, height-1, width-1, height-1);
		}
		
		g.setColor(oldColor);
	}
	
}
