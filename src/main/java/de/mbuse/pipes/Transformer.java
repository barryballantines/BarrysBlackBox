/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.pipes;

/**
 * Transformers can be used to manipulate the values of two connected Pipes.
 * 
 * Example:
 * {pre}
 * Transformer<Double, Double> maxTransform = new Transformer() {
 *   Double transform(Double currentTargetValue, Double newSourceValue) {
 *      return Math.max(currentTargetValue, newSourceValue);
 *   }
 * };
 * 
 * Pipe<Double> targetPipe = Pipe.newInstance("target", 0.0);
 * Pipe<Double> sourcePipe = Pipe.newInstance("source", 0.0);
 * 
 * targetPipe.connectTo(sourcePipe, maxTransform);
 * 
 * sourcePipe.set(1.0);
 * sourcePipe.set(5.0);
 * sourcePipe.set(2.4);
 * sourcePipe.set(3.4);
 * 
 * // targetPipe will contain the value 5.0!
 * {pre}
 * 
 * Transformers can also be used to connect pipes of different types:
 * 
 * {pre}
 * Transformer<Long, Date> milliSecondsTransformer = new Transformer<>() {
 *   Long transform(Long currentTargetValue, Date newSourceValue) {
 *      return newSourceValue.getTime();
 *   }
 * };
 * 
 * 
 * Pipe<Long> targetPipe = Pipe.newInstance("target", 0.0);
 * Pipe<Date> sourcePipe = Pipe.newInstance("source", new Date());
 * 
 * targetPipe.connectTo(sourcePipe, milliSecondsTransformer);
 * 
 * // targetPipe will hold the currentTimeMillis of the date object.
 * {pre}
 * 
 * NOTE: transformers should not be used with pipes, which are connected in circuits
 * like with the command Pipes.connect(...);
 * 
 * @author mbuse
 */
public interface Transformer<T,S> {
    
    /**
     * 
     */
    T transform(T currentTargetValue, S newSourceValue);
}
