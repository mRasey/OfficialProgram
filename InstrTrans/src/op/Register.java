package op;

import java.util.HashMap;

public class Register {

    public String dexName;
    public int stackNum;
    public String currentType;
    HashMap<Integer, String> typeInCurrentLine = new HashMap<>();
    //记录寄存器是否是临时变量,不需要保存的
    public boolean IfTempVar = true;

    public Register(String dexName) {
        this.dexName = dexName;
    }

    public Register(String dexName, String type, int stackNum) {
        currentType = type;
        this.dexName = dexName;
        this.stackNum = stackNum;
    }

    public Register(String dexName, String type, int stackNum, int lineNume) {
        currentType = type;
        this.dexName = dexName;
        this.stackNum = stackNum;
        typeInCurrentLine.put(lineNume, dexName);
    }

    public void updateType(int lineNum, String type) {
        currentType = type;
        typeInCurrentLine.put(lineNum, currentType);
    }

    public String getType(int lineNum) {
        return typeInCurrentLine.get(lineNum);
    }
    
    public void setIfTempVar(){
    	IfTempVar = false;
    }

}
