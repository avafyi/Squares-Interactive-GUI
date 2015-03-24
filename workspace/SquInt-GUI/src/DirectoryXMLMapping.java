import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class DirectoryXMLMapping {	
	
	private static File directory;

	public DirectoryXMLMapping(String folder) {
		directory = new File(folder);
	}

	public static void main(String[] args) 
	{
		DirectoryXMLMapping dirMapper = new DirectoryXMLMapping("res/");
//		System.out.println(directory.getAbsolutePath());
//		File[] contents = directory.listFiles();
//		for ( File f : contents) {
//			System.out.println(f.getAbsolutePath());
//		}
		searchDirectoriesRecursively("res/");
		System.exit(0);
		
		try {			 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();	
			Element rootElement = doc.createElement("company");
			doc.appendChild(rootElement);

			// staff elements
			Element staff = doc.createElement("Staff");
			rootElement.appendChild(staff);

			// set attribute to staff element
			Attr attr = doc.createAttribute("id");
			attr.setValue("1");
			staff.setAttributeNode(attr);

			// shorten way
			// staff.setAttribute("id", "1");

			// firstname elements
			Element firstname = doc.createElement("firstname");
			firstname.appendChild(doc.createTextNode("yong"));
			staff.appendChild(firstname);

			// lastname elements
			Element lastname = doc.createElement("lastname");
			lastname.appendChild(doc.createTextNode("mook kim"));
			staff.appendChild(lastname);

			// nickname elements
			Element nickname = doc.createElement("nickname");
			nickname.appendChild(doc.createTextNode("mkyong"));
			staff.appendChild(nickname);

			// salary elements
			Element salary = doc.createElement("salary");
			salary.appendChild(doc.createTextNode("100000"));
			staff.appendChild(salary);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("C:\\file.xml"));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	private static void searchDirectoriesRecursively(String top) {
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
		// If there are sub files
		for ( File f : contents) {
			// Print out the path to the subfile
//			System.out.println(f.getAbsolutePath());

			// create xml object for the subfile
			createXMLObject(f.getAbsolutePath());
			
			// recurse on the subfiles
			dirRecursion(f);
		}
	}
	
	private static void createXMLObject(String objPath) {
		// find a way to extract the last sectoin of the path out so something like
		//C:\Users\piekstra17\Documents\GitHub\Squares-Interactive-GUI\workspace\SquInt-GUI\res\json
		// would grab out: json
		Matcher m = Pattern.compile("^(.*)(\\.*)$").matcher(objPath);
		if (m.matches()) {
			System.out.println(m.group(1));
		}
		if (objPath.contains("avatars")) {
			// add avatar attributes to the object
		}
	}

}
