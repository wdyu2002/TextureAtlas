package com.devculture.tools.TextureAtlasCreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.devculture.swing.MainFrame;
import com.devculture.swing.ThinBorder;
import com.devculture.tools.TextureAtlasCreator.Data.ImageData;
import com.devculture.tools.TextureAtlasCreator.UI.ImageCanvas;
import com.devculture.util.StaticDefines;

public class TextureAtlasCreator extends JPanel {

	private static final long serialVersionUID = 1L;

	public TextureAtlasCreator() {
		// data initialization
		ImageData imageData = new ImageData();

		// canvas
		ImageCanvas canvas = new ImageCanvas(imageData);
		canvas.setPreferredSize(new Dimension(1280, 1024));
		canvas.setBorder(null);

		// scroll pane contains canvas
		JScrollPane scrollPane = new JScrollPane(canvas);
		scrollPane.setBorder(new ThinBorder(new Color(StaticDefines.GLOBAL_BORDER_COLOR), ThinBorder.THIN_BORDER_DIRECTION_LEFT));

		TextureAtlasStatusBar statusBar = TextureAtlasStatusBar.getInstance();
		
		// this panel contains scroll pane
		setLayout(new BorderLayout());
	    add(statusBar, BorderLayout.SOUTH);
		
	    setBorder(null);
		setVisible(true);
		add(scrollPane);
		
		MainFrame.getInstance().setAppMenu(new TextureAtlasMenuBar(canvas));
	}

}
