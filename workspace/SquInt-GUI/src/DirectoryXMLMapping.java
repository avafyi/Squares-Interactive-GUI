import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DirectoryXMLMapping {	
	
	private static File directory;
	private static Document doc;
	private static Element root;
	private static Element prevElement;
	private static Element currElement;
	private static Element nextElement;
//	private static boolean in_subDir = false;
//	private static Element prev_dir;

	public DirectoryXMLMapping(String folder) {
		directory = new File(folder);
	}

	public static void main(String[] args) 
	{
		doc = null;
		root = null;
		prevElement = currElement = nextElement = null;
		
		try {	
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();			

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
				
//		DirectoryXMLMapping dirMapper = new DirectoryXMLMapping("res/");

		if (doc != null) {
			mapDirectories("res/");			
		}
		
		try {
			printDocument(doc, System.out);		
		} catch (Exception e) {
			
		}
		
		
		System.exit(0);
		
//		try {			 
//			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//
//			// root elements
//			Document doc = docBuilder.newDocument();	
//			Element rootElement = doc.createElement("company");
//			doc.appendChild(rootElement);
//
//			// staff elements
//			Element staff = doc.createElement("Staff");
//			rootElement.appendChild(staff);
//
//			// set attribute to staff element
//			Attr attr = doc.createAttribute("id");
//			attr.setValue("1");
//			staff.setAttributeNode(attr);
//
//			// shorten way
//			// staff.setAttribute("id", "1");
//
//			// firstname elements
//			Element firstname = doc.createElement("firstname");
//			firstname.appendChild(doc.createTextNode("yong"));
//			staff.appendChild(firstname);
//
//			// lastname elements
//			Element lastname = doc.createElement("lastname");
//			lastname.appendChild(doc.createTextNode("mook kim"));
//			staff.appendChild(lastname);
//
//			// nickname elements
//			Element nickname = doc.createElement("nickname");
//			nickname.appendChild(doc.createTextNode("mkyong"));
//			staff.appendChild(nickname);
//
//			// salary elements
//			Element salary = doc.createElement("salary");
//			salary.appendChild(doc.createTextNode("100000"));
//			staff.appendChild(salary);
//
//			// write the content into xml file
//			TransformerFactory transformerFactory = TransformerFactory.newInstance();
//			Transformer transformer = transformerFactory.newTransformer();
//			DOMSource source = new DOMSource(doc);
//			StreamResult result = new StreamResult(new File("C:\\file.xml"));
//
//			// Output to console for testing
//			// StreamResult result = new StreamResult(System.out);
//
//			transformer.transform(source, result);
//
//			System.out.println("File saved!");
//
//		} catch (ParserConfigurationException pce) {
//			pce.printStackTrace();
//		} catch (TransformerException tfe) {
//			tfe.printStackTrace();
//		}
	}
	
	private static void mapDirectories(String top) {
		// Initialize the root element
		root = doc.createElement(top.substring(0, top.length() - 1));
		doc.appendChild(root);
		prevElement = root;
		
		File topDir = new File(top);
		dirRecursion(topDir);
	}
	
	/**
	 * Given a file, this function recursively does a linear search
	 * through all directories and subdirectories and creates an 
	 * xml object with 
	 * @param file
	 */
	private static void dirRecursion (File file) {
		// If there are no sub-files then leave
		File[] contents = file.listFiles();
		if (contents == null) {
			return;
		}
		if (nextElement != null) {
			prevElement = nextElement;
		}
		// If there are sub files
		for ( File f : contents) {
			// Print out the path to the subfile
//			System.out.println(f.getAbsolutePath());

			// create xml object for the subfile
			nextElement = createXMLObject(f.getAbsolutePath());
			
			if (nextElement != null) {
				prevElement.appendChild(nextElement);				
			}
			
			// recurse on the subfiles
			dirRecursion(f);
		}
		// if we are here then we have gone through all subfiles, then back out of the current directory and go up one (cd .. essentially)
		Node prevNode = prevElement.getParentNode();
		if (prevNode instanceof Element) {
			prevElement = (Element)prevNode;
		}
	}
	
	private static Element createXMLObject(String objPath) {
		int lastIdx = objPath.lastIndexOf("\\");
		String objName = null;
		String origName = null;
		
		if (lastIdx == -1) return null;
		
		objName = objPath.substring(lastIdx+1, objPath.length());			
		if (objName == null) return null;
		
//		System.out.println(objName + "\t");
		
		// Check if the object has a file extension of .png
		lastIdx = objName.lastIndexOf(".png");
		if (lastIdx != -1) {
			// If so, modify the object name to give it a prefix of i_
			origName = objName;
			objName = "file";
		}
		Element returnElement = doc.createElement(objName);
		// If we have a file and not a directory then set some attributes for the file
		if (objName == "file") {
			returnElement.setAttribute("name", origName);
		}
		return returnElement;
	}
	
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

}
