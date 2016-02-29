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

	private static String fileNameData = "Person.txt";
	private static File file = new File(fileNameData);
	private static FileInputStream fin = null;
	private static byte[] readBlock = new byte[4000]; // one block 4 KB = 40													// records
	private static String block;
	private static int blockSearchKey = 1; // 4 bytes
	// private static String sin;
	private static int age = 0; // 4 bytes
	private static byte[][] buckets = new byte[82][4000]; // 324 KB
	private static int[] iBucket = new int[82]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[82]; // 4 x 81 = 324 bytes
	private static PrintWriter out;
	private static String stt;
	private static int[] bucketBlockIndexSize = new int[82];
	private static int blockPointer = 0;
	private static int[] bPointerForBuckets = new int[82];
	private static boolean firstTime = true;
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
			age = Integer.parseInt(block.substring(i * 100 + 39, i * 100 + 41)) - 18;
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
		} catch (Exception e) {
			System.out.println("Error inside freeBucket().");
		}
		for (int i = 0; i < 4000; i++) {
			buckets[bucketNumber][i] = ' ';
		}
		iBucket[bucketNumber] = 0;
		// System.out.println("blockPointer");
		bPointerForBuckets[age] = blockPointer;
		blockPointer++;
	}

	/**
	 * writes the index blocks into txt file
	 */
	public static void writeToIndexFile() {
		try {
			for (int i = 0; i <= 81; i++) {
				stt = new String(buckets[i]);
				out.print(stt);
			}
			out.close();
		} catch (Exception e) {
			System.out.println("error inside writeToIndexFile().");
		}
	}
/**
 * 
 * @param new_age
 */
	public static void findAllBlocksForAge(int new_age) {

		String strB = new String(findBlock(new_age, "IndexFile.txt"));
		int counter = 0;

		// System.out.println(strB);

		if (new_age == Integer.parseInt(strB.substring(0, 2))) {

			// System.out.println("YES");

			int indexSize = Integer.parseInt(strB.substring(2, 3));

			// System.out.println(i);
			
			
			String strPointer = strB.substring(3, 10);
			
			System.out.println("Pointer fetched = "+ strPointer);
			
			
			

			int j = 10;
			while (j < 4000 - indexSize) {

				// System.out.println(strB.substring(j, j+i));

				try {
					String a = strB.substring(j, j + indexSize);

					int c = indexSize;
					for (int b = 0; b < c;) {
						if (a.charAt(b) == ' ') {
							a = a.substring(1, c);
							c--;
						} else {
							break;
						}
					}
					// System.out.print(a);

					int x = Integer.parseInt(a, 16);

					// System.out.println(x);

					String bytesAsString = new String(findBlock(x, fileNameData), StandardCharsets.UTF_8);
					Pattern pattern = Pattern.compile("(\\d{9})([^\"]{15})([^\"]{15})(\\d{2})(\\d{10})([^\"]{49})");
					Matcher matcher = pattern.matcher(bytesAsString);

					if (firstTime) {
						System.out.println("SIN        FIRST_NAME       LAST_NAME        AGE  YEARLY_INCOME  ADDRESS");
						System.out.println(
								"-----------------------------------------------------------------------------------------------------------");
						firstTime = false;
					}
					while (matcher.find()) {
						if (matcher.group(4).compareToIgnoreCase(Integer.toString(new_age)) == 0) {
							counter++;
							System.out.print(matcher.group(1));
							System.out.print("  " + matcher.group(2));
							System.out.print("  " + matcher.group(3));
							System.out.print("  " + matcher.group(4));
							System.out.print("   " + matcher.group(5));
							System.out.println("     " + matcher.group(6));
						}
					}

					// System.exit(0);

				} catch (Exception e) {
					// System.out.println("error inside findAllBlocksForAge()");
				}

				j = j + indexSize;

			}

		}
		System.out.println("There are " + counter + " people with age " + (new_age) + ".");

	}

	/**
	 * @param new_index
	 * @param new_filename
	 * @return
	 */
	public static byte[] findBlock(int new_index, String new_filename) {
		byte[] blockX = new byte[4000];
		try {
			File file = new File(new_filename);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			int n = 4000;
			// System.out.println("n= " + n);
			// System.out.println(file.length());
			if (new_filename == "IndexFile.txt") {
				raf.seek(file.length() - (100 - new_index) * n);
			} else {
				raf.seek((new_index - 1) * n);
			}
			raf.read(blockX); // read given block
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error inside findRecord()");
		}
		return blockX;
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
		System.out.println("1. Enter Age");
		System.out.println("2. Enter Range Age");
		int selectedOption = sc.nextInt();
		switch (selectedOption) {
		case 1:
			System.out.print("Enter the age: ");
			findAllBlocksForAge(sc.nextInt());
			break;
		case 2:
			break;
		}
		sc.close();
		System.out.println("Program terminated...");
	}
}// END
