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

    @Override
    public void pipeUpdated(Pipe<String> pipe) {
        System.out.println("Pipe updated: " + pipe);
    }
    
    
}
