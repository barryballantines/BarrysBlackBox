package de.mbuse.pipes;

import javax.swing.event.EventListenerList;

public class Pipe<T> {

    private static boolean notEquals(Object a, Object b) {
        return (a == null) ? (b != null) : !a.equals(b);
    }

    public static <U> Pipe<U> newInstance(String id) {
        return new Pipe<U>(id);
    }

    public static <U> Pipe<U> newInstance(String id, PipeUpdateListener listener) {
        Pipe<U> m = new Pipe<U>(id);
        m.addListener(listener);
        return m;
    }
    
    public static <U> Pipe<U> newInstance(String id, U value, PipeUpdateListener listener) {
        Pipe<U> m = new Pipe<U>(id);
        m.set(value);
        m.addListener(listener);
        return m;
    }

    private String id;
    private T value;
    private final EventListenerList listenerList = new EventListenerList();

    private final PipeUpdateListener<T> bindListener = new PipeUpdateListener<T>() {
        public void pipeUpdated(Pipe<T> model) {
            T incoming = model.get();
            T current = Pipe.this.value;
            if (notEquals(current, incoming)) {
                Pipe.this.set(incoming);
            }
        };
		
	public String toString() {
            return "Listener[" + Pipe.this.toString() + "]";
        }
    };

    protected Pipe(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public <V extends T> V get() {
        return (V) this.value;
    }

    public void set(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (notEquals(oldValue, newValue)) {
            fireUpdateEvent();
        }
    }

    public void addListener(PipeUpdateListener<?> listener) {
        listenerList.add(PipeUpdateListener.class, listener);
    }

    public void removeChangeListener(PipeUpdateListener<?> listener) {
        listenerList.remove(PipeUpdateListener.class, listener);
    }

    public void connectTo(Pipe<T> sourcePipe) {
        sourcePipe.addListener(bindListener);
        set(sourcePipe.get());
    }
    
    public void disconnectFrom(Pipe<T> sourcePipe) {
        sourcePipe.removeChangeListener(bindListener);
    }

    public final void fireUpdateEvent() {
        for (PipeUpdateListener l : listenerList.getListeners(PipeUpdateListener.class)) {
            l.pipeUpdated(this);
        }
    }

    @Override
    public String toString() {
        return "Pipe[id:'" + id + ", value:" + value + "]";
    }

}
