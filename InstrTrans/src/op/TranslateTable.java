package op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//每翻译完一个方法的指令调用一次，因为标签名可能会重名
public class TranslateTable {
	
	//记录每条标签和他下条byte指令
	public  Map <String,Integer> tabAndNextByteCodePC = new HashMap<>();
	
	public int finishedByteCodeNumber = 0;
	public int temp;
	
	
	public void clear(){
		tabAndNextByteCodePC.clear();
	}
	
	//读取翻译后的byteCode,记录标签信息
	public void readInf(){
		String [] byteCode;
		temp = finishedByteCodeNumber;
		
		for(;finishedByteCodeNumber<globalArguments.optimizedByteCodePC;finishedByteCodeNumber++){
			byteCode = globalArguments.optimizedByteCode.get(finishedByteCodeNumber).split(" ");
			
			if(byteCode[0].startsWith(":")){
				int i = 1;
				while(globalArguments.optimizedByteCode.get(finishedByteCodeNumber+i).startsWith(":")){
					i++;
				}
				String number = globalArguments.optimizedByteCode.get(finishedByteCodeNumber+i).split(" ")[0].replace(":", "");
				tabAndNextByteCodePC.put(byteCode[0],Integer.parseInt(number));
			}
		}
	}
	
	//处理跳转指令
	public void traTab(){
		String [] byteCode;
		int length = 0;
		String regex = "\\d+:";
		
		for(;temp<globalArguments.optimizedByteCodePC;temp++){
			globalArguments.traTabByteCode.add(globalArguments.optimizedByteCode.get(temp));
			globalArguments.traTabByteCodePC++;
			
			byteCode = globalArguments.optimizedByteCode.get(temp).split(" ");
			if(byteCode[0].startsWith(":")){ //tableswitch下面的标签先不处理
				continue;
			}
			else{
				length = byteCode.length;
				if(byteCode[length-1].startsWith(":")){
					byteCode[length-1] = tabAndNextByteCodePC.get(byteCode[length-1]).toString();
					String newByteCode = "";
					int i = 0;
					for(i=0;i<length-1;i++){
						newByteCode+=byteCode[i]+" ";
					}
					newByteCode+=byteCode[i];
					globalArguments.traTabByteCode.set(temp, newByteCode);
				}
			}
		}
	}
}
