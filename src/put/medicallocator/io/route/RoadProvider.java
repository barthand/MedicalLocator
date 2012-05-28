package put.medicallocator.io.route;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RoadProvider {

	public static Route getRoute(InputStream is) {
		KMLHandler handler = new KMLHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handler.route;
	}

	public static String getUrl(double fromLat, double fromLong, double toLat,
			double toLong) {
		// connect to map web service
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString(fromLat));
		urlString.append(",");
		urlString.append(Double.toString(fromLong));
		urlString.append("&daddr=");// to
		urlString.append(Double.toString(toLat));
		urlString.append(",");
		urlString.append(Double.toString(toLong));
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		return urlString.toString();
	}
}

@SuppressWarnings({"rawtypes","unchecked"})
class KMLHandler extends DefaultHandler {
	Route route;
	boolean isPlacemark;
	boolean isRoute;
	boolean isItemIcon;
	private Stack currentElement = new Stack();
	private String string;

	public KMLHandler() {
		route = new Route();
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		currentElement.push(localName);
		if (localName.equalsIgnoreCase("Placemark")) {
			isPlacemark = true;
			route.points = addPoint(route.points);
		} else if (localName.equalsIgnoreCase("ItemIcon")) {
			if (isPlacemark)
				isItemIcon = true;
		}
		string = new String();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String chars = new String(ch, start, length).trim();
		string = string.concat(chars);
	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (string.length() > 0) {
			if (localName.equalsIgnoreCase("name")) {
				if (isPlacemark) {
					isRoute = string.equalsIgnoreCase("Route");
					if (!isRoute) {
						route.points[route.points.length - 1].name = string;
					}
				} else {
					route.name = string;
				}
			} else if (localName.equalsIgnoreCase("color") && !isPlacemark) {
				route.color = Integer.parseInt(string, 16);
			} else if (localName.equalsIgnoreCase("width") && !isPlacemark) {
				route.width = Integer.parseInt(string);
			} else if (localName.equalsIgnoreCase("description")) {
				if (isPlacemark) {
					String description = cleanup(string);
					if (!isRoute)
						route.points[route.points.length - 1].description = description;
					else
						route.description = description;
				}
			} else if (localName.equalsIgnoreCase("href")) {
				if (isItemIcon) {
					route.points[route.points.length - 1].iconUrl = string;
				}
			} else if (localName.equalsIgnoreCase("coordinates")) {
				if (isPlacemark) {
					if (!isRoute) {
						String[] xyParsed = split(string, ",");
						double lon = Double.parseDouble(xyParsed[0]);
						double lat = Double.parseDouble(xyParsed[1]);
						route.points[route.points.length - 1].latitude = lat;
						route.points[route.points.length - 1].longitude = lon;
					} else {
						String[] coodrinatesParsed = split(string, " ");
						route.route = new double[coodrinatesParsed.length][2];
						for (int i = 0; i < coodrinatesParsed.length; i++) {
							String[] xyParsed = split(coodrinatesParsed[i], ",");
							for (int j = 0; j < 2 && j < xyParsed.length; j++)
								route.route[i][j] = Double
										.parseDouble(xyParsed[j]);
						}
					}
				}
			}
		}
		currentElement.pop();
		if (localName.equalsIgnoreCase("Placemark")) {
			isPlacemark = false;
			if (isRoute)
				isRoute = false;
		} else if (localName.equalsIgnoreCase("ItemIcon")) {
			if (isItemIcon)
				isItemIcon = false;
		}
	}

	private String cleanup(String value) {
		String remove = "<br/>";
		int index = value.indexOf(remove);
		if (index != -1)
			value = value.substring(0, index);
		remove = "&#160;";
		index = value.indexOf(remove);
		int len = remove.length();
		while (index != -1) {
			value = value.substring(0, index).concat(
					value.substring(index + len, value.length()));
			index = value.indexOf(remove);
		}
		return value;
	}

	public RoutePoint[] addPoint(RoutePoint[] points) {
		RoutePoint[] result = new RoutePoint[points.length + 1];
		for (int i = 0; i < points.length; i++)
			result[i] = points[i];
		result[points.length] = new RoutePoint();
		return result;
	}

	private static String[] split(String strString, String strDelimiter) {
		String[] strArray;
		int iOccurrences = 0;
		int iIndexOfInnerString = 0;
		int iIndexOfDelimiter = 0;
		int iCounter = 0;
		if (strString == null) {
			throw new IllegalArgumentException("Input string cannot be null.");
		}
		if (strDelimiter.length() <= 0 || strDelimiter == null) {
			throw new IllegalArgumentException(
					"Delimeter cannot be null or empty.");
		}
		if (strString.startsWith(strDelimiter)) {
			strString = strString.substring(strDelimiter.length());
		}
		if (!strString.endsWith(strDelimiter)) {
			strString += strDelimiter;
		}
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {
			iOccurrences += 1;
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
		}
		strArray = new String[iOccurrences];
		iIndexOfInnerString = 0;
		iIndexOfDelimiter = 0;
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {
			strArray[iCounter] = strString.substring(iIndexOfInnerString,
					iIndexOfDelimiter);
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
			iCounter += 1;
		}

		return strArray;
	}
}