package transToClass;

/**
 * 字符串结构
 */
public class CONSTANT_String_info {
    u1 tag = new u1();
    u2 string_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + string_index.toString();
    }
}
