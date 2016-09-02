package optimize;

import java.util.ArrayList;

public class SingleMethod {
    String methodName;
    ArrayList<SingleLine> lines = new ArrayList<>();

    public SingleMethod(String methodName) {
        this.methodName = methodName;
    }

    public void addNewLine(SingleLine singleLine) {
        lines.add(singleLine);
    }
}
