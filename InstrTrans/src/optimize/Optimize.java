package optimize;

import op.globalArguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static op.globalArguments.instrSizes;

public class Optimize {

    ArrayList<String> byteCodes = new ArrayList<>();
    //ArrayList<SingleMethod> singleMethods = new ArrayList<>();
    HashMap<String, Integer> instrStackSize = new HashMap<>();
    SingleMethod singleMethod;

    public int finishedByteCodeNumber = 0;

    public Optimize() {

    }

    /**
     * 读入每个指令占用操作栈空间大小
     * @return
     */
    public Optimize initStackSize() throws IOException {
        File file = new File("res/stackSize.txt");
        BufferedReader bfr = new BufferedReader(new FileReader(file));
        String readIn = bfr.readLine();
        while(readIn != null) {
            instrStackSize.put(readIn.split(" ")[0], Integer.parseInt(readIn.split(" ")[1]));
            readIn = bfr.readLine();
        }
        return this;
    }
    
    public Optimize clear(){
    	byteCodes.clear();
    	singleMethod = null;
    	return this;
	}

    public Optimize readInf(){		
		for( ; finishedByteCodeNumber<globalArguments.finalByteCodePC; finishedByteCodeNumber++){
			byteCodes.add(globalArguments.finalByteCode.get(finishedByteCodeNumber));
		}
		return this;
	}
    
    /**
     * 初始化分配指令
     * @return
     */
    public Optimize dispatchCodes() {
        String code;
        int i = 0;
        code = byteCodes.get(i);
        if(code.contains(".method")) {
            singleMethod = new SingleMethod(code);
            for(++i; !code.equals(".end method"); ) {
                code = byteCodes.get(i);
                if(code.contains(".line")) {
                    SingleLine singleLine = new SingleLine(Integer.parseInt(code.split(" ")[1]));
                    code = byteCodes.get(++i);
                    while(!code.contains(".line") && !code.equals(".end method")) {
                        singleLine.addByteCode(code);
                        code = byteCodes.get(++i);
                    }
                    singleMethod.addNewLine(singleLine);
                }
            }
        }
        return this;
    }

    /**
     * 输出结果
     * @return
     */
    public Optimize output() {
            globalArguments.optimizedByteCode.add(singleMethod.methodName);
            globalArguments.optimizedByteCodePC++;
            for(int lineIndex = 0; lineIndex < singleMethod.lines.size(); lineIndex++) {
                globalArguments.optimizedByteCode.add(".line" + " " + singleMethod.lines.get(lineIndex).lineNumber);
                globalArguments.optimizedByteCodePC++;
                singleMethod.lines.get(lineIndex).print();
            }
            globalArguments.optimizedByteCode.add(".end method");
            globalArguments.optimizedByteCodePC++;
        return this;
    }

    /**
     * 处理方法
     * @return
     */
    public Optimize deal() {
        dealSingleMethod(singleMethod);
        return this;
    }

    /**
     * 处理单个方法
     * @param singleMethod 单独一个方法
     */
    public Optimize dealSingleMethod(SingleMethod singleMethod) {
        for(int i = 0; i < singleMethod.lines.size(); i++) {
            dealSingleLine(singleMethod.lines.get(i), i);//删除多余指令
            simplifySingleLine(singleMethod.lines.get(i));//简化指令形式
        }
        globalArguments.method_max_stack.add(getMaxStack(singleMethod));//获得方法的操作栈大小
        rebuildSerialNumber(singleMethod);// 分配指令号
        return this;
    }

    /**
     * 删除多余指令
     * @param singleLine 单独一个方法
     * @param lineNumber 行号
     */
    public Optimize dealSingleLine(SingleLine singleLine, int lineNumber) {
        HashMap<Integer, SingleRegister> stackNumToState = new HashMap<>();
        //同一个寄存器先出现store在出现load
        ArrayList<String> byteCodes = singleLine.byteCodes;
        for(int i = 0; i < byteCodes.size(); i++) {
            String code = byteCodes.get(i);
            if(code.split(" ").length > 1){
            	if(code.contains("store")) {
                    int stackNum = Integer.parseInt(code.split(" ")[1]);
                    stackNumToState.put(stackNum, new SingleRegister(State.store, i));
                }
                else if(code.contains("load")) {
                	int stackNum = Integer.parseInt(code.split(" ")[1]);
                    SingleRegister singleRegister = stackNumToState.get(stackNum);
                    if(singleRegister != null && singleRegister.state == State.store) {
                        byteCodes.set(singleRegister.index, "");
                        byteCodes.set(i, "");
                        stackNumToState.remove(stackNum);
                    }
                }
            }
            
        }
        Iterator<String> iterator = byteCodes.iterator();
        while(iterator.hasNext()) {
            if(iterator.next().equals(""))
                iterator.remove();
        }
        return this;
    }

    /**
     * 简化指令形式，将load 1变成load_1
     * @param singleLine 单独一行
     * @return this
     */
    public Optimize simplifySingleLine(SingleLine singleLine) {
        ArrayList<String> byteCodes = singleLine.byteCodes;
        for(int i = 0; i < byteCodes.size(); i++) {
            String code = byteCodes.get(i);
            code = "assist: " + code;
            if(code.split(" ").length == 3 && code.split(" ")[2].length() == 1) {
                int stackNum = Integer.parseInt(code.split(" ")[2]);
                String op = code.split(" ")[1];
                if (0 <= stackNum && stackNum <= 3 && (op.substring(1).equals("load") || op.substring(1).equals("store")))
                    code = code.substring(0, code.lastIndexOf(" ")) + "_" + stackNum;
                else if (0 <= stackNum && stackNum <= 5 && op.equals("iconst"))
                    code = code.substring(0, code.lastIndexOf(" ")) + "_" + stackNum;
                else if (0 <= stackNum && stackNum <= 2 && op.equals("fconst"))
                    code = code.substring(0, code.lastIndexOf(" ")) + "_" + stackNum;
                else if (0 <= stackNum && stackNum <= 1 && op.substring(1).equals("const"))
                    code = code.substring(0, code.lastIndexOf(" ")) + "_" + stackNum;
                code = code.substring(code.indexOf(" ") + 1);
                byteCodes.set(i, code);
            }
        }
        return this;
    }

    /**
     * 读入初始化文件
     * @return this
     * @throws IOException
     */
    public Optimize initInstrSize() throws IOException {
        File file = new File("res/InstrSize.txt");
        BufferedReader bfr = new BufferedReader(new FileReader(file));
        String readIn = bfr.readLine();
        while(!readIn.startsWith("wide")) {
            String instrName = readIn.split(" ")[0];
            int instrSize = Integer.parseInt(readIn.split(" ")[1]);
            instrSizes.put(instrName, instrSize);
            readIn = bfr.readLine();
        }
        return this;
    }

    /**
     * 根据指令的大小改变指令之前的编号
     */
    public Optimize rebuildSerialNumber(SingleMethod singleMethod) {
        int lineIndex = 0;
        ArrayList<SingleLine> singleLines = singleMethod.lines;
        for(SingleLine singleLine : singleLines) {
            ArrayList<String> byteCodes = singleLine.byteCodes;
            for(int i = 0; i < byteCodes.size(); i++) {
                String byteCode = byteCodes.get(i);
                if(globalArguments.rf.ifAnInstruction(byteCode)) {
                	if(byteCode.contains("switch")) {
                	    String tempFlag = byteCode;// 记录下switch的类型
                        // 为tableswitch申请偏移值
                	    int instrSize = instrSizes.get(byteCode.split(" ")[0]);
                        byteCode = lineIndex + ": " + byteCode;
                        lineIndex += instrSize;
                        byteCodes.set(i, byteCode);
                        // 计算除去偏移表之后的偏移值
                		ArrayList<String> codes = new ArrayList<>();
                        do {
                            byteCode = byteCodes.get(++i);
                            codes.add(byteCode);
                        }while(!byteCode.startsWith("default"));
                        if(tempFlag.equals("tableswitch"))
                            lineIndex = lineIndex + 12 + (codes.size() - 1) * 4;// tableswitch
                        else
                            lineIndex = lineIndex + 8 + (codes.size() - 1) * 8;// lookupswitch
                    }
                	else{
                        int instrSize = instrSizes.get(byteCode.split(" ")[0]);
                        byteCode = lineIndex + ": " + byteCode;
                        lineIndex += instrSize;
                        byteCodes.set(i, byteCode);
                       
                	}
                }
            }
        }
        return this;
    }

    /**
     * 获得每一个方法所需的操作栈大小
     * @param singleMethod 单独一个方法
     * @return 栈大小
     */
    public int getMaxStack(SingleMethod singleMethod) {
        int maxStack = 0;
        ArrayList<String> regTypes = new ArrayList<>();
        ArrayList<SingleLine> singleLines = singleMethod.lines;
        for(SingleLine singleLine : singleLines) {
            ArrayList<String> byteCodes = singleLine.byteCodes;
            for(String byteCode : byteCodes) {
                if(globalArguments.rf.ifAnInstruction(byteCode)) {
                    if(byteCode.contains("invoke")) {
                        //解析调用函数的调用参数
                        String parameters = byteCode.split(" ")[1];
                        String types = parameters.substring(parameters.lastIndexOf(":") + 1);
                        while (!types.startsWith(")")) {
                            if (types.startsWith("L")) {
                                regTypes.add(types.substring(0, types.indexOf(";") + 1));
                                types = types.substring(types.indexOf(";"));
                            } else if (types.startsWith("[")) {
                                String tempType = "";
                                do {
                                    tempType += "[";
                                    types = types.substring(1);
                                }while (types.startsWith("["));
                                if((types.charAt(0) + "").equals("L")) {
                                    regTypes.add(tempType + types.substring(0, types.indexOf(";") + 1));
                                    types = types.substring(types.indexOf(";"));
                                }
                                else {
                                    regTypes.add(tempType + types.charAt(0));
                                }
                            } else {
                                regTypes.add(types.charAt(0) + "");
                            }
                            types = types.substring(1);
                        }
                        if(types.charAt(types.length() - 1) == 'V')
                            maxStack -= regTypes.size();
                        else if(types.charAt(types.length() - 1) == 'D'
                                || types.charAt(types.length() - 1) == 'J')
                            maxStack = maxStack - regTypes.size() + 2;
                        else
                            maxStack = maxStack - regTypes.size() + 1;
                    }
                    else
                        maxStack += instrStackSize.get(byteCode.split(" ")[0]);
                }
            }
        }
        return maxStack;
    }

    /**
     * 单元测试使用
     * @return
     */
    public Optimize test() {
        ArrayList<String> byteCodes = new ArrayList<>();
        byteCodes.add("lookupswitch");
        byteCodes.add("2 :tab2");
        byteCodes.add("3 :tab3");
        byteCodes.add("default :default_tab");
        byteCodes.add("aload 233");
        int lineIndex = 0;
        for (int i = 0; i < byteCodes.size(); i++) {
            String byteCode = byteCodes.get(i);
            if (globalArguments.rf.ifAnInstruction(byteCode)) {
                if (byteCode.contains("switch")) {
                    String tempFlag = byteCode;// 记录下switch的类型
                    // 为tableswitch申请偏移值
                    int instrSize = instrSizes.get(byteCode.split(" ")[0]);
                    byteCode = lineIndex + ": " + byteCode;
                    lineIndex += instrSize;
                    byteCodes.set(i, byteCode);
                    // 计算除去偏移表之后的偏移值
                    ArrayList<String> codes = new ArrayList<>();
                    do {
                        byteCode = byteCodes.get(++i);
                        codes.add(byteCode);
                    } while (!byteCode.startsWith("default"));
                    if (tempFlag.equals("tableswitch"))
                        lineIndex = lineIndex + 12 + (codes.size() - 1) * 4;// tableswitch
                    else
                        lineIndex = lineIndex + 8 + (codes.size() - 1) * 8;// lookupswitch
                } else {
                    int instrSize = instrSizes.get(byteCode.split(" ")[0]);
                    byteCode = lineIndex + ": " + byteCode;
                    lineIndex += instrSize;
                    byteCodes.set(i, byteCode);
                }
            }
            else
                System.err.println("not a instr");
        }

//        for(String string : byteCodes)
//            System.out.println(string);
        return this;
    }

    /**
     * 测试使用
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new Optimize().initInstrSize().test();
    }
}
