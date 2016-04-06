import java.io.File;
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

public class DecentSmoothing {

	public static void main(String[] args) throws IOException{
			
			/*
			 * Param Explanations
			 * minTolerance - Minimum tested tolerance in km. Ex, 0 would remove any points that
			 * 	are not at the exact same location as the other values (given other conditions met)
			 * 
			 * maxTolerance - Maximum tested tolerance in km. Ex, 100 would remove any points that
			 * 	are beyond 100km from the previous entry value (given other conditions met)
			 * 
			 * toleranceStep - Variance in toleration between each test (km)
			 * 
			 * timeTolerance - Time (min) between points beyond which the point will not be removed. This
			 * 	is done such that points are spaced greatly apart due to lack of connection on transportation,
			 *	logging turned off, etc. Set to large number to treat all data as effectively continuous
			 * 
			 * speedLimit - Set a max speed allowable. This allows the filtering of points that may be close enough
			 * 	to the previous location geographically to not be filtered, but would have traveled between
			 * 	the two impossibly quickly
			 * 
			 * printToFiles - Boolean value to whether or not you want it writing each output to a
			 * 	respectively named file or view removal data in console only (TRUE prints, FALSE view-only)
			 * 
			 * */		
		
			DecentSmoothing KML = new DecentSmoothing(); 
			String input = "C:\\Users\\Alex\\Documents\\France KMLs\\Raw Daily KMLs\\January\\history-2016-01-29.kml";
			
			/* Input Params */
			int minTolerance		= 10; /*km*/
		    int maxTolerance		= 100; /*km*/
		    int toleranceStep		= 5; /*km*/
		    int timeTolerance 		= 10; /*min*/
		    int speedLimit			= 400; /*km/hr*/
		    boolean printToFiles	= true;
						
			KML.smooth(input, minTolerance, maxTolerance, toleranceStep, timeTolerance, speedLimit, printToFiles);
			
	}
		/* Smoothes the first track of a multitrack type KML*/
		void smooth(String input, int _min, int _max, int _tolInc, int _timeTol, int _speedLim, boolean _print) throws IOException{
			DecentSmoothing text = new DecentSmoothing(); 
			List<String> lines = text.readSmallTextFile(input);
			String data = "";
			for (int i = 0; i<lines.size(); i++){
				data += lines.get(i);
			}
		    String[] entries= data.split("(?=<when>)");
		    String headerStuff = entries[0];
		    //log(headerStuff);
		    String endStuff = entries[(entries.length-1)].split("(?></gx:coord>)")[1];
		    
		    int minTolerance	= _min;
		    int maxTolerance	= _max;
		    int toleranceStep	= _tolInc; 
		    int timeTolerance 	= _timeTol; 
		    int speedLimit		= _speedLim; 
		    
		    
		    PrintWriter writer = new PrintWriter("KMLSmoothing.dat");
		    int j = minTolerance;
		    while(j<=maxTolerance){
		    	if(_print){
		    		String dest = input.split(".kml")[0]+"\\";
		    		//log(dest);
		    		File ftemp = new File(dest);
		    		ftemp.mkdir();
		    		writer = new PrintWriter(dest+"distCut"+Math.abs(j)+".kml", "UTF-8");
		    		headerStuff = headerStuff.replace("Location history", "Location history modified by distance limit of"+j+"km-");
		    		writer.print(headerStuff);
		    	}
			    double tolerance = (j);/*km*/
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
			    		if( dNext > tolerance && timeUntil < timeTolerance && dNext/timeUntil < speedLimit*60){
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
			    		if(dPrev > tolerance && timeSince < timeTolerance && dPrev/timeSince < speedLimit*60){
			    				removedCounter++;
			    				//log(i);
			    				lats[i]=lats[i-1];
			    				longs[i]=longs[i-1];
			    				continue;
			    		}
			    		else{ if(_print){writer.print(entries[i]);}}
			    	}
			    }

			    if(_print){writer.print(endStuff);}
			    double loss = ((removedCounter/totalCounter)*100);
			    log("Tolerance: "+tolerance+"km  \tEntries removed: " + round(loss,2) + "%" + "\t ("+removedCounter+")");
			    //writer.println("Tolerance: "+tolerance+"km  \tEntries removed: " + round(loss,2) + "%" + "\t ("+counter+")");
			    writer.close();
			    j = j + toleranceStep;
		    }		    
  
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
