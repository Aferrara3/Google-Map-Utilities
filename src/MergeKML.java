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

public class MergeKML {

	public static void main(String[] args) throws IOException{
		
			MergeKML KML = new MergeKML();
			String dir = "C:\\Users\\Alex\\Documents\\France KMLs\\takeout-20160404T143853Z\\Takeout\\Location History\\";
			String f1 = dir+"history-2016-03-10.kml";
			String f2 = dir+"history-2016-03-11.kml";
			String f3 = dir+"history-2016-03-12.kml";
			String f4 = dir+"history-2016-03-13.kml";
			String output = dir + "merged.kml";
			String[] inputs = {f1, f2, f3, f4};
			String[] inputDir = KML.getAllKml(dir);
			KML.merge(inputDir, output);
	}

		/* Merges multitrack KML files maintaining the header information from first file */
		void merge(String[] inputs, String dest) throws IOException{
			MergeKML text = new MergeKML(); 
			String f1= text.readSmallTextFile(inputs[0]).get(0);
			String headerStuff = f1.split("(?=<Placemark>)")[0];
			PrintWriter writer = new PrintWriter(dest, "UTF-8");
		    headerStuff = headerStuff.replace("Location history", "Merged location histories");
		    writer.print(headerStuff);
			
			for(int i=0; i<inputs.length; i++){
				List<String> lines = text.readSmallTextFile(inputs[i]);
				
				String data = "";
				for (int j = 0; j<lines.size(); j++){
					data += lines.get(j);
				}				
				
				//contents+=(lines.get(0).split("(?=<Placemark>)")[1]).split("(?<=</Placemark>)")[0];
				//log(i+": "+(lines.get(0).split("(?=<gx:Track>)")[1]).split("(?<=</gx:Track>)")[0]);
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
			
			KmlConvert converter = new KmlConvert();
			converter.convert(dest, ".gpx");
			
		}		    
		
		private String[] getAllKml(String dir) {
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			ArrayList<String> temp = new ArrayList<String>();
			
			for (int i = 0; i < listOfFiles.length; i++) {
				String name = listOfFiles[i].getName();
					if (listOfFiles[i].isFile()
							&& name.substring(name.length()-4, name.length()).equals(".kml")) {
						temp.add(dir+name);
						log(name + "added to merge set");
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
		  
		  Charset ENCODING = StandardCharsets.UTF_8;
		  List<String> readSmallTextFile(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
		  }
		  
		  static void log(Object aMsg){
		    System.out.println(String.valueOf(aMsg));
		  }

		} 
