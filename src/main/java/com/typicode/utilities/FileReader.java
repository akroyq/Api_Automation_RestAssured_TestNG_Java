package com.typicode.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class FileReader {

	public String readFile(String fileName) throws IOException {
		ClassLoader loader = getClass().getClassLoader();
		InputStream inputStream = loader.getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}
	
	public static Map<String, String> readProperties(final String path) {
		Properties prop = new Properties();
		Map<String, String> map = new HashMap<String, String>();
		InputStream input = null;
		try {
			input = new FileInputStream(path);
			prop.load(input);
		} catch (IOException e) {
			System.out.println("Exception at readproperties:" +e);
		}
		for (java.util.Map.Entry<Object, Object> entries : prop.entrySet()) {
			map.put((String) entries.getKey(), (String) entries.getValue());

		}
		return map;
	}
	
}
