package transToClass;

public class u1 {
    private byte u1 = -1;

    public u1() {

    }

    public u1(byte u1) {
        this.u1 = u1;
    }

    public void set(byte u1) {
        this.u1 = u1;
    }

    public byte get() {
        return u1;
    }

    @Override
    public String toString() {
    	if(this.u1 != -1){
    		String result = Integer.toHexString(u1);
            while(result.length() < 2)
                result = "0" + result;
            return result;
    	}
    	else{
    		return "";
    	}
        
    }
}
