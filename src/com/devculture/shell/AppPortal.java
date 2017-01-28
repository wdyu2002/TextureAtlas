package com.devculture.shell;

import javax.swing.SwingUtilities;

import com.devculture.swing.MainFrame;
import com.devculture.tools.TextureAtlasCreator.TextureAtlasCreator;
import com.devculture.util.Sound;

public class AppPortal {
	
	public enum AppName {
		AppleSalesReporter,
		TextureAtlasCreator,
		MapLevelEditor,
	};
	
	public static void OpenApplication(final AppName app) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = MainFrame.getInstance();
					switch(app) {
						case AppleSalesReporter:
							frame.setAppName("Sales Reporting Tool");
							frame.setAppPane(new TextureAtlasCreator());
							frame.setAppSize(968, 600);
							break;
						case TextureAtlasCreator:
							frame.setAppName("Texture Atlas Creator");
							frame.setAppPane(new TextureAtlasCreator());
							frame.setAppSize(528, 595);
							break;
						case MapLevelEditor:
							frame.setAppName("Map Level Editor");
							frame.setAppPane(new TextureAtlasCreator());
							frame.setAppSize(1280, 1024);
							break;
						}
				} catch(Exception ex) {
					Sound.beep();
					System.err.println("Error: Application failed during invocation. " + ex.getMessage());
				}
			}
		});
	}
	
}
