package es.ajifernandez.requestcityelevation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlis.location.model.impl.Address;
import com.atlis.location.model.impl.MapPoint;
import com.atlis.location.nominatim.NominatimAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestCityElevationApp {

	public static void main(String[] args) throws Exception {
		final Logger LOGGER = LoggerFactory.getLogger(RequestCityElevationApp.class);
		String endpointUrl = "https://nominatim.openstreetmap.org/";
		System.setProperty("https.protocols", "TLSv1.2");
		BufferedReader reader;
		try {

			String resourceFile = "nodes.txt";

			InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceFile);

			reader = new BufferedReader(new InputStreamReader(resourceStream));
			String node = reader.readLine();
			while (node != null) {

				if (node.indexOf("(") > 0) {
					node = node.substring(0, node.indexOf("(") - 1);
				}
				node = node.replaceAll(" ", "%20");

				Address address = new Address();
				address.setCity(node);
				MapPoint mapPointFromAddress = NominatimAPI.with(endpointUrl).getMapPointFromAddress(address, 5);
				Double lat = mapPointFromAddress.getLatitude();
				Double lon = mapPointFromAddress.getLongitude();

				String sURL = "https://elevation-api.io/api/elevation?points=(" + lat + "," + lon + ")";

				// Connect to the URL using java's native library
				URL url2 = new URL(sURL);
				URLConnection request2 = url2.openConnection();
				request2.connect();

				// Convert to a JSON object to print data
				JsonParser jp2 = new JsonParser(); // from gson
				JsonElement root2 = jp2.parse(new InputStreamReader((InputStream) request2.getContent()));
				JsonObject rootobj2 = root2.getAsJsonObject(); // May be an
																// array,
																// may
				// be an object.
				String elevation = ((JsonArray) rootobj2.get("elevations")).get(0).getAsJsonObject().get("elevation")
						.getAsString(); // just grab the
										// zipcode
				
				StringBuilder sb = new StringBuilder();
				sb.append(node);
				sb.append("\t");
				sb.append(lat);
				sb.append("\t");
				sb.append(lon);
				sb.append("\t");
				sb.append(elevation);
				LOGGER.info(sb.toString());

				// read next line
				node = reader.readLine();
			}
			reader.close();
		} catch (

		IOException e) {
			e.printStackTrace();
		}

	}

}
