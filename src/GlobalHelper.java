
public class GlobalHelper {
	
	// http://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-an-uri
	public static String getLastBitFromUrl(final String url){
	    return url.replaceFirst(".*\\\\([^\\\\?]+).*", "$1");
	}
	
	public static String getNthSegmentFromURLEnd(String url, final int n) {
		String segment = "";
		for (int i = 0; i <= n; i++) {
			segment = getLastBitFromUrl(url);
			int lastIdx = url.lastIndexOf(segment);
			url = new StringBuilder(url).replace(lastIdx, lastIdx+segment.length(),"").toString();
		}
		return segment;
	}	
	
}
