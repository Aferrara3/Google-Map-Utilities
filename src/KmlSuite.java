import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class KmlSuite {

	public static void main(String[] args) throws IOException{
			KmlSuite KML = new KmlSuite(); 
			
			String dir = "C:\\Users\\Alex\\Documents\\France KMLs\\Raw Daily KMLs\\April\\";
			String dest = "Apr_merged.kml";
			
			String[] inputs = KML.getAllKml(dir, dest);
			//Use the decent smoothing class to find good params for data
		    int tolerance		= 35; /*km*/
		    int timeLimit 		= 20; /*min*/
		    int speedLimit		= 400; /*km/hr*/
			String[] smoothed = KML.smooth(inputs, tolerance, timeLimit, speedLimit);
			
			KML.merge(smoothed, dir+dest);
			KML.convert(dir+dest, ".gpx");		
			
			KML.cleanUp(smoothed);
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
		/* Merges multitrack KML files maintaining the header information from first file */
		String merge(String[] inputs, String dest) throws IOException{
			MergeKML text = new MergeKML(); 
			String f1= text.readSmallTextFile(inputs[0]).get(0);
			String headerStuff = f1.split("(?=<Placemark>)")[0];
			PrintWriter writer = new PrintWriter(dest, "UTF-8");
		    headerStuff = headerStuff.replace("Location history", "Merged location histories starting");
		    headerStuff += "<Style id=\"bredder\"><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href></Icon></IconStyle><LineStyle><color>501400FF</color><width>3</width></LineStyle></Style>";
		    writer.print(headerStuff);
			
			for(int i=0; i<inputs.length; i++){
				List<String> lines = text.readSmallTextFile(inputs[i]);
				//contents+=(lines.get(0).split("(?=<Placemark>)")[1]).split("(?<=</Placemark>)")[0];
				//log(i+": "+(lines.get(0).split("(?=<gx:Track>)")[1]).split("(?<=</gx:Track>)")[0]);
				String data = "";
				for (int j = 0; j<lines.size(); j++){
					data += lines.get(j);
				}
				writer.print((data.split("(?=<Placemark>)")[1]).split("(?<=</Placemark>)")[0]);
			}
			
		    //log("Header: " + headerStuff);
		    //log(contents);
		    String endStuff = f1.split("(?></Placemark>)")[1];
		    //log(" Footer: "+endStuff);
		    writer.print(endStuff);
		    //log(headerStuff + contents + endStuff);
		    log(inputs.length + " files merged");
			writer.close();
			return dest;
		}		    
		
		private String[] getAllKml(String dir, String dest) {
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			ArrayList<String> temp = new ArrayList<String>();
			
			for (int i = 0; i < listOfFiles.length; i++) {
				String name = listOfFiles[i].getName();
					if (listOfFiles[i].isFile()
							&& name.substring(name.length()-4, name.length()).equals(".kml")) {
						
						if(name.equals(dest)){
							log(name + " skipped");
						}
						else{
							temp.add(dir+name);
							log(name + " added to merge set");
						}
					}
					else if (listOfFiles[i].isDirectory()) {
						log("Subdirectory detected ("+name+"), subfolders/files not included");
					}
					else if (listOfFiles[i].isFile()) {
						log("Non-kml file ("+name+"), not included");
					}
			}
			   Object[] objDays = temp.toArray();
               String[] output = Arrays.copyOf(objDays, objDays.length, String[].class);
               return temp.toArray(output);
		}
		  
		String[] smooth(String[] inputs, int _tol, int _timeLim, int _speedLim) throws IOException{
			String[] smoothed = new String[inputs.length];
			for(int j=0; j < inputs.length; j++){
				DecentSmoothing text = new DecentSmoothing(); 
				List<String> lines = text.readSmallTextFile(inputs[j]);
				String data = "";
				/*log(lines.size());
				log(lines.size()/100);*/
				for (int i = 0; i<lines.size(); i++){
					data += lines.get(i);
					/*if(i%100 == 0){
						System.out.print("\r" + i+"/"+lines.size()+" = "+(i*100)/lines.size()+"%");
					}*/
				}
			    String[] entries= data.split("(?=<when>)");
			    String headerStuff = entries[0];
			    headerStuff = headerStuff.replace("</description>", "</description><styleUrl>#bredder</styleUrl>");
			    //log(headerStuff);
			    String endStuff = entries[(entries.length-1)].split("(?></gx:coord>)")[1];
			    
			    
			    int tol	= 	_tol;
			    int timeLim 	= _timeLim; 
			    int speedLimit	= _speedLim; 
			    
			    		    
			    String dest = inputs[j].split(".kml")[0]+"\\";
			    //log(dest);
			    File ftemp = new File(dest);
			    PrintWriter writer = new PrintWriter(inputs[j]+"_smooth.kml", "UTF-8");
			    smoothed[j] = inputs[j]+"_smooth.kml";
			    headerStuff = headerStuff.replace("<name>Google User</name>", "<name></name>");
			    writer.print(headerStuff);
			    
				double tolerance = tol * 1.0;/*km*/
				double removedCounter = 0;
				double totalCounter = 0;
				  		
				DateTime[] dateTimes = new DateTime[entries.length];
				double[] lats = new double[entries.length];
				double[] longs = new double[entries.length];
				    		
				for(int i=1; i<entries.length-2; i++){
					String current = entries[i].split("(?><gx:coord>)")[1].split("(?><gx:coord>)")[0];
				    String dateTime = entries[i].substring(entries[i].indexOf(">") + 1, entries[i].indexOf("</when>"));
				    DateTimeZone dtz = DateTimeZone.forID("Etc/GMT+8");
				    DateTime dt = new DateTime(dateTime, dtz);
				    
					double longitude = Double.parseDouble(current.split(" ")[0]);
			    	double lattitude = Double.parseDouble(current.split(" ")[1]);
			    		
			    	lats[i] = lattitude;
			    	longs[i] = longitude;
			    	dateTimes[i] = dt;
				}
				    /* Find some averages for beginning  outlier resolution*/
				    int frontLen = 1;
				    double latFrontAvg = 0.0;
				    double longFrontAvg = 0.0;
				    if(entries.length > 40){
				    	frontLen = 10;
				    }else frontLen = entries.length/4;
				    /* Not going to handle the <4 elements case, you don't need a tool like this for
				     * that little data */
				    for(int i=1; i<=frontLen; i++){ 
				    	latFrontAvg += lats[i];
				    	longFrontAvg += longs[i];
				    }
				    latFrontAvg = latFrontAvg/(frontLen-1);
				    longFrontAvg = longFrontAvg/(frontLen-1);
				    
				    for(int i=1; i<entries.length-3; i++){
				    	totalCounter++;
			    		double dNext = earthdist(longs[i],lats[i],longs[i+1],lats[i+1]);
			    		double timeUntil = (dateTimes[i].getMillis()-dateTimes[i+1].getMillis())/(60.0*1000);
			    		
			    		if(i==1){
				    		if( dNext > tolerance && timeUntil < timeLim && dNext/timeUntil < speedLimit*60){
				    			removedCounter++;
			    				double d1 = Math.abs(earthdist(longs[i],lats[i],longFrontAvg,latFrontAvg)) ;
			    				double d2 = Math.abs(earthdist(longs[i+1],lats[i+1],longFrontAvg,latFrontAvg)) ;;		
			    				if(d1<d2){
			    					lats[i+1]=lats[i];
			    					longs[i+1]=longs[i];
			    				}else {
			    					lats[i]=lats[i+1];
			    					longs[i]=longs[i+1];
			    				}
			    				
				    		}
				    	}
				    	if(i>1){
				    		double dPrev = earthdist(longs[i],lats[i],longs[i-1],lats[i-1]);
				    		double timeSince = (dateTimes[i].getMillis()-dateTimes[i-1].getMillis())/(60.0*1000);
				    		if(dPrev > tolerance && timeSince < timeLim && dPrev/timeSince < speedLimit*60){
				    				removedCounter++;
				    				//log(i);
				    				lats[i]=lats[i-1];
				    				longs[i]=longs[i-1];
				    				continue;
				    		}
				    		else{ writer.print(entries[i]);}
				    	}
				    }
				    writer.print(endStuff);
				    double loss = ((removedCounter/totalCounter)*100);
				    String shortName = inputs[j].split("\\\\")[inputs[j].split("\\\\").length-1];
				    log("File smoothed: "+shortName+"\tEntries removed: " + round(loss,2) + "%" + "\t ("+removedCounter+")");
				    //writer.println("Tolerance: "+tolerance+"km  \tEntries removed: " + round(loss,2) + "%" + "\t ("+counter+")");
				    writer.close();
			    }	
				return smoothed;
			}
		  
		  
		  /* Methods Start Here*/
		  Charset ENCODING = StandardCharsets.UTF_8;
		  List<String> readSmallTextFile(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
		  }
		  
		  static double round(double value, int places) {
			    if (places < 0) throw new IllegalArgumentException();

			    long factor = (long) Math.pow(10, places);
			    value = value * factor;
			    long tmp = Math.round(value);
			    return (double) tmp / factor;
			}
		  
		  void cleanUp(String[] inputs) throws IOException{
			  for (int i=0; i<inputs.length; i++){
				  Files.delete(Paths.get(inputs[i]));
			  }
		  }
		  		  
		  /*earthdist code provided by stackoverflow user kng229*/
		  static double earthdist(double long1, double lat1, double long2, double lat2)
		  {
			  double rad, a1, a2, b1, b2, dlon, dlat, a, c, R, d;
			  rad = Math.PI/180;
			  a1 = lat1 * rad;
			  a2 = long1 * rad;
			  b1 = lat2 * rad;
			  b2 = long2 * rad;
			  dlon = b2 - a2;
			  dlat = b1 - a1;
			  a = (Math.sin(dlat/2))*(Math.sin(dlat/2)) + Math.cos(a1) * Math.cos(b1) * (Math.sin(dlon/2))*(Math.sin(dlon/2));
			  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			  R = 6378.145;
			  d = R * c;
			  return(d);
		  }

		  static void log(Object aMsg){
		    System.out.println(String.valueOf(aMsg));
		  }
		  
		} 
