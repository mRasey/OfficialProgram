package transToClass;

/**
 * methodHandle结构
 */
public class CONSTANT_MethodHandle_info {
    u1 tag = new u1();
    u1 reference_kind = new u1();
    u2 reference_index = new u2();

    @Override
    public String toString() {
        return tag.toString()
                + reference_kind.toString()
                + reference_index.toString();
    }
}
