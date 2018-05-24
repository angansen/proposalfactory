/*
 * $Id$
 * Created on Oct 30, 2012 by beardj 
 */
package org.oracle.ebs;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.chart.CTPieChart;
import org.docx4j.dml.chart.CTPieSer;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.BufferUtil;
import org.oracle.ebs.beans.Platform;
import org.pptx4j.Pptx4jException;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.Pic;
import org.pptx4j.pml.Shape;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;

import com.google.common.io.Files;

/**
 * Simple demonstration of editing charts in a PowerPoint deck
 *
 * @author Jeff Beard
 *
 */
public class EditEmbeddedCharts {
	public static final String HEADER_TEXT = "Header_text";
	public static final String CHALLENGE_1 = "Challenges_1";
	public static final String CHALLENGE_2 = "Challenges_2";
	public static final String CHALLENGE_3 = "Challenges_3";
	public static final String PLATFORM_DESCRIPTION = "Platform_Description";
	public static final String PLATFORM_OS_1 = "Platform_os_1";
	public static final String PLATFORM_OS_2 = "Platform_os_2";
	public static final String EBS_VERSION = "ebs_version";
	public static final String DB_VERSION = "Db_version";
	private static ArrayList<Integer> productLevel7 = new ArrayList<Integer>();// Creating
																				// arraylist;
	private static ArrayList<Integer> productLevel4 = new ArrayList<Integer>();// Creating
																				// arraylist;
	private static ArrayList<Integer> productPillarIndexes = new ArrayList<Integer>();// Creating
																						// arraylist;
	private static ArrayList<Integer> productPillarDatabaseIndexes = new ArrayList<Integer>();// Creating
																								// arraylist;
	private static ArrayList<Integer> platformDescriptionIndexes = new ArrayList<Integer>();
	private static ArrayList<Integer> indexesForEBusinessSuiteVersion = new ArrayList<Integer>();
	private static ArrayList<Integer> racIndex = new ArrayList<Integer>();// Creating
																			// arraylist;
	private static Map<String, Integer> modulePricingMap = new HashMap<String, Integer>();
	private static Map<String, String> delete_slide_map = new HashMap<String, String>();
	private static Map<String, String> platform_ver_map = new HashMap<String, String>();
	private static Map<String, Integer> topModulesMap = new HashMap<String, Integer>();

	private static List<String> failedClientsFolders = new ArrayList<String>();
	private static List<String> successClientsFolders = new ArrayList<String>();

	public static void main(String[] args) throws Docx4JException, IOException {

		// intialize
		String[] headerInstall = null;
		int columnIndexSecond = 0;
		int columnIndexFirst = 0;
		int columnIndexThird = 0;
		int columnIndexFourth = 0;
		int productPillar = 0;
		int platformDescription = 0;
		int componentVersion = 0;
		int componentDescription = 0;
		int resolutionCode = 0;
		int productGroup = 0;
		File dir = null;
		List<String[]> dataListInstall = new ArrayList<String[]>();
		List<String[]> dataListService = new ArrayList<String[]>();
		String[] headerService = null;
		InputStream in = null;
		BinaryPartAbstractImage imagePart;
		String csv_path = null;
		String CustomerName = null;
		String op_filename = null;
		String pic_location = null;
		File inside_currentDir = null;

		// the input folder can also be send as a command line argument
		// so it is not mandatory to place the jar in the proposal folder
		// this can be useful if we want to keep the jar in a central place and
		// not copy across all the folders

		try {
			if (args.length == 0) {
				dir = new File(".");
				// dir = new File(
				// "G:\\project\\corporate\\ProposalFactory\\project");
			} else {
				dir = new File(args[0]);
			}
		} catch (Exception e) {
			System.out.println("no inputs provided...setting default");

		}

		File currentDir = new File(dir.getAbsolutePath()); // current directory
		ArrayList<String> clientDirectories = null;
		try {
			clientDirectories = Utility.getClientDirectories(currentDir);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		/********************/

		// constants & default values
		// this are all default values, in case no value or error comes from the
		// data processing unit, this default value will be rendered in the ppt
		String[] platforms = { "Linux", "HP", "Microsoft", "Oracle Solaris", "IBM" };
		String Challenges_1 = "Performance Challenges – Growth and Capacity";
		String Challenges_2 = "Patching and Upgrade – Risk and Time";
		String Challenges_3 = "IT Resources and Cost – Administration and Complexity";
		String Platform_Description = "The Platform_Description text 1 goes here";
		String Platform_os_1 = "The Platform_os text 1 goes here";
		String Platform_os_2 = "The Platform_os text 1 goes here";
		String ebs_version = "The ebs_version text 1 goes here";
		String Db_version = "The Db_version text 1 goes here";

		// check in ppt/_rels/presentation.xml.rels for the correct rId values
		delete_slide_map.put("5", "rId9");
		delete_slide_map.put("6", "rId10");
		delete_slide_map.put("7", "rId11");
		String templateFile = "/org/oracle/ebs/resources/test.pptx";

		// this is the chart file location
		// do remember that chart1 will be the first occurrence of a chart in
		// the ppt, not necessarily it will be the first slide of the ppt.
		String chartPartName = "/ppt/charts/chart1.xml";
		String xlsPartName = "/ppt/embeddings/Microsoft_Excel_Worksheet1.xlsx";
		String Header_text = "CNO EBS";
		String[] ebusinessModules = { "eBusiness Suite", "Contract Lifecycle Management for Public Sector",
				"Governance, Risk and Compliance (GRC)" };

		for (String directoryPath : clientDirectories) {
			boolean flag = false;
			// we have to skip the failed ones and continue with the others,it
			// should be a non-blocking event

			// loop starts
			// this was a fix introduced to tackel special characters in
			// filename
			try {
				csv_path = directoryPath + "\\";
				op_filename = directoryPath.split("\\\\")[directoryPath.split("\\\\").length - 1];
				CustomerName = op_filename.substring(0, op_filename.lastIndexOf("_"));
				if (flag != true) {
					CustomerName = StringUtils.replaceEach(CustomerName, new String[] { "&", "\"", "<", ">", "." },
							new String[] { "&amp;", "&quot;", "&lt;", "&gt;", "\\." });
					flag = true;
				}

				// files inside sub
				// looking for the image file , all the standard image file
				// format supported
				inside_currentDir = new File(csv_path);
				File[] files_list = inside_currentDir.listFiles();
				// System.out.println("files_list" + files_list.toString());
				for (File file : files_list) {
					// System.out.println(" file:" + file.getCanonicalPath());

					if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")
							|| file.getName().toLowerCase().endsWith(".png")
							|| file.getName().toLowerCase().endsWith(".gif"))
						pic_location = file.getCanonicalPath();

					try {
						Utility.getScaledImage(pic_location, 75, 250);
					} catch (Exception e) {
						System.out.println("Image resizing failed. Terminating the process: " + e);
						// throw new Exception("Error in mergeFiles");
					}
				}

				// System.out.println("current dir path" + csv_path);
				System.out.println("Customer Name " + CustomerName);
				// System.out.println("op_filename" + op_filename);

				// String pic_location =csv_path+ "logo.png";
				long yourmilliseconds = System.currentTimeMillis();
				SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy_HH-mm");
				Date resultdate = new Date(yourmilliseconds);
				String outputfilepath = csv_path + op_filename + "_" + sdf.format(resultdate) + ".pptx";

				// Load the template file
				try {
					in = EditEmbeddedCharts.class.getResourceAsStream(templateFile);
					// System.out.println(in.toString());
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

				/**
				 * ALL this data is coming from the backend processing unit.
				 */
				headerInstall = Utility.getCSVHeader(csv_path, "Install Base Details");
				dataListInstall = Utility.readCSVData(csv_path, "Install Base Details");
				dataListService = Utility.readCSVData(csv_path, "Service Request Details");
				headerInstall = Utility.getCSVHeader(csv_path, "Install Base Details");
				headerService = Utility.getCSVHeader(csv_path, "Service Request Details");
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
				productLevel4 = Utility.getFilteredIndexes(dataListInstall, columnIndexFirst, "Applications");
				productLevel7 = Utility.getFilteredIndexes(dataListInstall, columnIndexSecond, ebusinessModules,
						productLevel4);
				racIndex = Utility.getFilteredIndexes(dataListInstall, columnIndexFirst, "Real Application Clusters",
						productLevel7);
				productPillarIndexes = Utility.getFilteredIndexes(dataListService, productPillar,
						"License/Applications");
				platformDescriptionIndexes = Utility.getFilteredIndexes(dataListService, platformDescription,
						platforms);
				indexesForEBusinessSuiteVersion = Utility.getFilteredIndexes(dataListService, productGroup,
						ebusinessModules, productPillarIndexes);
				Utility.getHighestEBSVersion(dataListService,ebusinessModules);
				productPillarDatabaseIndexes = Utility.getFilteredIndexes(dataListService, productPillar,
						"License/Database");
				modulePricingMap = Utility.findDistinctModuleAndPrice(dataListInstall, columnIndexThird,
						columnIndexFourth, productLevel7);
				platform_ver_map = Utility.topTwoPlatformWithVersions(
						Utility.getLatestVersionMap(Utility.findDistinctPlatformAndVersions(dataListService,
								platformDescription, componentVersion, platformDescriptionIndexes)),
						Utility.sortModulesByValue(Utility.getUniquePlatformsCount(Utility
								.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes))));
				topModulesMap = Utility.printTopModules(Utility.sortModulesByValue(modulePricingMap));

				// System.out.println(modulePricingMap.size());
				// System.out.println("topmodule size" + topModulesMap.size());

				List<Platform> filteredPlatform = Utility.getTopTwoPlatforms(
						Utility.findPlatformCount(dataListService, platformDescription, platformDescriptionIndexes));

				// Open the PPT template file
				PresentationMLPackage ppt = (PresentationMLPackage) OpcPackage.load(in);

				// *************************Slide1*****************************

				// open the slide

				SlidePart slide1 = (SlidePart) ppt.getParts().get(new PartName("/ppt/slides/slide1.xml"));

				ClassFinder dmlShapeFinder_slide1 = new ClassFinder(org.pptx4j.pml.Shape.class);

				// this is a callback method, the travers util will return the
				// matched object in to the class object
				new TraversalUtil(slide1.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),
						dmlShapeFinder_slide1);

				if (dmlShapeFinder_slide1.results.isEmpty())
					return;

				for (int i = 0; i < dmlShapeFinder_slide1.results.size(); i++) {
					Shape index_shp_slide1 = (Shape) dmlShapeFinder_slide1.results.get(i);

					String buff = XmlUtils.marshaltoString(index_shp_slide1.getTxBody().getP().get(0), true, true,
							org.pptx4j.jaxb.Context.jcPML, "http://schemas.openxmlformats.org/presentationml/2006/main",
							"txBody", CTTextParagraph.class);

					// this will do the string replacement in the slide1.xml

					if (buff.indexOf("Customer Name") != -1)
						buff = buff.replaceFirst("&lt;Customer Name&gt;", CustomerName);
					CTTextParagraph testtt = new CTTextParagraph();
					try {
						testtt = (CTTextParagraph) XmlUtils.unmarshalString(buff, org.pptx4j.jaxb.Context.jcPML,
								CTTextParagraph.class);
					} catch (JAXBException e) {
						e.printStackTrace();
					}

					// after the string is modified and unmarshalled add it back
					// to the slide
					index_shp_slide1.getTxBody().getP().set(0, testtt);
				}

				if (pic_location != null) {
					// Add the picture
					File file2 = new java.io.File(pic_location);

					try {
						imagePart = BinaryPartAbstractImage.createImagePart(ppt, slide1, file2);
						slide1.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()
								.add(createPicture(imagePart.getSourceRelationship().getId(), "10444564", "5082273",
										"1288648", "1139648"));
						// slide1.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(createPicture(imagePart.getSourceRelationship().getId(),"10444564","5082273","1288648","1139648"));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						throw new Exception("Error while creating Image part for Slide");
					}
				}
				// *************************Slide1*****************************

				// *************************Slide4*****************************

				SlidePart slide4 = (SlidePart) ppt.getParts().get(new PartName("/ppt/slides/slide4.xml"));

				Db_version = "";
				if (Utility.getHighestDatabaseVersion(productPillarDatabaseIndexes, componentVersion, dataListService)
						.length() > 0) {

					Db_version = "Oracle " + Utility.getHighestDatabaseVersion(productPillarDatabaseIndexes,
							componentVersion, dataListService);
				}
				ebs_version = "";
				if (Utility.getHighestEbusinessSuiteVersion(dataListService, componentVersion,
						indexesForEBusinessSuiteVersion).length() > 0) {
					ebs_version = "EBS " + Utility.getHighestEbusinessSuiteVersion(dataListService, componentVersion,
							indexesForEBusinessSuiteVersion);
				}

				Platform_os_1 = "";
				Platform_os_2 = "";

				System.out.println("EBS : > "+ebs_version);
				System.out.println("Top PLatform - " + filteredPlatform);
				if (!filteredPlatform.isEmpty()) {
					for (int i = 0; i < 2; i++) {
						if (filteredPlatform.size() > 0 && i == 0) {
							Platform_os_1 = filteredPlatform.get(i).getDisplayName();
						}
						if (filteredPlatform.size() > 1 && i == 1) {
							Platform_os_2 = filteredPlatform.get(i).getDisplayName();
						}
					}
				}

				// if(Utility.checkRAC(dataListService, componentDescription,
				// "Real Application Cluster"))Db_version=Db_version+"\n RAC";
				if (Utility.searchInstallBaseForRAC(dataListInstall, headerInstall, "Real Application Cluster")
						|| Utility.checkRAC(dataListService, componentDescription, "Real Application Cluster"))
					Db_version = Db_version + "\n RAC";
				Boolean[] painArea = Utility.getPainAreas(dataListService, indexesForEBusinessSuiteVersion,
						resolutionCode);
				// System.out.println(painArea[0]);
				// if (painArea[0] == false)
				// Challenges_1 = "";
				// if (painArea[1] == false)
				// Challenges_2 = "";
				// if (painArea[2] == false)
				// Challenges_3 = "";

				// Add the picture
				File file = new java.io.File(pic_location);

				try {
					imagePart = BinaryPartAbstractImage.createImagePart(ppt, slide4, file);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				ClassFinder dmlShapeFinder = new ClassFinder(org.pptx4j.pml.Shape.class);

				new TraversalUtil(slide4.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),
						dmlShapeFinder);
				if (dmlShapeFinder.results.isEmpty())
					return;

				for (int i = 0; i < dmlShapeFinder.results.size(); i++) {
					Shape index_shp = (Shape) dmlShapeFinder.results.get(i);

					String buff = XmlUtils.marshaltoString(index_shp.getTxBody().getP().get(0), true, true,
							org.pptx4j.jaxb.Context.jcPML, "http://schemas.openxmlformats.org/presentationml/2006/main",
							"txBody", CTTextParagraph.class);


					if (buff.indexOf("FILLER") != -1) {
						// System.out.println(i + "-->" + buff);
						slide4.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().remove(0);
						// slide4.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().get(0);
						slide4.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().remove(i);

					}

					// replace all the texts in the chart slide
					if (buff.indexOf(HEADER_TEXT) != -1)
						buff = buff.replaceFirst(HEADER_TEXT, Header_text);
					if (true)
						buff = buff.replaceFirst(CHALLENGE_1, Challenges_1);
					if (true)
						buff = buff.replaceFirst(CHALLENGE_2, Challenges_2);
					if (true)
						buff = buff.replaceFirst(CHALLENGE_3, Challenges_3);
					if (buff.indexOf(PLATFORM_DESCRIPTION) != -1)
						buff = buff.replaceFirst(PLATFORM_DESCRIPTION, Platform_Description);
					if (buff.indexOf(PLATFORM_OS_1) != -1)
						buff = buff.replaceFirst(PLATFORM_OS_1, Platform_os_1);
					if (buff.indexOf(EBS_VERSION) != -1)
						buff = buff.replaceFirst(EBS_VERSION, ebs_version);
					if (buff.indexOf(DB_VERSION) != -1)
						buff = buff.replaceFirst(DB_VERSION, Db_version);
					if (buff.indexOf(PLATFORM_OS_2) != -1)
						buff = buff.replaceFirst(PLATFORM_OS_2, Platform_os_2);
					if (buff.indexOf("Customer Name") != -1)
						buff = buff.replaceFirst("&lt;Customer Name&gt;", CustomerName);
					if (buff.indexOf("<Customer>") != -1)
						buff = buff.replaceFirst("&lt;Customer&gt;", CustomerName);

					CTTextParagraph testtt = new CTTextParagraph();
					try {
						testtt = (CTTextParagraph) XmlUtils.unmarshalString(buff, org.pptx4j.jaxb.Context.jcPML,
								CTTextParagraph.class);
					} catch (JAXBException e) {
						e.printStackTrace();
					}
					index_shp.getTxBody().getP().set(0, testtt);
				}

				// ******************* Chart starts**********************

				/***
				 * The basic idea here is to load the chart1.xml and update it on the fly. Now
				 * to edit the chart xml, we are going to create a string pattern that can be
				 * unmarshalled back as xml. to under stand the exact string pattern, please
				 * check ppt/charts/chart1.xml file under <c:pieChart> tag Once string is
				 * dynamically updated with all the chart data unmarshalled, we can replace the
				 * chart1.xml with the updated data. Also note that we need to update the
				 * worksheet excel associated with this pie chart.
				 */

				// get the chart component from the slide
				Chart chart = (Chart) ppt.getParts().get(new PartName(chartPartName));

				// list the number of pie chart objects, in this case only 1
				List<Object> objects = chart.getJaxbElement().getChart().getPlotArea()
						.getAreaChartOrArea3DChartOrLineChart();

				for (Object object : objects) {

					/**
					 * example o/p data: <c:ptCount val="16"/> <c:pt idx="0"> <c:v>Enterprise Asset
					 * Management</c:v> </c:pt> <c:pt idx="1"> <c:v>Self-Service Work Requests</c:v>
					 * </c:pt>
					 */

					String ptCount = "<c:ptCount val=\"" + topModulesMap.size() + "\"/>";
					String strRef = "";
					String numRef = "";
					Iterator<String> iterator = topModulesMap.keySet().iterator();
					int counter = 0;
					while (iterator.hasNext()) {
						String key = iterator.next().toString();
						Integer value = topModulesMap.get(key);

						// this are the label fields of the chart
						strRef = strRef + "<c:pt idx=\"" + counter + "\">" + "<c:v>" + key + "</c:v>" + "</c:pt>";
						// this are the value fields of the chart
						numRef = numRef + "<c:pt idx=\"" + counter + "\">" + "<c:v>" + value + "</c:v>" + "</c:pt>";
						counter += 1;
					}

					// every component needs to wrapped under names space and
					// txbody
					// here we are going to update the string pattern with the
					// values we composed in the loop above

					String addval = "<p:txBody xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" xmlns:ns6=\"http://schemas.openxmlformats.org/drawingml/2006/chartDrawing\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" xmlns:ns12=\"http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:ns11=\"http://schemas.openxmlformats.org/drawingml/2006/compatibility\" xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
							+ " <c:idx val=\"0\"/>" + " <c:order val=\"0\"/> " + "<c:tx> "
							+ "<c:strRef> <c:f>Sheet1!$B$1</c:f> <c:strCache> <c:ptCount val=\"1\"/> <c:pt idx=\"0\"> <c:v>EBS Purchasing History</c:v> </c:pt> </c:strCache> </c:strRef> "
							+ "</c:tx> " + "<c:cat> " + "<c:strRef> <c:f>Sheet1!$A$2:$A$" + (topModulesMap.size() + 1)
							+ "</c:f> <c:strCache>" + ptCount + strRef + "</c:strCache> </c:strRef>" + "</c:cat> "
							+ "<c:val>" + " <c:numRef> <c:f>Sheet1!$B$2:$B$" + (topModulesMap.size() + 1)
							+ "</c:f> <c:numCache> <c:formatCode>General</c:formatCode>" + ptCount + numRef
							+ "</c:numCache></c:numRef>" + "</c:val> "
							+ "<c:extLst> <c:ext uri=\"{C3380CC4-5D6E-409C-BE32-E72D297353CC}\"> <c16:uniqueId val=\"{00000000-310B-4ADF-8D12-0B51F5325448}\" xmlns:c16r2=\"http://schemas.microsoft.com/office/drawing/2015/06/chart\" xmlns:c16=\"http://schemas.microsoft.com/office/drawing/2014/chart\"/> </c:ext> </c:extLst> "
							+ "</p:txBody>";

					// intialize a new pie chart object
					CTPieSer testtt123 = new CTPieSer();

					// unmarshall the string above to in to a pie chart, the
					// first parameter will the string to unrmarshal and last
					// parameter will mention the class type
					try {
						testtt123 = (CTPieSer) XmlUtils.unmarshalString(addval, org.pptx4j.jaxb.Context.jcPML,
								CTPieSer.class);
					} catch (JAXBException e) {
						e.printStackTrace();
					}
					// this check is important to check if the object is really
					// a piechart
					if (object instanceof CTPieChart)
						((CTPieChart) object).getSer().set(0, testtt123);

				}

				// load the embedded excel in the pie chart
				EmbeddedPackagePart epp = (EmbeddedPackagePart) ppt.getParts().get(new PartName(xlsPartName));

				if (epp == null) {
					throw new Docx4JException("Could find EmbeddedPackagePart: " + xlsPartName);
				}

				InputStream is = BufferUtil.newInputStream(epp.getBuffer());
				SpreadsheetMLPackage spreadSheet = (SpreadsheetMLPackage) SpreadsheetMLPackage.load(is);
				Map<PartName, Part> partsMap = spreadSheet.getParts().getParts();
				Iterator<Entry<PartName, Part>> it = partsMap.entrySet().iterator();
				int counter = 1;
				while (it.hasNext()) {

					Map.Entry<PartName, Part> pairs = it.next();

					if (partsMap.get(pairs.getKey()) instanceof WorksheetPart) {

						WorksheetPart wsp = (WorksheetPart) partsMap.get(pairs.getKey());

						// this will give the list of all the rows,by default a
						// provision of 30 rows is made already in the template
						// ppt
						List<Row> rows = wsp.getJaxbElement().getSheetData().getRow();
						// top modules as sent from the backend data processing
						Iterator<String> iterator = topModulesMap.keySet().iterator();

						// System.out.println(topModulesMap.keySet().size());
						// System.out.println("rows-->" + rows.size());

						while (iterator.hasNext()) {

							if (counter > 15)
								break;

							String key = iterator.next().toString();
							// System.out.println("counter-->" + counter);
							Integer value = topModulesMap.get(key);

							// this is going to set excel with updated values
							// look out for the celltype
							rows.get(counter).getC().get(0).setT(STCellType.STR);
							rows.get(counter).getC().get(0).setV(key);
							rows.get(counter).getC().get(1).setT(STCellType.N);
							rows.get(counter).getC().get(1).setV(value.toString());

							counter += 1;
						}

					}

				}

				// ******************* Chart ends***********************

				// *************************Slide4*****************************

				// *************************delete
				// slide*****************************

				/**
				 * Here we are going to delete the small/medium/large slide 5,6,7 based on the
				 * value sent from the backend process
				 * 
				 * 
				 */

				Iterator<String> iterator_3 = delete_slide_map.keySet().iterator();

				while (iterator_3.hasNext()) {

					String key = iterator_3.next().toString();

					MainPresentationPart mpp = ppt.getMainPresentationPart();
					Relationship rel = mpp.getRelationshipsPart().getRelationshipByID(delete_slide_map.get(key));

//					System.out.println("keys-------->" + key + delete_slide_map.get(key));
					//
					String slide_to_retain = Integer.toString(Utility.selectSlide(modulePricingMap, racIndex,
							componentDescription, dataListService, dataListInstall, headerInstall));

					// System.out.println(slide_to_retain);

					if (!key.equals(slide_to_retain) && !slide_to_retain.equals("0")) {
						try {
							mpp.removeSlide(rel);
						} catch (Pptx4jException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}

				}

				// *************************delete
				// slide*****************************

				// *************************Sheet3*****************************

				SlidePart slide3 = (SlidePart) ppt.getParts().get(new PartName("/ppt/slides/slide3.xml"));
				ClassFinder dmlShapeFinder_slide3 = new ClassFinder(org.pptx4j.pml.Shape.class);
				new TraversalUtil(slide3.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),
						dmlShapeFinder_slide3);
				if (dmlShapeFinder_slide3.results.isEmpty())
					return;

				for (int i3 = 0; i3 < dmlShapeFinder_slide3.results.size(); i3++) {
					Shape index_shp_i3 = (Shape) dmlShapeFinder_slide3.results.get(i3);

					String buff2 = XmlUtils.marshaltoString(index_shp_i3.getTxBody().getP().get(0), true, true,
							org.pptx4j.jaxb.Context.jcPML, "http://schemas.openxmlformats.org/presentationml/2006/main",
							"txBody", CTTextParagraph.class);

					// System.out.println(buff2);

					if (buff2.indexOf("&lt;Customer&gt;") != -1)
						buff2 = buff2.replaceFirst("&lt;Customer&gt;", CustomerName);

					CTTextParagraph testtt = new CTTextParagraph();
					try {
						testtt = (CTTextParagraph) XmlUtils.unmarshalString(buff2, org.pptx4j.jaxb.Context.jcPML,
								CTTextParagraph.class);
					} catch (JAXBException e) {
						e.printStackTrace();
					}
					index_shp_i3.getTxBody().getP().set(0, testtt);
				}

				// Add the picture
				File file_3 = new java.io.File(pic_location);
				BinaryPartAbstractImage imagePart_3;

				// *************************Sheet3*****************************

				// Final ppt!!

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SaveToZipFile saver = new SaveToZipFile(spreadSheet);
				saver.save(baos);
				epp.setBinaryData(baos.toByteArray());
				
				ppt.save(new java.io.File(outputfilepath));
				baos.close();

				System.out.println("\n\n done .. saved " + outputfilepath );

				String currpath = inside_currentDir.getPath();
				successClientsFolders.add(currpath);

				// Add
				addImagesToSlide(outputfilepath, pic_location);

			} // try block ends
			catch (Exception e) {
				System.out.println("Creation of PPT HAS FAILED for " + CustomerName);
				e.printStackTrace();
				String newDirName = op_filename.replaceAll("---FAILED", "") + "";
				newDirName += "---FAILED";
				File newDir = new File(inside_currentDir.getParent() + "\\" + newDirName);
				System.out.println("----------------------------  End of Process --------------------------");
				failedClientsFolders.add(inside_currentDir.getParent() + "\\" + op_filename);
				// System.out.println("Renaming Client Folder:
				// "+inside_currentDir.renameTo(newDir));
				// Files.move(inside_currentDir, newDir);

				System.out.println("-----------------------------------------------------------------------");

				// File file = new File(newDirName+"\\"+"error.txt");
				continue;
			}

			/*
			 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); SaveToZipFile saver
			 * = new SaveToZipFile(spreadSheet); saver.save(baos);
			 * epp.setBinaryData(baos.toByteArray()); ppt.save(new
			 * java.io.File(outputfilepath)); System.out.println("\n\n done .. saved " +
			 * outputfilepath);
			 * 
			 * }// try block ends catch (Exception e) {
			 * System.out.println("Creation of PPT HAS FAILED for "+ CustomerName );
			 * e.printStackTrace(); String newDirName=op_filename+"---FAILED"; File newDir =
			 * new File(inside_currentDir.getParent() + "\\" + newDirName);
			 * inside_currentDir.renameTo(newDir); //File file = new
			 * File(newDirName+"\\"+"error.txt"); continue; }
			 */

		} // loop ends

		renameClientFolders();

	}

	private static boolean renameClientFolders() {
		String apend = "---FAILED";
		int renameCountfailed = 0, renameCountSucceed = 0;
		for (String filename : failedClientsFolders) {

			
			String updatedFilename = filename.replaceAll(apend, "") + apend;
			if (!filename.equals(updatedFilename)) {
				try {
					Files.move(new File(filename), new File(updatedFilename));
					System.out.println("Failed file marked : " + filename);
					renameCountfailed++;
				} catch (IOException e) {
					System.out.println("Rename failed: " + filename);
				}
			}
		}

		for (String filename : successClientsFolders) {
			
			String updatedFilename = filename.replaceAll(apend, "");
			if (!filename.equals(updatedFilename)) {
				try {
					Files.move(new File(filename), new File(updatedFilename));
					System.out.println("Succeeded file marked : " + filename);
					renameCountSucceed++;
				} catch (IOException e) {
					System.out.println("Rename failed: " + filename);
				}
			}
		}

		System.out.println("Successfully marked fail " + renameCountfailed + " folder(s)");
		System.out.println("Successfully marked success" + renameCountSucceed + " folder(s)");
		return true;
	}

	private static void addImagesToSlide(String pptpath, String pic_location) {

		try (FileInputStream fis = new FileInputStream(pptpath)) {

			try (XMLSlideShow ppt = new XMLSlideShow(fis)) {
				List<XSLFSlide> slides = ppt.getSlides();

				// SET LOGO FOR SLIDE 1
				XSLFSlide slide1 = slides.get(0);
				List<XSLFShape> shapes = slide1.getShapes();

				XSLFShape pic = shapes.get(3);
				java.awt.geom.Rectangle2D anchor = pic.getAnchor();

				slide1.removeShape(pic);

				byte[] pictureData = IOUtils.toByteArray(new FileInputStream(pic_location));

				ImageIcon logoimage;
				int s1Imagewidth;
				XSLFPictureData logopicobj;
				logopicobj = ppt.addPicture(pictureData, PictureType.PNG);
				logoimage = new ImageIcon(pic_location);
				try {

					int s1Imagex = (int) anchor.getX();
					int s1Imagey = (int) anchor.getY();
					int s1Imageheight = logoimage.getIconHeight();
					s1Imagewidth = logoimage.getIconWidth();

					XSLFPictureShape logo = slide1.createPicture(logopicobj);

					logo.setAnchor(
							new Rectangle(s1Imagex - (s1Imagewidth / 2), s1Imagey + 20, s1Imagewidth, s1Imageheight));

					System.out.println("Logo updated in Slide 1");
				} catch (Exception e) {
					System.out.println(e);
				}

				// ----------------- END -------------------

				// SET LOGO FOR SLIDE 3

				XSLFSlide slide3 = slides.get(2);
				List<XSLFShape> slide3shapes = slide3.getShapes();

				XSLFShape slide3logo = slide3shapes.get(17);

				java.awt.geom.Rectangle2D s3anchor = slide3logo.getAnchor();

				XSLFPictureData logo3picobj = ppt.addPicture(pictureData, PictureType.PNG);

				slide3.removeShape(slide3logo);

				try {
					int s3imagex = (int) s3anchor.getX();
					int s3imagey = (int) s3anchor.getY();
					int s3imageheight = logoimage.getIconHeight();
					int s3imagewidth = logoimage.getIconWidth();

					XSLFPictureShape s3logo = slide3.createPicture(logo3picobj);

					s3logo.setAnchor(new Rectangle(s3imagex - (s3imagewidth / 4), s3imagey + 10, s3imagewidth,
							s3imageheight - 10));
					System.out.println("Logo updated in Slide 3");
				} catch (Exception e) {
					System.out.println(e);
				}

				// ----------------- END -------------------

				// SET LOGO FOR SLIDE 4

				XSLFSlide slide4 = slides.get(3);
				List<XSLFShape> slide4shapes = slide4.getShapes();

				XSLFShape slide4logo = slide4shapes.get(13);

				XSLFPictureData logo4picobj = ppt.addPicture(pictureData, PictureType.PNG);
				java.awt.geom.Rectangle2D s4anchor = slide4logo.getAnchor();

				slide4.removeShape(slide4logo);

				try {
					int s4imagex = (int) s4anchor.getX();
					int s4imagey = (int) s4anchor.getY();
					int s4imageheight = logoimage.getIconHeight();
					int s4imagewidth = logoimage.getIconWidth();

					XSLFPictureShape s4logo = slide4.createPicture(logopicobj);

					s4logo.setAnchor(new Rectangle(s4imagex - (s4imagewidth / 2), s4imagey + 20, s4imagewidth,
							s4imageheight - 20));

					System.out.println("Logo updated in Slide 4");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(e);
				}

				FileOutputStream fos = new FileOutputStream(pptpath);
				ppt.write(fos);
				fos.close();

			} catch (Exception e1) {
				System.out.println(e1);
			}

		} catch (Exception e) {
			System.out.println(e);
		}finally {
			System.out.println();
		}
	}

	private static Object createPicture(String relId, String offx, String offy, String extcx, String extcy)
			throws JAXBException {

		java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();
		mappings.put("id1", "4");
		mappings.put("name", "Picture 3");
		mappings.put("descr", "greentick.png");
		mappings.put("rEmbedId", relId);
		mappings.put("offx", offx);
		mappings.put("offy", offy);
		mappings.put("extcx", extcx);
		mappings.put("extcy", extcy);
		return org.docx4j.XmlUtils.unmarshallFromTemplate(SAMPLE_PICTURE, mappings, Context.jcPML, Pic.class);
	}

	// picture template

	private static String SAMPLE_PICTURE = "<p:pic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"> "
			+ "<p:nvPicPr>" + "<p:cNvPr id=\"${id1}\" name=\"${name}\" descr=\"${descr}\"/>" + "<p:cNvPicPr>"
			+ "<a:picLocks noChangeAspect=\"1\"/>" + "</p:cNvPicPr>" + "<p:nvPr/>" + "</p:nvPicPr>" + "<p:blipFill>"
			+ "<a:blip r:embed=\"${rEmbedId}\" cstate=\"print\"/>" + "<a:stretch>" + "<a:fillRect/>" + "</a:stretch>"
			+ "</p:blipFill>" + "<p:spPr>" + "<a:xfrm>" + "<a:off x=\"${offx}\" y=\"${offy}\"/>"
			+ "<a:ext cx=\"${extcx}\" cy=\"${extcy}\"/>" + "</a:xfrm>" + "<a:prstGeom prst=\"rect\">" + "<a:avLst/>"
			+ "</a:prstGeom>" + "</p:spPr>" + "</p:pic>";

	// this will traverse through the slide and find out the required object

	static class ClassFinder extends CallbackImpl {

		protected Class<?> typeToFind;

		public ClassFinder(Class<?> typeToFind) {
			this.typeToFind = typeToFind;
		}

		public List<Object> results = new ArrayList<Object>();

		@Override
		public List<Object> apply(Object o) {

			// Adapt as required
			if (o.getClass().equals(typeToFind)) {
				results.add(o);
			}
			return null;
		}

		public List<Object> getChildren(Object o) {

			if (o instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
				org.docx4j.dml.Graphic graphic = ((org.pptx4j.pml.CTGraphicalObjectFrame) o).getGraphic();
				if (graphic != null && graphic.getGraphicData() != null) {
					return graphic.getGraphicData().getAny();
				} else {
					return null;
				}
			}

			return TraversalUtil.getChildrenImpl(o);
		}

		/********************/

	}

}