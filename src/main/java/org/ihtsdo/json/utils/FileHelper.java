package org.ihtsdo.json.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class FileHelper {
	
	private static final Logger log = Logger.getLogger(FileHelper.class);
	
	
	public static int countLines(File file, boolean firstLineHeader) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		LineNumberReader reader = new LineNumberReader(isr);
		int cnt = 0;
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {
		}
		
		cnt = reader.getLineNumber();
		reader.close();
		isr.close();
		fis.close();
		if(firstLineHeader){
			return cnt-1;
		}else{
			return cnt;
		}
	}

	public void findSimpleRefsetFiles(File releaseFolder,String stringKey,String nocontains, HashSet< String> hashSimpleRefsetList) {
		String name="";
		if (hashSimpleRefsetList==null){
			hashSimpleRefsetList=new HashSet<String>();
			
		}
		for (File file:releaseFolder.listFiles()){
			if (file.isDirectory()){
				findSimpleRefsetFiles(file, stringKey,nocontains,hashSimpleRefsetList);
			}else{
				name=file.getName().toLowerCase().trim().replaceAll("-","_");
				if (name.endsWith(".txt") 
						&& name.contains(stringKey)){ 
						
						if (nocontains!=null && !name.contains(nocontains)  ){
							hashSimpleRefsetList.add(file.getAbsolutePath());
						}
				}
			}
		}

	}


	public void findAllFiles(File releaseFolder, HashSet< String> hashSimpleRefsetList) {
		String name="";
		if (hashSimpleRefsetList==null){
			hashSimpleRefsetList=new HashSet<String>();
			
		}
		for (File file:releaseFolder.listFiles()){
			if (file.isDirectory()){
				findAllFiles(file, hashSimpleRefsetList);
			}else{
				name=file.getName();
				if (name.endsWith(".txt")){ 
					hashSimpleRefsetList.add(file.getAbsolutePath());
				}
			}
		}

	}
	
	public static String getFileTypeByHeader(File inputFile, File validationConfig) {
		String namePattern =null;
		try {
			Thread currThread = Thread.currentThread();
			if (currThread.isInterrupted()) {
				return null;
			}
			XMLConfiguration xmlConfig = new XMLConfiguration(validationConfig);

			List<String> namePatterns = new ArrayList<String>();

			Object prop = xmlConfig.getProperty("files.file.fileType");
			if (prop instanceof Collection) {
				namePatterns.addAll((Collection) prop);
			}
//			System.out.println("");
			boolean toCheck = false;
			String headerRule = null;
			FileInputStream fis = new FileInputStream(inputFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String header = br.readLine();
			for (int i = 0; i < namePatterns.size(); i++) {
				if (currThread.isInterrupted()) {
					return null;
				}
				headerRule = xmlConfig.getString("files.file(" + i + ").headerRule.regex");
				namePattern = namePatterns.get(i);
				if( header.matches(headerRule)){
					if ((inputFile.getName().toLowerCase().contains("textdefinition") 
							&& namePattern.equals("rf2-descriptions")) 
							|| (inputFile.getName().toLowerCase().contains("description") 
									&& namePattern.equals("rf2-textDefinition"))){
						continue;
					}
					toCheck = true;
					break;
				}
			}
			if (toCheck) {

				
				 //System.out.println( "File: " + inputFile.getAbsolutePath() +  " ** match file pattern: " + namePattern);
				
			} else {
				
				//System.out.println( "Cannot found header matcher for : " + inputFile.getName());
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println(  "FileAnalizer: " +    e.getMessage());
		} catch (UnsupportedEncodingException e) {
			System.out.println(  "FileAnalizer: " +    e.getMessage());
		} catch (IOException e) {
			System.out.println(  "FileAnalizer: " +    e.getMessage());
		} catch (ConfigurationException e) {
			System.out.println(  "FileAnalizer: " +    e.getMessage());
		}
		return namePattern;
	}

	public static void emptyFolder(File folder){
		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			for (File file : files) {
				if(file.isDirectory()){
					emptyFolder(file);
				}else{
					file.delete();
				}
			}
		}
	}

	public static void copyTo(File inputFile,File outputFile)  throws IOException {

		FileInputStream fis = new FileInputStream(inputFile);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		LineNumberReader reader = new LineNumberReader(isr);
		

		FileOutputStream fos = new FileOutputStream( outputFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {
			bw.append(lineRead);
			bw.append("\r\n");
		}
		reader.close();
		bw.close();
			
	}
}


class FileNameComparator implements Comparator<String>{

	private static final Logger log = Logger.getLogger(FileNameComparator.class);
	private int fieldToCompare;
	private String separator;
	
	public FileNameComparator(int fieldToCompare, String separator){
		this.separator = separator;
		this.fieldToCompare = fieldToCompare;
	}
	
	public int compare(String file1, String file2) {
		String[] file1Split = file1.split(separator); 
		String[] file2Split = file2.split(separator);
		
		String date1 = file1Split[fieldToCompare];
		String date2 = file2Split[fieldToCompare];
		log.debug("First file date: " + date1);
		log.debug("Second file date: " + date2);
		
		return date1.compareTo(date2);
	}
	
}