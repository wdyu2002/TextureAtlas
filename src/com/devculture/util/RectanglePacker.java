package com.devculture.util;

import java.awt.Point;
import java.awt.Rectangle;

public class RectanglePacker {

	private class RectangleNode {
		boolean used;
		int x, y, w, h;
		RectangleNode left, right;

		public RectangleNode(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
		
		public RectangleNode clone() {
			return new RectangleNode(x, y, w, h);
		}
	}
	
	/***
	 * this function sets up the root with all of the coordinates for the individual images
	 */
	
	public RectangleNode root;
	private int usedWidth;
	private int usedHeight;
	
	public RectanglePacker(int width, int height) {
		root = null;
		reset(width, height);
	}
	
	private void reset(int width, int height) {
		root = new RectangleNode(0, 0, width, height);
		root.left = null;
		root.right = null;
		
		usedWidth = 0;
		usedHeight = 0;
	}
	
	public Rectangle getDimensions() {
		return new Rectangle(usedWidth, usedHeight);
	}
	
	public Point findCoords(int w, int h) {
		Point coords = recursiveFindCoords(root, w, h);
		if(coords != null) {
			if(usedWidth < coords.x + w) {
				usedWidth = coords.x + w;
			}
			if(usedHeight < coords.y + h) {
				usedHeight = coords.y + h;
			}
		}
		return coords;
	}
	
	private RectangleNode cloneNode(RectangleNode node) {
		return node.clone();
	}
	
	private Point recursiveFindCoords(RectangleNode node, int w, int h) {
		if(node.left != null) {
			Point coords = recursiveFindCoords(node.left, w, h);
			return (coords != null)? coords : recursiveFindCoords(node.right, w, h);
		} else {
			if(node.used || w > node.w || h > node.h) {
				return null;
			}
			
			if(w == node.w && h == node.h) {
				node.used = true;
				return new Point(node.x, node.y);
			}
			
			node.left = cloneNode(node);
			node.right = cloneNode(node);
			
			if(node.w - w > node.h - h) {
				node.left.w = w;
				node.right.x = node.x + w;
				node.right.w = node.w - w;
			} else {
				node.left.h = h;
				node.right.y = node.y + h;
				node.right.h = node.h - h;
			}
			
			return recursiveFindCoords(node.left, w, h);
		}
	}
}
