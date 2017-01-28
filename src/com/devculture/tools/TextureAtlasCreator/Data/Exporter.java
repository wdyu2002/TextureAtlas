package com.devculture.tools.TextureAtlasCreator.Data;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.devculture.swing.PopupMessage;
import com.devculture.util.FileManager;

public class Exporter {
	
	/** exporter helper functions **/
	
	private static String getXMLTextureInfo(String filename, int imageCount, int width, int height) {
		StringBuffer xml = new StringBuffer();
		xml.append("\t<TEXTURE>").append("\n");
		xml.append("\t\t<PATH>").append(filename).append("</PATH>").append("\n");
		xml.append("\t\t<W>").append(width).append("</W>").append("\n");
		xml.append("\t\t<H>").append(height).append("</H>").append("\n");
		xml.append("\t\t<IMAGES>").append(imageCount).append("</IMAGES>").append("\n");
		xml.append("\t</TEXTURE>").append("\n");
		return xml.toString();
	}

	private static boolean isPNGImageInBounds(PNGImage image, Rectangle bounds) {
		// PNG is ignored if it is outside the largest texture size
		// otherwise, check to make sure this image fits within the boundaries
		return image.getBounds().x >= 1024 || image.getBounds().y >= 1024 || bounds.contains(image.getBounds());
	}
	
	private static Rectangle getMinimumTextureRectangleRequired(Vector<PNGImage> images) {
		Rectangle texture = new Rectangle(0, 0, 1024, 1024);
		
		int dimension = 1024;
		boolean tooSmall = false;
		
		// figure out dynamically what is the minimum texture size needed
		for(int power=2; power<=10; power++) {
			tooSmall = false;
			dimension = (int) Math.pow(2, power);
			texture.setBounds(0, 0, dimension, dimension);
			
			for(PNGImage image : images) {
				if(!isPNGImageInBounds(image, texture)) {
					tooSmall = true;
					break;
				}
			}
			
			if(!tooSmall) {
				break;
			}
		}
		
		return texture;
	}
	
	private static int getValidImageCount(Vector<PNGImage> images, Rectangle texture) {
		int imageCount = 0;
		for(PNGImage image : images) {
			if(Exporter.isPNGImageInBounds(image, texture)) {
				++imageCount;
			}
		}
		return imageCount;
	}
	
	/** exporting **/
	
	private static void exportHeaderFile(Vector<PNGImage> images, File file, Rectangle texture) throws Exception {
		int index = 0;
		FileWriter fw = null;
		BufferedWriter bw = null;
		StringBuffer hdrfile = null;

		try {
			fw = new FileWriter(file.getParentFile().getPath() + "/" + file.getName() + "_defines.h");
			bw = new BufferedWriter(fw);
			hdrfile = new StringBuffer();
			
			// images
			for(PNGImage image : images) {
				if(isPNGImageInBounds(image, texture)) {
					hdrfile.append(image.toHeader(index++));
				}
			}
			
			// output hdr
			bw.write(hdrfile.toString());
		} finally {
			if(bw != null) {
				bw.close();
				bw = null;
			}
			if(fw != null) {
				fw.close();
				fw = null;
			}
		}
	}
	
	private static void exportXMLFile(Vector<PNGImage> images, File file, Rectangle texture) throws Exception {
		int index = 0;
		FileWriter fw = null;
		BufferedWriter bw = null;
		StringBuffer xmlfile = null;
		
		try {
			// example texture1_defines.m
			fw = new FileWriter(file.getParentFile().getPath() + "/" + file.getName() + ".xml");
			bw = new BufferedWriter(fw);
			xmlfile = new StringBuffer();
			
			// header
			xmlfile.append("<?xml version=\"1.0\"?>").append("\n");
			xmlfile.append("<ATLAS xmlns=\"http://www.w3.org/2005/Atom\">").append("\n");
			xmlfile.append(getXMLTextureInfo(file.getName() + ".png", images.size(), texture.width, texture.height));

			// images
			for(PNGImage image : images) {
				if(isPNGImageInBounds(image, texture)) {
					xmlfile.append(image.toXML(index++));
				}
			}
			
			// footer
			xmlfile.append("</ATLAS>").append("\n");
			
			// output xml
			bw.write(xmlfile.toString());
		} finally {
			if(bw != null) {
				bw.close();
				bw = null;
			}
			if(fw != null) {
				fw.close();
				fw = null;
			}
		}
	}

	private static void exportPNG(Vector<PNGImage> images, File file, Rectangle texture) throws Exception {
		BufferedImage img = new BufferedImage(texture.width, texture.height, BufferedImage.TYPE_INT_ARGB);
		
		// draw to image buffer
		Graphics2D g = img.createGraphics();
		{
			for(PNGImage image : images) {
				if(isPNGImageInBounds(image, texture)) {
					g.drawImage(image.getImage(), image.getBounds().x, image.getBounds().y, null);
				}
			}
		}
		g.dispose();
		
		// export the texture image as a png file
		ImageIO.write(img, "png", new File(file.getParentFile().getPath() + "/" + file.getName() + ".png"));
	}
	
	public static void export(Vector<PNGImage> images) {
		try {
			File file = null;
			Rectangle texture = getMinimumTextureRectangleRequired(images);
			if(getValidImageCount(images, texture) > 0) {
				file = PopupMessage.askForSaveFileNameAndDestination(FileManager.getCurrentDirectoryString());
				if(file != null && file.getName() != null && file.getName().length() > 0) {
					exportPNG(images, file, texture);
					exportXMLFile(images, file, texture);
					exportHeaderFile(images, file, texture);
				}
			} else {
				PopupMessage.showErrorMessage("Empty texture detected, no image to export");
			}
		} catch(Exception ex) {
			System.err.println("Error: Failed to export to XML. " + ex.getMessage());
			
		}
	}
}
