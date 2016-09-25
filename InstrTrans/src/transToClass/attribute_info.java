package transToClass;

public class attribute_info {
    u2 attribute_name_index = new u2();
    u4 attribute_length = new u4();  //后面这些东西的总长
    u2 info = new u2();

    public attribute_info() {
    	attribute_name_index.set((short) 10);
    	attribute_length.set(2);
    	info.set((short) 11);
    }

    @Override
    public String toString() {  
        return attribute_name_index.toString()
                + attribute_length.toString()
                + info.toString();
    }
}
