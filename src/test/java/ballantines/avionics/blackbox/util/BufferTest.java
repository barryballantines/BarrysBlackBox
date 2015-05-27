/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

import ballantines.avionics.blackbox.util.Buffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class BufferTest {
    
    public BufferTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of put method, of class Buffer.
     */
    @Test
    public void testPut() {
       testBuffer(10, 0);
       testBuffer(10, 5);
       testBuffer(10, 10);
       testBuffer(10, 15);
       testBuffer(10, 20);
        
    }
    
    private void testBuffer(int capacity, int dataLength) {
        System.out.println("Testing buffer[" + capacity +"] with " + dataLength + " puts.");
        Buffer buffer = new Buffer(capacity);
        for (int i=0; i<dataLength; i++) {
            buffer.put(i);
        }
        
        double[] values = buffer.getValues();
        int expectedLength = (dataLength< capacity) ? dataLength : capacity;
        assertEquals("Value length is not correct!", expectedLength, values.length);
        for (int i=0; i<expectedLength; i++) {
            assertEquals("Wrong value at index " + i +".",
                    (double) (dataLength - expectedLength + i), values[i], 0.0001);
        }
    }
    
}
