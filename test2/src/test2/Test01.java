package test2;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Test01 {
	
	
	public static void main(String[] args){
		
		System.out.println("Program started...");

		File file = new File("Test111.txt");
		
		try{
		PrintWriter out = new PrintWriter(file);
		
		for(int i=1; i<=100000000; i++){
			
			// PRINTS SIN
			int x = i;
			for(int j=0; j<8; j++){
				
				if(x/10 == 0){
					out.print('0');
				}
				x = x/10;
				
			}
			out.print(i);
			
			// PRINTS NAME and SURNAME
			Random r = new Random();
			String str = "abcdefghiklmnopqrstvwxyz";
			for(int k=0; k<30; k++){
				out.print(str.charAt(r.nextInt(str.length())));
			}
			
			// PRINTS AGE
			out.print(r.nextInt((99-18)+1)+18);
			
			// PRINTS YEARLY INCOME
			out.print("0000");
			out.print(r.nextInt((899999-100999)+1)+100999);
			
			// PRINTS ADDRESS
			for(int k=0; k<49; k++){
				out.print(str.charAt(r.nextInt(str.length())));
			}
			
		}
		
		
		out.close();
		}catch(IOException e){
			System.out.println("error inside main().");
			e.printStackTrace();
		}
		
		
		System.out.print("Program terminated...");
	}
}
