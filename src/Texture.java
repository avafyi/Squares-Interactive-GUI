//import java.io.File;
//import java.util.ArrayList;
//
//import org.w3c.dom.Document;
//
//
//public class Texture {
//	ArrayList<File> textures = null;
//	ArrayList<File> directories = null;
//
//	public Texture(String textureType, String fileExten, String resourceLocation) {	
//		FileLoader tg = new FileLoader(textureType, fileExten, resourceLocation);		
//		this.textures = tg.getTextures();
//		this.directories = tg.getDirectories();
//	}
//	
//	public File getTextureFile(String texture, String textureType) {
//		if (textures == null) {
//			return null;
//		}
//		File textureFile = null;
//		for(File f : textures) {
//			String fileName = f.getName();
//			if (fileName.contains(texture) && fileName.endsWith(textureType)) {
//				textureFile = f;
//				break;
//			}
//		}
//		return textureFile;
//	}
//
//	public static void main(String[] args) {
//		// Get the floor texture, 0
//		Texture t = new Texture("floor", ".png", "res/xml/Textures.xml");
//		File myTexture = t.getTextureFile("0", ".png");		
//		System.out.println(myTexture.getAbsolutePath());
//	}
//
//}
