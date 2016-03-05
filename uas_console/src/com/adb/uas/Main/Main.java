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

	// private static String fileNameData =
	// "/Users/usbaitass/git/uas_console/test2/Test10000.txt";
	//private static String fileNameData = "/Users/usbaitass/git/uas_console/test2/Test20.txt";
	// private static String fileNameData =
	// "/Users/usbaitass/git/uas_console/test2/Test12.txt";
	private static String fileNameData = "Test30.txt";
	private static File file = new File(fileNameData);
	private static FileInputStream fin = null;
	private static byte[] readBlock = new byte[4000]; // 1 block
	private static byte[][] buckets = new byte[82][4000]; // 324 KB
	private static int[] iPosInBucket = new int[82]; // 4 x 81 = 324 bytes
	private static int[] prevBlockIndex = new int[82]; // 4 x 81 = 324 bytes
	private static int[] bucketBlockIndexSize = new int[82];
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

	/**
	 * reads the data from a file.
	 */
	public static void readDataFile() {
		try {
			System.out.println("file size = " + file.length() + " bytes.");
			System.out.println(file.length() / 100 + " records.");
			System.out.println("number of blocks required to READ = " + file.length() / 4000);

			fin = new FileInputStream(file);
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

			if (blockIndex > ((int) Math.pow(16, bucketBlockIndexSize[age]))) {
				System.out.println("HELLO WORLD "+ blockIndex);
				
				freeAllBuckets();
				
			}
			
			// checks if bucket is full, writes to file if it is.
			if (iPosInBucket[age] > 4000 - bucketBlockIndexSize[age] - 1) {
				freeBucket(age);
			}

			

			initializeBucket();

			// writes the block indexes in bucket
			int i = 0;
			for (; i < bucketBlockIndexSize[age] - blockIndexHex.length(); i++) {
				byte b = ' ';
				buckets[age][iPosInBucket[age] + i] = b;
			}

			for (int j = 0; j < blockIndexHex.length(); j++) {
				byte b = (byte) blockIndexHex.charAt(j);
				buckets[age][iPosInBucket[age] + i + j] = b;
			}

			iPosInBucket[age] += bucketBlockIndexSize[age];
		}

		prevBlockIndex[age] = blockIndex;

	}
	
	
	public static void freeAllBuckets(){
		for(int i=0; i<82; i++){
			//System.out.println(i+" ipos="+ iPosInBucket[i]+ " pointer="+ bPointerForBuckets[i]);
			bucketBlockIndexSize[i]++;
			freeBucket(i);
			initializeBucket();
			}
	}

	public static void initializeBucket() {
		// assign bucket info, pointer in 1st 10 bytes.
		if (iPosInBucket[age] == 0) {
			String strAge = Integer.toString(age + 18);
			buckets[age][0] = (byte) strAge.charAt(0);
			buckets[age][1] = (byte) strAge.charAt(1);

			String strSize = Integer.toString(bucketBlockIndexSize[age]);
			buckets[age][2] = (byte) strSize.charAt(0);

			String strPointer = Integer.toHexString(bPointerForBuckets[age]);

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

			iPosInBucket[age] += 10;
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
			tempStr = new String(buckets[bucketNumber]);
			//out.println(bucketOverflowCounter +" index");
			out.print(tempStr);
			//System.out.println("bucket number = "+ (bucketNumber+18));
			//out.println();
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
			// System.out.println("error inside writeToIndexFile().");
		}
	}

	/**
	 * This method finds all blocks for a given age
	 * 
	 * @param new_age
	 *            given age
	 */
	public static void findAllBlocksForAge(int new_age) {
		int index = new_age - 18;
		// check if the bucket for a certain age has any records.
		if (buckets[index].length > 0 && buckets[index][0] != 0) {
			recursiveMethod(buckets[index], new_age);
		} else {
			System.out.println("There are no records with age " + new_age + " in the file.");
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
		String tempStrBlock = new String(block);
		String strPointer = tempStrBlock.substring(3, 10);
		if (strPointer.compareTo("0000000") == 0) {
			readBlockIndexesFromBucket(block, new_age);
			nIO++;
			return true;
		} else {
			if (recursiveMethod(findBlock(decodePointer(strPointer), "IndexFile.txt"), new_age)) {
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
		int indexSize = Integer.parseInt(strB.substring(2, 3));
		int j = 10;
		while (j < 4000 - indexSize) {
			try {
				String sss = strB.substring(j, j + indexSize);
				String tempS = "";
				for (int k = 0; k < indexSize; k++) {
					tempS += " ";
				}
				if (new_block[j] != 0 && sss.compareTo(tempS) != 0) {
					int x = decodePointer(sss);
					readRecordsFromBlock(x, new_age);
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			j = j + indexSize;
		}
	}

	/**
	 * This method finds the block in file by given index
	 * 
	 * @param new_index
	 *            index of the block
	 * @param new_filename
	 *            from where to read
	 * @return the found block
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
			// e.printStackTrace();
			// System.out.println("error inside findRecord()");
		}
		nIO++;
		// System.out.println(new String(blockX));
		return blockX;
	}

	// private static int count = 0;
	/**
	 * This method decodes the Pointer from byte string to integer
	 * 
	 * @param new_pointer
	 *            Pointer string
	 * @return decoded decimal integer
	 */
	public static int decodePointer(String new_pointer) {

		String a = new_pointer;

		// if(searching) System.out.println("a="+a);

		int c = new_pointer.length();
		for (int b = 0; b < c;) {
			if (a.charAt(0) == '0' || a.charAt(0) == ' ') {
				a = a.substring(1, c);
				c--;

				// if(searching) System.out.println("a="+a);

			} else {
				break;
			}
		}
		// if(searching) System.out.println("a="+a);
		// System.exit(0);
		return Integer.parseInt(a, 16);
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
		String bytesAsString = new String(findBlock(pos, fileNameData), StandardCharsets.UTF_8);
		Pattern pattern = Pattern.compile("(\\d{9})([^\"]{15})([^\"]{15})(\\d{2})(\\d{10})([^\"]{49})");
		Matcher matcher = pattern.matcher(bytesAsString);

		if (firstTime) {
			out2.println("N   SIN        FIRST_NAME       LAST_NAME        AGE  YEARLY_INCOME  ADDRESS");
			out2.println(
					"-----------------------------------------------------------------------------------------------------------");
			firstTime = false;
		}

		while (matcher.find()) {
			if (matcher.group(4).compareToIgnoreCase(Integer.toString(new_age)) == 0) {
				countPeople++;
				out2.print(countPeople + ". ");
				out2.print(matcher.group(1));
				out2.print("  " + matcher.group(2));
				out2.print("  " + matcher.group(3));
				out2.print("  " + matcher.group(4));
				out2.print("   " + matcher.group(5));
				out2.println("     " + matcher.group(6));
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
			long start;
			long end;
			System.out.println("Program started...");
			// if(!indexConstructed){
			// initialize the first block index size for 3 bytes
			for (int i = 0; i <= 81; i++) {
				bucketBlockIndexSize[i] = 3;
			}

			start = System.currentTimeMillis();

			// create the index file
			out = new PrintWriter("indexFile.txt");
			// read the Data file
			readDataFile();
			// write index to file
			writeToIndexFile(); // back up
			out.close();

			end = System.currentTimeMillis();
			System.out.println("Index File has been constructed...");
			System.out.println("Time taken = " + (end - start) + " ms");
			System.out.println("Number of I/O WRITE = " + (nIO - file.length() / 4000));
			System.out.println("Number of blocks to store the index = " + ((nIO - file.length() / 4000) + 82));
			System.out.println("Total number of I/O = " + nIO);
			// }
			nIO = 0;

			// menu option for different executions
			Scanner sc = new Scanner(System.in);
			System.out.println("1. For one age:");
			System.out.println("2. For range ages:");

			int selectedOption = sc.nextInt();
			out2 = new PrintWriter("Output.txt");

			switch (selectedOption) {
			case 1:
				System.out.print("Enter the age 18-99: ");
				int tempN = sc.nextInt();
				start = System.currentTimeMillis();
				findAllBlocksForAge(tempN);
				end = System.currentTimeMillis();
				System.out.println("Number of people of age " + tempN + " = " + countPeople);
				System.out.println("The Average yearly Income = " + (yearlyIncomeSum / countPeople));
				System.out.println("Time taken = " + (end - start) + " ms");
				// -1 because first bucket is stored in Main Memory
				System.out.println("Number of I/O = " + (nIO - 1));
				yearlyIncomeSum = 0;
				countPeople = 0;

				// System.out.println("count="+count);
				break;
			case 2:
				for (int i = 18; i <= 99; i += 10) {
					findAllBlocksForAge(i);
				}
				break;
			}
			sc.close();
			out2.close();

			System.out.println("Program terminated...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}// END
