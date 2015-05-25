/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.pipes;

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
public class PipesTest implements PipeUpdateListener<String> {
    
    public PipesTest() {
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
     * Create 4 pipes and connect them. Change the value of one pipe and check if
     * the others are updated accordingly...
     */
    @Test
    public void testConnect() {
        Pipe<String> p1 = Pipe.newInstance("P1", this);
        Pipe<String> p2 = Pipe.newInstance("P2", this);
        Pipe<String> p3 = Pipe.newInstance("P3", this);
        Pipe<String> p4 = Pipe.newInstance("P4", this);
        
        Pipe<String>[] pipes = new Pipe[] { p1, p2, p3, p4 };
        
        Pipes.connect(p1, p2, p3, p4);
        
        for (int i=0; i<pipes.length; i++) {
            String expected = "Updating P"+(i+1);
            
            
            
            pipes[i].set(expected);
            
            assertEquals("P1 has wrong value!", expected, p1.get());
            assertEquals("P2 has wrong value!", expected, p2.get());
            assertEquals("P3 has wrong value!", expected, p3.get());
            assertEquals("P4 has wrong value!", expected, p4.get());
            
        }
        
    }
    
    @Test
    public void testPipesConnectPropagation() {
        Pipe<String> p1 = Pipe.newInstance("P1", this);
        Pipe<String> p2 = Pipe.newInstance("P2", this);
        
        p1.set("Initial Value");
        
        Pipes.connect(p1, p2);
        
        assertEquals("P1 value has been overriden!", "Initial Value", p1.get());
        assertEquals("P2 value hasn't been propagated", "Initial Value", p2.get());
    }
    
    @Test
    public void testMaxTransformer() {
        Pipe<Double> input = Pipe.newInstance("input", this);
        Pipe<Double> output = Pipe.newInstance("output", this);
        
        output.connectTo(input, Pipes.MAX_TRANSFORM);
        
        input.set(0.1);
        input.set(2.3);
        input.set(1.2);
        
        assertEquals("output should have max value.", 2.3, output.get(), 0.01);
    }
    
    @Test
    public void testMinTransformer() {
        Pipe<Double> input = Pipe.newInstance("input", this);
        Pipe<Double> output = Pipe.newInstance("output", this);
        
        output.connectTo(input, Pipes.MIN_TRANSFORM);
        
        input.set(2.3);
        input.set(0.1);
        input.set(1.2);
        
        assertEquals("output should have min value.", 0.1, output.get(), 0.01);
    }
    
    
    @Test
    public void testAvgTransformer() {
        Pipe<Double> input = Pipe.newInstance("input", this);
        Pipe<Double> output = Pipe.newInstance("output", this);
        
        output.connectTo(input, Pipes.AVG_TRANSFORM);
        
        input.set(2.0);
        input.set(1.0);
        
        assertEquals("output should have avg value.", 1.5, output.get(), 0.001);
    }
    
    @Test
    public void testConvertingTransformer() {
        Transformer<String, Integer> formatter = new Transformer<String, Integer>() {
            public String transform(String target, Integer source) {
                return source + " ft";
            }
        };
        
        Pipe<Integer> input = Pipe.newInstance("input", this);
        Pipe<String> output = Pipe.newInstance("output", this);
        
        output.connectTo(input, formatter);
        
        input.set(1234);
        
        assertEquals("output should have been transformed.", "1234 ft", output.get());
        
        output.disconnectFrom(input);
        input.set(4321);
        assertEquals("output should have been disconnected.", "1234 ft", output.get());
    }

    @Override
    public void pipeUpdated(Pipe<String> pipe) {
        System.out.println("Pipe updated: " + pipe);
    }
    
    
}
