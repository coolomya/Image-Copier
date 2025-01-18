package imagenavigator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ProperyFileHandler {
	
	Properties prop = new Properties();
	String propertyFilePath = "./" + ImageCopierConstants.PROPERTY_FILE_NAME;
	
	public ProperyFileHandler() {
		loadNewProperty(propertyFilePath);
	}
	
	private void loadNewProperty(String propertyFilePath) {
		
		//if property file name doesnt exists create new one
		File propertyFile = new File(propertyFilePath);
		try {
			//System.out.println("creating new file : " + propertyFilePath);
			propertyFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Could not create property file : " + propertyFilePath);
			e.printStackTrace();
		}
		
		try(FileReader file = new FileReader(propertyFile);){
			prop.load(file);
		} catch (IOException e) {
			System.err.println("Cannot read file : " + propertyFilePath);
			e.printStackTrace();
		};
	}
	

	/**
	 * Create new property if doesnt exists, otherwise update old property
	 * @param propertyKey
	 * @param propertyValue
	 * @return
	 */
	public void putProperty(String propertyKey, String propertyValue) {
		putProperty(prop, propertyFilePath, propertyKey, propertyValue);
	}


	private void putProperty(Properties property, String fileName, String propertyKey, String propertyValue) {
		
		if(fileName == null) {
			return;
		}
		
		try(FileOutputStream file = new FileOutputStream(fileName)){
			if (property.containsKey(propertyKey)){
				//System.out.println("contains key : " + propertyKey);
				property.put(propertyKey, propertyValue);			
			}
			//System.out.println("putting property : " + propertyKey + "=" + propertyValue);
			property.setProperty(propertyKey, propertyValue);
			property.store(file, null);
		} catch (FileNotFoundException e) {
			System.err.println("File not present " + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Some error while reading file " + fileName);
			e.printStackTrace();
		}
		return;
	}

	public String getProperty(String key) {
		return prop.containsKey(key)? (String) prop.get(key) : "";
	};

}
