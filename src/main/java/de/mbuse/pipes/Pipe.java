package de.mbuse.pipes;

import java.util.HashMap;
import java.util.Map;
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
    private final Map<Pipe, PipeUpdateListener> connectedListeners = new HashMap<>();

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
    
    private <S> PipeUpdateListener<S> createTransformingBindListener(final Transformer<T,S> transformer) {
      PipeUpdateListener<S> listener =  new PipeUpdateListener<S>() { 
          public void pipeUpdated(Pipe<S> model) {
            S incoming = model.get();
            T current = Pipe.this.value;
            T transformedIncomming = transformer.transform(current, incoming);
            if (notEquals(current, transformedIncomming)) {
                Pipe.this.set(transformedIncomming);
            }
          };
		
          public String toString() {
            return "Listener[" + Pipe.this.toString() + ", transformer=" + transformer + "]";
          }
      };
      
      return listener;
    }

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
        connectedListeners.put(sourcePipe, bindListener);
        sourcePipe.addListener(bindListener);
        set(sourcePipe.get());
    }
    
    public <S> void connectTo(Pipe<S> sourcePipe, Transformer<T, S> transformer) {
        PipeUpdateListener<S> listener = createTransformingBindListener(transformer);
        connectedListeners.put(sourcePipe, listener);
        sourcePipe.addListener(listener);
        set(transformer.transform(get(), sourcePipe.get()));
    }
    
    public void disconnectFrom(Pipe sourcePipe) {
        PipeUpdateListener listener = connectedListeners.remove(sourcePipe);
        if (listener!=null) {
            sourcePipe.removeChangeListener(listener);
        }
    }

    public final void fireUpdateEvent() {
        for (PipeUpdateListener l : listenerList.getListeners(PipeUpdateListener.class)) {
            try {
                l.pipeUpdated(this);
            } catch (Exception e) {
                System.err.printf("Failed to fire update event for listener '%s' and %s.\n", l, this );
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Pipe[id:'" + id + ", value:" + value + "]";
    }

}
