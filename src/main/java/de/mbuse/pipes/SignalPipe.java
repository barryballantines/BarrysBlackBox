package de.mbuse.pipes;

public class SignalPipe extends Pipe<Long> {

    @SuppressWarnings("unchecked")
    public static SignalPipe newInstance(String id) {
        return new SignalPipe(id);
    }

    public static SignalPipe newInstance(String id, PipeUpdateListener listener) {
        SignalPipe m = new SignalPipe(id);
        m.addListener(listener);
        return m;
    }

    protected SignalPipe(String id) {
        super(id);
        set(System.currentTimeMillis());
    }

    public void fire() {
        set(System.currentTimeMillis());
    }
}
