package com.adb.uas.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
//	private static String sin;
	private static int age = 0; // 4 bytes
	private static byte[][] buckets = new byte[82][4000]; // 324 KB
	private static int[] iBucket = new int[82]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[82]; // 4 x 81 = 324 bytes
	private static PrintWriter out;
	private static String stt;
	private static int[] bucketBlockIndexSize = new int[82];
	private static int blockPointer = 0;
	private static int[] bPointerForBuckets = new int[82];

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

		//	sin = block.substring(i * 100 + 0, i * 100 + 9);
			age = Integer.parseInt(block.substring(i * 100 + 39, i * 100 + 41))-18;
		//	System.out.println("sin = " + sin + " age = " + age);

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

			if (prevBlockIndex[age] != blockSearchKey) {

				if (iBucket[age] > 4000 - bucketBlockIndexSize[age] - 1) {
					freeBucket(age);
				}

				if (iBucket[age] == 0) {
					String strAge = Integer.toString(age+18);
					buckets[age][0] = (byte) strAge.charAt(0);
					buckets[age][1] = (byte) strAge.charAt(1);

					String strSize = Integer.toString(bucketBlockIndexSize[age]);
					buckets[age][2] = (byte) strSize.charAt(0);

					String strPointer = Integer.toHexString(blockPointer);

					int d = strPointer.length();
					for (int i = 0; i < 7 - d; i++) {
						strPointer = '0' + strPointer;
					}

					buckets[age][3] = (byte) strPointer.charAt(0);
					buckets[age][4] = (byte) strPointer.charAt(1);
					buckets[age][5] = (byte) strPointer.charAt(2);
					buckets[age][6] = (byte) strPointer.charAt(3);
					buckets[age][7] = (byte) strPointer.charAt(4);
					buckets[age][8] = (byte) strPointer.charAt(5);
					buckets[age][9] = (byte) strPointer.charAt(6);

					iBucket[age] += 10;
				}

				int i = 0;
				for (; i < bucketBlockIndexSize[age] - searchKeyHex.length(); i++) {
					byte b = ' ';
					// System.out.println(iBucket[age-18]);
					buckets[age][iBucket[age] + i] = b;
				}

				for (int j = 0; j < searchKeyHex.length(); j++) {
					byte b = (byte) searchKeyHex.charAt(j);
					buckets[age][iBucket[age] + i + j] = b;
				}

				iBucket[age] += bucketBlockIndexSize[age];
			}

			prevBlockIndex[age] = blockSearchKey;

	
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
			//out.println();
			//out.println();

		} catch (Exception e) {
			System.out.println("Error writing file inside freeBucket().");
		}

		for (int i = 0; i < 4000; i++) {
			buckets[bucketNumber][i] = ' ';
		}

		iBucket[bucketNumber] = 0;

	//	System.out.println("blockPointer");
		bPointerForBuckets[age] = blockPointer;
		blockPointer++;

	}

	/**
	 * writes the index blocks into txt file
	 */
	public static void writeToIndexFile() {

		try {

			int i;
			for (i = 0; i <= 81; i++) {
				// out.print("bucket " + (i + 18) + " = " + buckets[i]);
				// String stt = new String(buckets[i]);
				// out.println(" ,bucket string value = " + stt);
				stt = new String(buckets[i]);

				// out.println((i + 18) + "--------------------------------");
				out.print(stt);
				//out.println();
				//out.println();
			}

			out.close();
		} catch (Exception e) {
			System.out.println("error in writing to file inside writeToIndexFile().");

		}

	}
	
	
	public static void findRecord(int new_age){
		
		try{
			
			File file = new File("IndexFile.txt");
	
			RandomAccessFile raf = new RandomAccessFile(file, "r");

			// Seek to the end of file
			int n = 4000;
			System.out.println("n= "+ n);
			System.out.println(file.length());
			raf.seek(file.length() - n);
			// Read it out.
			byte[] blockX = new byte[4000];
			raf.read(blockX);
			
			
			System.out.println("for sajjad specially "+ new String(blockX).length());
			
/*			fin = new FileInputStream(file);
			byte[] blockX = new byte[4000];
			
			while ((fin.read(blockX)) != -1) { // we read ONE BLOCK at a time

				System.out.println(new String(blockX));
				System.out.println();
			
//				 break;

			}
*/			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("inside findRecord()");
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

		for (int i = 0; i <= 81; i++) {
			bucketBlockIndexSize[i] = 3;
		}
		
		
		

		readMyFile();

		writeToIndexFile();

		long end = System.currentTimeMillis();

		System.out.println("Index File has been constructed...");
		System.out.println("Time taken = " + (end - start) + " ms");

		 Scanner sc = new Scanner(System.in);
		// System.out.println("1. Enter Age");
		// System.out.println("2. Enter Range Age");
		 int selectedOption = sc.nextInt();
		 
		 findRecord(selectedOption);
		 
		// switch(selectedOption){
		// case 1:

		// FIND(sc.nextInt());

		// break;
		// case 2:
		// asdasdasdasdad
		// break;
		// }

		System.out.println("Program terminated...");
		
		
		
		

	}
}// END
