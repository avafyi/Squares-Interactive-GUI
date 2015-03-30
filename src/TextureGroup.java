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


public class TextureGroup {
	
	public static final String resourceLocation = "res/textures.zip";
	private ArrayList<File> textureFiles = null;
		
	public TextureGroup(String textureGroup, String fileExten) {
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
			        Document doc = getDoc(stream);
			        textureFiles = loadTextures(textureGroup, fileExten, doc);
		        }
		    }
		    
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public ArrayList<File> getTextures() {
		return textureFiles;
	}
	
	public static void main(String[] args) {
		TextureGroup t = new TextureGroup("floor", ".png");
	}
	
	private Document getDoc(InputStream stream) {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = docBuilder.parse(stream);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * Expects an xml file input stream
	 * The xml file will tell it where to look for textures
	 * 
	 * @param stream
	 */
	private ArrayList<File> loadTextures(String textureGroup, String textureType, Document doc) {
		if (doc == null || textureType == null) {
			return null;
		}
		return getFilesFromDir(textureGroup, textureType, doc);
	}
	
	private ArrayList<String> getDirs(Document doc) {
		ArrayList<String> directories = new ArrayList<String>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		try {
			expr = xPath.compile("//dir");
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
			String dirName = nl.item(i).getAttributes().item(0).getNodeValue();
			if (dirName != null) {
				directories.add(dirName);				
			}
		}
		return directories;
	}
	
	private ArrayList<File> getFilesFromDir(String directoryName, String fileType, Document doc) {
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

		ArrayList<File> files = new ArrayList<File>();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getParentNode().getAttributes().item(0).getNodeValue().contains(directoryName)) {
				String fileName = nl.item(i).getTextContent();
				if (fileName != null && fileName.endsWith(fileType)) {
					files.add(getFile(fileName, directoryName, doc));
				}
			}
		}	
		return files;
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
	
	private File getFile(String fileName, String folderName, Document doc) {
		return new File(getFilePath(fileName, folderName, doc));
	}
	
	private String getFilePath(String fileName, String folderName, Document doc) {
		String filePath = null;
		// Gets the path to all files
		NodeList nodes = doc.getElementsByTagName("file");
		for(int i = 0; i < nodes.getLength(); i++) {
			Node fileNode = nodes.item(i);
			if (fileNode.getTextContent() != null && fileNode.getTextContent().contains(fileName) && fileNode.getParentNode().hasAttributes() && fileNode.getParentNode().getAttributes().item(0).getNodeValue().contains(folderName)) {
				filePath = getFileURL(nodes.item(i)) + fileName;
				break;
			}			
		}
		return filePath;
	}

}
