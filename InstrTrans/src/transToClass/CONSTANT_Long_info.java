package transToClass;

/**
 * 长整型结构
 */
public class CONSTANT_Long_info {
    u1 tag = new u1();
    u4 high_bytes = new u4();
    u4 low_bytes = new u4();

    @Override
    public String toString() {
        return tag.toString()
                + high_bytes.toString()
                + low_bytes.toString();
    }
}
