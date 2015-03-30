import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public final class Texture {
	
	public static final String resourceLocation = "res/textures.zip";
		
	public Texture() {
	    ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(resourceLocation);

		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        // every entry is a folder and file in the directory
		        // could use this to look for a file
		        if (entry.getName().endsWith(".xml")) {
			        InputStream stream = zipFile.getInputStream(entry);
			        loadTextures(stream);
		        }
		    }
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Texture t = new Texture();
	}
	
	/**
	 * Expects an xml file input stream
	 * The xml file will tell it where to look for textures
	 * 
	 * @param stream
	 */
	private void loadTextures(InputStream stream) {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
//			doc = docBuilder.parse(new File("res\\xml\\Textures.xml"));
			doc = docBuilder.parse(stream);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return;
		}

		getFilesFromDir("floor", doc);
		
		
	}
	
	private void getFilesFromDir(String directoryName, Document doc) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		try {
			expr = xPath.compile("//file");
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nl = null;
		try {
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getParentNode().getAttributes().item(0).getNodeValue().contains(directoryName)) {
				System.out.println(nl.item(i).getTextContent());
			}
		}
		
		// Gets the path to all files
		NodeList nodes = doc.getElementsByTagName("file");
		for(int i = 0; i < nodes.getLength(); i++) {
			System.out.println(getFileURL(nodes.item(i)));
		}
		
	}
	
	private String getFileURL(Node n) {
		ArrayList<String> path = new ArrayList<String>();
		while (n.getParentNode() != null) {
			if (n.hasAttributes()) {
				String folderName = n.getAttributes().item(0).getNodeValue();
				if (!folderName.endsWith("\\")) {
					folderName += "\\";
				}
				if (folderName != null) {
					path.add(folderName);						
				}			
			}
			n = n.getParentNode();
		}
		Collections.reverse(path);
		String pathString = "";
		for (String s : path)
		{
			pathString += s;	
		}	
		return pathString;
	}
	
	private void getFileWithName(String fileName) {
		
	}

}
