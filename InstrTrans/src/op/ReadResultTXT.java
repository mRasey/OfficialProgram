package op;

import transToClass.Matchup;

import java.io.*;
import java.security.Key;
import java.util.HashMap;

public class ReadResultTXT {


	public ReadResultTXT readAndBuildLineTable() throws IOException {
		int endIns = 0;
		
		File file = new File("res/result.txt");
		BufferedReader bfr = new BufferedReader(new FileReader(file));
		String readIn = bfr.readLine();
		while (readIn != null) {
			if (readIn.startsWith(".method")) {
				readIn = bfr.readLine();
				HashMap<Integer, Integer> lineNumberTable = new HashMap<>();
				while (readIn != null && !readIn.contains(".end method")) {
					if (readIn.contains(".line")) {
						int lineNumber = Integer.parseInt(readIn.split(" ")[1]);// 获取line号
						do {
							readIn = bfr.readLine();
						} while (readIn.startsWith(".local") || readIn.startsWith(":"));// 跳过之前的.local
						//.line后面直接跟.end method，不记录这个.line
						if(readIn.startsWith(".end method")){
							break;
						}
						//System.out.println(readIn);
						int byteCodeNumber = Integer
								.parseInt(readIn.split(" ")[0].substring(0, readIn.split(" ")[0].lastIndexOf(":")));
						lineNumberTable.put(lineNumber, byteCodeNumber);
					}
					readIn = bfr.readLine();
				}
				globalArguments.lineNumberTables.add(lineNumberTable);
			} else {
				readIn = bfr.readLine();
			}
		}
		
		bfr = new BufferedReader(new FileReader(file));
		readIn = bfr.readLine();
		String regex = "\\d+:";
		while(!readIn.equals("{")){
			readIn = bfr.readLine();
		}
		while (readIn != null) {
			if(readIn.contains(".end method")){
				globalArguments.method_end_num.add(endIns);
				readIn = bfr.readLine();
			}
			else if(readIn.split(" ")[0].matches(regex)){
				endIns = Integer
						.parseInt(readIn.split(" ")[0].substring(0, readIn.split(" ")[0].lastIndexOf(":")));
				readIn = bfr.readLine();
			}
			else{
				readIn = bfr.readLine();
			}
		}
		
		
		return this;
	}
}
