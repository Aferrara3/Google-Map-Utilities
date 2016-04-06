import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class KmlConvert {

	public static void main(String[] args) throws IOException{
				
			KmlConvert KML = new KmlConvert(); 
			String input = "C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\take 3\\merged.kml";
			String format = ".gpx"; //Accepts ".gpx" and "_motion.kml"
			KML.convert(input, format);
			
	}
		void convert(String input, String format) throws IOException{
			String output = input.split(".kml")[0] + "_convert" + format;
			KmlConvert text = new KmlConvert(); 
			List<String> lines = text.readSmallTextFile(input);
			String data = "";
			for (int i = 0; i<lines.size(); i++){
				data += lines.get(i);
			}
					
		    String[] entries= data.split("(?=<when>)");
		    
		    	PrintWriter writer = new PrintWriter(output);
		    	//DateTime current = new DateTime(System.currentTimeMillis());
			    String[] dateTimes = new String[entries.length];
			    double[] lats = new double[entries.length];
			    double[] longs = new double[entries.length];
			    boolean[] trackEnd = new boolean[entries.length];		
			    boolean[] trackStart = new boolean[entries.length];		
			    for(int i=1; i<entries.length-2; i++){
			    	String current = entries[i].split("(?><gx:coord>)")[1].split("(?><gx:coord>)")[0];
			    	//log(current);
			    	String dateTime = entries[i].substring(entries[i].indexOf(">") + 1, entries[i].indexOf("</when>"));
			    	DateTimeZone dtz = DateTimeZone.forID("Etc/GMT+8");
			    	DateTime dt = new DateTime(dateTime, dtz);
			    	
				    double longitude = Double.parseDouble(current.split(" ")[0]);
		    		double lattitude = Double.parseDouble(current.split(" ")[1]);
		    		
		    		lats[i] = lattitude;
		    		longs[i] = longitude;
		    		dateTimes[i] = (dt.toString("yyyy-MM-dd HH:mm:ss")+"Z").replace(" ", "T");
		    		//log((dt.toString("yyyy-MM-dd HH:mm:ss")+"Z").replace(" ", "T"));
		    		if(current.contains("</gx:Track>"))trackEnd[i]=true; else trackEnd[i]=false;
		    		if(current.contains("<gx:Track>"))trackStart[i+1]=true; else trackStart[i+1]=false;
			    }
			    
			    if(format.equals(".gpx")){
				    String headerStuff = 
					    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpsies=\"http://www.gpsies.com/GPX/1/0\" creator=\"Alex\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.gpsies.com/GPX/1/0 http://www.gpsies.com/gpsies.xsd\">\n  <metadata>\n    <name>Converted</name>\n    <link href=\"http://www.euroblog.alexanderferrara.com/\">\n      <text>Converted with Java</text>\n    </link>\n    <time>2016-03-14T00:06:57Z</time>\n  </metadata>\n  <trk>\n    <name>Converted KML Data</name>\n    <trkseg>";
				    String endStuff = "\n    </trkseg>\n  </trk>\n</gpx>";
				    writer.println(headerStuff);
				    for(int i=1; i<entries.length-2; i++){
			    		if (trackStart[i]) writer.println("<trkseg>");
			    		writer.println("      <trkpt lat=\""+lats[i]+"\" lon=\""+longs[i]+"\">");
			            writer.println("        <ele>0.0000000</ele>");
			            writer.println("        <time>"+dateTimes[i]+"</time>");
			            writer.println("      </trkpt>");
			            if (trackEnd[i]) writer.println("    </trkseg>");
					}
				    writer.println(endStuff);
				    log("KML2GPX Conversion Complete");
			    }
			    
			    if(format.equals("_motion.kml")){
				    String headerStuff = 
					    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpsies=\"http://www.gpsies.com/GPX/1/0\" creator=\"Alex\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.gpsies.com/GPX/1/0 http://www.gpsies.com/gpsies.xsd\">\n  <metadata>\n    <name>Converted</name>\n    <link href=\"http://www.euroblog.alexanderferrara.com/\">\n      <text>Converted with Java</text>\n    </link>\n    <time>2016-03-14T00:06:57Z</time>\n  </metadata>\n  <trk>\n    <name>Converted KML Data</name>\n    <trkseg>";
				    String endStuff = "/n    </trkseg>\n  </trk>\n</gpx>";
				    writer.println(headerStuff);
				    for(int i=1; i<entries.length; i++){
			    		if (trackStart[i]) writer.println("<trkseg>");
			    		writer.println("      <trkpt lat=\""+lats[i]+"\" lon=\""+longs[i]+"\">");
			            writer.println("        <ele>0.0000000</ele>");
			            writer.println("        <time>"+dateTimes[i]+"</time>");
			            writer.println("      </trkpt>");
			            if (trackEnd[i]) writer.println("    </trkseg>");
					}
				    writer.println(endStuff);
			    }
			    
			    writer.close();
  
		  }
		  
		  /* Methods Start Here*/
		  Charset ENCODING = StandardCharsets.UTF_8;
		  List<String> readSmallTextFile(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
		  }
		  
		  static void log(Object aMsg){
		    System.out.println(String.valueOf(aMsg));
		  }
		  
		} 
