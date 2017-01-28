package com.devculture.tools.TextureAtlasCreator.UI;

import java.awt.AlphaComposite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import com.devculture.swing.GraphicsPane;
import com.devculture.swing.PopupMessage;
import com.devculture.tools.TextureAtlasCreator.TextureAtlasMenuBar;
import com.devculture.tools.TextureAtlasCreator.TextureAtlasStatusBar;
import com.devculture.tools.TextureAtlasCreator.Data.Exporter;
import com.devculture.tools.TextureAtlasCreator.Data.ImageData;
import com.devculture.tools.TextureAtlasCreator.Data.Importer;
import com.devculture.tools.TextureAtlasCreator.Data.PNGImage;
import com.devculture.tools.TextureAtlasCreator.Data.PNGImage.PNGHitbox;
import com.devculture.tools.TextureAtlasCreator.Data.PNGImage.PNGHitbox.HitboxType;
import com.devculture.util.PointStr;
import com.devculture.util.RectanglePacker;
import com.devculture.util.Sound;
import com.devculture.util.Utilities;

public class ImageCanvas extends GraphicsPane implements DropTargetListener, MouseInputListener, KeyListener, ActionListener {

	// sub-modes under hitbox mode
	private enum ApplicationMode {
		NORMAL_MODE(null),
		HITBOX_MODE(null),
		HITBOX_ADD_CIRCLE_MODE(HitboxType.CIRCLE),
		HITBOX_ADD_RECTANGLE_MODE(HitboxType.RECTANGLE),
		HITBOX_ADD_POLYGON_MODE(HitboxType.POLYGON);
		
		private final HitboxType shape;
		
		private ApplicationMode(HitboxType shape) {
			this.shape = shape;
		}
		
		public HitboxType shape() {
			return shape;
		}
	};
	
	private void setApplicationMode(ApplicationMode mode) {
		TextureAtlasStatusBar statusbar = TextureAtlasStatusBar.getInstance();
		switch(mode) {
		case NORMAL_MODE:
			statusbar.setMessage("Mode: Normal mode");
			break;
		case HITBOX_MODE:
			statusbar.setMessage("Mode: General hitbox mode");
			break;
		case HITBOX_ADD_CIRCLE_MODE:
			statusbar.setMessage("Mode: Create circular hitbox");
			break;
		case HITBOX_ADD_RECTANGLE_MODE:
			statusbar.setMessage("Mode: Create rectangular hitbox");
			break;
		case HITBOX_ADD_POLYGON_MODE:
			statusbar.setMessage("Mode: Create polygon hitbox");
			break;
		}
		applicationMode = mode;
	}
	
	private boolean isHitboxMode() {
		switch(applicationMode) {
		case HITBOX_MODE:
		case HITBOX_ADD_CIRCLE_MODE:
		case HITBOX_ADD_RECTANGLE_MODE:
		case HITBOX_ADD_POLYGON_MODE:
			return true;
		}
		return false;
	}
	
	private void initializePolygonCreation() {
		PNGHitbox hitbox = PNGHitbox.createHitbox(ApplicationMode.HITBOX_ADD_POLYGON_MODE.shape());
		selectedHitboxImage.addHitbox(hitbox);
		selectedHitboxImage.setCurrent(hitbox);
	}
	
	private void finalizePolygonCreation() {
		PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
		if(hitbox != null) {
			if(hitbox.getVerticesCount() < 3) {
				Sound.beep();
				selectedHitboxImage.removeSelectedHitbox();
				// hide property panes
				propertiesHandler.setAllPropertyPaneHidden();
			}
		}
	}
	
	// isolation mode mouse handler
	private class HitboxModeMouseHandler {
		
		private int mouseDownX = 0;
		private int mouseDownY = 0;
		private boolean hitboxCreationDistanceThreshold = false;
		private final int HITBOX_DISTANCE_THRESHOLD = 5;
		private PNGHitbox movingHitbox = null;
		
		private void hitboxBegan(int respectiveX, int respectiveY) {
			PNGHitbox hitbox;
			hitboxCreationDistanceThreshold = false;
			mouseDownX = respectiveX;
			mouseDownY = respectiveY;
			
			switch(applicationMode) {
				case HITBOX_MODE:
					movingHitbox = null;
					hitbox = selectedHitboxImage.getCurrentHitbox();
					if(hitbox != null && hitbox.contains(mouseDownX, mouseDownY)) {
						movingHitbox = hitbox;
						movingHitbox.setOffset(respectiveX, respectiveY);
					}
					break;
				case HITBOX_ADD_POLYGON_MODE:
					// assume we are moving a vertex
					hitbox = selectedHitboxImage.getCurrentHitbox();
					if(hitbox != null && hitbox.getShape() == HitboxType.POLYGON) {
						hitbox.setMovingVertex(mouseDownX, mouseDownY);
					}
					break;
			}
		}
		
		private void hitboxUpdate(int respectiveX, int respectiveY) {
			int dx = respectiveX - mouseDownX;
			int dy = respectiveY - mouseDownY;
			int dist = (int)Math.sqrt(dx*dx + dy*dy);
			
			switch(applicationMode) {
				case HITBOX_MODE:
					if(movingHitbox != null) {
						movingHitbox.updateOffset(respectiveX, respectiveY);
					}
					break;
				case HITBOX_ADD_CIRCLE_MODE:
				case HITBOX_ADD_RECTANGLE_MODE:
					// make sure we pass threshold for simple hitbox creation
					if(dist > HITBOX_DISTANCE_THRESHOLD) {
						// make sure we only create object once after the threshold is surpassed
						if(!hitboxCreationDistanceThreshold) {
							PNGHitbox hitbox = PNGHitbox.createHitbox(applicationMode.shape(), mouseDownX, mouseDownY);
							selectedHitboxImage.addHitbox(hitbox);
							selectedHitboxImage.setCurrent(hitbox);
							hitboxCreationDistanceThreshold = true;
						}
						PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
						if(hitbox != null && hitbox.getShape() == HitboxType.CIRCLE) {
							hitbox.setRadius(dist);
						} else if(hitbox.getShape() == HitboxType.RECTANGLE) {
							hitbox.setWidth(dx);
							hitbox.setHeight(dy);
						}
					}
					break;
				case HITBOX_ADD_POLYGON_MODE:
					// ensure this is not a click event
					if(dx != 0 && dy != 0) {
						PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
						if(hitbox != null && hitbox.getShape() == HitboxType.POLYGON) {
							hitbox.setMovingVertex(mouseDownX, mouseDownY);
							hitbox.moveMovingVertex(respectiveX, respectiveY);
						}
					}
					break;
			}
		}

		private void hitboxEnded(int respectiveX, int respectiveY) {
			int dx = respectiveX - mouseDownX;
			int dy = respectiveY - mouseDownY;

			switch(applicationMode) {
				case HITBOX_MODE:
					if(dx == 0 && dy == 0) {
						// select the next hitbox that my mouse-down touches
						selectedHitboxImage.setCurrent(selectedHitboxImage.getNextHitboxAt(respectiveX, respectiveY));
					} else {
						if(movingHitbox != null) {
							movingHitbox.updateOffset(respectiveX, respectiveY);
						}
						movingHitbox = null;
					}
					break;
				case HITBOX_ADD_CIRCLE_MODE:
				case HITBOX_ADD_RECTANGLE_MODE:
					if(hitboxCreationDistanceThreshold) {
						// once we created a simple hitbox, revert to hitbox mode
						setApplicationMode(ApplicationMode.HITBOX_MODE);
					}
					break;
				case HITBOX_ADD_POLYGON_MODE:
					PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
					if(hitbox != null && hitbox.getShape() == HitboxType.POLYGON) {
						if(dx == 0 && dy == 0) {
							// click event
							int index = hitbox.getFirstVertexIndexNear(mouseDownX, mouseDownY);
							if(index == -1) {
								// create new polygon at x, y
								hitbox.addPolygonVertex(respectiveX, respectiveY);
								hitbox.setSelectedVertex(-1);
							} else {
								// toggle selection
								if(hitbox.isSelectedVertex(index)) {
									hitbox.setSelectedVertex(-1);
								} else {
									hitbox.setSelectedVertex(index);
								}
							}
						} else {
							// finalize drag event
							hitbox.setMovingVertex(mouseDownX, mouseDownY);
							hitbox.moveMovingVertex(respectiveX, respectiveY);
						}
						hitbox.resetMovingVertex();
					}
					break;
			}
		}
		
		public void mousePressed(int mouseX, int mouseY) {
			if(selectedHitboxImage != null) {
				int x = mouseX - selectedHitboxImage.getPositionX();
				int y = mouseY - selectedHitboxImage.getPositionY();
				hitboxBegan(x, y);
			}
			repaint();
		}
		
		public void mouseDragged(int mouseX, int mouseY) {
			if(selectedHitboxImage != null) {
				int x = mouseX - selectedHitboxImage.getPositionX();
				int y = mouseY - selectedHitboxImage.getPositionY();
				hitboxUpdate(x, y);
			}
			repaint();
		}
		
		public void mouseReleased(int mouseX, int mouseY) {
			if(selectedHitboxImage != null) {
				int x = mouseX - selectedHitboxImage.getPositionX();
				int y = mouseY - selectedHitboxImage.getPositionY();
				hitboxUpdate(x, y);
				hitboxEnded(x, y);
				propertiesHandler.loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
			}
			repaint();
		}

	}

	// handles the image property pane that pops up
	private class PropertyPaneHandler implements MouseInputListener, FocusListener, KeyListener, ActionListener, ListSelectionListener {

		// not used
		public void mouseClicked(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mouseMoved(MouseEvent e) { }
		public void focusGained(FocusEvent e) { }
		public void keyPressed(KeyEvent e) { }
		public void keyTyped(KeyEvent e) { }

		private int mouseDownX;
		private int mouseDownY;
		
		// private JPanel propertyPane;
		private JPanel propertyPanes[] = new JPanel[HitboxType.TOTAL.index()];

		public void mousePressed(MouseEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			mouseDownX = e.getX();
			mouseDownY = e.getY();
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			int dx = e.getX() - mouseDownX;
			int dy = e.getY() - mouseDownY;
			setPropertyPanePosition(getPropertyPaneCurrent().getX() + dx, getPropertyPaneCurrent().getY() + dy);
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			int dx = e.getX() - mouseDownX;
			int dy = e.getY() - mouseDownY;
			setPropertyPanePosition(getPropertyPaneCurrent().getX() + dx, getPropertyPaneCurrent().getY() + dy);
			repaint();
		}

		public void focusLost(FocusEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			loadHitboxWithPropertyPaneValues();
			repaint();
		}

		public void keyReleased(KeyEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			if(e.getKeyCode() == 10) {
				loadHitboxWithPropertyPaneValues();
			}
			repaint();
		}

		public void actionPerformed(ActionEvent e) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) return;
			String action = e.getActionCommand();
			if(action != null) {
				loadHitboxWithPropertyPaneValues();
			}
			repaint();
		}
		
		// property pane methods
		
		public void setPropertyPanePosition(float x, float y) {
			getPropertyPaneCurrent().setLocation((int)(x * viewScale), (int)(y * viewScale));
		}

		public void setPropertyPanePosition(Point point) {
			setPropertyPanePosition(point.x, point.y);
		}

		private void showPropertyPane() {
			if(selectedHitboxImage != null) {
				PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
				if(hitbox != null) {
					setPropertyPanePosition(hitbox.getBottomRightPosition(selectedHitboxImage.getPositionX(), selectedHitboxImage.getPositionY()));
					setPropertyPaneToLocked(!hitbox.isRemovable());
					setAllPropertyPaneHidden();
					setPropertyPaneVisibility(true);
				}
			}
		}
		
		private void setAllPropertyPaneHidden() {
			try {
				for(int i=0; i<HitboxType.TOTAL.index(); i++) {
					getPropertyPaneByHitboxType(PNGHitbox.HitboxType.getHitboxType(i)).setVisible(false);
				}
			} catch(Exception ex) {
				System.err.println("Error: Failed to retrieve property pane. " + ex.getMessage());
			}
		}
		
		private void setPropertyPaneVisibility(boolean visible) {
			getPropertyPaneCurrent().setVisible(visible);
		}

		private void setPropertyPaneToLocked(boolean locked) {
			JPanel pane = getPropertyPaneCurrent();
			for(Component component : pane.getComponents()) {
				component.setEnabled(!locked);
			}
		}

		// additional getters
		
		private JPanel getPropertyPaneCurrent() {
			// property panes only exist in hitbox mode
			if(selectedHitboxImage != null) {
				PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
				if(hitbox != null) {
					return getPropertyPaneByHitboxType(hitbox.getShape());
				}
			}
			// actually this should never happen
			return getPropertyPaneByHitboxType(PNGHitbox.HITBOX_SHAPE_DEFAULT);
		}
		
		private JPanel getPropertyPaneByHitboxType(HitboxType type) {
			final int index = type.index();
			
			// validate the index
			if(index < 0 || index >= HitboxType.TOTAL.index()) return null;
			
			JPanel pane = propertyPanes[index];
			
			// check if pane already exists
			if(pane == null) {
				// make sure we initialize only once
				pane = new JPanel();
				
				// set up contents
				switch(type) {
				case CIRCLE:
					// set panel size
					pane.setBounds(0, 0, 120, 130+40);
					pane.add(createLabel(type.string(), 	10, 	10, 	100, 	20));
					pane.add(createLabel("ID",		10, 	40, 	20, 	20));
					pane.add(createField("ID", 	5,	30, 	40, 	80, 	20));
					pane.add(createLabel("X", 		10, 	70, 	20, 	20));
					pane.add(createField("X", 	5,	30, 	70, 	80, 	20));
					pane.add(createLabel("Y", 		10, 	100,	20,		20));
					pane.add(createField("Y", 	5,	30, 	100, 	80, 	20));
					pane.add(createLabel("R", 		10, 	130,	20,		20));
					pane.add(createField("R", 	5,	30, 	130, 	80, 	20));
					break;
				case RECTANGLE:
					// set panel size
					pane.setBounds(0, 0, 120, 160+40);
					pane.add(createLabel(type.string(), 	10, 	10, 	100, 	20));
					pane.add(createLabel("ID",		10, 	40, 	20, 	20));
					pane.add(createField("ID", 	5,	30, 	40, 	80, 	20));
					pane.add(createLabel("X", 		10, 	70, 	20, 	20));
					pane.add(createField("X", 	5,	30, 	70, 	80, 	20));
					pane.add(createLabel("Y", 		10, 	100,	20,		20));
					pane.add(createField("Y", 	5,	30, 	100, 	80, 	20));
					pane.add(createLabel("W", 		10, 	130,	20,		20));
					pane.add(createField("W", 	5,	30, 	130, 	80, 	20));
					pane.add(createLabel("H", 		10, 	160,	20,		20));
					pane.add(createField("H", 	5,	30, 	160, 	80, 	20));
					break;
				case POLYGON:
					// set panel size
					pane.setBounds(0, 0, 120, 200);
					pane.add(createLabel(type.string(), 	10, 	10, 	100, 	20));
					pane.add(createScrollableList("VTX", 	10, 	40, 	100, 	150));
					break;
				}

				// set properties
				pane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
				pane.addMouseMotionListener(this);
				pane.addMouseListener(this);
				pane.setFocusable(true);
				pane.setLayout(null);
				pane.setVisible(false);

				propertyPanes[index] = pane;
				
				// add to the canvas
				add(pane);
			}
			
			return pane;
		}
		
		private Component getComponentNamed(String name) {
			Component[] components = getPropertyPaneCurrent().getComponents();
			for(Component c : components) {
				if(name.equals(c.getName())) {
					return c;
				}
			}
			return null;
		}
		
		private int getIntValueFromJTextFieldNamed(String name) {
			int value = 0;
			JTextField field = (JTextField)getComponentNamed(name);
			if(field != null) {
				try {
					String text = field.getText();
					value = Integer.parseInt(text);
				} catch(Exception ex) {
					value = 0;
				}
			}
			return value;
		}

		// load & update hitbox information
		
		private void updatePropertyPaneJList(String id, Vector<PointStr> vertices) {
			JScrollPane scroller = ((JScrollPane)getComponentNamed(id));
			JList list = null;
			if(scroller != null) {
				JViewport port = scroller.getViewport();
				for(Component component : port.getComponents()) {
					if(id.equals(component.getName())) {
						list = (JList)component;
						list.setListData(vertices);
						break;
					}
				}
			}
		}
		
		private void updatePropertyPaneJTextField(String id, int value) {
			JTextField field = ((JTextField)getComponentNamed(id));
			if(field != null) {
				field.setText(Integer.toString(value));
			}
		}
		
		private void loadHitboxWithPropertyPaneValues() {
			if(selectedHitboxImage != null) {
				PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
				if(hitbox != null) {
					hitbox.setType(getIntValueFromJTextFieldNamed("ID"));
					switch(hitbox.getShape()) {
					case CIRCLE:
						hitbox.setCircularValues(getIntValueFromJTextFieldNamed("X"), getIntValueFromJTextFieldNamed("Y"), getIntValueFromJTextFieldNamed("R"));
						break;
					case RECTANGLE:
						hitbox.setRectangularValues(getIntValueFromJTextFieldNamed("X"), getIntValueFromJTextFieldNamed("Y"), getIntValueFromJTextFieldNamed("W"), getIntValueFromJTextFieldNamed("H"));
						break;
					case POLYGON:
						// do nothing, the values from the pane are read-only
						break;
					}
				}
			}
			
			// make sure the property pane shows up-to-date info
			loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
		}
		
		private void loadPropertyPaneWithHitboxValues(PNGHitbox hitbox) {
			if(hitbox != null) {
				switch(hitbox.getShape()) {
				case CIRCLE:
					updatePropertyPaneJTextField("ID", hitbox.getType());
					updatePropertyPaneJTextField("X", hitbox.getX());
					updatePropertyPaneJTextField("Y", hitbox.getY());
					updatePropertyPaneJTextField("R", hitbox.getRadius());
					break;
				case RECTANGLE:
					updatePropertyPaneJTextField("ID", hitbox.getType());
					updatePropertyPaneJTextField("X", hitbox.getX());
					updatePropertyPaneJTextField("Y", hitbox.getY());
					updatePropertyPaneJTextField("W", hitbox.getWidth());
					updatePropertyPaneJTextField("H", hitbox.getHeight());
					break;
				case POLYGON:
					updatePropertyPaneJList("VTX", hitbox.getVertices());
					break;
				}
				propertiesHandler.showPropertyPane();
			} else {
				propertiesHandler.setAllPropertyPaneHidden();
			}
		}
		
		// creators for initialization
		
		private JScrollPane createScrollableList(String id, int x, int y, int width, int height) {
			// use setListData() later
			JList list = new JList();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setLayoutOrientation(JList.VERTICAL);
			list.setVisibleRowCount(-1);
			list.addListSelectionListener(this);
			list.setEnabled(false);
			list.setName(id);
			
			JScrollPane scroller = new JScrollPane(list);
			scroller.setName(id);
			scroller.setBounds(x, y, width, height);
			scroller.setPreferredSize(new Dimension(width, height));
			return scroller;
		}
		
		private JLabel createLabel(String title, int x, int y, int width, int height) {
			JLabel label = new JLabel(title);
			label.setBounds(x, y, width, height);
			return label;
		}
		
		private JTextField createField(String id, int columns, int x, int y, int width, int height) {
			JTextField field = new JTextField(columns);
			field.setName(id);
			field.setBounds(x, y, width, height);
			field.addFocusListener(this);
			field.addKeyListener(this); 
			return field;
		}
		
		public void valueChanged(ListSelectionEvent e) {
			JList list = (JList)e.getSource();
			PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
			if(hitbox != null) {
				if(list == null) {
					hitbox.setSelectedVertex(-1);
				} else {
					hitbox.setSelectedVertex(list.getSelectedIndex());
				}
			}
		}
	}
	
	private static final long serialVersionUID = 1L;

	// iso mode
	private PropertyPaneHandler propertiesHandler = null;
	private HitboxModeMouseHandler hitboxmodeMouseHandler = null;
	private PNGImage selectedHitboxImage = null;
	private ApplicationMode applicationMode = ApplicationMode.NORMAL_MODE;

	// magnification
	private double viewScale = 1.0f;
	
	private ImageData imageDataRef = null;
	private Vector<PNGImage> imagesRef = null;
	
	private Point mouseDown = new Point(0, 0);
	private PNGImage mouseDownImage = null;
	private Rectangle mouseSelection = null;
	boolean dragmode = false;

	// modifier keys (unique to my computer)
	private int modifiers = 0;
	public final static int MODIFIER_SHIFT = InputEvent.SHIFT_MASK;
	public final static int MODIFIER_ALT = InputEvent.CTRL_MASK;
	public final static int MODIFIER_CTRL = InputEvent.META_MASK;
	
	private final BasicStroke THIN_STROKE = new BasicStroke(1f);
	private final BasicStroke DOTTED_STROKE = new BasicStroke(3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] {2f}, 0f);
	private final Color COLOR_TEXTURE_SIZE_OUTLINE = new Color(0xcccccc);
	private final Color COLOR_IMAGE_OUTLINE = Color.gray;
	private final Color COLOR_IMAGE_OUTLINE_SELECTED = new Color(0x55cc55);
	private final Color COLOR_IMAGE_OUTLINE_DRAGGED = Color.darkGray;
	private final Color DEFAULT_BACKGROUND_COLOR = Color.white;
	private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
	
	public ImageCanvas(ImageData imageData) {
		imageDataRef = imageData;
		setDropTarget(new DropTarget(this, this));
		
		// isolation mode handler
		hitboxmodeMouseHandler = new HitboxModeMouseHandler();
		
		// property pane handler
		propertiesHandler = new PropertyPaneHandler();
		
		// just grab reference
		imagesRef = imageDataRef.getPNGImages();
		
		// set up listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setFocusable(true);
		
		// layout null so the property panel is movable
		setLayout(null);
		
		setApplicationMode(ApplicationMode.NORMAL_MODE);
	}
	
	private void setViewScale(double scale) {
		setPreferredSize(new Dimension((int)(1280 * scale), (int)(1024 * scale)));
		viewScale = scale;
		propertiesHandler.showPropertyPane();
	}
	
	/** paint **/
	
	public static final AlphaComposite ALPHA_85 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f);
	public static final AlphaComposite ALPHA_55 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f);
	public static final AlphaComposite ALPHA_35 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f);
	public static final AlphaComposite ALPHA_15 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f);
	
	public void paintComponent(Graphics g) {
		// draw dotted rectangle for movement
		Graphics2D gfx = (Graphics2D) g.create();
		{
			gfx.scale(viewScale, viewScale);
			
			gfx.setColor(Color.lightGray);
			gfx.fillRect(0, 0, getWidth(), getHeight());
			gfx.setColor(backgroundColor);
			gfx.fillRect(0, 0, 1024, 1024);
			
			gfx.setColor(COLOR_TEXTURE_SIZE_OUTLINE);
			gfx.drawRect(-1, -1, 128, 128);
			gfx.drawRect(-1, -1, 256, 256);
			gfx.drawRect(-1, -1, 512, 512);
			gfx.drawRect(-1, -1, 1024, 1024);
			
			// draw the images
			for(PNGImage image : imagesRef) {
				if(isHitboxMode()) {
					// fade out non-selected images
					gfx.setComposite(image == selectedHitboxImage ? ALPHA_55 : ALPHA_15);
				}
				
				// draw image
				gfx.drawImage(image.getImage(), image.getPositionX(), image.getPositionY(), null);
				
				// draw image outline
				if(applicationMode == ApplicationMode.NORMAL_MODE) {
					gfx.setColor((image.isSelected() || image.isPreselected()) ? COLOR_IMAGE_OUTLINE_SELECTED : COLOR_IMAGE_OUTLINE);
					gfx.drawRect(image.getPositionX(), image.getPositionY(), image.getBounds().width-1, image.getBounds().height-1);
				}
				
				// draw the hitboxes
				if(isHitboxMode()) {
					if(image == selectedHitboxImage) {
						Vector<PNGHitbox> hitboxes = image.getHitboxes();

						// dotted stroke
						gfx.setStroke(THIN_STROKE);
						
						for(PNGHitbox hitbox : hitboxes) {
							boolean selected = image.isCurrentHitbox(hitbox);
							gfx.setColor(selected ? Color.RED : Color.BLUE);
							gfx.setComposite(selected ? ALPHA_35 : ALPHA_15);
							hitbox.draw(gfx, image.getPositionX(), image.getPositionY(), selected);
						}
					}
				}
			}

			// draw image selection
			gfx.setStroke(DOTTED_STROKE);
			gfx.setColor(COLOR_IMAGE_OUTLINE_DRAGGED);
			
			// draw mouse select rectangle
			if(mouseSelection != null) {
				gfx.drawRect(mouseSelection.x, mouseSelection.y, mouseSelection.width, mouseSelection.height);
			} else {
				for(PNGImage image : imagesRef) {
					// if outline is offset, draw the dotted line representation
					if(image.isOutlineMoved()) {
						gfx.drawRect(image.getOutlinePositionX(), image.getOutlinePositionY(), image.getWidth(), image.getHeight());
					}
				}
			}
		}
		gfx.dispose();
	}

	/** packer **/
	
	public void packImages() {
		RectanglePacker packer = null;
		Point coords = null;
		
		boolean tooSmall = false;
		int power = 2;
		int dimension = 0;
		
		do {
			dimension = (int) Math.pow(2, power);
			packer = new RectanglePacker(dimension, dimension);
			tooSmall = false;
			
			for(PNGImage image : imagesRef) {
				coords = packer.findCoords(image.getWidth(), image.getHeight());
				if(coords != null) {
					// successfully placed this block onto the canvas
					image.setPosition(coords.x, coords.y);
				} else {
					// failed to fit, place image outside the boundaries
					image.setPosition(1024, 0);
					tooSmall = true;
				}
			}
		} while (tooSmall && ++power <= 10);
	}
	
	/** mouse control **/
	
	public void keepRectInBounds(Rectangle rect, int left, int top, int right, int bottom) {
		if(rect.x < left) {
			rect.x = left;
		}
		if(rect.y < top) {
			rect.y = top;
		}
		if(rect.x + rect.width >= right) {
			rect.x = right - rect.width - 1;
		}
		if(rect.y + rect.height >= bottom) {
			rect.y = bottom - rect.height - 1;
		}
	}

	public PNGImage getTopImageTouchedBy(Point point) {
		PNGImage image = null;
		for(int i=imagesRef.size()-1; i>=0; i--) {
			image = imagesRef.elementAt(i);
			if(image.getBounds().contains(point)) {
				// top image found
				return image;
			}
		}
		// image was not found
		return null;
	}
	
	public int getSelectedImageCount() {
		int count = 0;
		for(PNGImage image : imagesRef) {
			if(image.isSelected()) {
				count++;
			}
		}
		return count;
	}
	
	public PNGImage getFirstSelectedImage() {
		for(PNGImage image : imagesRef) {
			if(image.isSelected()) {
				return image;
			}
		}
		return null;
	}
	
	public void deleteAllSelectedImages() {
		int count = imagesRef.size();
		for(int i=count-1; i>=0; i--) {
			PNGImage image = imagesRef.elementAt(i);
			if(image.isSelected() || image.isPreselected()) {
				imagesRef.remove(i);
			}
		}
	}
	
	public void deselectAllImages() {
		for(PNGImage image : imagesRef) {
			image.deselect();
		}
	}
	
	public void selectAllImages() {
		for(PNGImage image : imagesRef) {
			image.select();
		}
	}
	
	public void shiftSelectedImageOutlinesPositionBy(int dx, int dy) {
		Rectangle outline = null;
		for(PNGImage image : imagesRef) {
			if(image.isSelected() || image.isPreselected()) {
				outline = image.getOutline();
				outline.setLocation(image.getPositionX() + dx, image.getPositionY() + dy);
				keepRectInBounds(outline, 0, 0, 1024, 1024);
			}
		}
	}

	public void shiftSelectedImagesPositionBy(int dx, int dy) {
		Rectangle bounds = new Rectangle();
		for(PNGImage image : imagesRef) {
			if(image.isSelected() || image.isPreselected()) {
				bounds.setBounds(image.getPositionX()+dx, image.getPositionY()+dy, image.getWidth(), image.getHeight());
				keepRectInBounds(bounds, 0, 0, 1024, 1024);
				image.setPosition(bounds.x, bounds.y);
			}
		}
	}
	
	public void resizeSelectionRectangle(int x, int y, int width, int height) {
		if(width != 0 && height != 0) {
			mouseSelection = new Rectangle(x, y, width, height);
		} else {
			mouseSelection = null;
		}
	}
	
	public void mousePressed(MouseEvent me) {
		int mouseX = (int)((float)me.getPoint().x / viewScale);
		int mouseY = (int)((float)me.getPoint().y / viewScale);
		
		if(isHitboxMode()) {
			hitboxmodeMouseHandler.mousePressed(mouseX, mouseY);
			return;
		}
		
		mouseDown = new Point(mouseX, mouseY);
		mouseDownImage = getTopImageTouchedBy(mouseDown);
		
		if(mouseDownImage != null) {
			// if user does not pressed shift key, then deselect all
			if(!mouseDownImage.isSelected() && !isSingleModifierPressed(MODIFIER_SHIFT)) {
				deselectAllImages();
			}

			// user touched an image, preselect that image
			if(mouseDownImage != null && !mouseDownImage.isSelected()) {
				mouseDownImage.preselect();
			}
			
			// we can drag the selected images around
			dragmode = true;
		}
		
		repaint();
	}

	public void mouseDragged(MouseEvent me) {
		int mouseX = (int)((float)me.getPoint().x / viewScale);
		int mouseY = (int)((float)me.getPoint().y / viewScale);
		
		if(isHitboxMode()) {
			hitboxmodeMouseHandler.mouseDragged(mouseX, mouseY);
			return;
		}

		Point mouseCurrent = new Point(mouseX, mouseY);
		int dx = mouseCurrent.x - mouseDown.x; 
		int dy = mouseCurrent.y - mouseDown.y;
		
		if(dragmode) {
			// drag image outlines
			shiftSelectedImageOutlinesPositionBy(dx, dy);
		} else {
			// drag selection outline
			resizeSelectionRectangle(Math.min(mouseDown.x, mouseCurrent.x), Math.min(mouseDown.y, mouseCurrent.y), Math.abs(dx), Math.abs(dy));
		}
		
		repaint();
	}

	public void mouseReleased(MouseEvent me) {
		int mouseX = (int)((float)me.getPoint().x / viewScale);
		int mouseY = (int)((float)me.getPoint().y / viewScale);

		if(isHitboxMode()) {
			hitboxmodeMouseHandler.mouseReleased(mouseX, mouseY);
			return;
		}

		Point mouseUp = new Point(mouseX, mouseY);
		
		if(mouseSelection != null) {
			// every selection 
			if(!isSingleModifierPressed(MODIFIER_SHIFT)) {
				deselectAllImages();
			}
			
			// select images intersected by the mouse selection outline
			for(PNGImage image : imagesRef) {
				if(mouseSelection.intersects(image.getBounds())) {
					image.select();
				}
			}
		} else if(mouseUp.equals(mouseDown)) {
			if(mouseDownImage != null) {
				if(mouseDownImage.isPreselected()) {
					// select this image
					mouseDownImage.select();
				} else if(mouseDownImage.isSelected()) {
					if(isSingleModifierPressed(MODIFIER_SHIFT)) {
						// if shift is pressed, de-select the image
						mouseDownImage.deselect();
					} else {
						// ignore
					}
				} else if(!mouseDownImage.isSelected()) {
					// remove all other selected, and select only this image
					deselectAllImages();
					mouseDownImage.select();
				}
			} else {
				// deselect all
				deselectAllImages();
			}
		} else if(dragmode) {			
			// move selected images to outline position
			for(PNGImage image : imagesRef) {
				if(image.isSelected() || image.isPreselected()) {
					// dyu: fix preselectiong
					image.select();
					image.moveToOutlinePosition();
				}
			}
		}
		
		mouseSelection = null;
		dragmode = false;
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		// ignore
	}

	public void mouseEntered(MouseEvent e) {
		// ignore
	}

	public void mouseExited(MouseEvent e) {
		// ignore
	}
	
	public void mouseMoved(MouseEvent e) {
		// ignore
	}
	
	/** key events **/
	
	private boolean isModifierPressed(int modifier) {
		return (modifiers & modifier) != 0;
	}
	
	private boolean isSingleModifierPressed(int modifier) {
		return isModifierPressed(modifier)  && modifiers == modifier;
	}

	public void keyReleased(KeyEvent e) {
		modifiers = e.getModifiers();
	}

	public void keyTyped(KeyEvent e) {
		modifiers = e.getModifiers();
	}

	public void keyPressed(KeyEvent e) {
		modifiers = e.getModifiers();

		int shiftMultiplier = isSingleModifierPressed(MODIFIER_SHIFT) ? 10 : 1;
		
		switch(e.getKeyCode()) {
		case KeyEvent.VK_BACK_SPACE:
		case KeyEvent.VK_DELETE:
			switch(applicationMode) {
			case HITBOX_MODE:
				if(selectedHitboxImage != null) {
					if(!selectedHitboxImage.removeSelectedHitbox()) {
						// failed to remove... play audio
					} else {
						propertiesHandler.setAllPropertyPaneHidden();
					}
				}
				break;
			case HITBOX_ADD_POLYGON_MODE:
				if(selectedHitboxImage != null) {
					// delete the selected polygon vertex
					PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
					if(hitbox != null && hitbox.getShape() == HitboxType.POLYGON) {
						hitbox.removeSelectedVertex();
					}
				}
				break;
			case NORMAL_MODE:
				deleteAllSelectedImages();
				break;
			}
			break;
		case KeyEvent.VK_LEFT:
			if(isHitboxMode()) {
				selectedHitboxImage.shiftSelectedHitboxPositionBy(-1 * shiftMultiplier, 0);
				propertiesHandler.loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
			} else {
				shiftSelectedImagesPositionBy(-1 * shiftMultiplier, 0);
			}
			break;
		case KeyEvent.VK_RIGHT:
			if(isHitboxMode()) {
				selectedHitboxImage.shiftSelectedHitboxPositionBy(1 * shiftMultiplier, 0);
				propertiesHandler.loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
			} else {
				shiftSelectedImagesPositionBy(1 * shiftMultiplier, 0);
			}
			break;
		case KeyEvent.VK_DOWN:
			if(isHitboxMode()) {
				selectedHitboxImage.shiftSelectedHitboxPositionBy(0, 1 * shiftMultiplier);
				propertiesHandler.loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
			} else {
				shiftSelectedImagesPositionBy(0, 1 * shiftMultiplier);
			}
			break;
		case KeyEvent.VK_UP:
			if(isHitboxMode()) {
				selectedHitboxImage.shiftSelectedHitboxPositionBy(0, -1 * shiftMultiplier);
				propertiesHandler.loadPropertyPaneWithHitboxValues(selectedHitboxImage.getCurrentHitbox());
			} else {
				shiftSelectedImagesPositionBy(0, -1 * shiftMultiplier);
			}
			break;
		case KeyEvent.VK_ENTER:
			switch(applicationMode) {
			case HITBOX_ADD_POLYGON_MODE:
				finalizePolygonCreation();
				break;
			}
			setApplicationMode(ApplicationMode.HITBOX_MODE);
			break;
		}
		
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		String command = source.getText();
		
		if(command.equalsIgnoreCase(TextureAtlasMenuBar.OPEN_COMMAND)) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) {
				Importer.chooseXMLDataFile(imagesRef);
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.SAVE_COMMAND)) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) {
				Exporter.export(imagesRef);
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.REPACK_COMMAND)) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) {
				packImages();
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.QUIT_COMMAND)) {
			Sound.beep();
			int answer = PopupMessage.askForUserYesNo("Quit", "Are you sure you want to quick the applications?");
			if(answer == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.UNDO_COMMAND)) {
			// TODO
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.REDO_COMMAND)) {
			// TODO
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.TOGGLE_HITBOXMODE_COMMAND)) {
			// make sure only 1 image has been selected
			if(getSelectedImageCount() == 1) {
				// point to selected single image, or null it
				if(isHitboxMode()) {
					setApplicationMode(ApplicationMode.NORMAL_MODE);
					selectedHitboxImage = null;
					propertiesHandler.setAllPropertyPaneHidden(); // PropertyPaneVisibility(false);
				} else {
					setApplicationMode(ApplicationMode.HITBOX_MODE);
					selectedHitboxImage = getFirstSelectedImage();
					if(selectedHitboxImage != null) {
						PNGHitbox hitbox = selectedHitboxImage.getCurrentHitbox();
						if(hitbox != null) {
							propertiesHandler.loadPropertyPaneWithHitboxValues(hitbox);
							propertiesHandler.showPropertyPane();
						}
					}
				}
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.ADD_CIRCLE_HITBOX_COMMAND)) {
			switch(applicationMode) {
			case NORMAL_MODE:
				Sound.beep();
				break;
			case HITBOX_ADD_POLYGON_MODE:
				finalizePolygonCreation();
			case HITBOX_MODE:
			case HITBOX_ADD_CIRCLE_MODE:
			case HITBOX_ADD_RECTANGLE_MODE:
				setApplicationMode(ApplicationMode.HITBOX_ADD_CIRCLE_MODE);
				break;
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.ADD_RECTANGLE_HITBOX_COMMAND)) {
			switch(applicationMode) {
			case NORMAL_MODE:
				Sound.beep();
				break;
			case HITBOX_ADD_POLYGON_MODE:
				finalizePolygonCreation();
			case HITBOX_MODE:
			case HITBOX_ADD_CIRCLE_MODE:
			case HITBOX_ADD_RECTANGLE_MODE:
				setApplicationMode(ApplicationMode.HITBOX_ADD_RECTANGLE_MODE);
				break;
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.ADD_POLYGON_HITBOX_COMMAND)) {
			switch(applicationMode) {
			case NORMAL_MODE:
				Sound.beep();
				break;
			case HITBOX_ADD_POLYGON_MODE:
				finalizePolygonCreation();
			case HITBOX_MODE:
			case HITBOX_ADD_CIRCLE_MODE:
			case HITBOX_ADD_RECTANGLE_MODE:
				setApplicationMode(ApplicationMode.HITBOX_ADD_POLYGON_MODE);
				initializePolygonCreation();
				break;
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.SELECTALL_COMMAND)) {
			if(applicationMode == ApplicationMode.NORMAL_MODE) {
				selectAllImages();
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.CANVAS_1X_COMMAND)) {
			setViewScale(1.0f);
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.CANVAS_2X_COMMAND)) {
			setViewScale(2.0f);
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.CANVAS_3X_COMMAND)) {
			setViewScale(3.0f);
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.CANVAS_4X_COMMAND)) {
			setViewScale(4.0f);
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.COLOR_COMMAND)) {
			String colorString = PopupMessage.askForUserInput("Change Background Color", "Enter Hexidecimal Color: 0xff00ff");
			if(colorString != null) {
				if(!Utilities.isValidColorString(colorString)) {
					Sound.beep();
					PopupMessage.showErrorMessage("Could not recognize " + colorString + " as a valid hexidecimal color format");
				} else {
					backgroundColor = Utilities.getColorFromString(colorString);
				}
			} else {
				Sound.beep();
			}
		} else if(command.equalsIgnoreCase(TextureAtlasMenuBar.RESET_COLOR_COMMAND)) {
			backgroundColor = DEFAULT_BACKGROUND_COLOR;
		}
		repaint();
	}

	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent event) {
		boolean error = false;
		
		// disallow drop events
		if(applicationMode != ApplicationMode.NORMAL_MODE) {
			Sound.beep();
			event.rejectDrop();
			return;
		}
		
		try {
			Transferable transfer = event.getTransferable();
			if(transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<Object> files = (List<Object>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator<Object> iterator = files.iterator();
				
				while(iterator.hasNext()) {
					File file = (File)iterator.next();
					
					// make sure object is a file, and also is a valid dropped file
					if(file instanceof File && isValidDroppedFile(file)) {
						String filename = file.getName();
						if(filename.toLowerCase().endsWith(".xml")) {
							// is xml
							Importer.importXMLDataFile(imageDataRef.getPNGImages(), file);
							break;
						} else {
							// assume image
							PNGImage image = new PNGImage(file);
							if(imageDataRef.contains(image)) {
								error = true;
								System.err.println("Error: Image already exist in the current texture atlas '" + filename + "'");
							} else if(image.bounds.width > 1024 || image.bounds.height > 1024) {
								error = true;
								System.err.println("Error: Image dimension is larger than the maximum texture size '" + filename + "'");
							} else {
								imageDataRef.addImage(image);
							}
						}
					}
				}
				event.getDropTargetContext().dropComplete(true);
				repaint();
			} else {
				error = true;
				event.rejectDrop();
			}
		} catch(Exception ex) {
			System.err.println("Error: Drag and drop failed. " + ex.getMessage());
			error = true;
			event.rejectDrop();
		}
		
		if(error) {
			Sound.beep();
		}
	}
	
	public boolean isValidDroppedFile(File file) {
		if(file.isFile()) {
			String filename = file.getName();
			if(filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".gif") || filename.toLowerCase().endsWith(".xml")) {
				return true;
			} else {
				System.err.println("Error: Invalid dropped file found '" + filename + "'");
			}
		}
		return false;
	}

	public void dragEnter(DropTargetDragEvent event) {
		
	}

	public void dragExit(DropTargetEvent event) {
		
	}

	public void dragOver(DropTargetDragEvent event) {
		
	}
	
	public void dropActionChanged(DropTargetDragEvent event) {
		
	}

}