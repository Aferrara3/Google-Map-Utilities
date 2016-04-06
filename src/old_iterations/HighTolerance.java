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

public class HighTolerance {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		    HighTolerance text = new HighTolerance(); 
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
		    
		    for(int j=0; j<20; j++){
		    	
		    	PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\highTol"+Math.abs(j)+".kml", "UTF-8");
		    	writer.print(headerStuff);
			    double tolerance=(j*0.01);
			    double counter=0;
			    double counter2 = 0;
			    for(int i=1; i<entries.length-2; i++){
			    	String temp0 = entries[i];
			    	String temp = entries[i].split("(?><gx:coord>)")[1].split("(?><gx:coord>)")[0];
				    double xCoord = Double.parseDouble(temp.split(" ")[0]);
		    		double yCoord = Double.parseDouble(temp.split(" ")[1]);
		    		//log(xCoord + ", " + yCoord); //Print coordinate pairs
		    		xCoords[i] = xCoord;
		    		yCoords[i] = yCoord;
		    		counter2++;
		    		if( i>1 && (Math.abs((xCoords[i]-xCoords[i-1])) > tolerance
		    				|| Math.abs((yCoords[i]-yCoords[i-1])) > tolerance)
		    				){
		    			counter++;
		    			//xCoords[i]=xCoords[i-1];
		    			//yCoords[i]=yCoords[i-1];
		    			continue;
		    		}
		    		writer.print(entries[i]);
/*		    		PrintWriter writer = new PrintWriter("C:\\Users\\Alex\\Documents\\France KMLs\\Script Testing\\toleranceNeg"+Math.abs(j)+".kml", "UTF-8");
				    writer.println("The first line");
				    writer.println("The second line");
				    writer.close();
*/
			    }
			    writer.print(endStuff);
			    double loss = ((counter/counter2)*100);
			    log("Tolerance: "+round(tolerance,2)+"  \tEntries removed: " + round(loss,2) + "%");
			    writer.println("<!--Tolerance: "+round(tolerance,2)+"  \tEntries removed: " + round(loss,2) + "%-->");
			    writer.close();
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

		  private static void log(Object aMsg){
		    System.out.println(String.valueOf(aMsg));
		  }
		  
		} 
