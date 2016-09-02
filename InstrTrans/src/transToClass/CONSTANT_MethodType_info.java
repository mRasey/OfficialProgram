package transToClass;

/**
 * 方法类型结构
 */
public class CONSTANT_MethodType_info {
    u1 tag = new u1();
    u2 descriptor_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + descriptor_index.toString();
    }
}
