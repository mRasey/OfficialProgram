package op;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import optimize.SingleLine;
import optimize.SingleMethod;

/*
 * 调整行号
 * 1. .line后面直接跟着的.local都要放到上一个line中
 * 2. 相同的.line号合并
 * */
public class AdjustLineOrder {
	ArrayList<String> AllTheIns = new ArrayList<>();
	ArrayList<String> NewIns = new ArrayList<>();
	SingleMethod method;
	SingleLine line;
	
	public void run() throws IOException{
		init();
		readIns();
		output();
	}
	
	
	/*读取原smail文件,保存所有指令*/
	public void init(){
		File file = new File(globalArguments.smailFilePath);
		//File file = new File("res/test_endLocal.smali");
		String str = "";
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null; 
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		isr = new InputStreamReader(fis);
		br = new BufferedReader(isr);
		
		int i = 0;
		char[] temp;
		while(true){
			try {
				if((str = br.readLine()) != null){
					if(str.equals("")){
						continue;
					}
					temp = str.toCharArray();
					i = 0;
					while(temp[i] == ' '){
						i++;
					}
					str = str.substring(i);
					AllTheIns.add(str);
				}
				else{
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*从头开始遍历所有的指令，遇到.method就调用readMethod()去记录整个方法的信息。
	 * 然后调用去对这个方法的line进行排序
	 * */
	public void readIns(){
		int i = 0;
		int j = 0, y = 0;
		
		String methodName;
		for(i=0;i<AllTheIns.size();i++){
			if(AllTheIns.get(i).startsWith(".method")){
				methodName = AllTheIns.get(i).substring(AllTheIns.get(i).lastIndexOf(" "));
				method = new SingleMethod(methodName);
				i = readMethod(i);
				//排序
				sort();
				removeUselessGoto();
				removeSameLineNumber();
				for(j=0;j<method.lines.size();j++){
					line = method.lines.get(j);
					NewIns.add(".line "+line.lineNumber);
					for(y=0;y<line.byteCodes.size();y++){
						NewIns.add(line.byteCodes.get(y));
					}
				}
				NewIns.add(AllTheIns.get(i)); //.end method
				
			}
			else{
				NewIns.add(AllTheIns.get(i));
			}
		}
	}
	
	/*对method里的line进行排序
	 * 用个数组对记录line号,然后每次取出最小line放入newMethod中
	 * 调整时要注意加标签和指令,并把第一条指令前的.local放入到上一个.line中
	 * 调整一个line改变一次lineNumber判断是否已经排好序
	 * 排序之后再对重复的line进行处理
	 * 
	 * 排序的line分三种情况，只需连前面，只连后面，前后都连
	 * */
	public void sort(){
		SingleMethod newMethod = new SingleMethod(method.methodName);
		SingleLine tempLine;
		int num = 0; //放入newMethod中的line个数,用于对lineNumber进行重新排序
		int min = 0; //剩余line中最小值的下标
		ArrayList<Integer> lineNumber = new ArrayList<Integer>(); //记录method里面的line顺序
		int i = 0;
		
		for(i=0;i<method.lines.size();i++){
			lineNumber.add(method.lines.get(i).lineNumber);
		}
		//对line进行排序，并放入到newMethod中,放一个，删一个line
		int minLineNumber = 0; //最小line在method的lines中的下标
		while(num <method.lines.size() &&  !IfSequence(lineNumber)){
			//好像不需要转移.local
			
			min = getMinLine(lineNumber);
			minLineNumber = method.getLineIndex(lineNumber.get(min));
			tempLine = method.lines.get(minLineNumber);
			newMethod.addNewLine(tempLine);
			num++;
			lineNumber.remove(min);
		}
		
		//将剩余的已排序的line放入newMethod中
		for(i=0;i<lineNumber.size();i++){
			
			newMethod.addNewLine(method.lines.get(method.getLineIndex(lineNumber.get(i))));
		}
		method = newMethod;
	}
	
	//判断lineNumber是否是顺序的
	public boolean IfSequence(ArrayList<Integer> lineNumber){
		int i = 0;
		for(i=0;i<lineNumber.size()-1;i++){
			if(lineNumber.get(i) > lineNumber.get(i+1)){
				return false;
			}
		}
		return true;
	}
	
	//求出未放入newMethod的line中序号最小的那一行的下标
	public int getMinLine(ArrayList<Integer> lineNumber){
		int min = lineNumber.get(0);
		int n = 0;
		int i = 0;
		for(i=1;i<lineNumber.size();i++){
			if(lineNumber.get(i) < min){
				min = lineNumber.get(i);
				n = i;
			}
		}
		return n;
	}
	
	/*读取整个method里的指令，并加上标签，排序后传给NewIns*/
	public int readMethod(int n){
		int i = n;
		int lineNumber;
		int label = 0;
		while(!AllTheIns.get(i).startsWith(".line")){
			NewIns.add(AllTheIns.get(i));
			i++;
		}
		while(!AllTheIns.get(i).equals(".end method")){
			if(AllTheIns.get(i).startsWith(".line")){
				lineNumber = Integer.parseInt(AllTheIns.get(i).split(" ")[1]);
				line = new SingleLine(lineNumber);
				i++;
				if(label != 0){
					line.addByteCode(":label_"+label);
				}
				while(!AllTheIns.get(i).startsWith(".line") && !AllTheIns.get(i).equals(".end method")){
					line.addByteCode(AllTheIns.get(i));
					i++;
				}
				label++;
				if(!AllTheIns.get(i).equals(".end method")){
					line.addByteCode("goto :label_"+label);
				}
				method.addNewLine(line);
			}
		}
		return i;
	}
	
	//去掉相连的无用的goto
	public void removeUselessGoto(){
		SingleLine lastLine = method.lines.get(0);
		SingleLine nowLine = null;
		String label = null;
		int label_index = 0;
		//String goto_ins = null;
		int goto_index = 0;
		
		String str = null;
		int i = 0,j = 0,l = 0;
		for(i=1;i<method.lines.size();i++){
			nowLine = method.lines.get(i);
			for(j=0;j<nowLine.byteCodes.size();j++){
				str = nowLine.byteCodes.get(j);
				if(str.startsWith(":label_")){
					label = str;
					label_index = j;
					//遍历上个line有没有goto
					for(l=lastLine.byteCodes.size()-1;l>=0;l--){
						str = lastLine.byteCodes.get(l);
						if(str.equals("goto "+label)){
							goto_index = l;
							nowLine.byteCodes.remove(label_index);
							lastLine.byteCodes.remove(goto_index);
							break;
						}
						else if(globalArguments.rf.ifAnInstruction(str)){
							break;
						}
					}
				}
				//遇到第一条指令就停止遍历了
				else if(globalArguments.rf.ifAnInstruction(str)){
					break;
				}
			}
			method.lines.set(i, nowLine);
			method.lines.set(i-1, lastLine);
			lastLine = nowLine;
		}
		
	}
	
	//合并相同的line
	//一定要在removeUselessGoto()后面执行
	public void removeSameLineNumber(){
		int lastLineNum = -1;
		int nowLineNum = 0;
		SingleLine lastLine = null;
		SingleLine nowLine;
		int i = 0,j = 0;
		for(i=0;i<method.lines.size();i++){
			nowLine = method.lines.get(i);
			nowLineNum = nowLine.lineNumber;
			if(nowLineNum == lastLineNum){
				//把当前line的放入到上个line下
				for(j=0;j<nowLine.byteCodes.size();j++){
					lastLine.addByteCode(nowLine.byteCodes.get(j));
				}
				//替换掉原有lastLine，删掉nowLine
				method.lines.set(i-1, lastLine);
				method.lines.remove(i);
				i--;
			}
			else{
				lastLine = nowLine;
				lastLineNum = nowLineNum;
			}
		}
	}
	
	public void output() throws IOException{
		File file = new File("res/NewMainActivity.smali");
		FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		int i = 0 ;
		for(i=0;i<NewIns.size();i++){
			bw.write(NewIns.get(i) +"\n");
		}
		bw.close();
	}
}
