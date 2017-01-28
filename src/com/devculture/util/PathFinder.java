package com.devculture.util;

import java.io.File;

import com.devculture.swing.PopupMessage;

public class PathFinder {
	public static final String SUBDIR_DATA = "/data";
	public static final String SUBDIR_SAVED = "/saved";
	
	private File localDir = null;
	private static PathFinder self = null;
	
	private PathFinder() {
		localDir = new File(FileManager.getCurrentDirectoryString());
	}
	
	public static PathFinder getInstance() {
		if(self == null) {
			self = new PathFinder();
		}
		return self;
	}
	
	public String getSubdirCanonicalPath(String subdir) {
		String path = null;
		try {
			path = localDir.getCanonicalPath() + subdir;
		} catch(Exception ex) {
			PopupMessage.showErrorMessage("Failed to grab subdirectory path from " + subdir);
		}
		return path;
	}
	
}
