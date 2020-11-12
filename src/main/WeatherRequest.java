package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class WeatherRequest {
	public static FileWriter file;
	      
	

	public static String getCurrentWeather(double lat, double lon, String token) throws IOException, InterruptedException {
		
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=6422210f735db5d7502e8bc5b6e6d78a&units=metric"))
			    .header("content-type", "multipart/form-data;" )
			    .method("POST", HttpRequest.BodyPublishers.noBody())
			    .build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
				
			try {
				file = new FileWriter("current_"+token+".json");
	            file.write("["+response.body()+"]");
	            return "Current weather is : " + response.body();
			} catch (IOException e) {
	            e.printStackTrace();
	            
	        } finally {
	        	 
	            try {
	                file.flush();
	                file.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
			return "Request Failed";
	}
	
	public static String get5Days(double lat, double lon, String token) throws IOException, InterruptedException {
		//{API key}
		long DAY_IN_MS = 1000 * 60 * 60 * 24;
		
		Date prev = new Date(System.currentTimeMillis());
		      
		// Using DateFormat format method we can create a string 
		// representation of a date with the defined format.
		
		String millis = prev.getTime()/1000 + "";
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create("https://api.openweathermap.org/data/2.5/onecall/timemachine?lat="+ lat+ "&lon="+ lon+ "&dt="+millis+"&appid=6422210f735db5d7502e8bc5b6e6d78a&units=metric"))
			    .header("content-type", "multipart/form-data;" )
			    .method("POST", HttpRequest.BodyPublishers.noBody())
			    .build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			try {
			 file = new FileWriter("historical_"+token+".json");
	            file.write("["+response.body()+"]");
	            return "Historical 5 Days Weather Forecast : " + response.body();
			} catch (IOException e) {
	            e.printStackTrace();
	            
	        } finally {
	        	 
	            try {
	                file.flush();
	                file.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
			return "Request failed ";
	}
	
	public static String getMinuteForecast(double lat, double lon, String token) throws IOException, InterruptedException {
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openweathermap.org/data/2.5/onecall?lat="+ lat+"&lon="+lon+"&exclude=minute&appid=6422210f735db5d7502e8bc5b6e6d78a&units=metric"))
			    .header("content-type", "multipart/form-data;" )
			    .method("POST", HttpRequest.BodyPublishers.noBody())
			    .build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			try {
				 file = new FileWriter("minute_"+token+".json");
		            file.write("["+response.body()+"]");
		            return "Minute Forecast for 1 Hour: " + response.body();
				} catch (IOException e) {
		            e.printStackTrace();
		            
		        } finally {
		        	 
		            try {
		                file.flush();
		                file.close();
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		        }
			return "Request failed";
			
	}
public static String getMap(String layer, int zoom, int x, int y, String token) throws IOException, InterruptedException {
		
	  URL url = new URL("https://tile.openweathermap.org/map/"+ layer + "/"+ zoom+ "/"+x+"/"+y+".png?appid=6422210f735db5d7502e8bc5b6e6d78a");
	    try(InputStream is = url.openStream();
	    OutputStream os = new FileOutputStream("basic_"+token+".png")){

	            byte[] b = new byte[2048];
	            int length;

	            while ((length = is.read(b)) != -1) {
	                os.write(b, 0, length);
	            }
	            return "Map Downloaded successfully";

	            }catch(IOException  e){
	            throw e;
	            }

			
	}
//https://api.openweathermap.org/data/2.5/onecall?lat=26&lon=45
public static String getSevenDays(double lat, double lon, String token) throws IOException, InterruptedException{
	
	  HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openweathermap.org/data/2.5/onecall?lat="+ lat+ "&lon="+ lon + "&exclude=daily&appid=6422210f735db5d7502e8bc5b6e6d78a"))
			    .header("content-type", "multipart/form-data;" )
			    .method("POST", HttpRequest.BodyPublishers.noBody())
			    .build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			try {
				 file = new FileWriter("DailySeven_"+token+".json");
		            file.write("["+response.body()+"]");
		            return "Daily forecast for 7 days: " + response.body();
				} catch (IOException e) {
		            e.printStackTrace();
		            
		        } finally {
		        	 
		            try {
		                file.flush();
		                file.close();
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		        }
			return "Request failed";
			
	}
	

	
	public static Object readJson(String cityName) throws ParseException {
		
		   JSONParser parser = new JSONParser();
		
	        try {     
	        	
	        	   Object obj = parser.parse(new FileReader("cities.json"));
		            //System.out.println(obj);
		      
		            JSONArray jsonObject =  (JSONArray) obj;
		            for(int i = 0; i<jsonObject.size(); i++) {
		            	  JSONObject temp = (JSONObject) jsonObject.get(i);
		            	  String current =  (String) temp.get("name") ;
		            	  if(current.equals(cityName)) {
		            		  return temp.get("coord");
		            	  }
		            	  if(isNumeric(cityName) && (Long) temp.get("id") == Long.parseLong(cityName)) {
		            		 
		            		  return temp.get("coord");
		            	  }
		            }
		         
		            
	           
	          
	         
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return null; 
	    }
	
	public static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
}

