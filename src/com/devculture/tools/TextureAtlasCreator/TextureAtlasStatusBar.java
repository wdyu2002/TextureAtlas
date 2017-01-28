package com.devculture.tools.TextureAtlasCreator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.devculture.swing.ThinBorder;

public class TextureAtlasStatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel label;
	private Dimension preferredSize;

	private static TextureAtlasStatusBar instance;
	private static Font font;
	
	public TextureAtlasStatusBar() {
		label = new JLabel("");
		font = new Font("Arial", Font.PLAIN, 12);
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(new ThinBorder(Color.GRAY, ThinBorder.THIN_BORDER_DIRECTION_TOP));
		setFont(font);
		label.setFont(font);
		preferredSize = new Dimension(getWidth(label.getText()), 23);
		this.add(label);
	}

	public static TextureAtlasStatusBar getInstance() {
		if (instance == null) {
			instance = new TextureAtlasStatusBar();
		}
		return instance;
	}

	protected int getWidth(String s) {
		FontMetrics fm = this.getFontMetrics(this.getFont());
		if (fm == null) {
			return 0;
		}
		return fm.stringWidth(s);
	}

	protected int getFontHeight() {
		FontMetrics fm = this.getFontMetrics(this.getFont());
		if (fm == null) {
			return 0;
		}
		return fm.getHeight();
	}

	public Dimension getPreferredSize() {
		return preferredSize;
	}

	public void setMessage(String message) {
		label.setText(message);
	}
}
