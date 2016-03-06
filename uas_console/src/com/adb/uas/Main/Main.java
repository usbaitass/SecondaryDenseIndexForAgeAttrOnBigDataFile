package com.adb.uas.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

	// private static String fileNameData =
	// "/Users/usbaitass/git/uas_console/test2/Test10000.txt";
	// private static String fileNameData =
	// "/Users/usbaitass/git/uas_console/test2/Test20.txt";
	// private static String fileNameData =
	// "/Users/usbaitass/git/uas_console/test2/Test12.txt";
	// private static String fileNameData = "Person.txt";
	private static String fileNameData = "/Users/usbaitass/Public/person_fnl/person_fnl.txt";
	private static File fileData = new File(fileNameData);
	private static FileInputStream fin = null;
	private static byte[] readBlock = new byte[4000]; // 1 block
	private static byte[][] buckets = new byte[82][4000]; // 324 KB
	private static int[] iPosInBucket = new int[82]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[82]; // 4 x 81 = 324 bytes
	private static int bucketBlockIndexSize;
	private static int[] bPointerForBuckets = new int[82];
	private static String strBlock;
	private static int blockIndex = 1; // 4 bytes
	private static int age = 0; // 4 bytes
	private static String tempStr;
	private static int bucketOverflowCounter = 1;
	private static boolean firstTime = true;
	private static String blockIndexHex;
	private static RandomAccessFile raf;
	private static PrintWriter out;
	private static PrintWriter out2;
	private static long yearlyIncomeSum = 0;
	private static int countPeople = 0;
	private static int nIO = 0;
	// private static boolean indexConstructed = true;
	private static Pattern pattern = Pattern.compile("(\\d{9})([^\"]{15})([^\"]{15})(\\d{2})(\\d{10})([^\"]{49})");
	private static Matcher matcher;
	private static String bytesAsString;
	private static int selectedOption;

	private static File fileIndex = new File("IndexFile.txt");
	private static RandomAccessFile rafData;

	/**
	 * reads the data from a file.
	 */
	public static void readDataFile() {
		try {
			fin = new FileInputStream(fileData);
			while ((fin.read(readBlock)) != -1) { // we read ONE BLOCK at a time
				nIO++; // count number of I/O READS
				processBlock();
				blockIndex++;
			}
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
		// for each record in a block
		for (int i = 0; i < 40; i++) {
			age = Integer.parseInt(strBlock.substring(i * 100 + 39, i * 100 + 41)) - 18;
			InputSearchKeyIntoBucket();
		}
	}

	/**
	 * converts the decimal block index into hex
	 */
	public static void decToHexKey() {
		blockIndexHex = Integer.toHexString(blockIndex);
	}

	/**
	 * puts hashed key into appropriate bucket number according to age
	 */
	public static void InputSearchKeyIntoBucket() {
		// prevents the same block indexes in one bucket
		if (prevBlockIndex[age] != blockIndex) {
			// checks if bucket is full, writes to file if it is.
			if (iPosInBucket[age] > 4000 - bucketBlockIndexSize - 1) {
				freeBucket(age);
			}
			// assign bucket info, pointer in 1st 10 bytes.
			if (iPosInBucket[age] == 0) {
				tempStr = Integer.toString(age + 18);
				buckets[age][0] = (byte) tempStr.charAt(0);
				buckets[age][1] = (byte) tempStr.charAt(1);

				tempStr = Integer.toHexString(bPointerForBuckets[age]);

				int d = tempStr.length();
				for (int i = 0; i < 8 - d; i++) {
					tempStr = '0' + tempStr;
				}

				buckets[age][2] = (byte) tempStr.charAt(0);
				buckets[age][3] = (byte) tempStr.charAt(1);
				buckets[age][4] = (byte) tempStr.charAt(2);
				buckets[age][5] = (byte) tempStr.charAt(3);
				buckets[age][6] = (byte) tempStr.charAt(4);
				buckets[age][7] = (byte) tempStr.charAt(5);
				buckets[age][8] = (byte) tempStr.charAt(6);
				buckets[age][9] = (byte) tempStr.charAt(7);

				iPosInBucket[age] += 10;
			}
			// writes the block indexes in bucket
			int i = 0;
			for (; i < bucketBlockIndexSize - blockIndexHex.length(); i++) {
				byte b = ' ';
				buckets[age][iPosInBucket[age] + i] = b;
			}

			for (int j = 0; j < blockIndexHex.length(); j++) {
				byte b = (byte) blockIndexHex.charAt(j);
				buckets[age][iPosInBucket[age] + i + j] = b;
			}

			iPosInBucket[age] += bucketBlockIndexSize;
		}

		prevBlockIndex[age] = blockIndex;

	}

	/**
	 * writes the bucket to the file.
	 * 
	 * @param bucketNumber
	 *            bucket index
	 */
	public static void freeBucket(int bucketNumber) {

		try {
			tempStr = new String(buckets[bucketNumber]);
			out.print(tempStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// reinitialize bucket
		for (int i = 0; i < 4000; i++) {
			buckets[bucketNumber][i] = ' ';
		}

		iPosInBucket[bucketNumber] = 0;
		bPointerForBuckets[age] = bucketOverflowCounter;
		bucketOverflowCounter++; // count number of overflow buckets
		nIO++; // count number of I/O WRITES
	}

	/**
	 * writes the sorted buckets into txt file (back up)
	 */
	public static void writeToIndexFile() {
		try {
			PrintWriter tempOut = new PrintWriter("indexFileZERO.txt");
			for (int i = 0; i <= 81; i++) {
				tempStr = new String(buckets[i]);
				tempOut.print(tempStr);
			}
			tempOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method finds all blocks for a given age
	 * 
	 * @param new_age
	 *            given age
	 */
	public static void findAllBucketsForAge(int new_age) {
		try {
			raf = new RandomAccessFile(fileIndex, "r");
			rafData = new RandomAccessFile(fileData, "r");

			int index = new_age - 18;
			// check if the bucket for a certain age has any records.
			if (buckets[index].length > 0 && buckets[index][0] != 0) {
				recursiveMethod(buckets[index], new_age);
			} else {
				System.out.println("There are no records with age " + new_age + " in the file.");
			}
			raf.close();
			rafData.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is recursive, for each bucket for a given age, it retrieves
	 * all buckets from an index file using pointer and terminates when reaches
	 * very first bucket
	 * 
	 * @param block
	 * @param new_age
	 * @return
	 */
	public static boolean recursiveMethod(byte[] block, int new_age) {
		tempStr = new String(block);
		tempStr = tempStr.substring(2, 10);
		if (tempStr.compareTo("00000000") == 0) {
			readBlockIndexesFromBucket(block, new_age);
			return true;
		} else {
			if (recursiveMethod(findBucket(decodePointer(tempStr)), new_age)) {
				readBlockIndexesFromBucket(block, new_age);
				return true;
			}
		}
		return false;
	}

	/**
	 * This method reads data from given block
	 * 
	 * @param block
	 *            given block
	 * @param new_age
	 *            given age
	 */
	public static void readBlockIndexesFromBucket(byte[] new_block, int new_age) {
		String strB = new String(new_block);
		int j = 10;
		String sss = "";
		while (j < 4000 - bucketBlockIndexSize) {
			try {
				sss = strB.substring(j, j + bucketBlockIndexSize);
				sss = sss.replaceAll(" ", "");
				if (sss.length() > 0) {
					int x = decodePointer(sss);
					readRecordsFromBlock(x, new_age);
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			j += bucketBlockIndexSize;
		}
	}

	public static byte[] findBucket(int new_index) {
		byte[] blockX = new byte[4000];
		try {
			raf.seek(((long) new_index - 1) * 4000);
			raf.read(blockX);
		} catch (Exception e) {
			e.printStackTrace();
		}
		nIO++;
		return blockX;
	}

	public static byte[] findDataBlock(int new_index) {
		try {
			rafData.seek(((long) new_index - 1) * 4000);
			rafData.read(readBlock);
		} catch (Exception e) {
			e.printStackTrace();
		}
		nIO++;
		return readBlock;
	}

	public static int decodePointer(String new_pointer) {
		new_pointer = new_pointer.replace(" ", "");
		new_pointer = new_pointer.replaceAll("^0+", "");
		return Integer.parseInt(new_pointer, 16);
	}

	/**
	 * This method reads all 40 records from given block and prints only the
	 * ones which age is same as given age
	 * 
	 * @param pos
	 *            block position in the file
	 * @param new_age
	 *            given age
	 */
	public static void readRecordsFromBlock(int pos, int new_age) {
		bytesAsString = new String(findDataBlock(pos), StandardCharsets.UTF_8);

		matcher = pattern.matcher(bytesAsString);

		if (firstTime) {
			out2.println("N   SIN        FIRST_NAME       LAST_NAME        AGE  YEARLY_INCOME  ADDRESS");
			out2.println(
					"-----------------------------------------------------------------------------------------------------------");
			firstTime = false;
		}

		while (matcher.find()) {
			if (matcher.group(4).compareToIgnoreCase(Integer.toString(new_age)) == 0) {
				countPeople++;
				if (selectedOption == 1) {
					out2.print(countPeople + ". ");
					out2.print(matcher.group(1));
					out2.print(" " + matcher.group(2));
					out2.print(" " + matcher.group(3));
					out2.print(" " + matcher.group(4));
					out2.print(" " + matcher.group(5));
					out2.println(" " + matcher.group(6));
				}
				yearlyIncomeSum += Integer.parseInt(matcher.group(5));
			}
		}

	}

	/**
	 * Main Method which initiates the program
	 * 
	 * @param args
	 *            standard params
	 */
	public static void main(String[] args) {
		// findBlock(536877, fileNameData);
		try {
			System.out.println("file size = " + fileData.length() + " bytes.");
			System.out.println(fileData.length() / 100 + " records.");
			System.out.println("number of blocks required to READ = " + fileData.length() / 4000);
			System.out.println("Program started...");

			bucketBlockIndexSize = Long.toHexString(fileData.length() / 4000).length();

			long start = System.currentTimeMillis();

			// create the index file
			out = new PrintWriter("indexFile.txt");
			// read the Data file
			readDataFile();
			// write index to file
			// writeToIndexFile(); // back up
			out.close();

			long end = System.currentTimeMillis();
			System.out.println("Index File has been constructed...");
			System.out.println("Time taken = " + (end - start) + " ms");
			System.out.println("Number of I/O WRITE = " + (nIO - fileData.length() / 4000));
			System.out.println("Number of blocks to store the index = " + ((nIO - fileData.length() / 4000) + 82));
			System.out.println("Total number of I/O = " + nIO);
			nIO = 0;

			// menu option for different executions
			Scanner sc = new Scanner(System.in);
			
			

			while (selectedOption != -1) {
				System.out.println("1. For one age:");
				System.out.println("2. Print average income for every 10 ages:");
				selectedOption = sc.nextInt();
				switch (selectedOption) {
				case 1:
					out2 = new PrintWriter("Output.txt");
					System.out.print("Enter the age 18-99: ");
					int tempN = sc.nextInt();
					start = System.currentTimeMillis();
					findAllBucketsForAge(tempN);
					end = System.currentTimeMillis();
					System.out.println("Number of people of age " + tempN + " = " + countPeople);
					System.out.println("The Average yearly Income = " + (yearlyIncomeSum / countPeople));
					System.out.println("Time taken = " + (end - start) + " ms");
					// -1 because first bucket is stored in Main Memory
					System.out.println("Number of I/O = " + (nIO - 1));
					yearlyIncomeSum = 0;
					countPeople = 0;

					// System.out.println("count="+count);
					out2.close();
					break;
				case 2:
					int counter = 0;
					long sum = 0;
					long countPeople2 = 0;
					String str = "";
					for (int i = 18; i <= 19; i++) {
						findAllBucketsForAge(i);

						countPeople2 += countPeople;
						sum += yearlyIncomeSum;
						if (counter == 1 && i < 20) {
							str += " In the range of " + i + " - " + (i + counter) + " there are " + countPeople2
									+ " and average salary is " + sum / countPeople2 + "\r\n";
							countPeople2 = 0;
							sum = 0;
							counter = 0;
						}
						if (i == 30) {
							str += " In the range of " + i + " - " + (i + counter) + " there are " + countPeople2
									+ " and average salary is " + sum / countPeople2 + "\r\n";

						}

						counter++;
					}
					if (str != "") {
						System.out.println(str);
					}
					break;
				}
			}
			sc.close();

			System.out.println("Program terminated...");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}// END
