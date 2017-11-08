package org.oracle.ebs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;



public class Utility {

	public static  String highestVersion = "0.0.0.0";

	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String[] getCSVHeader(String path, String fileName) {

		String[] header = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

		try (BufferedReader br = new BufferedReader(new FileReader(Utility.mergeFiles(path, fileName)))) {
			int counter = 0;
			while ((line = br.readLine()) != null && !line.equals("")) {
				if (counter == 0 ){

					header = line.split(cvsSplitBy);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < header.length; i++) {
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(header[i]);
					}
					System.out.println("-----------  headers -----------");
					System.out.println(sb.toString());					
				}
				counter+=1;
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		} catch (IOException e) {
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return header;
	}

	/**
	 * Read the 
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public static List<String[]> readCSVData(String path, String fileName) {


		String[] dataLine = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<String[]> dataList = new ArrayList<String[]>();

		if(path != null && fileName != null) {
			try (BufferedReader br = new BufferedReader(new FileReader(Utility.mergeFiles(path, fileName)))) {
				int counter = 0;
				while ((line = br.readLine()) != null) {
					if (counter != 0 ){
						dataLine = line.split(cvsSplitBy);
						dataList.add(dataLine);
					}
					counter+=1;
				}

			} catch (FileNotFoundException e) {
				System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
			} catch (IOException e) {
				System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
			}
		}
		return dataList;
	}

	/**
	 * Merge all CSV files
	 * @param directoryPath - Path of the CSV files
	 * @return Merged file
	 * @throws IOException
	 */

	public static String mergeFiles(String directoryPath, String fileName) throws IOException {

		File[] filesInDirectory = new File(directoryPath).listFiles();
		List<Path> paths = new ArrayList<Path>();

		for(File f : filesInDirectory){

			if(f.getName().contains(fileName) && !f.getName().contains("merged")){
				// System.out.println(f.getName());
				String filePath = f.getAbsolutePath();
				String fileExtenstion = filePath.substring(filePath.lastIndexOf(".") + 1,filePath.length());
				if("csv".equals(fileExtenstion)){
					// System.out.println("CSV file found -> " + filePath);
					paths.add(Paths.get(filePath));
				}
			}

		} 

		List<String> mergedLines = getMergedLines(paths);
		Path target = Paths.get(directoryPath+ fileName+"_merged.csv");
		File f = new File(directoryPath+ fileName+"_merged.csv");
		if(!f.exists()){
			Files.write(target, mergedLines, Charset.forName("UTF-8"));
		}else {
			System.out.println("File already exists");
		}
		return f.getAbsolutePath();
	}

	/**
	 * Get all the data rows of the merged CSV file
	 * @param paths - Path of the CSV files
	 * @return List of the data rows
	 * @throws IOException
	 */
	private static List<String> getMergedLines(List<Path> paths)  {
		List<String> mergedLines = new ArrayList<String>();
		if(paths.size() > 0){
			for (Path p : paths){
				List<String> lines;
				try {
					lines = Files.readAllLines(p, Charset.forName("UTF-8"));

					if (!lines.isEmpty()) {
						if (mergedLines.isEmpty()) {
							mergedLines.add(lines.get(0)); //add header only once
						}
						mergedLines.addAll(lines.subList(1, lines.size()));
					}
				} catch (IOException e) {
					System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
				}
			}
		}		
		return mergedLines;
	}

	/**
	 * 
	 * @param my_array
	 * @param t
	 * @return
	 */
	public static int findIndex(String[] my_array, String t) {

		if (my_array == null) return -1;
		int len = my_array.length;
		int i = 0;
		if(len > 0) {
			while (i < len) {
				if (my_array[i].equals(t)) return i;
				else i=i+1;
			}
		}

		return -1; 
	}

	/**
	 * Find the list of indexes of the column that contains the keyword
	 * @param dataList - List of all the rows
	 * @param index - index of the column to search
	 * @param keyword - the keyword to look for in the selected column
	 * @return  List of indexes that contains the keyword
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int productPillar, String keyword){
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter =1;
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  

					String valueToCheck = dataLine[productPillar];
					if(valueToCheck.indexOf(keyword) != -1){
						productLevel.add(counter);
					}else{
						// System.out.println("Not matched  -> "+ counter + "  "+  valueToCheck);
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}

		return productLevel;
	}

	/**
	 * 
	 * @param dataList
	 * @param index
	 * @param keywords
	 * @return
	 */

	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String[] keywords){
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter =1;
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  

					String valueToCheck = dataLine[index];
					for(String keyword : keywords){
						if(valueToCheck.indexOf(keyword) != -1){
							productLevel.add(counter);
						}else{
							// System.out.println("Not matched  -> "+ counter + "  "+  valueToCheck);
						}
					}

					counter+=1;
				} 
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}

		return productLevel;
	}

	/**
	 * 
	 * @param dataList - List of all the rows
	 * @param index - index of the column to search
	 * @param keyword - index of the column to search
	 * @param indexToSearch - selected index from the first filter
	 * @return List of indexes that contains the keyword
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String[] keywords, ArrayList<Integer> indexToSearch){
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		int counter =1;
		try {		
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  
					if (indexToSearch.contains(counter)){
						String valueToCheck = dataLine[index];
						for(String keyword : keywords){
							if(valueToCheck.indexOf(keyword) != -1){
								productLevel.add(counter);
							}else{
								//System.out.println("Not matched  -> "+ counter + "  "+  valueToCheck);
							}
						}

					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}

		return productLevel;
	}

	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String keyword, ArrayList<Integer> indexToSearch){
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		int counter =1;
		try {
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  
					if (indexToSearch.contains(counter)){
						String valueToCheck = dataLine[index];
						if(valueToCheck.indexOf(keyword) != -1){
							productLevel.add(counter);
						}else{
							//System.out.println("Not matched  -> "+ counter + "  "+  valueToCheck);
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return productLevel;
	}

	/**
	 * finds the sum of pricing of distinct modules
	 * 
	 * @param dataList
	 * @param firstIndex
	 * @param secondIndex
	 * @param indexToSearch
	 * @return Distinct modules with price
	 * 
	 */
	public static Map<String,Integer> findDistinctModuleAndPrice(List<String[]> dataList, int firstIndex, int secondIndex, ArrayList<Integer> indexToSearch){

		int counter =1;
		Map<String,Integer> modulePricing = new HashMap<String,Integer>();
		try{
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  
					if (indexToSearch != null &&  indexToSearch.contains(counter) && !dataLine[secondIndex].equals("") ){			
						
						if(modulePricing.containsKey(dataLine[firstIndex])){
							modulePricing.put(dataLine[firstIndex], modulePricing.get(dataLine[firstIndex]) + Integer.parseInt(dataLine[secondIndex]));
						}else{
							modulePricing.put(dataLine[firstIndex],Integer.parseInt(dataLine[secondIndex]));
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage() + " Counter -> " +counter);
		}
		return modulePricing;
	}

	/**
	 * 
	 * @param dataList
	 * @param firstIndex
	 * @param secondIndex
	 * @param indexToSearch
	 * @return
	 */

	public static Map<String,String> findDistinctPlatformAndVersions(List<String[]> dataList, int firstIndex, int secondIndex, ArrayList<Integer> indexToSearch){

		int counter =1;
		Map<String,String> platformVersion = new HashMap<String,String>();
		try {
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  
					if (indexToSearch != null && indexToSearch.contains(counter)){
						if(platformVersion.containsKey(dataLine[firstIndex]) && platformVersion.get(dataLine[firstIndex]).equals(dataLine[secondIndex])){

						}else{
							platformVersion.put(dataLine[firstIndex], dataLine[secondIndex]);
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return platformVersion;
	}

	/**
	 * 
	 * @param dataList
	 * @param index
	 * @param indexToSearch
	 * @return
	 */

	public static Map<String,Integer> findPlatformCount(List<String[]> dataList, int index, ArrayList<Integer> indexToSearch){

		int counter =1;
		Map<String,Integer> platformCount = new HashMap<String,Integer>();
		try {
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList)  
				{  
					if (indexToSearch != null && indexToSearch.contains(counter)){
						if(platformCount.containsKey(dataLine[index])){
							platformCount.put(dataLine[index], platformCount.get(dataLine[index]) + 1);
						}else{
							platformCount.put(dataLine[index],1);
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return platformCount;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */

	public static Map<String,Integer> getUniquePlatformsCount(Map<String,Integer> map){
		String[] platforms = {"Linux", "HP", "Microsoft", "Oracle Solaris", "IBM"};
		Map<String,Integer> topTwoPlatforms = new HashMap<String,Integer>();
		try {

			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for(String platformInArray: platforms){
					if(platform.contains(platformInArray)){
						if(topTwoPlatforms.get(platformInArray) == null) {
							topTwoPlatforms.put(platformInArray, map.get(platform));
						} else {
							topTwoPlatforms.put(platformInArray,map.get(platform)+topTwoPlatforms.get(platformInArray));
						}
					}
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return topTwoPlatforms;
	}


	/**
	 * Sorts the module in descending order of pricing value
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortModulesByValue(Map<K, V> map) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		try {
			List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
			Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return (o2.getValue()).compareTo( o1.getValue() );
				}
			});		
			for (Map.Entry<K, V> entry : list) {
				result.put(entry.getKey(), entry.getValue());
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return result;
	}


	/**
	 * Prints the map
	 * @param mapToPrint
	 */
	public static void printMap(Map<String,Integer> mapToPrint){
		try {
			Iterator<String> iterator = mapToPrint.keySet().iterator();

			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				Integer value = mapToPrint.get(key);
				System.out.println(key + " " + value);
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
	}

	/**
	 * 
	 * @param mapToSort
	 * @return
	 */
	public static Map<String,Integer> printTopModules(Map<String,Integer> mapToSort){

		Iterator<String> iterator = mapToSort.keySet().iterator();
		List<Integer> topValues = new ArrayList<>();
		Map<String, Integer> topModulesMap = new HashMap<String, Integer>();
		int counter = 0;
		try {
			while (iterator.hasNext()) {
				if (counter <7){
					String key = iterator.next().toString();
					Integer value = mapToSort.get(key);
					topValues.add(value);
				}else{
					break;
				}
				counter+=1;

			}

			Iterator<String> iterator2 = mapToSort.keySet().iterator();

			while (iterator2.hasNext()) {
				String key = iterator2.next().toString();
				if (topValues.contains(mapToSort.get(key)) && topModulesMap.size()< 15){
					topModulesMap.put(key, mapToSort.get(key));

				}


			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return topModulesMap;

	}

	/**
	 * 
	 * @param map
	 */

	public static void convertToJSON(Map<String, Integer> map){
		Gson gson = new Gson(); 
		String json = gson.toJson(map); 
		System.out.println("JSON: "+ json);
	}

	/**
	 * 
	 * @param modulePricingMap
	 * @param racIndex
	 * @return
	 */

	public static int selectSlide(Map<String, Integer> modulePricingMap, ArrayList<Integer> racIndex  ){
		int slideNumber = 0;
		try {
			if(racIndex.size() > 0 ){
				slideNumber = 7;
			}else if (sortModulesByValue(modulePricingMap).size() <= 15 && racIndex.size() == 0){
				slideNumber = 5;
			} else if(sortModulesByValue(modulePricingMap).size() <= 20 && racIndex.size() == 0)  {
				slideNumber = 6;
			}else if(sortModulesByValue(modulePricingMap).size() > 20 && racIndex.size() == 0)  {
				slideNumber = 7;
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return slideNumber;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */

	public static Map<String,String> getLatestVersionMap(Map<String, String> map){

		String[] platforms = {"Linux", "HP", "Microsoft", "Oracle Solaris", "IBM"};

		Map<String,String> latestVersionMap = new HashMap<String,String>();
		try {
			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for(String platformInArray: platforms){
					if(platform.contains(platformInArray)){
						if(latestVersionMap.get(platformInArray) != null) {
							latestVersionMap.put(platformInArray, getLatestVersion(map.get(platform), latestVersionMap.get(platformInArray)));
						} else {
							latestVersionMap.put(platformInArray,map.get(platform));
						}
					}
				}
				/*if(latestVersionMap.containsKey(platform)){
				latestVersionMap.put(platform, getLatestVersion(map.get(platform), latestVersionMap.get(platform)));
			}else {
				latestVersionMap.put(platform, map.get(platform));
			}*/
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return latestVersionMap;
	}

	/**
	 * 
	 * @param version1
	 * @param version2
	 * @return
	 */

	public static String getLatestVersion(String version1, String version2) {
		String higherVersion = null;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if(version1.matches(checkForNumberDot) && version2.matches(checkForNumberDot) ){

				//System.out.println(version1.split("\\.").length);
				if(version1.split("\\.").length >=1 && version2.split("\\.").length >=1 && Integer.parseInt(version1.split("\\.")[0]) > Integer.parseInt(version2.split("\\.")[0])){
					higherVersion = version1; 
				} else if (version1.split("\\.").length >=1 && version2.split("\\.").length >=1 && Integer.parseInt(version1.split("\\.")[0]) < Integer.parseInt(version2.split("\\.")[0])){
					higherVersion = version2;
				} else if (version1.split("\\.").length >=2 && version2.split("\\.").length >=2 && Integer.parseInt(version1.split("\\.")[1]) > Integer.parseInt(version2.split("\\.")[1])){
					higherVersion = version1;
				} else if (version1.split("\\.").length >=2 && version2.split("\\.").length >=2 && (Integer.parseInt(version1.split("\\.")[1]) < Integer.parseInt(version2.split("\\.")[1])) && version2.length() >=2 && version1.length() >=2){
					higherVersion = version2;
				} else if (version1.split("\\.").length >=3 && version2.split("\\.").length >=3 && (Integer.parseInt(version1.split("\\.")[2]) > Integer.parseInt(version2.split("\\.")[2])) ){
					higherVersion = version1;
				} else if (version1.split("\\.").length >=3 && version2.split("\\.").length >=3 && (Integer.parseInt(version1.split("\\.")[2]) < Integer.parseInt(version2.split("\\.")[2])) ){
					higherVersion = version2;
				} else{
					higherVersion = version1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return higherVersion;
	}

	/**
	 * 
	 * @param mapwithVersion
	 * @param mapwithCount
	 * @return
	 */

	public static Map<String,String> topTwoPlatformWithVersions(Map<String,String> mapwithVersion, Map<String,Integer> mapwithCount){

		Iterator<String> iterator = mapwithVersion.keySet().iterator();

		Map<String,String> topTwoPlatformMap = new HashMap<String,String>();
		try {
			while (iterator.hasNext() ) {
				String platform = iterator.next().toString();
				int counter = 0;
				Iterator<String> iterator2 = mapwithCount.keySet().iterator();
				while(iterator2.hasNext() &&  counter <2){
					String uniquePlatform = iterator2.next().toString();
					if(platform.equals(uniquePlatform)){
						topTwoPlatformMap.put(uniquePlatform,mapwithVersion.get(uniquePlatform));
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return topTwoPlatformMap;
	}

	/**
	 * 
	 * @param dataListService
	 * @param componentVersion
	 * @param indexesForEBusinessSuiteVersion
	 * @return
	 */
	public static String getHighestEbusinessSuiteVersion(List<String[]> dataListService,int componentVersion, ArrayList<Integer> indexesForEBusinessSuiteVersion) {
		int counter =1;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if(dataListService.size() != 0) {
				for(String[] dataLine : dataListService){
					// System.out.println("Counter -> "+counter);
					if(indexesForEBusinessSuiteVersion.contains(counter) && dataLine[componentVersion].matches(checkForNumberDot)) {
						highestVersion = getLatestVersion(highestVersion, dataLine[componentVersion]);			
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return highestVersion;
	}

	/**
	 * 
	 * @param indexOfDatabase
	 * @param componentVersion
	 * @param dataListService
	 * @return
	 */
	public static String getHighestDatabaseVersion(ArrayList<Integer> indexOfDatabase, int componentVersion, List<String[]> dataListService) {

		int counter =1;
		int highestVersion = 0;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		String version = null;
		//Map<String,Integer> databaseVersion = new HashMap<String,Integer>();
		try {
			if(dataListService.size() != 0) {
				for(String[] dataLine : dataListService) {
					if(indexOfDatabase.contains(counter) && dataLine[componentVersion].matches(checkForNumberDot)){
						if(highestVersion < Integer.parseInt(dataLine[componentVersion].split("\\.")[0])) {
							highestVersion = Integer.parseInt(dataLine[componentVersion].split("\\.")[0]);
						}
					}
					counter+=1;
				}
			}

			if(highestVersion >= 12 ){
				version = "12C";			
			}else if (highestVersion == 11){
				version = "11G";
			}else {
				version = "empty";
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return version;
	}

	/**
	 * 
	 * @param dataListService
	 * @param componentDescription
	 * @param keyword
	 * @return
	 */
	public static boolean checkRAC(List<String[]> dataListService,int componentDescription,String keyword) {
		boolean exist = false;
		try {
			if(dataListService.size() != 0) {
				for(String[] dataLine : dataListService) {
					if(dataLine[componentDescription].equals(keyword) && !exist){
						exist = true;
					}
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return exist;
	}


	/**
	 * 
	 * @param dataListService
	 * @param indexesForEBusinessSuiteVersion
	 * @param resolutionCode
	 * @return
	 */
	public static Boolean[] getPainAreas(List<String[]> dataListService, ArrayList<Integer> indexesForEBusinessSuiteVersion, int resolutionCode) {
		String[] performaneChallenge = {"Performance", "Capacity", "Data Issues", "Defect", "Customization"};
		String[] patchingUpgrade = {"Patch","Maintenance","Upgrade","Enhancement","End-User","Customization"};
		String[] resourceCost  = {"End-User","Knowledge","Setup","Admin","Documentation"};
		Boolean painArea[] = new Boolean[3];
		Arrays.fill(painArea, Boolean.FALSE);
		int counter =1;
		try {
			if(dataListService.size() != 0) {
				for(String[] dataLine : dataListService) {
					if(indexesForEBusinessSuiteVersion.contains(counter)){
						for(String performance: performaneChallenge){
							if(dataLine[resolutionCode].contains(performance)){
								painArea[0]=true;
							}
						}
						for(String patching: patchingUpgrade){
							if(dataLine[resolutionCode].contains(patching)){
								painArea[1]=true;
							}
						}
						for(String resource: resourceCost){
							if(dataLine[resolutionCode].contains(resource)){
								painArea[2]=true;
							}
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return painArea;
	}

	public static ArrayList<String> getUniqueValues(List<String[]> dataList, ArrayList<Integer>indexes,  int columnIndex){

		ArrayList<String> uniqueValues = new ArrayList<String>();
		int counter =1;
		try {
			if(dataList.size() != 0) {
				for(String[] dataLine : dataList) {
					if(indexes.contains(counter)){
						if(!uniqueValues.contains(dataLine[columnIndex])){
							uniqueValues.add(dataLine[columnIndex]);
						}
					}
					counter+=1;
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return uniqueValues;		
	}

	public static ArrayList<String> getClientDirectories(File dir) {
		ArrayList<String> clientDirectoryPaths = new ArrayList<String>();
		File[] files = null;
		try {
			if (dir.isDirectory()){
				files = dir.listFiles();
			}

			if(files.length != 0) {
				for (File file : files) {
					if (file.isDirectory()) {
						System.out.println("directory:" + file.getCanonicalPath()+"\\");
						clientDirectoryPaths.add(file.getCanonicalPath());
						//getClientDirectories(file);
					} else {
						System.out.println("     file:" + file.getCanonicalPath());
					}
				}
			}else {
				System.out.println("No Client directories in "+ dir.getCanonicalPath());
			}

		} catch (IOException e) {
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}
		return clientDirectoryPaths;
	}
	
	public static boolean isObjectInteger(Object o)
	{
	    return o instanceof Integer;
	}


}
