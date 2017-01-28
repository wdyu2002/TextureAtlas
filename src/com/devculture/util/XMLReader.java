package com.devculture.util;

import java.io.File;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader {
	
	/** variables **/
	
	private DocumentBuilderFactory docBuilderFactory = null;
	private DocumentBuilder docBuilder = null;
	private Document doc = null;
	
	public XMLReader(String filename) throws Exception {
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.parse(new File(filename));
		doc.getDocumentElement().normalize();
	}

	public Node getAttribute(Node node, String attribute) {
		NamedNodeMap attributes = node.getAttributes();
		
		for(int j=0; j<attributes.getLength(); j++) {
			Node attr = attributes.item(j);
			if(attr != null && attr.getNodeName().equalsIgnoreCase(attribute)) {
				return attr;
			}
		}
		return null;
	}
	
	public String getAttributeValue(Node node, String attribute) {
		NamedNodeMap attributes = node.getAttributes();
		
		for(int j=0; j<attributes.getLength(); j++) {
			Node attr = attributes.item(j);
			if(attr != null && attr.getNodeName().equalsIgnoreCase(attribute)) {
				return attr.getNodeValue();
			}
		}
		return null;
	}
	
	public Node getFirstChildNodeByTagName(Node node, String tag) {
		NodeList children = node.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if(child.getNodeName() == tag) {
				return child;
			}
		}
		return null;
	}
	
	public Vector<Node> getChildrenNodesByTagName(Node node, String tag) {
		Vector<Node> temp = new Vector<Node>();
		NodeList children = node.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if(child.getNodeName() == tag) {
				temp.add(child);
			}
		}
		return temp;
	}
	
	public Vector<Node> getDocumentNodesByTagName(String tag) {
		Vector<Node> temp = new Vector<Node>();
		NodeList children = doc.getElementsByTagName(tag);
		for(int i=0; i<children.getLength(); i++){
			temp.add(children.item(i));
		}
		return temp;
	}

}
