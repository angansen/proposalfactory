package org.oracle.ebs;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

//import org.imgscalr.Scalr;
import org.oracle.ebs.Utility;
import org.oracle.ebs.beans.Platform;
import org.oracle.ebs.comparator.PlatformCountComparator;

import com.google.gson.Gson;

public class Utility {

	public static String highestVersion = "0.0.0.0";

	static Platform[] recommendedPlatforms = { new Platform("linux", "Linux"),
			new Platform("windows", "Microsoft windows"), new Platform("solaris", "Oracle Solaris"),
			new Platform("hp", "HP-UX"), new Platform("ibm", "IBM AIX") };

	/**
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public static String[] getCSVHeader(String path, String fileName) throws Exception {

		String[] header = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

		try (BufferedReader br = new BufferedReader(new FileReader(Utility.mergeFiles(path, fileName)))) {
			int counter = 0;
			while ((line = br.readLine()) != null && !line.equals("")) {
				if (counter == 0) {

					header = line.split(cvsSplitBy);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < header.length; i++) {
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(header[i]);
					}
					// System.out.println("----------- headers -----------");
					// System.out.println(sb.toString());
				}
				counter += 1;
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getCSVHeader");
		} catch (IOException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in  getCSVHeader");
		}
		return header;
	}

	/**
	 * Read the
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String[]> readCSVData(String path, String fileName) throws Exception {

		String[] dataLine = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<String[]> dataList = new ArrayList<String[]>();

		if (path != null && fileName != null) {
			try (BufferedReader br = new BufferedReader(new FileReader(Utility.mergeFiles(path, fileName)))) {
				int counter = 0;
				while ((line = br.readLine()) != null) {
					if (counter != 0) {
						dataLine = line.split(cvsSplitBy);
						dataList.add(dataLine);
					}
					counter += 1;
				}

			} catch (FileNotFoundException e) {
				System.out.println("Error in " + new Object() {
				}.getClass() + "Class and Method " + new Object() {
				}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
				throw new Exception("Error in  readCSVData");
			} catch (IOException e) {
				System.out.println("Error in " + new Object() {
				}.getClass() + "Class and Method " + new Object() {
				}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
				throw new Exception("Error in  readCSVData");
			}
		}
		return dataList;
	}

	/**
	 * Merge all CSV files
	 * 
	 * @param directoryPath
	 *            - Path of the CSV files
	 * @return Merged file
	 * @throws Exception
	 */
	public static String mergeFiles(String directoryPath, String fileName) throws Exception {

		File[] filesInDirectory = new File(directoryPath).listFiles();
		List<Path> paths = new ArrayList<Path>();

		for (File f : filesInDirectory) {
			// System.out.println(f.getAbsolutePath());
			// if (f.getName().toLowerCase().endsWith(".jpg") ||
			// f.getName().toLowerCase().endsWith(".jpeg")
			// || f.getName().toLowerCase().endsWith(".png") ||
			// f.getName().toLowerCase().endsWith(".gif")) {
			//
			// try {
			// getScaledImage(f.getAbsolutePath().toString(), 75, 250);
			// } catch (Exception e) {
			// System.out.println("Image resizing failed. Terminating the process: " + e);
			// // throw new Exception("Error in mergeFiles");
			// }
			//
			// // resizeImage(f.getAbsolutePath().toString(), 200);
			//
			// }

			if (f.getName().contains(fileName) && !f.getName().contains("merged")) {

				String filePath = f.getAbsolutePath();
				String fileExtenstion = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());

				if ("csv".equals(fileExtenstion)) {
					// System.out.println("CSV file found -> " + filePath);
					paths.add(Paths.get(filePath));
				}

			}

		}

		List<String> mergedLines = getMergedLines(paths);
		Path target = Paths.get(directoryPath + fileName + "_merged.csv");
		File f = new File(directoryPath + fileName + "_merged.csv");
		if (!f.exists()) {
			Files.write(target, mergedLines, Charset.forName("UTF-8"));
		}
		return f.getAbsolutePath();
	}

	/**
	 * Get all the data rows of the merged CSV file
	 * 
	 * @param paths
	 *            - Path of the CSV files
	 * @return List of the data rows
	 * @throws Exception
	 * @throws IOException
	 */
	private static List<String> getMergedLines(List<Path> paths) throws Exception {
		List<String> mergedLines = new ArrayList<String>();
		if (paths.size() > 0) {
			for (Path p : paths) {
				List<String> lines;
				try {
					lines = Files.readAllLines(p, Charset.forName("UTF-8"));

					if (!lines.isEmpty()) {
						if (mergedLines.isEmpty()) {
							mergedLines.add(lines.get(0)); // add header only
															// once
						}
						mergedLines.addAll(lines.subList(1, lines.size()));
					}
				} catch (IOException e) {
					System.out.println("Error in " + new Object() {
					}.getClass() + "Class and Method " + new Object() {
					}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
					throw new Exception("Error in getMergedLines");
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

		if (my_array == null)
			return -1;
		int len = my_array.length;
		int i = 0;
		if (len > 0) {
			while (i < len) {
				if (my_array[i].equals(t))
					return i;
				else
					i = i + 1;
			}
		}

		return -1;
	}

	/**
	 * Find the list of indexes of the column that contains the keyword
	 * 
	 * @param dataList
	 *            - List of all the rows
	 * @param index
	 *            - index of the column to search
	 * @param keyword
	 *            - the keyword to look for in the selected column
	 * @return List of indexes that contains the keyword
	 * @throws Exception
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int productPillar, String keyword)
			throws Exception {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter = 1;
			if (dataList.size() != 0 && productPillar != -1) {
				for (String[] dataLine : dataList) {

					String valueToCheck = dataLine[productPillar];
					if (valueToCheck.indexOf(keyword) != -1) {
						productLevel.add(counter);
					} else {
						// System.out.println("Not matched -> "+ counter +
						// " "+ valueToCheck);
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());

			throw new Exception("Error in getFilteredIndexes");
		}

		return productLevel;
	}

	/**
	 * 
	 * @param dataList
	 * @param index
	 * @param keywords
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String[] keywords)
			throws Exception {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter = 1;
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {

					String valueToCheck = dataLine[index];
					for (String keyword : keywords) {
						if (valueToCheck.toLowerCase().indexOf(keyword.toLowerCase()) != -1) {
							productLevel.add(counter);
						} else {
							// System.out.println("Not matched -> "+ counter +
							// " "+ valueToCheck);
						}
					}

					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getFilteredIndexes");
		}

		return productLevel;
	}

	/**
	 * 
	 * @param dataList
	 *            - List of all the rows
	 * @param index
	 *            - index of the column to search
	 * @param keyword
	 *            - index of the column to search
	 * @param indexToSearch
	 *            - selected index from the first filter
	 * @return List of indexes that contains the keyword
	 * @throws Exception
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String[] keywords,
			ArrayList<Integer> indexToSearch) throws Exception {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		int counter = 1;
		try {
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch.contains(counter)) {
						String valueToCheck = dataLine[index];
						for (String keyword : keywords) {
							if (valueToCheck.indexOf(keyword) != -1) {
								productLevel.add(counter);
							}
						}

					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getFilteredIndexes");
		}

		return productLevel;
	}

	/**
	 * 
	 * @param dataList
	 * @param index
	 * @param keyword
	 * @param indexToSearch
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Integer> getFilteredIndexes(List<String[]> dataList, int index, String keyword,
			ArrayList<Integer> indexToSearch) throws Exception {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		int counter = 1;
		try {
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch.contains(counter)) {
						String valueToCheck = dataLine[index];
						if (valueToCheck.indexOf(keyword) != -1) {
							productLevel.add(counter);
						} else {
							// System.out.println("Not matched -> "+ counter +
							// " "+ valueToCheck);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getFilteredIndexes");
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
	 * @throws Exception
	 * 
	 */
	public static Map<String, Integer> findDistinctModuleAndPrice(List<String[]> dataList, int firstIndex,
			int secondIndex, ArrayList<Integer> indexToSearch) throws Exception {

		int counter = 1;
		Map<String, Integer> modulePricing = new HashMap<String, Integer>();
		try {
			if (dataList.size() != 0 && firstIndex != -1 && secondIndex != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null && indexToSearch.contains(counter) && !dataLine[secondIndex].equals("")) {

						if (modulePricing.containsKey(dataLine[firstIndex])) {
							modulePricing.put(dataLine[firstIndex],
									modulePricing.get(dataLine[firstIndex]) + Integer.parseInt(dataLine[secondIndex]));
						} else {
							modulePricing.put(dataLine[firstIndex], Integer.parseInt(dataLine[secondIndex]));
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage() + " Counter -> " + counter);
			throw new Exception("Error in findDistinctModuleAndPrice");
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
	 * @throws Exception
	 */
	public static Map<String, String> findDistinctPlatformAndVersions(List<String[]> dataList, int firstIndex,
			int secondIndex, ArrayList<Integer> indexToSearch) throws Exception {

		int counter = 1;
		Map<String, String> platformVersion = new HashMap<String, String>();
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if (dataList.size() != 0 && firstIndex != -1 && secondIndex != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null && indexToSearch.contains(counter)
							&& dataLine[secondIndex].matches(checkForNumberDot)) {
						if (platformVersion.containsKey(dataLine[firstIndex])
								&& platformVersion.get(dataLine[firstIndex]).equals(dataLine[secondIndex])) {

						} else {
							platformVersion.put(dataLine[firstIndex], dataLine[secondIndex]);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in findDistinctPlatformAndVersions");
		}
		return platformVersion;
	}

	/**
	 * 
	 * @param dataList
	 * @param index
	 * @param indexToSearch
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Integer> findPlatformCount(List<String[]> dataList, int index,
			ArrayList<Integer> indexToSearch) throws Exception {

		int counter = 1;
		Map<String, Integer> platformCount = new HashMap<String, Integer>();
		try {
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null && indexToSearch.contains(counter)) {
						if (platformCount.containsKey(dataLine[index])) {
							platformCount.put(dataLine[index], platformCount.get(dataLine[index]) + 1);
						} else {
							platformCount.put(dataLine[index], 1);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in findPlatformCount");
		}
		return platformCount;
	}

	/**
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Integer> getUniquePlatformsCount(Map<String, Integer> map) throws Exception {
		String[] platforms = { "LINUX", "Apple Mac OS X", "Microsoft Windows", "Microsoft Windows Itanium",
				"Fujitsu BS2000/OSD (SQ series)", "Fujitsu BS2000/OSD (SX series)", "Fujitsu BS2000",
				"IBM AIX on POWER Systems", "Fujitsu BS2000", "Oracle JRockit Virtual Edition", "Oracle Solaris",
				"Oracle Solaris on SPARC", "HP OpenVMS ItaniumHP OpenVMS VAX", "HP-UX PA-RISC", "HP Tru64 UNIX",
				"HP-UX Itanium" };
		Map<String, Integer> topTwoPlatforms = new HashMap<String, Integer>();
		try {

			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for (String platformInArray : platforms) {
					if (platform.toLowerCase().contains(platformInArray.toLowerCase())) {
						if (topTwoPlatforms.get(platformInArray) == null) {
							topTwoPlatforms.put(platformInArray, map.get(platform));
						} else {
							topTwoPlatforms.put(platformInArray,
									map.get(platform) + topTwoPlatforms.get(platformInArray));
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getUniquePlatformsCount");
		}
		return topTwoPlatforms;
	}

	/**
	 * Sorts the module in descending order of pricing value
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortModulesByValue(Map<K, V> map) throws Exception {
		Map<K, V> result = new LinkedHashMap<K, V>();
		try {
			List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});
			for (Map.Entry<K, V> entry : list) {
				result.put(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in sortModulesByValue");
		}
		return result;
	}

	/**
	 * 
	 * @param sortModulesByValue
	 * @return
	 */
	public static ArrayList<String> getTopTwoModulesByValue(Map<String, Integer> sortModulesByValue) {
		Iterator<String> iterator = sortModulesByValue.keySet().iterator();
		ArrayList<String> topTwoModulesByValue = new ArrayList<String>();
		while (iterator.hasNext() && topTwoModulesByValue.size() < 2) {
			String module = iterator.next().toString();
			topTwoModulesByValue.add(module);
		}
		return topTwoModulesByValue;

	}

	/**
	 * Prints the map
	 * 
	 * @param mapToPrint
	 * @throws Exception
	 */
	public static void printMap(Map<String, Integer> mapToPrint) throws Exception {
		try {
			Iterator<String> iterator = mapToPrint.keySet().iterator();

			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				Integer value = mapToPrint.get(key);
				// System.out.println(key + " " + value);
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in printMap");
		}
	}

	/**
	 * 
	 * @param mapToSort
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Integer> printTopModules(Map<String, Integer> mapToSort) throws Exception {

		Iterator<String> iterator = mapToSort.keySet().iterator();
		List<Integer> topValues = new ArrayList<>();
		Map<String, Integer> topModulesMap = new HashMap<String, Integer>();
		int counter = 0;
		try {
			while (iterator.hasNext()) {
				if (counter < 7) {
					String key = iterator.next().toString();
					Integer value = mapToSort.get(key);
					topValues.add(value);
				} else {
					break;
				}
				counter += 1;

			}

			Iterator<String> iterator2 = mapToSort.keySet().iterator();

			while (iterator2.hasNext()) {
				String key = iterator2.next().toString();
				if (topValues.contains(mapToSort.get(key)) && topModulesMap.size() < 15) {
					topModulesMap.put(key, mapToSort.get(key));

				}

			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in printTopModules");
		}
		return topModulesMap;
	}

	/**
	 * 
	 * @param map
	 */
	public static void convertToJSON(Map<String, Integer> map) {
		Gson gson = new Gson();
		String json = gson.toJson(map);
		System.out.println("JSON: " + json);
	}

	/**
	 * 
	 * @param modulePricingMap
	 * @param racIndex
	 * @return
	 * @throws Exception
	 */
	public static int selectSlide(Map<String, Integer> modulePricingMap, ArrayList<Integer> racIndex,
			int componentDescription, List<String[]> dataListService, List<String[]> dataListInstall,
			String[] headerInstall) throws Exception {
		int slideNumber = 0;
		// System.out.println("modulePricingMap --> " + modulePricingMap);
		for (int racInd : racIndex) {
			// System.out.println("racInd ---. " + racInd);
		}
		for (String headerIns : headerInstall) {
			// System.out.println("racInd ---. " + headerIns);
		}
		// System.out.println("modulePricingMap --> " + modulePricingMap);
		// System.out.println(dataListInstall.toString());
		// System.out.println("componentDescription ---> " + componentDescription);

		try {
			if (Utility.searchInstallBaseForRAC(dataListInstall, headerInstall, "Real Application Cluster")
					|| Utility.checkRAC(dataListService, componentDescription, "Real Application Cluster")) {
				// if (checkRAC(dataListService, componentDescription,
				// "Real Application Cluster") ){
				/*
				 * if (checkRAC(dataListService, componentDescription,
				 * "Real Application Cluster")){ slideNumber = 7; } System.out.println
				 * ("@@@@@@@@@@@@@@@ RAC EXISTS @@@@@@@@@@@@@@");
				 */
				slideNumber = 7;
			} else if (sortModulesByValue(modulePricingMap).size() > 0
					&& sortModulesByValue(modulePricingMap).size() <= 15 && racIndex.size() == 0) {
				slideNumber = 5;
			} else if (sortModulesByValue(modulePricingMap).size() > 15
					&& sortModulesByValue(modulePricingMap).size() <= 20 && racIndex.size() == 0) {
				slideNumber = 6;
			} else if (sortModulesByValue(modulePricingMap).size() > 20 && racIndex.size() == 0) {
				slideNumber = 7;
			}
			// }

		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			e.printStackTrace();
			throw new Exception("Error in selectSlide");
		}

		return slideNumber;
	}

	/**
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getLatestVersionMap(Map<String, String> map) throws Exception {

		String[] platforms = { "LINUX", "Apple Mac OS X", "Microsoft Windows", "Microsoft Windows Itanium",
				"Fujitsu BS2000/OSD (SQ series)", "Fujitsu BS2000/OSD (SX series)", "Fujitsu BS2000",
				"IBM AIX on POWER Systems", "Fujitsu BS2000", "Oracle JRockit Virtual Edition", "Oracle Solaris",
				"Oracle Solaris on SPARC", "HP OpenVMS ItaniumHP OpenVMS VAX", "HP-UX PA-RISC", "HP Tru64 UNIX",
				"HP-UX Itanium" };

		Map<String, String> latestVersionMap = new HashMap<String, String>();
		try {
			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for (String platformInArray : platforms) {
					if (platform.toLowerCase().contains(platformInArray.toLowerCase())) {
						if (latestVersionMap.get(platformInArray) != null) {
							latestVersionMap.put(platformInArray,
									getLatestVersion(map.get(platform), latestVersionMap.get(platformInArray)));
						} else {
							latestVersionMap.put(platformInArray, map.get(platform));
						}
					}
				}
				/*
				 * if(latestVersionMap.containsKey(platform)){ latestVersionMap.put(platform,
				 * getLatestVersion(map.get(platform), latestVersionMap.get(platform))); }else {
				 * latestVersionMap.put(platform, map.get(platform)); }
				 */
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getLatestVersionMap");
		}
		return latestVersionMap;
	}

	public static List<Platform> getTopTwoPlatforms(Map<String, Integer> map) {

		List<Platform> filteredList = new ArrayList<>();

		map = normalizePlatformCalculation(map);

		for (String platform : map.keySet()) {

			for (Platform rplatform : recommendedPlatforms) {
				if (platform.toLowerCase().contains(rplatform.getName().toLowerCase())) {
					if (map.containsKey(platform)) {

						Platform form = new Platform(rplatform.getName(), rplatform.getDisplayName(),
								map.get(platform));
						filteredList.add(form);
					} else {
						Platform form = new Platform(rplatform.getName(), rplatform.getDisplayName(),
								map.get(platform));
						filteredList.add(form);
					}
				}
			}
		}

		filteredList.sort(new PlatformCountComparator());

		return filteredList;
	}

	public static Map<String, Integer> normalizePlatformCalculation(Map<String, Integer> map) {

		Map<String, Integer> reccommendedPlatformMap = new HashMap<>();

		for (String keyPlatform : map.keySet()) {
			for (Platform recPlatformKey : recommendedPlatforms) {
				if (keyPlatform.toLowerCase().contains(recPlatformKey.getName())) {

					if (reccommendedPlatformMap.containsKey(recPlatformKey.getName())) {

						int platformCount = reccommendedPlatformMap.get(recPlatformKey.getName());
						platformCount = platformCount + map.get(keyPlatform);
						reccommendedPlatformMap.put(recPlatformKey.getName(), platformCount);
					} else {
						reccommendedPlatformMap.put(recPlatformKey.getName(), map.get(keyPlatform));
					}
				}
			}

		}

		return reccommendedPlatformMap;
	}

	/**
	 * Calculates the similarity (a number within 0 and 1) between two strings.
	 */
	public static double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) { // longer should always have greater length
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0;
			/* both strings are zero length */ }
		/*
		 * // If you have Apache Commons Text, you can use it to calculate the edit
		 * distance: LevenshteinDistance levenshteinDistance = new
		 * LevenshteinDistance(); return (longerLength -
		 * levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
		 */
		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

	}

	// Example implementation of the Levenshtein Edit Distance
	// See http://rosettacode.org/wiki/Levenshtein_distance#Java
	public static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	/**
	 * 
	 * @param version1
	 * @param version2
	 * @return
	 * @throws Exception
	 */
	public static String getLatestVersion(String version1, String version2) throws Exception {
		String higherVersion = null;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if (version1.matches(checkForNumberDot) && version2.matches(checkForNumberDot)) {

				higherVersion = (versionCompare(version1, version2) < 1) ? version2 : version1;
				if (versionCompare(version1, version2) == 0) {
					higherVersion = version2;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in printTopModules");
		}
		return higherVersion;
	}

	public static String getLatestVersion2(String version1, String version2) throws Exception {
		String higherVersion = version1;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";

		// Normallize version to 4 digit
		String normversion1 = normalizeVersion(version1, 4);
		String[] ver1Split = normversion1.split("\\.");

		String normversion2 = normalizeVersion(version2, 4);
		String[] ver2Split = normversion2.split("\\.");

		try {

			for (int i = 0; i < ver1Split.length; i++) {
				if (Integer.valueOf(ver1Split[i]) < Integer.valueOf(ver2Split[i])) {
					higherVersion = version2;
					break;
				} else if (Integer.valueOf(ver2Split[i]) < Integer.valueOf(ver1Split[i])) {
					higherVersion = version1;
					break;
				}
			}

		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in printTopModules");
		}
		return higherVersion;
	}

	private static String normalizeVersion(String version, int digitcount) {

		String[] digits = version.split("\\.");
		String newVersion = "";

		for (int i = 0; i < digitcount; i++) {
			if (i < digits.length) {
				newVersion += digits[i] + ".";
			} else {
				newVersion += "0.";
			}
		}

		newVersion = newVersion.charAt(newVersion.length() - 1) == '.'
				? newVersion.substring(0, newVersion.length() - 1)
				: newVersion;
		return newVersion;
	}

	/**
	 * Compares two version strings.
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical comparison
	 * that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1
	 *            a string of ordinal numbers separated by decimal points.
	 * @param str2
	 *            a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than
	 *         str2. The result is a positive integer if str1 is _numerically_
	 *         greater than str2. The result is zero if the strings are
	 *         _numerically_ equal.
	 * @throws Exception
	 */
	public static Integer versionCompare(String str1, String str2) throws Exception {
		try {
			String[] vals1 = str1.split("\\.");
			String[] vals2 = str2.split("\\.");
			int i = 0;
			// set index to first non-equal ordinal or length of shortest
			// version
			// string
			while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
				i++;
			}
			// compare first non-equal ordinal number
			if (i < vals1.length && i < vals2.length) {
				int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
				return Integer.signum(diff);
			}
			// the strings are equal or one string is a substring of the other
			// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
			else {
				return Integer.signum(vals1.length - vals2.length);
			}
		} catch (NumberFormatException e) {
			throw new Exception("Error in versionCompare");
		}
	}

	/**
	 * 
	 * @param mapwithVersion
	 * @param mapwithCount
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> topTwoPlatformWithVersions(Map<String, String> mapwithVersion,
			Map<String, Integer> mapwithCount) throws Exception {

		Iterator<String> iterator = mapwithVersion.keySet().iterator();
		Map<String, Integer> sortedMap = sortModulesByValue(mapwithCount);
		Utility.printMap(sortedMap);
		Map<String, String> topTwoPlatformMap = new HashMap<String, String>();
		try {
			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				int counter = 0;
				Iterator<String> iterator2 = sortedMap.keySet().iterator();
				// while(iterator2.hasNext() && topTwoPlatformMap.size() <2){
				while (iterator2.hasNext() && counter < 2) {
					String uniquePlatform = iterator2.next().toString();
					if (platform.equals(uniquePlatform) && mapwithVersion.get(uniquePlatform) != null) {
						topTwoPlatformMap.put(uniquePlatform, mapwithVersion.get(uniquePlatform));
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in topTwoPlatformWithVersions");
		}
		return topTwoPlatformMap;
	}

	/**
	 * 
	 * @param dataListService
	 * @param componentVersion
	 * @param indexesForEBusinessSuiteVersion
	 * @return
	 * @throws Exception
	 */
	public static String getHighestEbusinessSuiteVersion(List<String[]> dataListService, int componentVersion,
			ArrayList<Integer> indexesForEBusinessSuiteVersion) throws Exception {
		int counter = 1;
		highestVersion="0.0.0.0";
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					// System.out.println("Counter -> "+counter);
					if (indexesForEBusinessSuiteVersion.contains(counter)
							&& dataLine[componentVersion].matches(checkForNumberDot)
							&& Integer.parseInt(dataLine[componentVersion].split("\\.")[0]) < 13) {
						// System.out.println("dataLine[componentVersion] -->
						// "+dataLine[componentVersion]);
						highestVersion = getLatestVersion2(highestVersion, dataLine[componentVersion]);
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getHighestEbusinessSuiteVersion");
		}
		return highestVersion;
	}

	/**
	 * 
	 * @param indexOfDatabase
	 * @param componentVersion
	 * @param dataListService
	 * @return
	 * @throws Exception
	 */
	public static String getHighestDatabaseVersion(ArrayList<Integer> indexOfDatabase, int componentVersion,
			List<String[]> dataListService) throws Exception {

		int counter = 1;
		int highestVersion = 0;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		String version = null;
		// Map<String,Integer> databaseVersion = new HashMap<String,Integer>();
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					if (indexOfDatabase.contains(counter) && dataLine[componentVersion].matches(checkForNumberDot)) {
						if (highestVersion < Integer.parseInt(dataLine[componentVersion].split("\\.")[0])) {
							highestVersion = Integer.parseInt(dataLine[componentVersion].split("\\.")[0]);
						}
					}
					counter += 1;
				}
			}

			if (highestVersion >= 12) {
				version = "12C";
			} else if (highestVersion == 11) {
				version = "11G";
			} else {
				version = "";
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getHighestDatabaseVersion");
		}
		return version;
	}

	/**
	 * 
	 * @param dataListService
	 * @param componentDescription
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public static boolean checkRAC(List<String[]> dataListService, int componentDescription, String keyword)
			throws Exception {
		boolean exist = false;
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					if (dataLine.length > componentDescription && dataLine[componentDescription].contains(keyword)
							&& !exist) {
						exist = true;
						if (exist) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			e.printStackTrace();
			throw new Exception("Error in checkRAC");
		}
		return exist;
	}

	/**
	 * 
	 * @param dataListService
	 * @param indexesForEBusinessSuiteVersion
	 * @param resolutionCode
	 * @return
	 * @throws Exception
	 */
	public static Boolean[] getPainAreas(List<String[]> dataListService,
			ArrayList<Integer> indexesForEBusinessSuiteVersion, int resolutionCode) throws Exception {
		String[] performaneChallenge = { "Performance", "Capacity", "Data Issues", "Defect", "Customization" };
		String[] patchingUpgrade = { "Patch", "Maintenance", "Upgrade", "Enhancement", "End-User", "Customization" };
		String[] resourceCost = { "End-User", "Knowledge", "Setup", "Admin", "Documentation" };
		Boolean painArea[] = new Boolean[3];
		Arrays.fill(painArea, Boolean.FALSE);
		int counter = 1;
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					if (indexesForEBusinessSuiteVersion.contains(counter)) {
						for (String performance : performaneChallenge) {
							if (dataLine[resolutionCode].contains(performance)) {
								painArea[0] = true;
							}
						}
						for (String patching : patchingUpgrade) {
							if (dataLine[resolutionCode].contains(patching)) {
								painArea[1] = true;
							}
						}
						for (String resource : resourceCost) {
							if (dataLine[resolutionCode].contains(resource)) {
								painArea[2] = true;
							}
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getPainAreas");
		}
		return painArea;
	}

	/**
	 * 
	 * @param dataList
	 * @param indexes
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getUniqueValues(List<String[]> dataList, ArrayList<Integer> indexes,
			int columnIndex) throws Exception {

		ArrayList<String> uniqueValues = new ArrayList<String>();
		int counter = 1;
		try {
			if (dataList.size() != 0) {
				for (String[] dataLine : dataList) {
					if (indexes.contains(counter)) {
						if (!uniqueValues.contains(dataLine[columnIndex])) {
							uniqueValues.add(dataLine[columnIndex]);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getUniqueValues");
		}
		return uniqueValues;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getClientDirectories(File dir) throws Exception {
		ArrayList<String> clientDirectoryPaths = new ArrayList<String>();
		File[] files = null;
		try {
			if (dir.isDirectory()) {
				files = dir.listFiles();
			}

			if (files.length != 0) {
				for (File file : files) {
					if (file.isDirectory()) {
						// System.out.println("directory:" + file.getCanonicalPath() + "\\");
						clientDirectoryPaths.add(file.getCanonicalPath());
						// getClientDirectories(file);
					} else {
						// System.out.println(" file:" + file.getCanonicalPath());
					}
				}
			} else {
				// System.out.println("No Client directories in " + dir.getCanonicalPath());
			}

		} catch (IOException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			throw new Exception("Error in getClientDirectories");
		}
		return clientDirectoryPaths;
	}

	/**
	 * 
	 * @param dataListInstall
	 * @param headerInstall
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public static boolean searchInstallBaseForRAC(List<String[]> dataListInstall, String[] headerInstall,
			String keyword) throws Exception {

		boolean exist = false;
		try {
			if (dataListInstall.size() != 0 && !exist) {

				for (String header : headerInstall) {
					exist = checkRAC(dataListInstall, findIndex(headerInstall, header), "Real Application Cluster");
					if (exist) {
						break;
					}
				}

			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			e.printStackTrace();
			throw new Exception("Error in searchInstallBaseForRAC");
		}
		return exist;
	}

	/**
	 * 
	 * @param sourceFile
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static String resizeImage(String sourceFile, int width) throws Exception {
		try {
			String ResizedFile = sourceFile;
			File f = new File(sourceFile);
			if (!f.exists()) {
				return "Source Image File not Found";
			}

			// Logic to implement image resizing
			BufferedImage bim = ImageIO.read(new FileInputStream(sourceFile));
			Image resizedImg = bim.getScaledInstance(width, -1, Image.SCALE_AREA_AVERAGING);

			int scaled_height = resizedImg.getHeight(null);

			BufferedImage rBimg = new BufferedImage(width, scaled_height, bim.getType());
			// Create Graphics object
			Graphics2D g = rBimg.createGraphics();// Draw the resizedImg from
													// 0,0 with no ImageObserver
			g.drawImage(resizedImg, 0, 0, null);

			// Dispose the Graphics object, we no longer need it
			g.dispose();

			ImageIO.write(rBimg, ResizedFile.substring(ResizedFile.indexOf(".") + 1),
					new FileOutputStream(ResizedFile));
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Picture Resized Successfully";
	}

	/**
	 * 
	 * @param filepath
	 * @param heightLimit
	 * @param widthLimit
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void getScaledImage(String filepath, int heightLimit, int widthLimit)
			throws FileNotFoundException, IOException {

		int derivedHeight = 0;
		int derivedWidth = 0;
		int imageHeight = 0;
		int imageWidth = 0;

		// Open and load the Image
		File f = new File(filepath);
		try {
			if (!f.exists()) {
				throw new Exception("Source Image File not Found");
			}
		} catch (Exception e) {
			System.out.println(e);
			return;
		}

		// Load Image and retrive dimension.
		BufferedImage bim = ImageIO.read(new FileInputStream(filepath));

		if (bim != null) {
			// Get Image dimension
			imageHeight = bim.getHeight();
			imageWidth = bim.getWidth();

			// Calculate respective dimension if the best fit image
			if (imageHeight > heightLimit) {
				derivedHeight = heightLimit;
				derivedWidth = (imageWidth * derivedHeight) / imageHeight;
			} else if (imageWidth > widthLimit) {
				derivedWidth = widthLimit;
				derivedHeight = (imageHeight * derivedWidth) / imageWidth;
			} else {
				derivedHeight = imageHeight;
				derivedWidth = imageWidth;
			}

			// Calculate required Image dimension

			Image resizedImg = bim.getScaledInstance(derivedWidth, derivedHeight, Image.SCALE_AREA_AVERAGING);

			BufferedImage rBimg = new BufferedImage(derivedWidth, derivedHeight, bim.getType());
			// Create Graphics object
			Graphics2D g = rBimg.createGraphics();// Draw the resizedImg from
													// 0,0 with no ImageObserver
			g.drawImage(resizedImg, 0, 0, null);

			// Dispose the Graphics object, we no longer need it
			g.dispose();

			String format = filepath.substring(filepath.lastIndexOf(".") + 1);
			ImageIO.write(rBimg, format.toLowerCase(), new FileOutputStream(filepath));
//			System.out.println("Successfully corrected image dimention : " + derivedHeight + " X " + derivedWidth);
		}
	}

	public static boolean isObjectInteger(Object o) {
		return o instanceof Integer;
	}

	public static void getHighestEBSVersion(List<String[]> dataListService, String[] ebusinessModules) {
		// TODO Auto-generated method stub

	}

}
