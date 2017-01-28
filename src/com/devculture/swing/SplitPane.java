package com.devculture.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.devculture.util.StaticDefines;

public class SplitPane extends JSplitPane {

	private static final long serialVersionUID = 1L;

	public SplitPane() {
		setDividerSize(5);
		setDividerLocation(200);
		setEnabled(false);
		setBorder(null);
	}
	
	public void setLeftPane(Component component) {
		if(component != null) {
			JScrollPane scrollPane = new JScrollPane(component);
			scrollPane.setBorder(new ThinBorder(new Color(StaticDefines.GLOBAL_BORDER_COLOR), ThinBorder.THIN_BORDER_DIRECTION_RIGHT));
			setLeftComponent(scrollPane);
		}
	}

	public void setRightPane(Component component) {
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setBorder(new ThinBorder(new Color(StaticDefines.GLOBAL_BORDER_COLOR), ThinBorder.THIN_BORDER_DIRECTION_LEFT));
		setRightComponent(scrollPane);
	}
	
}
