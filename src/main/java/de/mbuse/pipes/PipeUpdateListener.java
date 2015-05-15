package de.mbuse.pipes;

import java.util.EventListener;

public interface PipeUpdateListener<T> extends EventListener {

    void pipeUpdated(Pipe<T> model);
}
