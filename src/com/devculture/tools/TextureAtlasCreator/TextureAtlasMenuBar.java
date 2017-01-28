package com.devculture.tools.TextureAtlasCreator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class TextureAtlasMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	public static final String OPEN_COMMAND = "Open";
	public static final String SAVE_COMMAND = "Export";
	public static final String REPACK_COMMAND = "Repack";
	public static final String QUIT_COMMAND = "Quit";
	public static final String UNDO_COMMAND = "Undo";
	public static final String REDO_COMMAND = "Redo";
	public static final String SELECTALL_COMMAND = "Select All";
	
	// command for toggling collision setting
	public static final String TOGGLE_HITBOXMODE_COMMAND = "Toggle Hitbox Mode";
	public static final String ADD_CIRCLE_HITBOX_COMMAND = "Add Circle";
	public static final String ADD_RECTANGLE_HITBOX_COMMAND = "Add Rectangle";
	public static final String ADD_POLYGON_HITBOX_COMMAND = "Add Polygon";
	
	public static final String COLOR_COMMAND = "Change Background Color";
	public static final String RESET_COLOR_COMMAND = "Reset Background Color";

	public static final String CANVAS_1X_COMMAND = "1X";
	public static final String CANVAS_2X_COMMAND = "2X";
	public static final String CANVAS_3X_COMMAND = "3X";
	public static final String CANVAS_4X_COMMAND = "4X";

	// dyu: specific to my computer
	private static final int CTRL_MASK = ActionEvent.META_MASK;
	
	public TextureAtlasMenuBar(ActionListener listener) {
		JMenu menu;
		JMenu submenu;
	
		// initial menu
		menu = addMenu("File", KeyEvent.VK_F);
		addMenuItem(menu, listener, OPEN_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL_MASK), KeyEvent.VK_O);
		addMenuItem(menu, listener, SAVE_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_MASK), KeyEvent.VK_S);
		addMenuItem(menu, listener, REPACK_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_R, CTRL_MASK), KeyEvent.VK_R);
		addMenuItem(menu, listener, QUIT_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_Q, CTRL_MASK), KeyEvent.VK_Q);
		
		menu = addMenu("Edit", KeyEvent.VK_E);
		addMenuItem(menu, listener, SELECTALL_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL_MASK), KeyEvent.VK_A);
		
		menu = addMenu("Hitbox", KeyEvent.VK_H);
		addMenuItem(menu, listener, TOGGLE_HITBOXMODE_COMMAND, KeyStroke.getKeyStroke('`'), KeyEvent.VK_T);

		// Hitbox creation belongs to hitbox mode
		submenu = addMenu("Add Hitbox", KeyEvent.VK_A);
		addMenuItem(submenu, listener, ADD_CIRCLE_HITBOX_COMMAND, KeyStroke.getKeyStroke('1'), KeyEvent.VK_1);
		addMenuItem(submenu, listener, ADD_RECTANGLE_HITBOX_COMMAND, KeyStroke.getKeyStroke('2'), KeyEvent.VK_2);
		addMenuItem(submenu, listener, ADD_POLYGON_HITBOX_COMMAND, KeyStroke.getKeyStroke('3'), KeyEvent.VK_3);
		menu.add(submenu);
		
		menu = addMenu("View", KeyEvent.VK_V);
		addMenuItem(menu, listener, COLOR_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_MASK), KeyEvent.VK_C);
		addMenuItem(menu, listener, RESET_COLOR_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL_MASK), KeyEvent.VK_V);
		
		// magnification belongs under view
		submenu = addMenu("Magnification", KeyEvent.VK_M);
		addMenuItem(submenu, listener, CANVAS_1X_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_1, CTRL_MASK), KeyEvent.VK_1);
		addMenuItem(submenu, listener, CANVAS_2X_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_2, CTRL_MASK), KeyEvent.VK_2);
		addMenuItem(submenu, listener, CANVAS_3X_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_3, CTRL_MASK), KeyEvent.VK_3);
		addMenuItem(submenu, listener, CANVAS_4X_COMMAND, KeyStroke.getKeyStroke(KeyEvent.VK_4, CTRL_MASK), KeyEvent.VK_4);
		menu.add(submenu);
	}
	
	public JMenu addMenu(String title, int mnemonic) {
		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic);
		add(menu);
		return menu;
	}
	
	public void addMenuItem(JMenu target, ActionListener listener, String title, KeyStroke shortcut, int mnemonic) {
		JMenuItem item = new JMenuItem(title);
		item.setAccelerator(shortcut);
		item.setMnemonic(mnemonic);
		item.addActionListener(listener);
		target.add(item);
	}
}
