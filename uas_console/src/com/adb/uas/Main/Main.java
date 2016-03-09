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

	private static String fileNameData = "C:\\Users\\u_baitas\\Downloads\\person_fnl.txt";
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
	private static Pattern pattern = Pattern.compile("(\\d{9})([^\"]{15})([^\"]{15})(\\d{2})(\\d{10})([^\"]{49})");
	private static Matcher matcher;
	private static String bytesAsString;
	private static int selectedOption;

	private static File fileIndex = new File("C:\\Users\\u_baitas\\Downloads\\IndexFile.txt");
	private static RandomAccessFile rafData;

	/**
	 * reads the data blocks from a data file.
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
	 * This method process every block that was read
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
	 * converts the decimal block index into hexadecimal string
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
				// put Zero's in front of a pointer string
				tempStr = String.format("%0" + (8 - tempStr.length()) + "d%s", 0, tempStr);

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
				buckets[age][iPosInBucket[age] + i] = (byte) ' ';
			}

			for (int j = 0; j < blockIndexHex.length(); j++) {
				buckets[age][iPosInBucket[age] + i + j] = (byte) blockIndexHex.charAt(j);
			}

			iPosInBucket[age] += bucketBlockIndexSize;
		}

		prevBlockIndex[age] = blockIndex;

	}

	/**
	 * writes the overflow bucket to the index file.
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
		buckets[bucketNumber] = null;
		buckets[bucketNumber] = new byte[4000];

		iPosInBucket[bucketNumber] = 0;
		bPointerForBuckets[age] = bucketOverflowCounter;
		bucketOverflowCounter++; // count index of overflowed buckets
		nIO++; // increment I/O WRITEs each time a bucket is written
	}

	/**
	 * writes the sorted buckets into Output.txt file (back up)
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
	 * This method initiates the search for all records of a given age
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
	 *            bucket
	 * @param new_age
	 *            given age
	 * @return true if reaches the first Overflowed bucket safely
	 */
	public static boolean recursiveMethod(byte[] block, int new_age) {
		String tempStr1 = new String(block);
		String tempStr2 = tempStr1.substring(2, 10);
		tempStr1 = null;
		System.gc();
		if (tempStr2.compareTo("00000000") == 0) {
			tempStr2 = null;
			readBlockIndexesFromBucket(block, new_age);
			block = null;
			return true;
		} else {
			if (recursiveMethod(findBucket(decodePointer(tempStr2)), new_age)) {
				tempStr2 = null;
				readBlockIndexesFromBucket(block, new_age);
				block = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * This method reads data block indexes from a given bucket
	 * 
	 * @param block
	 *            given bucket
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
				// sss = sss.replaceAll(" ", "");
				sss = sss.trim();
				sss = sss.replaceAll("^\\s+", "");
				sss = sss.replaceAll("(^ )|( $)", "");

				// System.out.println("sss:"+sss +",Length:"+sss.length());
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

	/**
	 * This method finds the bucket in index file
	 * 
	 * @param new_index
	 *            bucket position in the file
	 * @return found bucket
	 */
	public static byte[] findBucket(int new_index) {
		byte[] blockX = new byte[4000];
		try {
			raf.seek(((long) new_index - 1) * 4000);
			raf.read(blockX);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// increment I/O each time bucket is READ from index file
		nIO++;
		return blockX;
	}

	/*
	 * This method finds the data block in the data file
	 * 
	 * @param new_index block position
	 * 
	 * @return found data block
	 */
	public static byte[] findDataBlock(int new_index) {
		try {
			rafData.seek(((long) new_index - 1) * 4000);
			rafData.read(readBlock);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// increment I/O each time data block is READ from data file
		nIO++;
		return readBlock;
	}

	/**
	 * This method decodes the hexadecimal pointer string to decimal value
	 * 
	 * @param new_pointer
	 *            Hexadecimal string
	 * @return decimal integer
	 */
	public static int decodePointer(String new_pointer) {
		new_pointer = new_pointer.replace(" ", "");
		new_pointer = new_pointer.replaceAll("^0+", "");
		return Integer.parseInt(new_pointer, 16);
	}

	/**
	 * This method reads all 40 records from a given block and prints only the
	 * ones which age is same as the given age
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
			// out2.println("N SIN FIRST_NAME LAST_NAME AGE YEARLY_INCOME
			// ADDRESS");
			// out2.println(
			// "-----------------------------------------------------------------------------------------------------------");
			firstTime = false;
		}

		while (matcher.find()) {
			if (matcher.group(4).compareToIgnoreCase(Integer.toString(new_age)) == 0) {
				countPeople++;
				if (selectedOption == 1) {
					// out2.print(countPeople + ". ");
					// out2.print(matcher.group(1));
					// out2.print(" " + matcher.group(2));
					// out2.print(" " + matcher.group(3));
					// out2.print(" " + matcher.group(4));
					// out2.print(" " + matcher.group(5));
					// out2.println(" " + matcher.group(6));
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
		try {
			System.out.println("file size = " + fileData.length() + " bytes.");
			System.out.println(fileData.length() / 100 + " records.");
			System.out.println("number of blocks required to READ = " + fileData.length() / 4000);
			System.out.println("Program started...");

			bucketBlockIndexSize = Long.toHexString(fileData.length() / 4000).length();

			long start = System.currentTimeMillis();

			// create the index file
			out = new PrintWriter("C:\\Users\\u_baitas\\Downloads\\indexFile.txt");
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
				System.out.println("2. Print average income and count of people for range queries:");
				selectedOption = sc.nextInt();
				switch (selectedOption) {
				case 1:
					nIO = 0;
					yearlyIncomeSum = 0;
					countPeople = 0;
					out2 = new PrintWriter("Output.txt");

					System.out.print("Enter the age 18-99: ");
					int tempN = sc.nextInt();
					start = System.currentTimeMillis();

					findAllBucketsForAge(tempN);

					end = System.currentTimeMillis();
					System.out.println("Number of people of age " + tempN + " = " + countPeople);
					System.out.println("The Average yearly Income = " + (yearlyIncomeSum / (double) countPeople));
					System.out.println("Time taken = " + (end - start) + " ms");
					// -1 because first bucket is stored in Main Memory
					System.out.println("Number of I/O = " + (nIO - 1));
					out2.close();
					break;
				case 2:
					long sum = 0;
					long countPeople2 = 0;
					countPeople = 0;
					yearlyIncomeSum = 0;
					String str = "";
					nIO = 0;
					int youngest = 18;
					int oldest = 99;
					System.out.println("Enter youngest age in query:");
					youngest = sc.nextInt();
					System.out.println("Enter oldest age in query:");
					oldest = sc.nextInt();
					start = System.currentTimeMillis();
					
					for (int i = 18; i <= 27; i++) {
						findAllBucketsForAge(i);
						countPeople2 += countPeople;
						sum += yearlyIncomeSum;
						countPeople = 0;
						yearlyIncomeSum = 0;
					}
					str += " In the range of " + youngest + " - " + oldest + " there are " + countPeople2
							+ " and average salary is " + (double)sum / countPeople2 + "\r\n";
					end = System.currentTimeMillis();	
					if (str != "") {
						System.out.println(str);
					}
					System.out.println("Time taken = " + (end - start) + " ms");
					// the initial bucket for each age are stored in main memory
					// therefore we do not count those I/Os
					System.out.println("Number of I/O = " + (nIO - (oldest-youngest)));
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
