package transToClass;

/**
 * 整型结构
 */
public class CONSTANT_Integer_info {
    u1 tag = new u1();
    u4 bytes = new u4();

    @Override
    public String toString() {
        return tag.toString()
                + bytes.toString();
    }
}
