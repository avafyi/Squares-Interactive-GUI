import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


public class DirectoryXMLMapping{	
	
	private static Document doc = null;
	private static Element root	= null;
	private static Element prevElement = null;
	private static Element nextElement = null;
	private static final Boolean operate = true; 
	private static final Boolean useThreading = false;	// this makes it a lot slower in my tests
	private static final Boolean timeProgram = true;
	public static long startTime = 0;
	private static final Pattern lastIntPattern = Pattern.compile("[^0-9]+(([0-9]+)[)]?)$");

	public static void main(String[] args) 
	{
//		String rootDir = "C:\\users\\piekstra17\\";
		String rootDir = "res\\";
		System.out.println(rootDir);
		try {	
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();			

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}

		
		
		if (timeProgram) {
			startTime = System.currentTimeMillis();
		}
		
		if (doc != null) {
			mapDirectories(rootDir);			
		}
		

	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	    		if (timeProgram) {
	    			final long endTime = System.currentTimeMillis();
	    			System.out.println("Total execution time: " + (endTime - startTime) );
	    		}
	        }
	    }, "Shutdown-thread"));
		if (operate) {
			printDocumentToConsole(doc, System.out);	
			outputDocument(doc, rootDir, "dirMap");	
		}
//		
//		System.exit(0);
	}
	
	private static void mapDirectories(String dirPath) {
		// We need to establish the root of the document, which will be the directory at the end of the dirPath - path\\end\\
		int lastIdx = lastIdx = (dirPath.substring(0, dirPath.lastIndexOf("\\"))).lastIndexOf("\\") +1;

		// Initialize the root element
		root = doc.createElement(dirPath.substring(lastIdx, dirPath.length() - 1));
		doc.appendChild(root);
		prevElement = root;

		File topDir = new File(dirPath);
		
		if (useThreading) {
			Thread t1 = new Thread(new Recursor(topDir));
			t1.start();
		} else {
			dirRecursion(topDir);
		}
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
			System.out.println(f.getAbsolutePath());

			// create xml object for the subfile
			nextElement = createXMLObject(f.getAbsolutePath());
			
			if (nextElement != null) {
				prevElement.appendChild(nextElement);				
			}

			// recurse on the subfiles
			if (useThreading) {
				Thread t2 = new Thread(new Recursor(f));
				t2.start();
			} else {
				dirRecursion(f);
			}
		}
		// if we are here then we have gone through all subfiles, then back out of the current rootDirectory and go up one (cd .. essentially)
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
		lastIdx = objName.lastIndexOf(".");
		if (lastIdx != -1) {
			// If so, modify the object name to give it a prefix of i_
			origName = objName;
			objName = "file";
		}
		// Here, check for any invalid characters for naming and remove them
		objName = objName.replace("$", "");
		// Here, check if the name starts with an invalid character and add an underscore (_)
		if (objName.startsWith(".") || Character.isDigit(objName.charAt(0))) {
			objName = "_" + objName;
		}

		Element returnElement = null;
		try {
			returnElement = doc.createElement(objName);
		} catch (Exception e){
			System.out.println(e);
		}
		// If we have a file and not a rootDirectory then set some attributes for the file
		if (objName == "file") {
			returnElement.setAttribute("name", origName);
		}
		return returnElement;
	}
	
	public static void printDocumentToConsole(Document doc, OutputStream out) {
		try {
		    TransformerFactory tf = TransformerFactory.newInstance();
		    Transformer transformer = tf.newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		    transformer.transform(new DOMSource(doc), 
		         new StreamResult(new OutputStreamWriter(out, "UTF-8")));			
		} catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
	}
	
	public static void outputDocument(Document doc, String saveLocation, String fileName) {
		 try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // send DOM to file
//            File outputFile = new File(saveLocation + fileName + ".xml");
            // Make sure the file does not exist yet but if it does
        	boolean checkAgain = true;
        	long lastNumberInt = 0;
        	
        	String filepath = saveLocation + fileName + ".xml";
            File aFile = new File(filepath);
            FileOutputStream outputFile = null;
            if (aFile.isFile()) {
              File newFile = aFile;
              do {
                String name = newFile.getName();
                int period = name.indexOf('.');
                newFile = new File(newFile.getParent(), name.substring(0, period) + "_old"
                    + name.substring(period));
              } while (newFile.exists());
              aFile.renameTo(newFile);
            }
            try {
              outputFile = new FileOutputStream(aFile);
              System.out.println("myFile.txt output stream created");
            } catch (Exception e) {
              e.printStackTrace(System.err);
            }
//        	while (checkAgain) {
//        		checkAgain = false;
//	            if (outputFile.exists()) {
//                	// Check if the file name already has a number at the end
//                	Matcher matcher = lastIntPattern.matcher(fileName);
//                	if (matcher.find()) {
//                	    String someNumberStr = matcher.group(1);
//                	    boolean addParen = false;
//                	    if (someNumberStr.endsWith(")")) {
//                	    	someNumberStr = someNumberStr.substring(0, someNumberStr.length()-1);
//                	    	addParen = true;
//                	    }
//                	    lastNumberInt = Integer.parseInt(someNumberStr);
//                	    // rename the file to be the next number so we don't overwrite
//                	    fileName = fileName.replace(matcher.group(1), "" + (lastNumberInt+1));
//                	    if (addParen) {	
//                	    	fileName += ")";
//                	    	checkAgain = true;
//                	    }
//                	} else {
//                		// If the fileName exists and does not have a number at the end of the name
//                		// then add a number so we don't overwrite the existing file
//                		if(fileName.contains("(1)")) {	
//                			break;
//                		}
//            			fileName += " (1)";
//                		checkAgain = true;	// If we rename the filename, check again that it doesn't exist
//                	}
//                	outputFile = 
//            	}
//            }
            tr.transform(new DOMSource(doc), 
                    new StreamResult(outputFile));
//            tr.transform(new DOMSource(doc), 
//                                 new StreamResult(new FileOutputStream(saveLocation + fileName + ".xml")));

        } catch (TransformerException te) {
            System.out.println(te.getMessage());
        } 
//		 catch (IOException ioe) {
//            System.out.println(ioe.getMessage());
//        }
	}
	
	public static class Recursor implements Runnable {
		File topDir = null;
		
		public Recursor(File f) {
			topDir = f;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			dirRecursion(topDir);
		}
	}

}
