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
	
	private String resourceLocation = "res/textures.zip";
	private ArrayList<File> textureFiles = null;
	private ArrayList<File> directoryFiles = null;
	private ZipFile zipFile = null;
		
	public TextureGroup(String textureGroup, String fileExten, String location) {
		setResourceLocation(location);
		InputStream stream = createInputStream();
        Document doc = getDoc(stream);
        textureFiles = loadTextures(textureGroup, fileExten, doc);
        directoryFiles = loadDirs(doc);
        try {
			zipFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setResourceLocation(String location) {
		resourceLocation = location;
	}
	
	public InputStream createInputStream() {
		InputStream stream = null;
		try {
			zipFile = new ZipFile(resourceLocation);
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        // every entry is a folder and file in the directory
		        // could use this to look for a file
		        if (entry.getName().endsWith(".xml")) {
			        stream = zipFile.getInputStream(entry);
			        break;
		        }
		    }
		    
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return stream;
	}
	
	public ArrayList<File> getDirectories() {
		return directoryFiles;
	}
	
	public ArrayList<File> getTextures() {
		return textureFiles;
	}
	
	public static void main(String[] args) {
		TextureGroup t = new TextureGroup("floor", ".png", "res/textures.zip");
	}
	
	public Document getDoc(InputStream stream) {
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
	
	private ArrayList<File> loadDirs(Document doc) {
		if (doc == null) {
			return null;
		}
		return getDirs(doc);
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
	
	public ArrayList<File> getDirs(Document doc) {
		ArrayList<File> directoryFiles = new ArrayList<File>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		try {
			expr = xPath.compile("//dir");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		NodeList nl = null;
		try {
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < nl.getLength(); i++) {
			String dirName = nl.item(i).getAttributes().item(0).getNodeValue();
			if (dirName != null) {
				directoryFiles.add(getDirFile(dirName, doc));				
			}
		}
		return directoryFiles;
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
	
	public File getDirFile(String dirName, Document doc) {
		String filePath = getFilePath(dirName, null, doc, "dir");
		if (filePath != null) return new File(filePath);		
		else return null;
	}
	
	public File getFile(String fileName, String folderName, Document doc) {
		String filePath = getFilePath(fileName, folderName, doc, "file");
		if (filePath != null) return new File(filePath);		
		else return null;
	}
	
	private String getFilePath(String fileName, String folderName, Document doc, String fileClassification) {
		String filePath = null;
		// Gets the path to all files
		NodeList nodes = doc.getElementsByTagName(fileClassification);
		for(int i = 0; i < nodes.getLength(); i++) {
			Node fileNode = nodes.item(i);
			if (fileClassification.contains("file")) {
				// If we are looking for a specific file, we want to check the folder it is inside in the case of duplicate filenames
				if (fileNode.getTextContent() != null && fileNode.getTextContent().contains(fileName) && fileNode.getParentNode().hasAttributes() && fileNode.getParentNode().getAttributes().item(0).getNodeValue().contains(folderName)) {						
					filePath = getFileURL(nodes.item(i)) + fileName;
					break;
				}		
			} else {
				if (fileNode.hasAttributes() && fileNode.getAttributes().item(0).getNodeValue().contains(fileName)) {
					filePath = getFileURL(nodes.item(i));
					break;
				}	
			}
				
		}
		return filePath;
	}

}
