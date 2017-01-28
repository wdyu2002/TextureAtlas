package com.devculture.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class PopupMessage {
	
	/** static methods **/
	
	public static File askForSaveFileNameAndDestination(String defaultDirectory) {
		JFileChooser fc = new JFileChooser(defaultDirectory);
		int result = fc.showSaveDialog(MainFrame.getInstance());
		switch(result) {
		case JFileChooser.APPROVE_OPTION:
			return fc.getSelectedFile();
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
			break;
		}
		return null;
	}
	
	public static File askForFileToLoad(String defaultDirectory) {
		JFileChooser fc = new JFileChooser(defaultDirectory);
		int result = fc.showOpenDialog(MainFrame.getInstance());
		switch(result) {
		case JFileChooser.APPROVE_OPTION:
			return fc.getSelectedFile();
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
			break;
		}
		return null;
	}
	
	public static File askForDirectoryToOpen(String defaultDirectory) {
		JFileChooser fc = new JFileChooser(defaultDirectory);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fc.showOpenDialog(MainFrame.getInstance());
		switch(result) {
		case JFileChooser.APPROVE_OPTION:
			return fc.getSelectedFile();
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
			break;
		}
		return null;
	}
	
	public static String askForUserInput(String title, String text) {
		return JOptionPane.showInputDialog(MainFrame.getInstance(), text, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static int askForUserYesNo(String title, String text) {
		return JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showErrorMessage(String error) {
		JOptionPane.showMessageDialog(MainFrame.getInstance(), error, "Error", JOptionPane.WARNING_MESSAGE);
	}
	
}
