package instructions;

import op.Register;
import op.globalArguments;

import java.util.ArrayList;


/*“add-type”：vBB寄存器与vCC寄存器值进行加法运算（vBB + vCC）
"sub-type"：vBB寄存器与vCC寄存器值进行减法运算（vBB - vCC）
"mul-type"：vBB寄存器与vCC寄存器值进行乘法运算（vBB * vCC）
"div-type"：vBB寄存器与vCC寄存器值进行除法运算（vBB / vCC）
"rem-type"：vBB寄存器与vCC寄存器值进行模运算（vBB % vCC）
"and-type"：vBB寄存器与vCC寄存器值进行与运算（vBB & vCC）
"or-type"：vBB寄存器与vCC寄存器值进行或运算（vBB | vCC）
"xor-type"：vBB寄存器与vCC寄存器值进行异或运算（vBB ^ vCC）
"shl-type"：vBB寄存器值（有符号数）左移vCC位（vBB << vCC ）
"shr-type"：vBB寄存器值（有符号）右移vCC位（vBB >> vCC）
"ushr-type"：vBB寄存器值（无符号数）右移vCC位（vBB >>> vCC）
其中基础字节码后面的-type可以是-int，-long， -float，-double。后面3类指令与之类似。*/
public class _op extends Instruction {

    @Override
    public void analyze(String[] dexCodes) {
        super.analyze(dexCodes);
        String opType = dexCodes[0].substring(0, dexCodes[0].indexOf("-"));//获得操作类型
        String dataType = dexCodes[0].substring(dexCodes[0].indexOf("-") + 1, dexCodes[0].indexOf("-") + 2);//获得数据类型
        Register firstRegister = globalArguments.registerQueue.getByDexName(dexCodes[1]);
        Register secondRegister = globalArguments.registerQueue.getByDexName(dexCodes[2]);

        if(dexCodes[0].contains("2addr")) {
            globalArguments.finalByteCode.add(dataType + "load" + " " + firstRegister.stackNum);  //load dexCode[1]
            globalArguments.finalByteCode.add(dataType + "load" + " " + secondRegister.stackNum);  //load dexCode[2]
        }
        else if(dexCodes[0].contains("lit8")) {
            globalArguments.finalByteCode.add(dataType + "load" + " " + secondRegister.stackNum); //load dexCode[2]
            globalArguments.finalByteCode.add("bipush" + " " + dexCodes[3]);
        }
        else if(dexCodes[0].contains("lit16")) {
            globalArguments.finalByteCode.add(dataType + "load" + " " + secondRegister.stackNum); //load dexCode[2]
            globalArguments.finalByteCode.add("sipush" + " " + dexCodes[3]);
        }
        else {
            Register thirdRegister = globalArguments.registerQueue.getByDexName(dexCodes[3]);
            globalArguments.finalByteCode.add(dataType + "load" + " " + secondRegister.stackNum);  //load dexCode[2]
            globalArguments.finalByteCode.add(dataType + "load" + " " + thirdRegister.stackNum);  //load dexCode[3]
        }
        globalArguments.finalByteCode.add(dataType + opType);                                          //Type
        globalArguments.finalByteCode.add(dataType + "store" + " " + firstRegister.stackNum); //store dexCode[1]
        globalArguments.finalByteCodePC += 4;
    }

    //像double这种用到两个寄存器的，第二个寄存器怎么搞？
    @Override
    public boolean ifUpgrade(ArrayList<String> dexCode, int lineNum) {
        String dataType = dexCode.get(0).substring(dexCode.get(0).indexOf("-") + 1,dexCode.get(0).indexOf("-") + 2).toUpperCase();//获得数据类型
        if(dataType.equals("L")){
        	dataType = "J";
        }
    	Register firstRegister = globalArguments.registerQueue.getByDexName(dexCode.get(1));
        Register secondRegister = globalArguments.registerQueue.getByDexName(dexCode.get(2));
        if(dexCode.get(0).contains("/")){
        	
            firstRegister.updateType(lineNum, dataType);
            secondRegister.updateType(lineNum, dataType);
        }
        else{
            Register thirdRegister = globalArguments.registerQueue.getByDexName(dexCode.get(3));
            firstRegister.updateType(lineNum, dataType);
            secondRegister.updateType(lineNum, dataType);
            thirdRegister.updateType(lineNum, dataType);
        }
        return true;
    }
}
