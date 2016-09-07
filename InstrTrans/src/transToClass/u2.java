package transToClass;

public class u2 {

    short u2 = -1;

    public u2(){
    	
    }
    
    
    public u2(short u2) {
    	this.u2 = u2;
	}

	public void set(short u2) {
        this.u2 = u2;
    }

    public short get() {
        return u2;
    }

    @Override
    public String toString() {
    	if(u2 != -1){
    		String result = Integer.toHexString(u2);
            while(result.length() < 4)
                result = "0" + result;
            return result;
    	}
    	else{
    		return "";
    	}
        
    }
}
