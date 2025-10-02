package org.smileaf.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * TODO: Reload or refresh resources.
 * TODO: Better Organization of loading resources.
 */

/**
 * Contains all Resources loaded from XML files.
 * @author smileaf
 *
 */
public class ResourceStore implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Collection of all Resources
	 */
	protected ArrayList<Resource> store = new ArrayList<Resource>();
	// Used to find the Resources of classes implementing this class.
	private static Class <?>clazz;
	
	public ResourceStore() {}
	/**
	 * This must be called before any other method. Otherwise the clazz will not be set
	 * and some methods will fail!
	 * @param paths Paths to load resources from.
	 * @param clazz a class containing the Resources
	 * @return True on success, False on failure.
	 */
	public boolean load(ArrayList<String> paths, Class<?> clazz) {
		ResourceStore.clazz = clazz;
		store.clear();
		Dialog.p("Loading Resources [ ");
		try {
			// Load all resources:
			// Creatures
			//   Abilities
			//   Items
			// Jobs
			//   Abilities
			
			Creature mon;
			Item item;
			Ability ability;
			Job job;
			for (String path : paths) {
				ArrayList<String>mList = getFileListing(clazz, path, new ArrayList<String>());
				mList.removeIf(n -> (!n.endsWith("xml")));
				load: for (String filename : mList) {
					//System.out.println("loading: "+filename);
					Document doc = getDoc(path + "/" + filename);
					if (doc == null) {
						Dialog.pln("] Failed!");
						Dialog.pln("Error opening Document: " + filename);
						return false;
					}

					try {
						NodeList nList = doc.getElementsByTagName("Ability");
						for (int x = 0; x < nList.getLength(); x++) {
							Node nNode = nList.item(x);
							ability = new Ability();
							ability.load((Element) nNode, this);
							ability.resourceType = Resource.ABILITY;
							ability.filename = filename;
							store.add(ability);
							Dialog.p("A");
						}
						
						nList = doc.getElementsByTagName("Item");
						for (int x = 0; x < nList.getLength(); x++) {
							Node nNode = nList.item(x);
							item = new Item();
							item.load((Element) nNode, this);
							item.resourceType = Resource.ITEM;
							item.filename = filename;
							store.add(item);
							Dialog.p("I");
						}
						if (path.equals("jobs")) {
							job = new Job();
							job.load(doc.getDocumentElement(), this);
							job.resourceType = Resource.JOB;
							job.filename = filename;
							store.add(job);
							Dialog.p("J");
						} else {
							mon = new Creature();
							mon.load(doc.getDocumentElement(), this);
							mon.resourceType = Resource.CREATURE;
							mon.filename = filename;
							store.add(mon);
							Dialog.p("C");
						}
					} catch (ResourceLoadException e) {
						e.printStackTrace();
						Dialog.p("!");
						continue load;
					}
				}
			}
		} catch (URISyntaxException | IOException e1) {
			Dialog.pln(" ] Failed!");
			e1.printStackTrace();
			return false;
		}
		Dialog.pln(" ] Done!");
		return true;
	}
	/**
	 * XML Document loader
	 * @param filename File to load
	 * @return XML Document
	 */
	public static Document getDoc(String filename) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
			//System.out.println("loading: "+filename);
			InputStream is = clazz.getResourceAsStream("/"+filename);
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Dialog.debugln("Failed");
			return null;
		}
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from
	 * @param tag tag to read
	 * @param d default value
	 * @param element True Element Content, False for Attribute
	 * @return Value
	 */
	public static int getValueFrom(Element node, String tag, int d, boolean element) {
		int value = d;
		try {
			if (element) {
				Node e = ResourceStore.getChildNodeByName(node, tag);
				if (e != null)
					value = Integer.parseInt(e.getTextContent());
			} else if (node.hasAttribute(tag)){
				value = Integer.parseInt(node.getAttribute(tag));
			}
		} catch (NumberFormatException e) {
			value = d;
		}
		return value;
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from
	 * @param tag tag to read
	 * @param d default value
	 * @param element True Element Content, False for Attribute
	 * @return Value
	 */
	public static float getValueFrom(Element node, String tag, float d, boolean element) {
		float value = d;
		try {
			if (element) {
				Node e = ResourceStore.getChildNodeByName(node, tag);
				if (e != null)
					value = Float.parseFloat(e.getTextContent());
			} else if (node.hasAttribute(tag)){
				value = Float.parseFloat(node.getAttribute(tag));
			}
		} catch (NumberFormatException e) {
			value = d;
		}
		return value;
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from
	 * @param tag tag to read
	 * @param d default value
	 * @param element True Element Content, False for Attribute
	 * @return Value
	 */
	public static boolean getValueFrom(Element node, String tag, boolean d, boolean element) {
		if (node == null) return d;
		boolean value = d;
		try {
			if (element) {
				Node e = ResourceStore.getChildNodeByName(node, tag);
				if (e != null)
					value = Boolean.parseBoolean(e.getTextContent());
			} else if (node.hasAttribute(tag)){
				value = Boolean.parseBoolean(node.getAttribute(tag));
			}
		} catch (NumberFormatException e) {
			value = d;
		}
		return value;
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from
	 * @param tag tag to read
	 * @param d default value
	 * @param element True Element Content, False for Attribute
	 * @return Value
	 */
	public static String getValueFrom(Element node, String tag, String d, boolean element) {
		String value = d;
		if (element) {
			Node e = ResourceStore.getChildNodeByName(node, tag);
			if (e != null)
				value = e.getTextContent();
		} else if (node.hasAttribute(tag)){
			value = node.getAttribute(tag);
		}
		return value;
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from.
	 * @param name Name of the Child node.
	 * @return Child Node
	 */
	public static Node getChildNodeByName(Node node, String name) {
		if (node == null) return null;
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals(name)) {
				return nodes.item(i);
			}
		}
		return null;
	}
	/**
	 * XML Helper method
	 * @param node Node to load data from.
	 * @param name Name of the Child node.
	 * @return Child Nodes
	 */
	public static ArrayList<Node> getChildNodesByName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();
		if (node == null) return list;
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals(name)) {
				list.add(nodes.item(i));
			}
		}
		return list;
	}
	/**
	 * Returns a list of resources matching the name given.
	 * @param name Resource name.
	 * @return List of Resources
	 */
	public ArrayList<Resource> getResourcesByName(String name) {
		return ResourceStore.filter(this.store, r -> r.name.equals(name));
	}
	/**
	 * Returns a list of resources matching the type given.
	 * @param type Type of Resource
	 * @return List of Resources
	 */
	public ArrayList<Resource> getResourcesByType(int type) {
		return ResourceStore.filter(this.store, r -> r.resourceType == type);
	}
	/**
	 * Returns a list of resources matching the name AND type.
	 * @param name Name of Resource
	 * @param type Type of Resource
	 * @return List of Resources
	 */
	public ArrayList<Resource> getResourcesByNameAndType(String name, int type) {
		return ResourceStore.filter(this.store, r -> r.name.equals(name) && 
													 r.resourceType == type);
	}
	/**
	 * Returns a Filtered list of Resources
	 * @param <T> Type of contained Object
	 * @param list List to filter
	 * @param predicate List of rules to match.
	 * @return Filtered List of Resources
	 */
	public static <T> ArrayList<T> filter(List<T> list, Predicate<T> predicate) {
		ArrayList<T> accumulator = new ArrayList<>();
	     for (T item : list)
	          if(predicate.test(item))
	              accumulator.add(item);
	     return accumulator;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (Resource item : this.store) {
			s += item.name + " : " + item.resourceType + "\n";
		}
		return s;
	}
	/**
	 * Returns the Resource List
	 * @return Full Resource List
	 */
	public ArrayList<Resource> getStore() { return this.store; }
	/*
	 * JSON Helper methods.
	 */
	/*
	public static boolean getKey(JSONObject iData, String key, boolean d) {
		if (iData.has(key))
			return iData.getBoolean(key);
		return d;
	}
	public static String getKey(JSONObject iData, String key, String d) {
		if (iData.has(key))
			return iData.getString(key);
		return d;
	}
	public static float getKey(JSONObject iData, String key, float d) {
		if (iData.has(key))
			return (float)iData.getDouble(key);
		return d;
	}
	public static double getKey(JSONObject iData, String key, double d) {
		if (iData.has(key))
			return iData.getDouble(key);
		return d;
	}
	public static int getKey(JSONObject iData, String key, int d) {
		if (iData.has(key))
			return iData.getInt(key);
		return d;
	}*/
	
	/* XML Example
	 * 
		try {

			File fXmlFile = new File("/Users/mkyong/staff.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
					
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList nList = doc.getElementsByTagName("staff");
					
			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
						
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
						
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					System.out.println("Staff id : " + eElement.getAttribute("id"));
					System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
					System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
					System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
					System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());

				}
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	 */
	
	/**
	 * Loads a Text resource file
	 * @param filename resource file to load
	 * @return String contents of the file.
	 * @throws IOException On read error
	 */
	public static String loadResource(String filename) throws IOException {
		InputStream is = clazz.getResourceAsStream(filename);
		if (is == null) {
			//System.out.println("Error loading: " + filename);
			// Lets assume if this is true, we're inside our jar.
			is = clazz.getResourceAsStream("/src" + filename);
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			  sb.append(line + '\n');
		}
		br.close();
		isr.close();
		is.close();
		return sb.toString();
	}
	
	/**
	* List directory contents for a resource folder into a target collection. Not recursive. 
	* This is basically a brute-force implementation. Works for regular files and also JARs.
	* Based on an approach by Greg Briggs
	* 
	* @param clazz any java class that lives in the same place as the resources you want.
	* @param path should end with "/", but not start with one.
	* @param result the collection to store the result.
	* @return the target collection, 
	*         where just the name of each member item, not the full path, is added.
	* @throws URISyntaxException
	* @throws IOException
	*/
	static <T extends Collection<? super String>> T getFileListing(
	    final Class<?> clazz, final String path, final T result) throws URISyntaxException, 
	                                                                   IOException {
		URL dirURL = clazz.getResource("/"+path);
		//URL dirURL = new URL(resourcePath + path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			result.addAll(Arrays.asList(new File(dirURL.toURI()).list()));
			return result;
		}
		if (dirURL == null) {
			 // In case of a jar file, we can't actually find a directory. 
			 // Have to assume the same jar as clazz.
			 final String me = clazz.getName().replace(".", "/") + ".class";
			 dirURL = clazz.getResource(me);
		}
		// if its still null nothing else we can do.
		if (dirURL == null)
			throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
		if (dirURL.getProtocol().equals("jar")) { /* A JAR path */
			// strip out only the JAR file
			final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
			final JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
			while (entries.hasMoreElements()) {
				final String name = entries.nextElement().getName();
				final int pathIndex = name.lastIndexOf(path);
				if (pathIndex >= 0) {
					final String nameWithPath = name.substring(name.lastIndexOf(path));
					//final String nameWithPath = name;
					result.add(nameWithPath.substring(path.length()+1));
					//result.add(nameWithPath);
					
					//System.out.println("Adding: " + nameWithPath + "");
				}
			}
			jar.close();
			return result;
		}
		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	public <T extends Collection<? super String>> T getDir(final String path, final T result) throws URISyntaxException, IOException {
		Path p = this.getFolderPath(path);
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(p);
        for (Path pt : directoryStream) {
        	result.add(pt.toString());
        }
		return result;
	}
	public Path getFolderPath(final String path) throws URISyntaxException, IOException {
		Dialog.debugln("Loading " + path);
		URI uri = getClass().getClassLoader().getResource("folder").toURI();
		if ("jar".equals(uri.getScheme())) {
			FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
			return fileSystem.getPath(path);
	    } else {
	    	return Paths.get(uri);
	    }
	}
}
