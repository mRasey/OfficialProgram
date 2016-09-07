package instructions;

import op.Register;
import op.globalArguments;

import java.util.ArrayList;
/*方法调用指令
方法调用指令负责调用类实例的方法。它的基础指令为 invoke，方法调用指令有“invoke-kind {vC, vD, vE, vF, vG},meth@BBBB”
与“invoke-kind/range {vCCCC  .. vNNNN},meth@BBBB”两类，两类指令在作用上并无不同，只是后者在设置参数寄存器时使用了range来指定寄存器的范围。
根据方法类型的不同，共有如下五条方法调用指令：
“invoke-virtual” 或 “invoke-virtual/range”调用实例的虚方法。
“invoke-super”或"invoke-super/range"调用实例的父类方法。
“invoke-direct”或“invoke-direct/range”调用实例的直接方法。
“invoke-static”或“invoke-static/range”调用实例的静态方法。
“invoke-interface”或“invoke-interface/range”调用实例的接口方法。
在Android4.0系统中，Dalvik指令集中增加了“invoke-kind/jumbo {vCCCC  .. vNNNN},meth@BBBBBBBB”这类指令，
它与上面介绍的两类指令作用相同，只是在指令中增加了jumbo字节码后缀，且寄存器值与指令的索引取值范围更大。*/
public class _invoke extends Instruction {

    
    public void analyze(String[] dexCodes,int lineNum) {
    	super.analyze(dexCodes);
    	//先提取数据到操作数栈
    	int i = 1;
    	for(i=1; i<dexCodes.length-1;i++){
    		Register register = globalArguments.registerQueue.getByDexName(dexCodes[i]);
    		String dataType = register.getType(globalArguments.dexCodeNumber).toLowerCase();
    		if(dataType.equals("j")) {
    			dataType = "l";
            }
    		if(dataType.equals("d") || dataType.equals("f") || dataType.equals("l")) {
                globalArguments.finalByteCode.add(dataType + "load" + " " + register.stackNum);
            }
            else if(dataType.equals("i") || dataType.equals("b") || dataType.equals("s") || dataType.equals("c")){
                globalArguments.finalByteCode.add("iload" + " " + register.stackNum);
            }
            else{
            	globalArguments.finalByteCode.add("aload" + " " + register.stackNum);
            }
    		globalArguments.finalByteCodePC++;
    	}
    	
    	//判断返回值是否被用到
    	boolean IfUsed = ifUsedReturn(lineNum);
    	String returnType = dexCodes[dexCodes.length-1].substring(dexCodes[dexCodes.length-1].indexOf("(")+1);
    	String code = "pop";
    	if(returnType.equals("D") || returnType.equals("J")){
    		code = "pop2";
    	}
    	else if(returnType.equals("V")){
    		code = null;
    	}
    	
    	
    	//翻译成invoke指令
    	String classname = dexCodes[dexCodes.length-1].split("->")[0];
    	String methodname = dexCodes[dexCodes.length-1].split("->")[1];
    	if(dexCodes[0].contains("virtual")){
    		globalArguments.finalByteCode.add("invokevirtual"+" "+classname.replace(";", ".")+methodname.replace("(", ":("));
    	}
    	else if(dexCodes[0].contains("static")){
    		globalArguments.finalByteCode.add("invokestatic"+" "+classname.replace(";", ".")+methodname.replace("(", ":("));
    	}
    	else if(dexCodes[0].contains("interface")){
    		globalArguments.finalByteCode.add("invokeinterface"+" "+classname.replace(";", ".")+methodname.replace("(", ":("));
    	}
    	else{
    		globalArguments.finalByteCode.add("invokespecial"+" "+classname.replace(";", ".")+methodname.replace("(", ":("));
    	}
    	globalArguments.finalByteCodePC++;
    	
    	if(!IfUsed && code != null){
			globalArguments.finalByteCode.add(code);
			globalArguments.finalByteCodePC++;
		}
    }

    @Override
    public boolean ifUpgrade(ArrayList<String> dexCode, int lineNum) {
        //先求出所有参数的类型，保存在regTypes中，最后再依次赋值
        ArrayList<String> regTypes = new ArrayList<>();
        regTypes = getRegType(dexCode);
        //参数寄存器的名字
        int i = 0;
        String[] regName = new String[regTypes.size()];
        for(i=0;i<regTypes.size();i++){
        	regName[i]=dexCode.get(i+1);
        }
        //为之前的const所用寄存器赋类型
        int order = lineNum-1;
        int mark = -1; 	 //记录const中用到的寄存器在invoke指令参数集中的位置
        ArrayList<String> lastIns;
        do{
        	lastIns = globalArguments.rf.getInstruction(order);
        	if(lastIns.get(0).contains("const")){
        		mark = ifCont(regName, lastIns.get(1));
        	}
        	else{
        		mark = -1;
        	}
        	if(mark >=0 ){
        		Register register = globalArguments.registerQueue.getByDexName(lastIns.get(1));
        		register.updateType(order, regTypes.get(mark));
        	}
        	order--;
        }while(order >= 0 && mark >=0);
        i = 1;
        //为寄存器分配类型
        for (String aRegType : regTypes) {
            Register register1 = globalArguments.registerQueue.getByDexName(dexCode.get(i++));
            register1.updateType(lineNum, aRegType);
            //long double 为两个连续寄存器赋类型
            if (aRegType.equals("J") || aRegType.equals("D")) {
                Register register2 = globalArguments.registerQueue.getByDexName(dexCode.get(i++));
                register2.updateType(lineNum, aRegType);
            }
        }
        
        
        
        return true;
    }
    
    public int ifCont(String []datas, String data){
    	int i=0;
    	for(i=0;i<datas.length;i++){
    		if(datas[i].equals(data)){
    			return i;
    		}
    	}
    	
    	return -1;
    }

    public ArrayList<String> getRegType(ArrayList<String> dexCode){
        ArrayList<String> regTypes = new ArrayList<>();
        String[] temp = dexCode.get(dexCode.size() - 1).split(";->");
        String types = temp[1].substring(temp[1].indexOf("(") + 1, temp[1].indexOf(")") + 1);//括号中参数的类型 example: ILstring;D)
        if(!dexCode.get(0).contains("static")){
        	regTypes.add(temp[0]);//默认this的类型
        }
        while (!types.equals(")")) {
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
        return regTypes;
    }

    public boolean ifUsedReturn(int lineNum){
    	return true;
    }
}
