package optimize;

import java.util.ArrayList;

import op.globalArguments;

public class SingleLine {

    ArrayList<String> byteCodes = new ArrayList<>();
    int lineNumber;
    
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
