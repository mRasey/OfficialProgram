package transToClass;

/**
 * 类结构
 */
public class CONSTANT_Class_info {
    u1 tag = new u1();
    u2 name_index = new u2(); //常量池索引

    @Override
    public String toString() {
        return tag.toString()
                + name_index.toString();
    }
}
