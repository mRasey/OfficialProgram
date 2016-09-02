package transToClass;

/**
 * 方法结构
 */
public class CONSTANT_Methodref_info {
    u1 tag = new u1();
    u2 class_index = new u2();
    u2 name_and_type_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + class_index.toString()
                + name_and_type_index.toString();
    }
}
