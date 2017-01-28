package com.devculture.tools.TextureAtlasCreator.Data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.w3c.dom.Node;
import com.devculture.swing.PopupMessage;
import com.devculture.util.FileManager;
import com.devculture.util.XMLReader;

public class Importer {

	private static boolean isValidXMLFile(String filename) {
		return filename != null && filename.endsWith(".xml");
	}
	
	public static void importXMLDataFile(Vector<PNGImage> images, File file) {
		try {
			BufferedImage image = null;
			Vector<Node> found = null;
			XMLReader reader = new XMLReader(file.getAbsolutePath());

			images.removeAllElements();
			
			found = reader.getDocumentNodesByTagName("PATH");
			for(Node pathNode : found) {
				image = ImageIO.read(new File(file.getParentFile().getCanonicalPath() + "/" + pathNode.getTextContent()));
			}

			found = reader.getDocumentNodesByTagName("IMAGE");
			for(Node imageNode : found) {
				images.add(PNGImage.fromXML(image, reader, imageNode));
			}
		} catch(Exception ex) {
			System.err.println("Error: Failed to load atlas from file '" + file.getName() + "'. " + ex.getMessage());
		}
	}
	
	public static void chooseXMLDataFile(Vector<PNGImage> images) {
		// attempt to open file
		File file = PopupMessage.askForFileToLoad(FileManager.getCurrentDirectoryString());
		if(file != null && isValidXMLFile(file.getName())) {
			importXMLDataFile(images, file);
		} else {
			PopupMessage.showErrorMessage("Failed to load xml file");
		}
	}
	
}
