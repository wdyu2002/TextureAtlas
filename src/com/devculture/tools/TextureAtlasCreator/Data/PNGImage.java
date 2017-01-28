package com.devculture.tools.TextureAtlasCreator.Data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;

import org.w3c.dom.Node;

import com.devculture.tools.TextureAtlasCreator.Data.PNGImage.PNGHitbox.HitboxType;
import com.devculture.tools.TextureAtlasCreator.UI.ImageCanvas;
import com.devculture.util.PointStr;
import com.devculture.util.XMLReader;

public class PNGImage {
	
	// hitbox
	public static class PNGHitbox {
	
		// types
		public static final int HITBOX_DEFAULT_IDENTIFIER_VALUE = 0;
		public static final HitboxType HITBOX_SHAPE_DEFAULT = HitboxType.CIRCLE;
		
		public static enum HitboxType {
			CIRCLE(0, "Circle"),
			RECTANGLE(1, "Rectangle"),
			POLYGON(2, "Polygon"),
			TOTAL(3, "Total");
			
			private final int _index;
			private final String _name;
			
			private HitboxType(int index, String name) {
				_index = index;
				_name = name;
			}
			
			public final int index() {
				return _index;
			}
			
			public final String string() {
				return _name;
			}
			
			public final static HitboxType getHitboxType(int index) throws Exception {
				switch(index) {
				case 0:
					return CIRCLE;
				case 1:
					return RECTANGLE;
				case 2:
					return POLYGON;
				}
				throw new Exception("Error: Attempting to retrieve a hitbox type that is undefined. index = " + index);
			}
			
			public final static HitboxType getHitboxType(String string) throws Exception {
				if(CIRCLE.string().equals(string)) {
					return CIRCLE;
				} else if(RECTANGLE.string().equals(string)) {
					return RECTANGLE;
				} else if(POLYGON.string().equals(string)) {
					return POLYGON;
				}
				throw new Exception("Error: Attempting to retrieve a hitbox type that is undefined. name = " + string);
			}
		};
		
		// coordinates are with respect to top-left of the actual image
		private int offsetX;
		private int offsetY;
		private int x = 0;
		private int y = 0;
		private int width = 0;
		private int height = 0;
		private int radius = 0;
		private Vector<PointStr> vertices = new Vector<PointStr>();
		private PointStr selectedVertex = null;
		private PointStr movingVertex = null;
		
		private int type = HITBOX_DEFAULT_IDENTIFIER_VALUE;
		private HitboxType shape = HITBOX_SHAPE_DEFAULT;
		private boolean removable = true;

		// default color is blue
		private Color color = Color.RED;
		
		public static PNGHitbox createHitbox(HitboxType shape) {
			return new PNGHitbox(shape);
		}
		
		public static PNGHitbox createHitbox(HitboxType shape, int x, int y) {
			return new PNGHitbox(shape, x, y);
		}
		
		private PNGHitbox(HitboxType shape, int x, int y) {
			setX(x);
			setY(y);
			this.shape = shape;
		}

		private PNGHitbox(HitboxType shape) {
			this.shape = shape;
		}

		public final int MINIMUM_RADIUS_SIZE = 5;
		public final int MINIMUM_WIDTH_SIZE = 10;
		public final int MINIMUM_HEIGHT_SIZE = 10;
		public final int POLYGON_SELECTION_CLOSENESS_THRESHOLD = 5;
		
		public final int getX() { return x; }
		public final int getY() { return y; }
		public final int getRadius() { return radius; }
		public final int getWidth() { return width; }
		public final int getHeight() { return height; }
		
		public final void setX(int x) {
			this.x = x;
		}
		
		public final void setY(int y) {
			this.y = y;
		}

		public final void setOffset(int x, int y) {
			offsetX = x;
			offsetY = y;
		}
		
		public final void updateOffset(int x, int y) {
			if(removable) {
				translate(x - offsetX, y - offsetY);
				setOffset(x, y);
			}
		}
		
		private void translate(int dx, int dy) {
			switch(shape) {
			case CIRCLE:
			case RECTANGLE:
				x += dx;
				y += dy;
				break;
			case POLYGON:
				for(PointStr vertex : vertices) {
					vertex.x += dx;
					vertex.y += dy;
				}
				break;
			}
		}
		
		public final void setRadius(int r) {
			radius = r < MINIMUM_RADIUS_SIZE ? MINIMUM_RADIUS_SIZE : r;
		}
		
		public final void setWidth(int w) {
			width = w < MINIMUM_WIDTH_SIZE ? MINIMUM_WIDTH_SIZE : w;
		}
		
		public final void setHeight(int h) {
			height = h < MINIMUM_HEIGHT_SIZE ? MINIMUM_HEIGHT_SIZE : h;
		}
		
		public final void setCircularValues(int x, int y, int r) {
			setX(x);
			setY(y);
			setRadius(r);
			setWidth(0);
			setHeight(0);
			shape = HitboxType.CIRCLE;
		}
		
		public final void setRectangularValues(int x, int y, int w, int h) {
			setX(x);
			setY(y);
			setRadius(0);
			setWidth(w);
			setHeight(h);
			shape = HitboxType.RECTANGLE;
		}
		
		/** 
		 * using an array is still preferred because during read-in, we first read in
		 * the # of vertices to create a buffer large enough for all vertex data.
		 * after that, we fill in each individual index with the vertex data, to
		 * prevent any accidental out-of-order error during read-in.
		 **/
		public final void setPolygonVertexValues(Vector<PointStr> points) {
			vertices.clear();
			vertices.addAll(points);
		}
		
		public final void addPolygonVertex(int x, int y) {
			vertices.add(new PointStr(x, y));
		}
		
		public final int getVerticesCount() {
			// make sure there are at least 3 vertices
			return vertices.size();
		}
		
		private final double getDistance(double x1, double y1, double x2, double y2) {
			double dx = x1-x2;
			double dy = y1-y2;
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		/**
		 * this method returns the index of the first point that is within a certain
		 * threshold distance to the point provided (most likely the mouse-down 
		 * coordinates).
		 **/
		private final PointStr getFirstVertexNear(double x, double y) {
			for(PointStr point : vertices) {
				if(getDistance(x, y, point.x, point.y) < POLYGON_SELECTION_CLOSENESS_THRESHOLD) {
					return point;
				}
			}
			return null;
		}
		
		public final int getFirstVertexIndexNear(double x, double y) {
			for(int i=0; i<vertices.size(); i++) {
				PointStr point = vertices.elementAt(i);
				if(getDistance(x, y, point.x, point.y) < POLYGON_SELECTION_CLOSENESS_THRESHOLD) {
					return i;
				}
			}
			return -1;
		}
		
		public final void setMovingVertex(int x, int y) {
			if(movingVertex == null) {
				movingVertex = getFirstVertexNear(x, y);
			}
		}

		public final void moveMovingVertex(int x, int y) {
			if(movingVertex != null) {
				movingVertex.x = x;
				movingVertex.y = y;
				
				// once a vertex starts moving, null selection
				selectedVertex = null;
			}
		}
		
		public final void resetMovingVertex() {
			movingVertex = null;
		}
		
		public final boolean isSelectedVertex(int index) {
			if(index >= 0 && index < vertices.size()) {
				return selectedVertex == vertices.elementAt(index);
			}
			return false;
		}
		
		public final void setSelectedVertex(int index) {
			if(index >= 0 && index < vertices.size()) {
				selectedVertex = vertices.elementAt(index);
			} else {
				selectedVertex = null;
			}
			if(selectedVertex != null) {
				movingVertex = null;
			}
		}

		public final void removeSelectedVertex() {
			if(selectedVertex != null) {
				vertices.remove(selectedVertex);
				selectedVertex = null;
			}
		}

		private final Polygon getPolygon() {
			Polygon polygon = new Polygon();
			for(Point point : vertices) {
				polygon.addPoint(point.x, point.y);
			}
			return polygon;
		}
		
		public final Vector<PointStr> getVertices() {
			return vertices;
		}
		
		public void setType(int identifier) { 
			this.type = identifier;
		}
		
		public int getType() {
			return type;
		}
		
		public HitboxType getShape() {
			return shape;
		}

		public void setColor(Color color) { this.color = color; }
		public Color getColor() { return color; }

		public void setRemovable(boolean removable) { this.removable = removable; }
		public boolean isRemovable() { return removable; }
		
		public boolean isCircular() { return shape == HitboxType.CIRCLE; }
		public boolean isRectangular() { return shape == HitboxType.RECTANGLE; }
		public boolean isPolygon() { return shape == HitboxType.POLYGON; }
		
		public Point getBottomRightPosition(int offsetX, int offsetY) {
			Point point = new Point();
			int x = offsetX + getX();
			int y = offsetY + getY();
			switch(shape) {
			case CIRCLE:
				int dr = (int) Math.sqrt((radius * radius) / 2);
				point.x = x + dr;
				point.y = y + dr >= (1024-100) ? y - dr - 100 : y + dr;
				break;
			case RECTANGLE:
				point.x = x + width;
				point.y = y + height >= (1024-100) ? y - 100 : y + height;
				break;
			case POLYGON:
				Rectangle bounds = getPolygon().getBounds();
				point.x = bounds.x + bounds.width + offsetX;
				point.y = bounds.y + bounds.height + offsetY;
				break;
			}
			return point;
		}
		
		private void drawCircular(Graphics2D gfx, int imageX, int imageY, boolean selected) {
			int x = imageX + getX();
			int y = imageY + getY();
			int r = getRadius();
			gfx.fillOval(x - r, y - r, 2*r, 2*r);
			gfx.setComposite(selected ? ImageCanvas.ALPHA_85 : ImageCanvas.ALPHA_35);
			gfx.drawOval(x - r, y - r, 2*r, 2*r);
		}
		
		private void drawRectangular(Graphics2D gfx, int imageX, int imageY, boolean selected) {
			int x = imageX + getX();
			int y = imageY + getY();
			gfx.fillRect(x, y, getWidth(), getHeight());
			gfx.setComposite(selected ? ImageCanvas.ALPHA_85 : ImageCanvas.ALPHA_35);
			gfx.drawRect(x, y, getWidth(), getHeight());
		}
		
		private void drawPolygon(Graphics2D gfx, int imageX, int imageY, boolean selected) {
			Polygon polygon = getPolygon();
			if(vertices.size() > 0) {
				polygon.translate(imageX, imageY);
				gfx.fillPolygon(polygon);
				gfx.setComposite(selected ? ImageCanvas.ALPHA_85 : ImageCanvas.ALPHA_35);
				gfx.drawPolygon(polygon);
				
				// render all vertices as circles
				Color originalColor = gfx.getColor();
				gfx.setComposite(ImageCanvas.ALPHA_35);
				for(Point point : vertices) {
					if(point == selectedVertex || point == movingVertex) {
						gfx.setColor(Color.DARK_GRAY);
						gfx.setComposite(ImageCanvas.ALPHA_85);
					}
					gfx.fillRect(point.x + imageX - 2, point.y + imageY - 2, 4, 4);
					gfx.drawOval(point.x + imageX - 5, point.y + imageY - 5, 10, 10);
					if(point == selectedVertex || point == movingVertex) {
						gfx.setColor(originalColor);
						gfx.setComposite(ImageCanvas.ALPHA_35);
					}
				}
			}
		}
		
		public void draw(Graphics2D gfx, int imageX, int imageY, boolean selected) {
			switch(shape) {
			case CIRCLE:
				drawCircular(gfx, imageX, imageY, selected);
				break;
			case RECTANGLE:
				drawRectangular(gfx, imageX, imageY, selected);
				break;
			case POLYGON:
				drawPolygon(gfx, imageX, imageY, selected);
				break;
			}
		}
		
		public boolean contains(int x, int y) {
			if(isCircular()) {
				int dx = x - this.x;
				int dy = y - this.y;
				return radius >= (int)Math.sqrt(dx*dx + dy*dy);
			} else if(isRectangular()) {
				return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
			} else if(isPolygon()) {
				return getPolygon().contains(x, y);
			}
			return true;
		}
	}
	
	/** vars **/
	
	private boolean preselected = false;
	private boolean selected = false;
	private BufferedImage image;
	private String name;
	public Rectangle bounds = new Rectangle();
	private Rectangle outline = new Rectangle();
	private Vector<PNGHitbox> hitboxes = new Vector<PNGHitbox>();	
	public final static int IMAGE_PADDING = 2;
	
	/** constructors **/

	private PNGImage() {

	}
	
	// used for image loading from png
	public PNGImage(File file) throws IOException {
		image = ImageIO.read(file);
		name = formatName(file.getName());
		
		// insert padding to images for placement
		int width = image.getWidth();
		width = (width < 1024) ? width+IMAGE_PADDING : width;
		
		int height = image.getHeight();
		height = (height < 1024) ? height+IMAGE_PADDING : height;
		
		bounds.setBounds(0, 0, width, height);
		outline.setBounds(0, 0, width, height);
		
		// add a collision hitbox covering the entire image
		PNGHitbox hitbox = PNGHitbox.createHitbox(HitboxType.RECTANGLE);
		hitbox.setRectangularValues(0, 0, width, height);
		hitbox.setRemovable(false); // this is the default collision box, and cannot be removed
		hitboxes.add(hitbox);
	}

	/** hitbox mouse events **/
	
	private PNGHitbox currentHitbox = null;
	
	public PNGHitbox getNextHitboxAt(int respectiveX, int respectiveY) {
		Vector<PNGHitbox> tmp = new Vector<PNGHitbox>(); 
		
		// create a vector of hitboxes that are affected by my touch event
		for(PNGHitbox hitbox : hitboxes) {
			if(hitbox.contains(respectiveX, respectiveY)) {
				tmp.add(hitbox);
			}
		}
		
		// based on the currentHitbox, figure out which hitbox to return
		if(tmp.size() > 0) {
			if(currentHitbox == null || !tmp.contains(currentHitbox) || currentHitbox == tmp.lastElement()) {
				return tmp.elementAt(0);
			} else {
				// tmp contains current hitbox and it is not the last element
				return tmp.elementAt(tmp.indexOf(currentHitbox) + 1);
			}
		}
		
		return null;
	}
	
	public boolean isCurrentHitbox(PNGHitbox hitbox) {
		return currentHitbox == hitbox;
	}
	
	public PNGHitbox getCurrentHitbox() {
		return currentHitbox;
	}
	
	public void setCurrent(PNGHitbox hitbox) {
		currentHitbox = hitbox;
	}

	public void addHitbox(PNGHitbox hitbox) {
		hitboxes.add(hitbox);
	}
	
	public boolean removeSelectedHitbox() {
		if(currentHitbox != null && currentHitbox.isRemovable() && hitboxes.contains(currentHitbox)) {
			hitboxes.remove(currentHitbox);
			currentHitbox = null;
			return true;
		}
		return false;
	}
	
	public void shiftSelectedHitboxPositionBy(int dx, int dy) {
		if(currentHitbox != null && currentHitbox.isRemovable() && hitboxes.contains(currentHitbox)) {
			currentHitbox.translate(dx, dy);
		}
	}
	
	/** getters **/
	
	public String getName() {
		return name;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public Vector<PNGHitbox> getHitboxes() {
		return hitboxes;
	}
	
	/** sizing **/
	
	public int getWidth() {
		return bounds.width;
	}
	
	public int getHeight() {
		return bounds.height;
	}

	/** position **/
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public int getPositionX() {
		return bounds.x;
	}
	
	public int getPositionY() {
		return bounds.y;
	}
	
	public void setPosition(int x, int y) {
		outline.x = bounds.x = x;
		outline.y = bounds.y = y;
	}

	/** outline **/
	
	public Rectangle getOutline() {
		return outline;
	}
	
	public int getOutlinePositionX() {
		return outline.x;
	}
	
	public int getOutlinePositionY() {
		return outline.y;
	}
	
	public void moveToOutlinePosition() {
		setPosition(outline.x, outline.y);
	}

	/** selection **/

	public boolean isOutlineMoved() {
		return outline.x != bounds.x || outline.y != bounds.y;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public boolean isPreselected() {
		return preselected;
	}
	
	public void preselect() {
		preselected = true;
	}
	
	public void select() {
		preselected = false;
		selected = true;
	}
	
	public void deselect() {
		preselected = false;
		selected = false;
	}
	
	/** string representation **/
	
	public boolean isValidNameCharacter(char c) {
		if(c >= '0' && c <= '9') {
			return true;
		} else if(c >= 'a' && c <= 'z') {
			return true;
		} else if(c >= 'A' && c <= 'Z') {
			return true;
		} else if(c == '_') {
			return true;
		}
		return false;
	}
	
	public String formatName(String name) {
		// remove extension
		char[] nameChars = name.toCharArray();
		int extBegin = name.lastIndexOf('.');
		if(extBegin == -1) extBegin = name.length();
		
		for(int i=0; i<extBegin; i++) {
			if(!isValidNameCharacter(nameChars[i])) {
				nameChars[i] = '_';
			}
		}
		
		return new String(nameChars, 0, extBegin).toUpperCase();
	}
	
	private static boolean getBooleanContextFromFirstImageNodeChild(XMLReader reader, Node imageNode, String childNode) {
		Vector<Node> nodes = reader.getChildrenNodesByTagName(imageNode, childNode);
		String content;
		
		// expecting only 1 child
		if(nodes.size() > 0) {
			content = nodes.get(0).getTextContent();
			if(content.equalsIgnoreCase("true")) {
				return true;
			}
		}
		return false;
	}
	
	private static String getStringContentFromFirstImageNodeChild(XMLReader reader, Node imageNode, String childNode) {
		Vector<Node> nodes = reader.getChildrenNodesByTagName(imageNode, childNode);
		
		// expecting only 1 child
		if(nodes.size() > 0) {
			return nodes.get(0).getTextContent();
		}
		return null;
	}
	
	private static int getIntContentFromFirstImageNodeChild(XMLReader reader, Node imageNode, String childNode) {
		String value = getStringContentFromFirstImageNodeChild(reader, imageNode, childNode);
		return Integer.parseInt(value);
	}
	
	private static BufferedImage getClippedImage(BufferedImage parentImage, int x, int y, int w, int h) {
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		{
			g.drawImage(parentImage, -x, -y, null);
		}
		g.dispose();
		return image;
	}
	
	public static PNGImage fromXML(BufferedImage parentImage, XMLReader reader, Node xmlImageNode) throws Exception {
		PNGImage output = new PNGImage();
		int x, y, w, h;
		int count, index;
		
		output.name = getStringContentFromFirstImageNodeChild(reader, xmlImageNode, "NAME");
		
		x = getIntContentFromFirstImageNodeChild(reader, xmlImageNode, "X");
		y = getIntContentFromFirstImageNodeChild(reader, xmlImageNode, "Y");
		w = getIntContentFromFirstImageNodeChild(reader, xmlImageNode, "W");
		h = getIntContentFromFirstImageNodeChild(reader, xmlImageNode, "H");
		output.image = getClippedImage(parentImage, x, y, w, h);
		
		w = (w < 1024) ? w+IMAGE_PADDING : w;
		h = (h < 1024) ? h+IMAGE_PADDING : h;
		output.bounds.setBounds(x, y, w, h);
		output.outline.setBounds(x, y, w, h);

		// import hitbox data
		Vector<Node> hitboxNodes = reader.getChildrenNodesByTagName(xmlImageNode, "HITBOX");
		for(Node hitboxNode : hitboxNodes) {
			HitboxType shape = HitboxType.getHitboxType(getStringContentFromFirstImageNodeChild(reader, hitboxNode, "SHAPE"));
			PNGHitbox hitbox = PNGHitbox.createHitbox(shape);
			hitbox.removable = !getBooleanContextFromFirstImageNodeChild(reader, hitboxNode, "PERM");
			hitbox.type = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "ID");
			
			switch(shape) {
			case CIRCLE:
				hitbox.x = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "X");
				hitbox.y = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "Y");
				hitbox.radius = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "RADIUS");
				break;
			case RECTANGLE:
				hitbox.x = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "X");
				hitbox.y = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "Y");
				hitbox.width = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "WIDTH");
				hitbox.height = getIntContentFromFirstImageNodeChild(reader, hitboxNode, "HEIGHT");
				break;
			case POLYGON:
				Node verticesNode = reader.getFirstChildNodeByTagName(hitboxNode, "VERTICES");
				count = getIntContentFromFirstImageNodeChild(reader, verticesNode, "COUNT");
				Vector<PointStr> vertices = new Vector<PointStr>(count);
				vertices.setSize(count);
				Vector<Node> vertexNodes = reader.getChildrenNodesByTagName(verticesNode, "VERTEX");
				for(Node vertexNode : vertexNodes) {
					index = getIntContentFromFirstImageNodeChild(reader, vertexNode, "ID");
					x = getIntContentFromFirstImageNodeChild(reader, vertexNode, "X");
					y = getIntContentFromFirstImageNodeChild(reader, vertexNode, "Y");
					vertices.set(index, new PointStr(x, y));
				}
				hitbox.setPolygonVertexValues(vertices);
				break;
			}
			output.hitboxes.add(hitbox);
		}
		return output;
	}
	
	public String toXML(int index) {
		StringBuffer xml = new StringBuffer();
		xml.append("\t<IMAGE NUMBER=\"").append(index).append("\">").append("\n");
		xml.append("\t\t<NAME>").append(name).append("</NAME>").append("\n");
		xml.append("\t\t<X>").append(bounds.x).append("</X>").append("\n");
		xml.append("\t\t<Y>").append(bounds.y).append("</Y>").append("\n");
		
		if(bounds.width >= 1024) {
			xml.append("\t\t<W>").append(bounds.width).append("</W>").append("\n");
		} else {
			xml.append("\t\t<W>").append(bounds.width-IMAGE_PADDING).append("</W>").append("\n");
		}
		
		if(bounds.height >= 1024) {
			xml.append("\t\t<H>").append(bounds.height).append("</H>").append("\n");
		} else {
			xml.append("\t\t<H>").append(bounds.height-IMAGE_PADDING).append("</H>").append("\n");
		}
		
		// hitbox information
		PNGHitbox hitbox;
		for(int i=0; i<hitboxes.size(); i++) {
			hitbox = hitboxes.elementAt(i);
			xml.append("\t\t<HITBOX NUMBER=\"").append(i).append("\">").append("\n");
			xml.append("\t\t\t<SHAPE>").append(hitbox.getShape().string()).append("</SHAPE>").append("\n");
			xml.append("\t\t\t<PERM>").append(!hitbox.isRemovable()).append("</PERM>").append("\n");
			xml.append("\t\t\t<ID>").append(hitbox.getType()).append("</ID>").append("\n");
			switch(hitbox.getShape()) {
			case CIRCLE:
				xml.append("\t\t\t<X>").append(hitbox.getX()).append("</X>").append("\n");
				xml.append("\t\t\t<Y>").append(hitbox.getY()).append("</Y>").append("\n");
				xml.append("\t\t\t<RADIUS>").append(hitbox.getRadius()).append("</RADIUS>").append("\n");
				break;
			case RECTANGLE:
				xml.append("\t\t\t<X>").append(hitbox.getX()).append("</X>").append("\n");
				xml.append("\t\t\t<Y>").append(hitbox.getY()).append("</Y>").append("\n");
				xml.append("\t\t\t<WIDTH>").append(hitbox.getWidth()).append("</WIDTH>").append("\n");
				xml.append("\t\t\t<HEIGHT>").append(hitbox.getHeight()).append("</HEIGHT>").append("\n");
				break;
			case POLYGON:
				{
					int which = 0;
					int count = hitbox.getVerticesCount();
					xml.append("\t\t\t<VERTICES>").append("\n");
					xml.append("\t\t\t\t<COUNT>").append(count).append("</COUNT>").append("\n");
					for(PointStr vertex : hitbox.getVertices()) {
						xml.append("\t\t\t\t<VERTEX>").append("\n");
						xml.append("\t\t\t\t\t<ID>").append(which++).append("</ID>").append("\n");
						xml.append("\t\t\t\t\t<X>").append(vertex.x).append("</X>").append("\n");
						xml.append("\t\t\t\t\t<Y>").append(vertex.y).append("</Y>").append("\n");
						xml.append("\t\t\t\t</VERTEX>").append("\n");
					}
					xml.append("\t\t\t</VERTICES>").append("\n");
				}
				break;
			}
			xml.append("\t\t</HITBOX>").append("\n");
		}
		
		xml.append("\t</IMAGE>").append("\n");
		return xml.toString();
	}
	
	public String toHeader(int index) {
		StringBuffer header = new StringBuffer();
		header.append("#define ").append(name).append("\t").append(index).append("\n");
		return header.toString();
	}

	public boolean equals(Object obj) {
		if(obj instanceof PNGImage) {
			PNGImage image = (PNGImage) obj;
			if(image.getName().equalsIgnoreCase(getName())) {
				return true;
			}
		}
		return false;
	}

}