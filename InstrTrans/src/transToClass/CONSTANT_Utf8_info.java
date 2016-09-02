package transToClass;

/**
 * utf8结构
 */
public class CONSTANT_Utf8_info {
    u1 tag = new u1();
    u2 length = new u2();
    u1[] bytes = new u1[length.get()];

    @Override
    public String toString() {
        String bytes_string = "";
        for(int i = 0; i < length.get(); i++)
            bytes_string += bytes[i].toString();
        return tag.toString()
                + length.toString()
                + bytes_string;
    }
}
