package transToClass;

/**
 * 名称和类型结构
 */
public class CONSTANT_NameAndType_info {
    u1 tag = new u1();
    u2 name_index = new u2();
    u2 descriptor_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + name_index.toString()
                + descriptor_index.toString();
    }
}
