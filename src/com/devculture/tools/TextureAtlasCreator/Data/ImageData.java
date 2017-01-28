package com.devculture.tools.TextureAtlasCreator.Data;

import java.util.Vector;

public class ImageData {
	
	/** variables **/
	
	private static final long serialVersionUID = 1L;
	
	private Vector<PNGImage> images = new Vector<PNGImage>();
	
	/** getters **/
	
	public Vector<PNGImage> getPNGImages() {
		return images;
	}
	
	public boolean contains(PNGImage image) {
		return images.contains(image);
	}
	
	public void addImage(PNGImage image) {
		images.add(image);
	}
	
}