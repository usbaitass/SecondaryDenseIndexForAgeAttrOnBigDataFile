package test2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class test02 {

	public static void main(String[] args){
		
		//File file = new File("/Users/usbaitass/git/uas_console/test2/Test10000.txt");
		//File file = new File("/Users/usbaitass/git/uas_console/test2/Test20.txt");
		File file = new File("/Users/usbaitass/git/uas_console/uas_console/Test30.txt");
		try{
		FileInputStream fin = new FileInputStream(file);
		int age = 18;
		int countPeople = 0;
		int r_age = 0;
		
		byte[] block = new byte[4000];
		String strBlock;
		long start = System.currentTimeMillis();
		while(fin.read(block) != -1){
			strBlock = new String(block);
			
			for(int i=0; i<40; i++){
				
				r_age = Integer.parseInt(strBlock.substring(i * 100 + 39, i * 100 + 41));
				if(age == r_age){
					System.out.println(strBlock.substring(i*100, i*100+9));
					countPeople++;
				}	
			}
			
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken = "+ (end - start) +"ms");
		System.out.println(countPeople);
		
		
		fin.close();
		}catch(IOException e){
			System.out.println("error inside Main.");
		}
	}
	
	
}
