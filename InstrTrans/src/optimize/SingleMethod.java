package optimize;

import java.util.ArrayList;

public class SingleMethod {
    public String methodName;
    public ArrayList<SingleLine> lines = new ArrayList<>();
    ArrayList<Integer> mark = new ArrayList<>();

    public SingleMethod(String methodName) {
        this.methodName = methodName;
    }

    public void addNewLine(SingleLine singleLine) {
        lines.add(singleLine);
        mark.add(0);
    }
    
    public void changeLine(int n ,SingleLine singleLine) {
        lines.set(n, singleLine);
    }
    
    public int getLineIndex(int lineNumber){
    	int i = 0;
    	for(i=0;i<lines.size();i++){
    		if(lines.get(i).lineNumber == lineNumber && mark.get(i) == 0){
    			mark.set(i, 1);
    			return i;
    		}
    	}
    	return -1;
    }
}
