/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

/**
 *
 * @author mbuse
 */
public class Buffer {
    private double[] buffer;
    private int size;
    private int nextIndex;
    private int capacity;

    public Buffer(int capacity) {
        this.buffer = new double[capacity];
        this.capacity = capacity;
        this.nextIndex = 0;
        this.size = 0;
    }

    public void put(double value) {
        this.buffer[nextIndex] = value;
        nextIndex = (nextIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    public double[] getValues() {
        int startIndex = (capacity + nextIndex - size) % capacity;
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            int index = (startIndex + i) % capacity;
            result[i] = buffer[index];
        }
        return result;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return size;
    }
    
}
