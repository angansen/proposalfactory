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

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.oracle.ebs.Utility;

import com.google.gson.Gson;

public class Utility {

	public static String highestVersion = "0.0.0.0";

	/**
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public static String[] getCSVHeader(String path, String fileName)
			throws Exception {

		String[] header = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

		try (BufferedReader br = new BufferedReader(new FileReader(
				Utility.mergeFiles(path, fileName)))) {
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
					System.out.println("-----------  headers -----------");
					System.out.println(sb.toString());
				}
				counter += 1;
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static List<String[]> readCSVData(String path, String fileName)
			throws Exception {

		String[] dataLine = {};
		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<String[]> dataList = new ArrayList<String[]>();

		if (path != null && fileName != null) {
			try (BufferedReader br = new BufferedReader(new FileReader(
					Utility.mergeFiles(path, fileName)))) {
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
				}.getClass().getEnclosingMethod().getName() + " "
						+ e.getMessage());
			} catch (IOException e) {
				System.out.println("Error in " + new Object() {
				}.getClass() + "Class and Method " + new Object() {
				}.getClass().getEnclosingMethod().getName() + " "
						+ e.getMessage());
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
	public static String mergeFiles(String directoryPath, String fileName)
			throws Exception {

		File[] filesInDirectory = new File(directoryPath).listFiles();
		List<Path> paths = new ArrayList<Path>();

		for (File f : filesInDirectory) {
			System.out.println(f.getAbsolutePath());
			if (f.getName().toLowerCase().endsWith(".jpg")
					|| f.getName().toLowerCase().endsWith(".jpeg")
					|| f.getName().toLowerCase().endsWith(".png")
					|| f.getName().toLowerCase().endsWith(".gif")) {
				
				getScaledImage(f.getAbsolutePath().toString(), 75,250);
				
//				resizeImage(f.getAbsolutePath().toString(), 200);

			}

			if (f.getName().contains(fileName)
					&& !f.getName().contains("merged")) {

				String filePath = f.getAbsolutePath();
				String fileExtenstion = filePath.substring(
						filePath.lastIndexOf(".") + 1, filePath.length());

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
		} else {
			System.out.println("File already exists");
		}
		return f.getAbsolutePath();
	}

	/**
	 * Get all the data rows of the merged CSV file
	 * 
	 * @param paths
	 *            - Path of the CSV files
	 * @return List of the data rows
	 * @throws IOException
	 */
	private static List<String> getMergedLines(List<Path> paths) {
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
					}.getClass().getEnclosingMethod().getName() + " "
							+ e.getMessage());
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
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(
			List<String[]> dataList, int productPillar, String keyword) {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter = 1;
			if (dataList.size() != 0 && productPillar != -1) {
				for (String[] dataLine : dataList) {

					String valueToCheck = dataLine[productPillar];
					if (valueToCheck.indexOf(keyword) != -1) {
						productLevel.add(counter);
					} else {
						// System.out.println("Not matched  -> "+ counter +
						// "  "+ valueToCheck);
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static ArrayList<Integer> getFilteredIndexes(
			List<String[]> dataList, int index, String[] keywords) {
		ArrayList<Integer> productLevel = new ArrayList<Integer>();
		try {
			int counter = 1;
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {

					String valueToCheck = dataLine[index];
					for (String keyword : keywords) {
						if (valueToCheck.toLowerCase().indexOf(
								keyword.toLowerCase()) != -1) {
							productLevel.add(counter);
						} else {
							// System.out.println("Not matched  -> "+ counter +
							// "  "+ valueToCheck);
						}
					}

					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	 * 
	 */
	public static ArrayList<Integer> getFilteredIndexes(
			List<String[]> dataList, int index, String[] keywords,
			ArrayList<Integer> indexToSearch) {
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
							} else {
								// System.out.println("Not matched  -> "+
								// counter + "  "+ valueToCheck);
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
	 */
	public static ArrayList<Integer> getFilteredIndexes(
			List<String[]> dataList, int index, String keyword,
			ArrayList<Integer> indexToSearch) {
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
							// System.out.println("Not matched  -> "+ counter +
							// "  "+ valueToCheck);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static Map<String, Integer> findDistinctModuleAndPrice(
			List<String[]> dataList, int firstIndex, int secondIndex,
			ArrayList<Integer> indexToSearch) {

		int counter = 1;
		Map<String, Integer> modulePricing = new HashMap<String, Integer>();
		try {
			if (dataList.size() != 0 && firstIndex != -1 && secondIndex != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null
							&& indexToSearch.contains(counter)
							&& !dataLine[secondIndex].equals("")) {

						if (modulePricing.containsKey(dataLine[firstIndex])) {
							modulePricing
									.put(dataLine[firstIndex],
											modulePricing
													.get(dataLine[firstIndex])
													+ Integer
															.parseInt(dataLine[secondIndex]));
						} else {
							modulePricing.put(dataLine[firstIndex],
									Integer.parseInt(dataLine[secondIndex]));
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage()
					+ " Counter -> " + counter);
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
	public static Map<String, String> findDistinctPlatformAndVersions(
			List<String[]> dataList, int firstIndex, int secondIndex,
			ArrayList<Integer> indexToSearch) {

		int counter = 1;
		Map<String, String> platformVersion = new HashMap<String, String>();
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if (dataList.size() != 0 && firstIndex != -1 && secondIndex != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null
							&& indexToSearch.contains(counter)
							&& dataLine[secondIndex].matches(checkForNumberDot)) {
						if (platformVersion.containsKey(dataLine[firstIndex])
								&& platformVersion.get(dataLine[firstIndex])
										.equals(dataLine[secondIndex])) {

						} else {
							platformVersion.put(dataLine[firstIndex],
									dataLine[secondIndex]);
						}
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static Map<String, Integer> findPlatformCount(
			List<String[]> dataList, int index, ArrayList<Integer> indexToSearch) {

		int counter = 1;
		Map<String, Integer> platformCount = new HashMap<String, Integer>();
		try {
			if (dataList.size() != 0 && index != -1) {
				for (String[] dataLine : dataList) {
					if (indexToSearch != null
							&& indexToSearch.contains(counter)) {
						if (platformCount.containsKey(dataLine[index])) {
							platformCount.put(dataLine[index],
									platformCount.get(dataLine[index]) + 1);
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
		}
		return platformCount;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, Integer> getUniquePlatformsCount(
			Map<String, Integer> map) {
		String[] platforms = { "LINUX", "Apple Mac OS X", "Microsoft Windows",
				"Microsoft Windows Itanium", "Fujitsu BS2000/OSD (SQ series)",
				"Fujitsu BS2000/OSD (SX series)", "Fujitsu BS2000",
				"IBM AIX on POWER Systems", "Fujitsu BS2000",
				"Oracle JRockit Virtual Edition", "Oracle Solaris",
				"Oracle Solaris on SPARC", "HP OpenVMS ItaniumHP OpenVMS VAX",
				"HP-UX PA-RISC", "HP Tru64 UNIX", "HP-UX Itanium" };
		Map<String, Integer> topTwoPlatforms = new HashMap<String, Integer>();
		try {

			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for (String platformInArray : platforms) {
					if (platform.toLowerCase().contains(
							platformInArray.toLowerCase())) {
						if (topTwoPlatforms.get(platformInArray) == null) {
							topTwoPlatforms.put(platformInArray,
									map.get(platform));
						} else {
							topTwoPlatforms.put(
									platformInArray,
									map.get(platform)
											+ topTwoPlatforms
													.get(platformInArray));
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
		}
		return topTwoPlatforms;
	}

	/**
	 * Sorts the module in descending order of pricing value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortModulesByValue(
			Map<K, V> map) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		try {
			List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
					map.entrySet());
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
		}
		return result;
	}

	/**
	 * 
	 * @param sortModulesByValue
	 * @return
	 */
	public static ArrayList<String> getTopTwoModulesByValue(
			Map<String, Integer> sortModulesByValue) {
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
	 */
	public static void printMap(Map<String, Integer> mapToPrint) {
		try {
			Iterator<String> iterator = mapToPrint.keySet().iterator();

			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				Integer value = mapToPrint.get(key);
				System.out.println(key + " " + value);
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
		}
	}

	/**
	 * 
	 * @param mapToSort
	 * @return
	 */
	public static Map<String, Integer> printTopModules(
			Map<String, Integer> mapToSort) {

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
				if (topValues.contains(mapToSort.get(key))
						&& topModulesMap.size() < 15) {
					topModulesMap.put(key, mapToSort.get(key));

				}

			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	 */
	public static int selectSlide(Map<String, Integer> modulePricingMap,
			ArrayList<Integer> racIndex, int componentDescription,
			List<String[]> dataListService, List<String[]> dataListInstall,
			String[] headerInstall) {
		int slideNumber = 0;
		try {
			if (Utility.searchInstallBaseForRAC(dataListInstall, headerInstall,
					"Real Application Cluster")
					|| Utility.checkRAC(dataListService, componentDescription,
							"Real Application Cluster")) {
				// if (checkRAC(dataListService, componentDescription,
				// "Real Application Cluster") ){
				/*
				 * if (checkRAC(dataListService, componentDescription,
				 * "Real Application Cluster")){ slideNumber = 7; }
				 * System.out.println
				 * ("@@@@@@@@@@@@@@@ RAC EXISTS @@@@@@@@@@@@@@");
				 */
				slideNumber = 7;
			} else if (sortModulesByValue(modulePricingMap).size() > 0
					&& sortModulesByValue(modulePricingMap).size() <= 15
					&& racIndex.size() == 0) {
				slideNumber = 5;
			} else if (sortModulesByValue(modulePricingMap).size() > 15
					&& sortModulesByValue(modulePricingMap).size() <= 20
					&& racIndex.size() == 0) {
				slideNumber = 6;
			} else if (sortModulesByValue(modulePricingMap).size() > 20
					&& racIndex.size() == 0) {
				slideNumber = 7;
			}
			// }

		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
			e.printStackTrace();
		}
		return slideNumber;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, String> getLatestVersionMap(
			Map<String, String> map) {

		String[] platforms = { "LINUX", "Apple Mac OS X", "Microsoft Windows",
				"Microsoft Windows Itanium", "Fujitsu BS2000/OSD (SQ series)",
				"Fujitsu BS2000/OSD (SX series)", "Fujitsu BS2000",
				"IBM AIX on POWER Systems", "Fujitsu BS2000",
				"Oracle JRockit Virtual Edition", "Oracle Solaris",
				"Oracle Solaris on SPARC", "HP OpenVMS ItaniumHP OpenVMS VAX",
				"HP-UX PA-RISC", "HP Tru64 UNIX", "HP-UX Itanium" };

		Map<String, String> latestVersionMap = new HashMap<String, String>();
		try {
			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String platform = iterator.next().toString();
				for (String platformInArray : platforms) {
					if (platform.toLowerCase().contains(
							platformInArray.toLowerCase())) {
						if (latestVersionMap.get(platformInArray) != null) {
							latestVersionMap.put(
									platformInArray,
									getLatestVersion(map.get(platform),
											latestVersionMap
													.get(platformInArray)));
						} else {
							latestVersionMap.put(platformInArray,
									map.get(platform));
						}
					}
				}
				/*
				 * if(latestVersionMap.containsKey(platform)){
				 * latestVersionMap.put(platform,
				 * getLatestVersion(map.get(platform),
				 * latestVersionMap.get(platform))); }else {
				 * latestVersionMap.put(platform, map.get(platform)); }
				 */
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
			if (version1.matches(checkForNumberDot)
					&& version2.matches(checkForNumberDot)) {

				higherVersion = (versionCompare(version1, version2) < 1) ? version2
						: version1;
				if (versionCompare(version1, version2) == 0) {
					higherVersion = version2;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
		}
		return higherVersion;
	}

	/**
	 * Compares two version strings.
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1
	 *            a string of ordinal numbers separated by decimal points.
	 * @param str2
	 *            a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less
	 *         than str2. The result is a positive integer if str1 is
	 *         _numerically_ greater than str2. The result is zero if the
	 *         strings are _numerically_ equal.
	 */
	public static Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version
		// string
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(
					Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	/**
	 * 
	 * @param mapwithVersion
	 * @param mapwithCount
	 * @return
	 */
	public static Map<String, String> topTwoPlatformWithVersions(
			Map<String, String> mapwithVersion,
			Map<String, Integer> mapwithCount) {

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
					if (platform.equals(uniquePlatform)
							&& mapwithVersion.get(uniquePlatform) != null) {
						topTwoPlatformMap.put(uniquePlatform,
								mapwithVersion.get(uniquePlatform));
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static String getHighestEbusinessSuiteVersion(
			List<String[]> dataListService, int componentVersion,
			ArrayList<Integer> indexesForEBusinessSuiteVersion) {
		int counter = 1;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					// System.out.println("Counter -> "+counter);
					if (indexesForEBusinessSuiteVersion.contains(counter)
							&& dataLine[componentVersion]
									.matches(checkForNumberDot)
							&& Integer.parseInt(dataLine[componentVersion]
									.split("\\.")[0]) < 13) {
						// System.out.println("dataLine[componentVersion]  --> "+dataLine[componentVersion]);
						highestVersion = getLatestVersion(highestVersion,
								dataLine[componentVersion]);
					}
					counter += 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
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
	public static String getHighestDatabaseVersion(
			ArrayList<Integer> indexOfDatabase, int componentVersion,
			List<String[]> dataListService) {

		int counter = 1;
		int highestVersion = 0;
		String checkForNumberDot = "^\\d+(\\.\\d+)*$";
		String version = null;
		// Map<String,Integer> databaseVersion = new HashMap<String,Integer>();
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					if (indexOfDatabase.contains(counter)
							&& dataLine[componentVersion]
									.matches(checkForNumberDot)) {
						if (highestVersion < Integer
								.parseInt(dataLine[componentVersion]
										.split("\\.")[0])) {
							highestVersion = Integer
									.parseInt(dataLine[componentVersion]
											.split("\\.")[0]);
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
	public static boolean checkRAC(List<String[]> dataListService,
			int componentDescription, String keyword) {
		boolean exist = false;
		try {
			if (dataListService.size() != 0) {
				for (String[] dataLine : dataListService) {
					if (dataLine[componentDescription].contains(keyword)
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
	public static Boolean[] getPainAreas(List<String[]> dataListService,
			ArrayList<Integer> indexesForEBusinessSuiteVersion,
			int resolutionCode) {
		String[] performaneChallenge = { "Performance", "Capacity",
				"Data Issues", "Defect", "Customization" };
		String[] patchingUpgrade = { "Patch", "Maintenance", "Upgrade",
				"Enhancement", "End-User", "Customization" };
		String[] resourceCost = { "End-User", "Knowledge", "Setup", "Admin",
				"Documentation" };
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
		}
		return painArea;
	}

	/**
	 * 
	 * @param dataList
	 * @param indexes
	 * @param columnIndex
	 * @return
	 */
	public static ArrayList<String> getUniqueValues(List<String[]> dataList,
			ArrayList<Integer> indexes, int columnIndex) {

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
		}
		return uniqueValues;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
	public static ArrayList<String> getClientDirectories(File dir) {
		ArrayList<String> clientDirectoryPaths = new ArrayList<String>();
		File[] files = null;
		try {
			if (dir.isDirectory()) {
				files = dir.listFiles();
			}

			if (files.length != 0) {
				for (File file : files) {
					if (file.isDirectory()) {
						System.out.println("directory:"
								+ file.getCanonicalPath() + "\\");
						clientDirectoryPaths.add(file.getCanonicalPath());
						// getClientDirectories(file);
					} else {
						System.out.println("     file:"
								+ file.getCanonicalPath());
					}
				}
			} else {
				System.out.println("No Client directories in "
						+ dir.getCanonicalPath());
			}

		} catch (IOException e) {
			System.out.println("Error in " + new Object() {
			}.getClass() + "Class and Method " + new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
		}
		return clientDirectoryPaths;
	}

	/**
	 * 
	 * @param dataListInstall
	 * @param headerInstall
	 * @param keyword
	 * @return
	 */
	public static boolean searchInstallBaseForRAC(
			List<String[]> dataListInstall, String[] headerInstall,
			String keyword) {

		boolean exist = false;
		try {
			if (dataListInstall.size() != 0 && !exist) {

				for (String header : headerInstall) {
					exist = checkRAC(dataListInstall,
							findIndex(headerInstall, header),
							"Real Application Cluster");
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
	public static String resizeImage(String sourceFile, int width)
			throws Exception {
		try {
			String ResizedFile = sourceFile;
			File f = new File(sourceFile);
			if (!f.exists()) {
				return "Source Image File not Found";
			}

			// Logic to implement image resizing
			BufferedImage bim = ImageIO.read(new FileInputStream(sourceFile));
			Image resizedImg = bim.getScaledInstance(width, -1,
					Image.SCALE_AREA_AVERAGING);

			int scaled_height = resizedImg.getHeight(null);

			BufferedImage rBimg = new BufferedImage(width, scaled_height,
					bim.getType());
			// Create Graphics object
			Graphics2D g = rBimg.createGraphics();// Draw the resizedImg from
													// 0,0 with no ImageObserver
			g.drawImage(resizedImg, 0, 0, null);

			// Dispose the Graphics object, we no longer need it
			g.dispose();

			ImageIO.write(rBimg,
					ResizedFile.substring(ResizedFile.indexOf(".") + 1),
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
	public static void getScaledImage(String filepath, int heightLimit,
			int widthLimit) throws FileNotFoundException, IOException {

		int derivedHeight = 0;
		int derivedWidth = 0;
		int imageHeight=0;
		int imageWidth=0;
				

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
		
		// Get Image dimension 
		imageHeight=bim.getHeight();
		imageWidth=bim.getWidth();
		
		// Calculate respective dimension if the best fit image
		if(imageHeight>heightLimit){
			derivedHeight=heightLimit;
			derivedWidth=(imageWidth*derivedHeight)/imageHeight;
		}else if(imageWidth>widthLimit){
			derivedWidth=widthLimit;
			derivedHeight=(imageHeight*derivedWidth)/imageWidth;
		}else{
			derivedHeight=imageHeight;
			derivedWidth=imageWidth;
		}
		
		// Calculate required Image dimension
		
		
		
		Image resizedImg = bim.getScaledInstance(derivedWidth, derivedHeight,
				Image.SCALE_AREA_AVERAGING);
		
		
		BufferedImage rBimg = new BufferedImage(derivedWidth, derivedHeight,
				bim.getType());
		// Create Graphics object
		Graphics2D g = rBimg.createGraphics();// Draw the resizedImg from
												// 0,0 with no ImageObserver
		g.drawImage(resizedImg, 0, 0, null);

		// Dispose the Graphics object, we no longer need it
		g.dispose();

		ImageIO.write(rBimg,
				filepath.substring(filepath.indexOf(".") + 1),
				new FileOutputStream(filepath));
		System.out.println("Successfully corrected image dimention : "+derivedHeight+" X "+derivedWidth);
	}

	public static boolean isObjectInteger(Object o) {
		return o instanceof Integer;
	}

}
