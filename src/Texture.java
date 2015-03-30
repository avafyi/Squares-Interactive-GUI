import java.io.File;
import java.util.ArrayList;


public class Texture extends TextureGroup{
	ArrayList<File> textures = null;

	public Texture(String textureType, String fileExten) {
		super(textureType, fileExten);
		this.textures = super.getTextures();
	}
	
	public File getTextureFile(String texture, String textureType) {
		if (textures == null) {
			return null;
		}
		File textureFile = null;
		for(File f : textures) {
			String fileName = f.getName();
			if (fileName.contains(texture) && fileName.endsWith(textureType)) {
				textureFile = f;
				break;
			}
		}
		return textureFile;
	}

	public static void main(String[] args) {
		// Get the floor texture, 0
		Texture t = new Texture("blonde", ".png");
		File myTexture = t.getTextureFile("0-1", ".png");
		System.out.println(myTexture.getAbsolutePath());
	}

}
