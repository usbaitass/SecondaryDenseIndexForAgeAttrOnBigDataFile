package com.adb.uas.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Test class for various test codes to run
 * 
 * @author usbaitass
 *
 */
public class Main {

	private static File file = new File("Person.txt");
	private static FileInputStream fin = null;
	private static byte[] readBlock = new byte[4000]; // one block 4 KB = 40
														// records
	private static String block;
	private static int blockSearchKey = 1; // 4 bytes
	// private static String sin;
	private static int age; // 4 bytes
	private static byte[][] buckets = new byte[81][4000]; // 324 KB
	private static int[] iBucket = new int[81]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[81]; // 4 x 81 = 324 bytes
	private static PrintWriter out;
	private static String stt;

	private static String searchKeyHex;

	/**
	 * reads the data from a file.
	 */
	public static void readMyFile() {

		try {

			System.out.println("file size = " + file.length() + " bytes.");
			System.out.println(file.length() / 100 + " records.");

			fin = new FileInputStream(file);

			while ((fin.read(readBlock)) != -1) { // we read ONE BLOCK at a time

				processBlock();
				blockSearchKey++; // assign search key to block

				// break;

			}

			// System.out.println("Number of blocks read = " + blockSearchKey);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method process each block that was read
	 */
	public static void processBlock() {

		block = new String(readBlock);

		hashKey();

		for (int i = 0; i < 40; i++) {

			// sin = block.substring(i * 100 + 0, i * 100 + 9);
			age = Integer.parseInt(block.substring(i * 100 + 39, i * 100 + 41));
			// System.out.println("sin = " + sin + " age = " + age);

			InputSearchKeyIntoBucket();

		}

	}

	/**
	 * converts the decimal block index into hex
	 */
	public static void hashKey() {
		searchKeyHex = Integer.toHexString(blockSearchKey);
		// System.out.println("block index = " + blockSearchKey + " Hex = " +
		// searchKeyHex);
	}

	/**
	 * puts hashed search key index into appropriate bucket number according to
	 * age
	 */
	public static void InputSearchKeyIntoBucket() {

		try {
			if (prevBlockIndex[age - 18] != blockSearchKey) { // exclude
																// repetition
																// from bucket

				// System.out.println("HERE = "+ iBucket[age-18]);
				if (iBucket[age - 18] > 3996) {
					freeBucket(age - 18);
				}
				int i = 0;
				for (; i < 3 - searchKeyHex.length(); i++) {
					byte b = ' ';
					// System.out.println(iBucket[age-18]);
					buckets[age - 18][iBucket[age - 18] + i] = b;
				}

				for (int j = 0; j < searchKeyHex.length(); j++) {
					byte b = (byte) searchKeyHex.charAt(j);
					buckets[age - 18][iBucket[age - 18] + i + j] = b;
				}

				iBucket[age - 18] += 3;
			}

			prevBlockIndex[age - 18] = blockSearchKey;

		} catch (Exception e) {
			System.out.println("HERE = " + age);
			System.out.println("iBucket = " + iBucket[age - 18]);
			System.exit(0);
		}

	}

	/**
	 * writes the bucket to the file.
	 * 
	 * @param bucketNumber
	 *            bucket index
	 */
	public static void freeBucket(int bucketNumber) {

		try {
			stt = new String(buckets[bucketNumber]);

			out.print(stt);
			out.println();
			out.println();
			
		} catch (Exception e) {
			System.out.println("Error writing file inside freeBucket().");
		}

		for (int i = 0; i < 4000; i++) {
			buckets[bucketNumber][i] = ' ';
		}

		iBucket[bucketNumber] = 0;

	}

	/**
	 * writes the index blocks into txt file
	 */
	public static void writeToIndexFile() {

		try {

			int i;
			for (i = 0; i < 81; i++) {
				// out.print("bucket " + (i + 18) + " = " + buckets[i]);
				// String stt = new String(buckets[i]);
				// out.println(" ,bucket string value = " + stt);
				stt = new String(buckets[i]);

			//	out.println((i + 18) + "--------------------------------");
				out.print(stt);
			//	out.println();
			//	out.println();
			}

			out.close();
		} catch (Exception e) {
			System.out.println("error in writing to file inside writeToIndexFile().");

		}

	}

	/**
	 * Main Method which initiates the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Program started...");
		long start = System.currentTimeMillis();
		try {
			out = new PrintWriter("indexFile.txt");
		} catch (Exception e) {
			System.out.println("Error in writing file inside main.");
		}
		readMyFile();

		writeToIndexFile();
		

		System.out.println("Program terminated...");
		long end = System.currentTimeMillis();

		System.out.println("Time taken = " + (end - start) + " ms");

		
		Scanner sc = new Scanner(System.in);
		System.out.println("1. Enter Age");
		System.out.println("2. Enter Range Age");
		int selectedOption = sc.nextInt();
		switch(selectedOption){
		case 1: 
			
			//	FIND(sc.nextInt());
			
			break;
		case 2:
			//asdasdasdasdad
			break;
		}
		
		
	}
}// END
