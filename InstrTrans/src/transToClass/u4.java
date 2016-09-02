package transToClass;

public class u4 {
    int u4 = -1;

    public u4() {

    }

    public u4(int u4) {
       this.u4 = u4;
    }

    public void set(int u4) {
        this.u4 = u4;
    }

    public int get() {
        return u4;
    }

    @Override
    public String toString() {
    	if(u4 != -1){
    		String result = Integer.toHexString(u4);
            while(result.length() != 8) {
                result = "0" + result;
            }
            return result;
    	}
    	else{
    		return "";
    	}
        
    }
}
