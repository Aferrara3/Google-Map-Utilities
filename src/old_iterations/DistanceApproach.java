package old_iterations;
		import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DistanceApproach {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		    DistanceApproach text = new DistanceApproach(); 
		    //treat as a small file
		    List<String> lines = text.readSmallTextFile(FILE_NAME);
		    log(lines);
		    //lines.add("This is a line added in code.");
		    text.writeSmallTextFile(lines, OUTPUT_FILE_NAME);
		    
		    //Because Google history KMLs are all on one line
		    String firstLine = lines.get(0);
		    String[] entries= firstLine.split("(?=<when>)");
		    String headerStuff = entries[0];
		    String endStuff = entries[(entries.length-1)].split("(?></gx:coord>)")[1];
		    //log(entries[1]);
		    
		    double[] xCoords = new double[entries.length-1];
		    double[] yCoords = new double[entries.length-1];
		    int j = 0;
		    while(j<40){
		    	//PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\highTol"+Math.abs(j)+".kml", "UTF-8");
		    	PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\tests.kml", "UTF-8");
		    	writer.print(headerStuff);
			    double tolerance=(j);
			    double counter=0;
			    double counter2 = 0;
			    for(int i=1; i<entries.length-3; i++){
			    	String current = entries[i].split("(?><gx:coord>)")[1].split("(?><gx:coord>)")[0];
			    	String next = entries[i+1].split("(?><gx:coord>)")[1].split("(?><gx:coord>)")[0];
			    	//log(temp0);
				    double xCoord = Double.parseDouble(current.split(" ")[0]);
		    		double yCoord = Double.parseDouble(current.split(" ")[1]);
		    		
		    		double xCoord2 = Double.parseDouble(next.split(" ")[0]);
		    		double yCoord2 = Double.parseDouble(next.split(" ")[1]);
		    		//log(xCoord + ", " + yCoord); //Print coordinate pairs
		    		counter2++;
		    		double dist = earthdist(xCoord,yCoord,xCoord2,yCoord2);
		    		//log(xCoords[i] + " " +xCoords[i-1] + " " +yCoords[i] + " " +yCoords[i-1] + " " + dist);
		    		if( dist > tolerance){
		    			counter++;
		    			//Need better logic to decide which value to keep
		    			//Need to account for first value being the shitty one as is the case here
		    			xCoords[i]=xCoord2;
		    			yCoords[i]=yCoord2;
		    			continue;
		    		}
		    		else{
		    			xCoords[i] = xCoord;
		    			yCoords[i] = yCoord;
		    			writer.print(entries[i]);
		    		}
		    		
/*		    		PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\toleranceNeg"+Math.abs(j)+".kml", "UTF-8");
				    writer.println("The first line");
				    writer.println("The second line");
				    writer.close();
*/
			    }
			    writer.print(endStuff);
			    double loss = ((counter/counter2)*100);
			    log("Tolerance: "+tolerance+"m  \tEntries removed: " + round(loss,2) + "%" + "\t ("+counter+")");
			    //writer.println("<!--Tolerance: "+tolerance+"m  \tEntries removed: " + round(loss,2) + "%-->");
			    writer.close();
			    j = j + 10;
		    }
		    
		    
		  }

		  final static String FILE_NAME = "C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\history-2016-02-05.kml";
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
