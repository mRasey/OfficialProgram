package instructions;

import op.Register;
import op.globalArguments;

import java.util.ArrayList;

/*数组操作指令
数组操作包括获取数组长度，新建数组，数组赋值，数组元素取值与赋值等操作。
“array-length vA, vB”：获取给定vB寄存器中数组的长度并将值赋给vA寄存器，数组长度指的是数组的条目个数。
“new-array vA, vB, type@CCCC”：构造指定类型（type@CCCC）与大小（vB）的数组，并将值赋给vA寄存器。
"new-array/jumbo vAAAA, vBBBB,type@CCCCCCCC"指令功能与“new-array vA,vB,type@CCCC”相同，只是寄存器值与指令的索引取值范围更大（Android4.0中新增的指令）。
“filled-new-array {vC, vD, vE, vF, vG},type@BBBB”构造指定类型（type@BBBB）与大小（vA）的数组并填充数组内容。
                                            vA寄存器是隐含使用的，除了指定数组的大小外还指定了参数的个数，vC~vG是使用到的参数寄存序列。
“filled-new-array/range {vCCCC  ..vNNNN}, type@BBBB”指令功能与“filled-new-array {vC, vD, vE, vF, vG},type@BBBB”相同，
"filled-new-array/jumbo {vCCCC  ..vNNNN},type@BBBBBBBB"指令功能与“filled-new-array/range {vCCCC  ..vNNNN},type@BBBB”相同，只是索引取值范围更大（Android4.0中新增的指令）。
                                                        只是参数寄存器使用range字节码后缀指定了取值范围 ，vC是第一个参数寄存器，N = A +C -1。
"fill-array-data vAA, +BBBBBBBB"用指定的数据来填充数组，vAA寄存器为数组引用，引用必须为基础类型的数组，在指令后面会紧跟一个数据表。
"arrayop vAA, vBB, vCC"对vBB寄存器指定的数组元素进入取值与赋值。vCC寄存器指定数组元素索引，
                        vAA寄存器用来存放读取的或需要设置的数组元素的值。读取元素使用aget类指令，元素赋值使用aput类指定，
                        根据数组中存储的类型指令后面会紧跟不同的指令后缀，指令列表有 aget, aget-wide, aget-object,
                        aget-boolean, aget-byte,aget-char, aget-short, aput, aput-wide, aput-object, aput-boolean,
                        aput-byte, aput-char, aput-short。*/
public class _array extends Instruction{

    @Override
    public void analyze(String[] dexCodes) {
        super.analyze(dexCodes);
        Register firstRegister = globalArguments.registerQueue.getByDexName(dexCodes[1]);
        Register secondRegister = null;
        if(dexCodes[2].matches("[p,v]\\d+")) {
        	secondRegister = globalArguments.registerQueue.getByDexName(dexCodes[2]);
        }
        
        Register thirdRegister = null;
        if(dexCodes.length == 4) {
            thirdRegister = globalArguments.registerQueue.getByDexName(dexCodes[3]);
        }
        switch (dexCodes[0]) {
            case "array-length" :
            	globalArguments.finalByteCode.add("aload" + " " + secondRegister.stackNum);
            	globalArguments.finalByteCode.add("arraylength");
            	globalArguments.finalByteCode.add("istore" + " " + firstRegister.stackNum);
            	globalArguments.finalByteCodePC += 3;
                break;
            case "new-array" :
            case "new-array/jumbo" :
            	globalArguments.finalByteCode.add("iload" + " " + secondRegister.stackNum);
            	if( dexCodes[3].charAt(dexCodes[3].indexOf("[")+1) == 'L' || dexCodes[3].charAt(dexCodes[3].indexOf("[")+1) == '['){
            		globalArguments.finalByteCode.add("anewarray" + " " + dexCodes[3]);
            	}
            	else{
            		globalArguments.finalByteCode.add("newarray" + " " + dexCodes[3]);
            	}
            	globalArguments.finalByteCode.add("astore" + " "+ firstRegister.stackNum);
            	globalArguments.finalByteCodePC += 3;
                break;
                
            case "filled-new-array" :
            case "filled-new-array/range" :
            case "filled-new-array/jumbo" :
            	//创建新数组
            	String arrayType = dexCodes[dexCodes.length-1];
            	globalArguments.finalByteCode.add("ldc" + " " + (dexCodes.length-2));
            	if( dexCodes[3].charAt(dexCodes[3].indexOf("[")+1) == 'L' || dexCodes[3].charAt(dexCodes[3].indexOf("[")+1) == '['){
            		globalArguments.finalByteCode.add("anewarray" + " " + arrayType);
            	}
            	else{
            		globalArguments.finalByteCode.add("newarray" + " " + arrayType);
            	}
            	//globalArguments.finalByteCode.add("newarray" + " " + arrayType);
            	globalArguments.finalByteCodePC += 2;
            	
            	//取出数据类型
            	String dataType3 = arrayType.substring(arrayType.lastIndexOf("[") + 1).toLowerCase();
            	if(dataType3.equals("j"))
                    dataType3 = "l";
                else if(dataType3.equals("l"))
                    dataType3 = "a";
                else if(dataType3.equals("z"))
                	dataType3 = "i";
            	String optype3 = "";
            	if(dataType3.equals("f") || dataType3.equals("l") || dataType3.equals("d")){
            		optype3 = dataType3 + "load";
            	}
            	else if(dataType3.equals("l")){
            		optype3 = "aload";
            	}
            	else{
            		optype3 = "iload";
            	}
            	
            	//为数组赋值
            	int j = 0;
            	while(j<dexCodes.length-2){
            		//数组引用和下标
            		globalArguments.finalByteCode.add("dup");
            		globalArguments.finalByteCode.add("ldc" + " " + j);
            		//从寄存器中取出值
            		Register register = globalArguments.registerQueue.getByDexName(dexCodes[j+1]);
            		globalArguments.finalByteCode.add(optype3 + " " + register.stackNum);
            		//赋给数组
            		globalArguments.finalByteCode.add(dataType3 + "astore");
            		globalArguments.finalByteCodePC += 4;
            	}
            	
            	break;
                
            case "fill-array-data" :
            	//获取数组类型，上一条指令可能就只是new-array
            	ArrayList<String> lastDexCode = globalArguments.rf.getInstruction(globalArguments.dexCodeNumber-1);
            	String dataType1 = lastDexCode.get(3).substring(lastDexCode.get(3).lastIndexOf("[") + 1).toLowerCase();
            	if(dataType1.equals("j"))
                    dataType1 = "l";
                else if(dataType1.equals("l"))
                    dataType1 = "a";
                else if(dataType1.equals("z"))
                	dataType1 = "i";
            	//定义赋值方式
            	String optype1 = "";
            	if(dataType1.equals("b") || dataType1.equals("s")){
            		optype1 = dataType1 + "ipush";
            	}
            	else if(dataType1.equals("l") || dataType1.equals("d")){
            		optype1 = "ldc2_w";
            	}
            	else{
            		optype1 = "ldc";
            	}
            	//获取数组数据
            	ArrayList<String> ad = new ArrayList<String>();
            	ad = globalArguments.arrayData.get(dexCodes[2]);
            	//为数组赋值
            	int i = 0;
            	while(i< ad.size()){
            		globalArguments.finalByteCode.add("aload" + " " + firstRegister.stackNum);
                    globalArguments.finalByteCode.add("ldc" + " " + i);
                    //获取数据
                    globalArguments.finalByteCode.add(optype1 + " " + ad.get(i));
                    globalArguments.finalByteCode.add(dataType1 + "astore");
                    globalArguments.finalByteCodePC += 4;
                    i++;
            	}
            	
                break;
                
            case "aget" :
            case "aget-wide" :
            case "aget-object" :
            case "aget-boolean" :
            case "aget-byte" :
            case "aget-char" :
            case "aget-short" :
            case "aput" :
            case "aput-wide" :
            case "aput-object" :
            case "aput-boolean" :
            case "aput-byte" :
            case "aput-char" :
            case "aput-short" :
//                 = dexCodes[0].charAt(dexCodes[0].lastIndexOf("-") + 1) + "";
                globalArguments.finalByteCode.add("aload" + " " + secondRegister.stackNum);
                globalArguments.finalByteCode.add("iload" + " " + thirdRegister.stackNum);
                String dataType2 = firstRegister.getType(globalArguments.dexCodeNumber).toLowerCase();
                if(dataType2.equals("j"))
                    dataType2 = "l";
                else if(dataType2.startsWith("l"))
                    dataType2 = "a";
                else if(dataType2.equals("z"))
                	dataType2 = "i";
                
                if(dexCodes[0].contains("get")) {
                    globalArguments.finalByteCode.add(dataType2 + "aload");
                    globalArguments.finalByteCode.add(dataType2 + "store" + " " + firstRegister.stackNum);
                    globalArguments.finalByteCodePC += 4;
                }
                else {
                    globalArguments.finalByteCode.add(dataType2 + "load" + " " + firstRegister.stackNum);
                    globalArguments.finalByteCode.add(dataType2 + "astore");
                    globalArguments.finalByteCodePC += 4;
                }
        }
    }

    @Override
    public boolean ifUpgrade(ArrayList<String> dexCode, int lineNum) {
    	ArrayList<String> lastIns;
    	Register register;
        Register firstRegister = globalArguments.registerQueue.getByDexName(dexCode.get(1));
        Register secondRegister = null;
        if(dexCode.get(2).matches("[p,v]\\d+")) {
            secondRegister = globalArguments.registerQueue.getByDexName(dexCode.get(2));
        }

        Register thirdRegister = null;
        if(dexCode.size() == 4) {
            thirdRegister = globalArguments.registerQueue.getByDexName(dexCode.get(3));
        }
        switch (dexCode.get(0)) {
            case "array-length" :
                firstRegister.updateType(lineNum, "I");
                //在这个方法调用之前应该就指定了这个寄存器的类型了
                secondRegister.updateType(lineNum, secondRegister.currentType);
                break;
            case "new-array" :
            case "new-array/jumbo" :
            	//为上一个const寄存器赋类型
            	//会不会在前一条之外给第三个寄存器赋值，上一条给第二个寄存器赋值？？？
                lastIns = globalArguments.rf.getInstruction(lineNum-1);
                if(lastIns.get(0).contains("const") && lastIns.get(1).equals(secondRegister.dexName)){
                	secondRegister.updateType(lineNum-1, "I");
                }
                firstRegister.updateType(lineNum, dexCode.get(3));
                secondRegister.updateType(lineNum, "I");
                
                break;
            case "filled-new-array" :
            case "filled-new-array/range" :
            case "filled-new-array/jumbo" :
                //这里创建的新数组会放在栈顶，用move-result-object赋给寄存器
                String arrayType = dexCode.get(dexCode.size()-1);
                String dataType = arrayType.substring(arrayType.indexOf("[") + 1);

                int j = 1;
              //为上面若干const寄存器赋类型
                String[] regName = new String[dexCode.size()-2];
                for(j=0;j<regName.length;j++){
                	regName[j] = dexCode.get(j+1);
                }
                int order = lineNum-1;
                int mark = -1; 	 //记录const中用到的寄存器在invoke指令参数集中的位置
                do{
                	lastIns = globalArguments.rf.getInstruction(order);
                	if(lastIns.get(0).contains("const")){
                		mark = ifCont(regName, lastIns.get(1));
                	}
                	else{
                		mark = -1;
                	}
                	if(mark >=0 ){
                		register = globalArguments.registerQueue.getByDexName(lastIns.get(1));
                		register.updateType(order, dataType);
                	}
                	order--;
                }while(order >= 0 && mark >=0);
                //为当前寄存器赋类型
                j = 1;
                while(j<dexCode.size()){
                    register = globalArguments.registerQueue.getByDexName(dexCode.get(j));
                    register.updateType(lineNum, dataType);
                }
                break;

            case "fill-array-data" :
                firstRegister.updateType(lineNum, firstRegister.currentType);
                break;

            case "aget" :
            case "aget-wide" :
            case "aget-object" :
            case "aget-boolean" :
            case "aget-byte" :
            case "aget-char" :
            case "aget-short" :
            	lastIns = globalArguments.rf.getInstruction(lineNum-1);
                if(lastIns.get(0).contains("const")){
                	if(lastIns.get(1).equals(thirdRegister.dexName)){
                		thirdRegister.updateType(lineNum-1, "I");
                	}
                }
            	
            	firstRegister.updateType(lineNum, secondRegister.currentType.substring(secondRegister.currentType.indexOf("[") + 1));
                secondRegister.updateType(lineNum, secondRegister.currentType);
                thirdRegister.updateType(lineNum, "I");
                
                break;
            case "aput" :
            case "aput-wide" :
            case "aput-object" :
            case "aput-boolean" :
            case "aput-byte" :
            case "aput-char" :
            case "aput-short" :
            	
            	for(int i=1;;i++){
                	lastIns = globalArguments.rf.getInstruction(lineNum-i);
                    if(lastIns.get(0).contains("const")){
                    	if(lastIns.get(1).equals(thirdRegister.dexName)){
                    		thirdRegister.updateType(lineNum-i, "I");
                    	}
                    	else if(lastIns.get(1).equals(firstRegister.dexName)){
                    		firstRegister.updateType(lineNum-i, secondRegister.currentType.substring(secondRegister.currentType.indexOf("[") + 1));
                    		//System.err.println((lineNum-i)+" "+ firstRegister.dexName+" "+firstRegister.getType(lineNum-i));
                    	}
                    	else{
                    		break;
                    	}
                    }
                    else if(lastIns.get(0).startsWith(":")){
                    	continue;
                    }
                    else if(lastIns.get(0).startsWith(".")){
                    	continue;
                    }
                    else{
                    	break;
                    }
                }
            	
                firstRegister.updateType(lineNum, secondRegister.currentType.substring(secondRegister.currentType.indexOf("[") + 1));
                secondRegister.updateType(lineNum, secondRegister.currentType);
                thirdRegister.updateType(lineNum, "I");
                //System.out.println(firstRegister.dexName+" "+secondRegister.dexName+" "+thirdRegister.dexName);
                
                
                break;

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
    
    
}

