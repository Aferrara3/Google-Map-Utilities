package old_iterations;
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

public class DistanceApproachSecond {

	public static void main(String[] args) throws IOException{
		    
			DistanceApproachSecond text = new DistanceApproachSecond(); 
		    //treat as a small file
		    List<String> lines = text.readSmallTextFile(FILE_NAME);
		    log(lines);
		    //lines.add("This is a line added in code.");
		    text.writeSmallTextFile(lines, OUTPUT_FILE_NAME);
		    
		    //Because Google history KMLs are all on one line
		    String data = lines.get(0);
		    String[] entries= data.split("(?=<when>)");
		    String headerStuff = entries[0];
		    log(headerStuff);
		    String endStuff = entries[(entries.length-1)].split("(?></gx:coord>)")[1];
		    
		    int minTolerance		= 0; /*km*/
		    int maxTolerance		= 40; /*km*/
		    int toleranceIncrements	= 5; /*km*/
		    int timeTolerance 		= 20; /*min*/
		    int speedLimit			= 200; /*km/hr*/
		    
		    int j = minTolerance;
		    while(j<=maxTolerance){
		    	PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\distCut"+Math.abs(j)+".kml", "UTF-8");
		    	headerStuff = headerStuff.replace("Location history", "Location history modified by distance limit of"+j+"km-");
		    	writer.print(headerStuff);
			    double tolerance = (j);/*km*/
			    double counter = 0;
			    double counter2 = 0;
			    		
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
			    
			    for(int i=2; i<entries.length-3; i++){
			    	counter2++;
			    	double dPrev = earthdist(longs[i],lats[i],longs[i-1],lats[i-1]);
		    		double dNext = earthdist(longs[i],lats[i],longs[i+1],lats[i+1]);
		    		double timeSince = (dateTimes[i].getMillis()-dateTimes[i-1].getMillis())/(60.0*1000);
		    		double timeUntil = (dateTimes[i].getMillis()-dateTimes[i+1].getMillis())/(60.0*1000);
			    	if(i==2){
			    		if( dNext > tolerance && timeUntil < timeTolerance && dNext/timeUntil < speedLimit*60){
			    			counter++;
		    				log(i);
		    				lats[i]=lats[i+1];
		    				longs[i]=longs[i+1];
			    		}
			    	}
			    	if(i>2){
			    		if(dPrev > tolerance && timeSince < timeTolerance && dPrev/timeSince < speedLimit*60){
			    				counter++;
			    				//log(i);
			    				lats[i]=lats[i-1];
			    				longs[i]=longs[i-1];
			    				continue;
			    		}
			    		else{writer.println(entries[i]);}
			    	}
			    }

			    writer.print(endStuff);
			    double loss = ((counter/counter2)*100);
			    log("Tolerance: "+tolerance+"km  \tEntries removed: " + round(loss,2) + "%" + "\t ("+counter+")");
			    //writer.println("Tolerance: "+tolerance+"km  \tEntries removed: " + round(loss,2) + "%" + "\t ("+counter+")");
			    writer.close();
			    j = j + toleranceIncrements;
		    }
		    
		    
		  }

		  final static String FILE_NAME = "C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\take1\\history-2016-02-16.kml";
		  final static String OUTPUT_FILE_NAME = "C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\output.kml";
		  final static Charset ENCODING = StandardCharsets.UTF_8;
		  
		  //For smaller files

		  /**
		   Note: the javadoc of Files.readAllLines says it's intended for small
		   files. But its implementation uses buffering, so it's likely good 
		   even for fairly large files.
		  */  
		  List<String> readSmallTextFile(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
		  }
		  
		  void writeSmallTextFile(List<String> aLines, String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    Files.write(path, aLines, ENCODING);
		  }
		  static double round(double value, int places) {
			    if (places < 0) throw new IllegalArgumentException();

			    long factor = (long) Math.pow(10, places);
			    value = value * factor;
			    long tmp = Math.round(value);
			    return (double) tmp / factor;
			}
		  private static double earthdist(double long1, double lat1, double long2, double lat2)
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


		  private static void log(Object aMsg){
		    System.out.println(String.valueOf(aMsg));
		  }
		  
		} 
