package org.oracle.ebs;

import java.io.File;
import java.util.ArrayList;

public class ExcelReader {

	private static File dir;

	public static void main(String[] args) {

//		 Read Client location from command line argument
		try {
			if (args.length == 0) {
				dir = new File(".");
			} else {
				dir = new File(args[0]);
			}
		} catch (Exception e) {
			System.out.println("no inputs provided...setting default");

		}

//		Get the List of Client directories
		File currentDir = new File(dir.getAbsolutePath()); 
		ArrayList<String> clientDirectories = null;
		try {
			clientDirectories = Utility.getClientDirectories(currentDir);
		} catch (Exception e2) {
			e2.printStackTrace();
		}

	}
}
