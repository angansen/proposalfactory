package org.oracle.ebs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CSVReader {

	private static ArrayList<Integer> productLevel7 = new ArrayList<Integer>();//Creating arraylist;
	private static ArrayList<Integer> productLevel4 = new ArrayList<Integer>();//Creating arraylist;
	private static ArrayList<Integer> productPillarIndexes = new ArrayList<Integer>();//Creating arraylist;
	private static ArrayList<Integer> productPillarDatabaseIndexes = new ArrayList<Integer>();//Creating arraylist;
	private static ArrayList<Integer> platformDescriptionIndexes = new ArrayList<Integer>();
	private static ArrayList<Integer> indexesForEBusinessSuiteVersion = new ArrayList<Integer>();
	private static ArrayList<Integer> racIndex = new ArrayList<Integer>();//Creating arraylist;
	private static Map<String,Integer> modulePricingMap = new HashMap<String,Integer>();

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		String[] headerInstall = null;
		String[] headerService = null;
		int columnIndexSecond = 0;
		int productGroup = 0;
		int columnIndexFirst = 0;
		int columnIndexThird = 0;
		int columnIndexFourth = 0;
		int productPillar = 0;	
		int platformDescription = 0;
		int componentVersion = 0;
		int componentDescription = 0;
		int resolutionCode = 0;
		File dir = null;

		/*Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		String time = dateFormat.format(now);*/
		if(args[0] == null ){
			dir = new File(".");
		}else {
			dir = new File(args[0]);
		}

		//dir.mkdir();
		File currentDir = new File(dir.getAbsolutePath()); // current directory
		ArrayList<String> clientDirectories = Utility.getClientDirectories(currentDir);
		try {
			for(String directoryPath: clientDirectories){

				System.out.println("########################   "+directoryPath.split("\\\\")[directoryPath.split("\\\\").length-1] + "   ########################");
				List<String[]> dataListInstall = new ArrayList<String[]>();
				List<String[]> dataListService = new ArrayList<String[]>();
				// String path = "E:\\EBS\\Ubisoft\\";
				//String path = args[0];
				String path = directoryPath+"\\";
				String[] platforms = {"Linux", "HP", "Microsoft", "Oracle Solaris", "IBM", "Fujitsu"};
				String[] ebusinessModules = {"eBusiness Suite","Contract Lifecycle Management for Public Sector","Governance, Risk and Compliance (GRC)"};


				headerInstall = Utility.getCSVHeader(path,"Install Base Details");
				headerService = Utility.getCSVHeader(path,"Service Request Details");
				dataListInstall =  Utility.readCSVData(path,"Install Base Details");
				dataListService  =  Utility.readCSVData(path,"Service Request Details");
				columnIndexFirst = Utility.findIndex(headerInstall, "Product Level4");
				columnIndexSecond = Utility.findIndex(headerInstall, "Product Level7");
				columnIndexThird = Utility.findIndex(headerInstall, "Product Name");
				columnIndexFourth = Utility.findIndex(headerInstall, "Pricing Quantity");
				productPillar = Utility.findIndex(headerService, "Product Pillar");
				platformDescription = Utility.findIndex(headerService, "Platform Description");
				componentVersion = Utility.findIndex(headerService, "Component Version");
				productGroup = Utility.findIndex(headerService, "Product Group");
				componentDescription = Utility.findIndex(headerService, "Component Description");
				resolutionCode = Utility.findIndex(headerService, "Resolution Code");




				// System.out.println("-----------  Index of columns -----------");
				// System.out.println("columnIndexFirst -> "+columnIndexFirst);
				// System.out.println("columnIndexSecond ->"+columnIndexSecond);
				// System.out.println("columnIndexThird ->"+columnIndexThird);
				// System.out.println("columnIndexFourth ->"+columnIndexFourth);


				productLevel4 = Utility.getFilteredIndexes(dataListInstall, columnIndexFirst, "Applications");
				// productLevel7 = Utility.getFilteredIndexes(dataListInstall, columnIndexSecond, "eBusiness Suite",productLevel4);
				productLevel7 = Utility.getFilteredIndexes(dataListInstall, columnIndexSecond, ebusinessModules,productLevel4);
				racIndex = Utility.getFilteredIndexes(dataListInstall, columnIndexFirst, "Real Application Clusters", productLevel7);
				productPillarIndexes = Utility.getFilteredIndexes(dataListService, productPillar, "License/Applications");
				platformDescriptionIndexes = Utility.getFilteredIndexes(dataListService, platformDescription, platforms);
				// indexesForEBusinessSuiteVersion = Utility.getFilteredIndexes(dataListService, productGroup, "eBusiness Suite",productPillarIndexes);
				indexesForEBusinessSuiteVersion = Utility.getFilteredIndexes(dataListService, productGroup, ebusinessModules, productPillarIndexes);
				productPillarDatabaseIndexes = Utility.getFilteredIndexes(dataListService, productPillar, "License/Database");



				// System.out.println("-----------  Count of filtered records -----------");
				// System.out.println("No of filtered records in productLevel4 -> "+productLevel4.size());
				// System.out.println("No of filtered records in productLevel7 -> "+productLevel7.size());


				System.out.println("-----------  Module and Pricing Mapping -----------");
				modulePricingMap = Utility.findDistinctModuleAndPrice(dataListInstall,columnIndexThird,columnIndexFourth,productLevel7);
				// Utility.printMap(modulePricingMap);
				// System.out.println("**************************************************");
				// Utility.printMap(Utility.sortModulesByValue(modulePricingMap));
				System.out.println("**************************************************");
				System.out.println("No. of Modules : "+Utility.sortModulesByValue(modulePricingMap).size());
				System.out.println("******************* Top Modules for Pie Chart *******************************");
				Utility.printMap(Utility.printTopModules(Utility.sortModulesByValue(modulePricingMap)));
				// Utility.convertToJSON(Utility.printTopModules(Utility.sortModulesByValue(modulePricingMap)));
				System.out.println("*********************** Silde Number to Select ***************************");
				System.out.println("Slide Number : "+ Utility.selectSlide(modulePricingMap, racIndex));
				// System.out.println("**************************************************");
				// Utility.printMap(Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes));
				// System.out.println("**************************************************");
				// Utility.printMap(Utility.sortModulesByValue(Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes)));
				// System.out.println("**************************************************");
				// System.out.println(Utility.findDistinctPlatformAndVersions(dataListService, platformDescription, componentVersion, platformDescriptionIndexes));
				// System.out.println("**************************************************");
				// Utility.printMap(Utility.getUniquePlatformsCount(Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes)));
				// System.out.println("**************************************************");
				// Utility.printMap(Utility.sortModulesByValue(Utility.getUniquePlatformsCount(Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes))));
				// System.out.println("**************************************************");
				// System.out.println(Utility.getLatestVersionMap(Utility.findDistinctPlatformAndVersions(dataListService, platformDescription, componentVersion, platformDescriptionIndexes)));
				System.out.println("****************** Top Two Platforms ********************************");
				System.out.println(Utility.topTwoPlatformWithVersions(Utility.getLatestVersionMap(Utility.findDistinctPlatformAndVersions(dataListService, platformDescription, componentVersion, platformDescriptionIndexes)), Utility.sortModulesByValue(Utility.getUniquePlatformsCount(Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes)))));
				System.out.println("****************** EBS Version ********************************");
				System.out.println(Utility.getHighestEbusinessSuiteVersion(dataListService, componentVersion, indexesForEBusinessSuiteVersion));
				System.out.println("******************** Database Version ******************************");
				System.out.println(Utility.getHighestDatabaseVersion(productPillarDatabaseIndexes, componentVersion, dataListService));

				System.out.println("*********************RAC Exists *****************************");
				System.out.println(Utility.checkRAC(dataListService, componentDescription, "Real Application Cluster"));
				/*System.out.println("**************************************************");
			ArrayList<String> uniqueValues = Utility.getUniqueValues(dataListService, indexesForEBusinessSuiteVersion, resolutionCode);
			for(String value : uniqueValues){
				System.out.println("Unique Value -> "+ value);
			}*/
				System.out.println("******************** Pain Areas ******************************");
				Boolean[] painArea = Utility.getPainAreas(dataListService, indexesForEBusinessSuiteVersion, resolutionCode);
				for(boolean pain : painArea){
					System.out.println(pain);
				}
			}
		}catch(Exception e){
			System.out.println("Error in "+new Object(){}.getClass() + "Class and Method "+ new Object(){}.getClass().getEnclosingMethod().getName() + " " +e.getMessage());
		}



	}

}