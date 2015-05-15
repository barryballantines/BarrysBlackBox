/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.pipes;

/**
 *
 * @author mbuse
 */
public class Pipes {
    
    public static <T> void connect(Pipe<T>... pipes) {
        if (pipes.length < 2) {
            return;
        }
        Pipe<T> firstPipe = null;
        Pipe<T> lastPipe = null;
        for (Pipe<T> current : pipes) {
            if (lastPipe == null) {
                firstPipe = current;
            } else {
                current.connectTo(lastPipe);
            }
            lastPipe = current;
        }
        firstPipe.connectTo(lastPipe);
    }
}
