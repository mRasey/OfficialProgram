package transToClass;

/**
 * 接口结构
 */
public class CONSTANT_InterfaceMethod_info {
    u1 tag = new u1();
    u2 bootstrap_method_attr_index = new u2();
    u2 name_and_type_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + bootstrap_method_attr_index.toString()
                + name_and_type_index.toString();
    }
}
