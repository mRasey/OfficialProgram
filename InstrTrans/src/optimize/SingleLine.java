package optimize;

import java.util.ArrayList;

import op.globalArguments;

public class SingleLine {

    public ArrayList<String> byteCodes = new ArrayList<>();
    public int lineNumber;
    
    public SingleLine(int n) {
    	lineNumber = n;
    }

    public void addByteCode(String byteCode) {
        byteCodes.add(byteCode);
    }

    
    public SingleLine print() {
        for(String code : byteCodes) {
        	globalArguments.optimizedByteCode.add(code);
            globalArguments.optimizedByteCodePC++;
        }
        return this;
    }
}
