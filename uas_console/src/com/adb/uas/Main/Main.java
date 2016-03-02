package com.adb.uas.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test class for various test codes to run
 * 
 * @author usbaitass
 *
 */
public class Main {

	private static String fileNameData = "Test15.txt";
	private static File file = new File(fileNameData);
	private static FileInputStream fin = null;
	private static byte[] readBlock = new byte[4000]; // 1 block
	private static byte[][] buckets = new byte[82][4000]; // 324 KB
	private static int[] iBucket = new int[82]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[82]; // 4 x 81 = 324 bytes
	private static int[] bucketBlockIndexSize = new int[82];
	private static int[] bPointerForBuckets = new int[82];
	private static String strBlock;
	private static int blockSearchKey = 1; // 4 bytes
	private static int age = 0; // 4 bytes
	private static PrintWriter out;
	private static String stt;
	private static int blockPointerCounter = 0;
	private static boolean firstTime = true;
	private static String searchKeyHex;
	private static int countPeople = 0;
	private static RandomAccessFile raf;
	private static long yearlyIncomeSum = 0;
	private static int gSelectedOption = 0;
	private static String sin;
	private static PrintWriter out3;
	

	/**
	 * reads the data from a file.
	 */
	public static void readDataFile() {
		try {
			System.out.println("file size = " + file.length() + " bytes.");
			System.out.println(file.length() / 100 + " records.");
			
			System.out.println("number of blocks required = "+ file.length()/4000);
			

			fin = new FileInputStream(file);

			while ((fin.read(readBlock)) != -1) { // we read ONE BLOCK at a time
				processBlock();
				blockSearchKey++; // assign search key to block
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
		strBlock = new String(readBlock);
		decToHexKey();

		for (int i = 0; i < 40; i++) {
		//	 sin = strBlock.substring(i * 100 + 0, i * 100 + 9);
			age = Integer.parseInt(strBlock.substring(i * 100 + 39, i * 100 + 41)) - 18;
		//	 System.out.println("sin = " + sin + " age = " + (age+18));
			InputSearchKeyIntoBucket();
		}
	}

	/**
	 * converts the decimal block index into hex
	 */
	public static void decToHexKey() {
		searchKeyHex = Integer.toHexString(blockSearchKey);
	}

	/**
	 * puts hashed key into appropriate bucket number according to age
	 */
	public static void InputSearchKeyIntoBucket() {

		if (prevBlockIndex[age] != blockSearchKey) {

			if (iBucket[age] > 4000 - bucketBlockIndexSize[age] - 1) {
				freeBucket(age);
			}

			if (iBucket[age] == 0) {
				String strAge = Integer.toString(age + 18);
				buckets[age][0] = (byte) strAge.charAt(0);
				buckets[age][1] = (byte) strAge.charAt(1);

				String strSize = Integer.toString(bucketBlockIndexSize[age]);
				buckets[age][2] = (byte) strSize.charAt(0);

				String strPointer = Integer.toHexString(blockPointerCounter);

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
		} catch (Exception e) {
			System.out.println("Error inside freeBucket().");
		}

		for (int i = 0; i < 4000; i++) {
			buckets[bucketNumber][i] = ' ';
		}

		iBucket[bucketNumber] = 0;
		// System.out.println("blockPointer");
		bPointerForBuckets[age] = blockPointerCounter;
		blockPointerCounter++;
	}

	/**
	 * writes the index blocks into txt file
	 */
	public static void writeToIndexFile() {
		try {
			PrintWriter out2 = new PrintWriter("indexFileZERO.txt");

			for (int i = 0; i <= 81; i++) {
				stt = new String(buckets[i]);
				out2.print(stt);
			}

			out2.close();
		} catch (Exception e) {
			System.out.println("error inside writeToIndexFile().");
		}
	}

	/**
	 * 
	 * @param new_age
	 */
	public static void findAllBlocksForAge(int new_age) {

		int index = new_age - 18;

		// check if the bucket for a certain age has any records.
		if (buckets[index].length > 0 && buckets[index][0] != 0) {

			System.out.println(recursiveMethod(buckets[index], new_age));

		} else {
			System.out.println("There are no records with age " + new_age + " in the file.");
		}

	}

	/**
	 * 
	 * @param block
	 * @param new_age
	 * @return
	 */
	public static boolean recursiveMethod(byte[] block, int new_age) {

		String tempStrBlock = new String(block);

		String strPointer = tempStrBlock.substring(3, 10);
		if (strPointer.compareTo("0000000") == 0) {
			readBlockIndexesFromBucket(block, new_age);
			return true;
		} else {
			if(recursiveMethod(findBlock(decodePointer(strPointer), "IndexFile.txt"), new_age)){
				readBlockIndexesFromBucket(block, new_age);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param block
	 * @param new_age
	 */
	public static void readBlockIndexesFromBucket(byte[] block, int new_age) {

		String strB = new String(block);
		int indexSize = Integer.parseInt(strB.substring(2, 3));

		int j = 10;
		while (j < 4000 - indexSize) {
			try {
				int x = decodePointer(strB.substring(j, j + indexSize));
				readRecordsFromBlock(x, new_age);
			} catch (Exception e) {
		//		 System.out.println("error inside findAllBlocksForAge()");
			}
			j = j + indexSize;
		}
	}

	/**
	 * @param new_index
	 * @param new_filename
	 * @return
	 */
	public static byte[] findBlock(int new_index, String new_filename) {
		byte[] blockX = new byte[4000];
		try {
			file = new File(new_filename);
			raf = new RandomAccessFile(file, "r");
			int n = 4000;

			raf.seek((new_index - 1) * n);
			raf.read(blockX); // read given block
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error inside findRecord()");
		}
		return blockX;
	}

	/**
	 * 
	 * @param new_pointer
	 * @return
	 */
	public static int decodePointer(String new_pointer) {
		String a = new_pointer;
		int c = new_pointer.length();
		for (int b = 0; b < c;) {
			if (a.charAt(b) == '0' || a.charAt(b) == ' ') {
				a = a.substring(1, c);
				c--;
			} else {
				break;
			}
		}
		return Integer.parseInt(a, 16);
	}

	/**
	 * 
	 * @param pos
	 * @param new_age
	 */
	public static void readRecordsFromBlock(int pos, int new_age) {
		String bytesAsString = new String(findBlock(pos, fileNameData), StandardCharsets.UTF_8);
		Pattern pattern = Pattern.compile("(\\d{9})([^\"]{15})([^\"]{15})(\\d{2})(\\d{10})([^\"]{49})");
		Matcher matcher = pattern.matcher(bytesAsString);
		if (gSelectedOption == 1) {
			if (firstTime) {
				System.out.println("N   SIN        FIRST_NAME       LAST_NAME        AGE  YEARLY_INCOME  ADDRESS");
				System.out.println(
						"-----------------------------------------------------------------------------------------------------------");
				firstTime = false;
			}
		}
		while (matcher.find()) {
			if (matcher.group(4).compareToIgnoreCase(Integer.toString(new_age)) == 0) {
				countPeople++;
				if (gSelectedOption == 1) {
					out3.print(countPeople + ". ");
					out3.print(matcher.group(1));
					out3.print("  " + matcher.group(2));
					out3.print("  " + matcher.group(3));
					out3.print("  " + matcher.group(4));
					out3.print("   " + matcher.group(5));
					out3.println("     " + matcher.group(6));

				/*	System.out.print(countPeople + ". ");
					System.out.print(matcher.group(1));
					System.out.print("  " + matcher.group(2));
					System.out.print("  " + matcher.group(3));
					System.out.print("  " + matcher.group(4));
					System.out.print("   " + matcher.group(5));
					System.out.println("     " + matcher.group(6));
					*/
				} else if (gSelectedOption == 3) {
					yearlyIncomeSum += Integer.parseInt(matcher.group(5));
				}
			}
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

		// create the index file
		try {
			out = new PrintWriter("indexFile.txt");
		} catch (Exception e) {
			System.out.println("Error in writing file inside main.");
		}

		// initialize the first block index size for 3 bytes
		for (int i = 0; i <= 81; i++) {
			bucketBlockIndexSize[i] = 3;
		}

		// read the Data file
		readDataFile();
		// write index to file
		writeToIndexFile();
		out.close();

		long end = System.currentTimeMillis();
		System.out.println("Index File has been constructed...");
		System.out.println("Time taken = " + (end - start) + " ms");

		// menu option for different executions
		Scanner sc = new Scanner(System.in);
		System.out.println("1. Enter Age:");
		System.out.println("2. Enter Range Age:");
		System.out.println("3. Output average salaries for all group ages.");

		int selectedOption = sc.nextInt();
		gSelectedOption = selectedOption;
		try{
		out3 = new PrintWriter("Output.txt");
		}catch(Exception e){}
		switch (selectedOption) {
		case 1:
			System.out.print("Enter the age: ");
			findAllBlocksForAge(sc.nextInt());
			break;
		case 2:
			int youngest = sc.nextInt();
			int olderst = sc.nextInt();
			for (int i = youngest; i <= olderst; i++) {
				findAllBlocksForAge(i);
			}
			break;
		case 3:
			System.out.print("Enter the age: ");
			findAllBlocksForAge(sc.nextInt());
			if (countPeople > 0) {
				System.out.println("The Average yearly Income = " + ((int) yearlyIncomeSum / countPeople));
			}
			break;
		}
		sc.close();
		out3.close();

		System.out.println("Program terminated...");
	}
}// END
