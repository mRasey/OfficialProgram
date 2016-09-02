package optimize;

public class SingleRegister {
    int index;//字节码行号
    State state = State.idle;

    public SingleRegister(State state, int index) {
        this.state = state;
        this.index = index;
    }
}

enum State {
    store,
    load,
    idle
}
