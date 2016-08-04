package ballantines.avionics.blackbox.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class CalculusTest {
    
    public CalculusTest() {
    }

    @Test
    public void testParseDegreeToDecimal() {
        double deg = Calculus.parseDegreeToDecimal("N50°30'00.00\"");
        assertEquals("N50°30'00.00\"", 50.5, deg, 0.0001);
        deg = Calculus.parseDegreeToDecimal("S50°30'00.00\"");
        assertEquals("S50°30'00.00\"", -50.5, deg, 0.0001);
        deg = Calculus.parseDegreeToDecimal("E050°30'00.00\"");
        assertEquals("E050°30'00.00\"", 50.5, deg, 0.0001);
        deg = Calculus.parseDegreeToDecimal("W050°30'00.00\"");
        assertEquals("W050°30'00.00\"", -50.5, deg, 0.0001);
        deg = Calculus.parseDegreeToDecimal("N52°31'12.025\"");
        assertEquals("N52°31'12.025\"", 52.520007, deg, 0.000001);    
        deg = Calculus.parseDegreeToDecimal("E013°24'17.834\"");
        assertEquals("E013°24'17.834\"", 13.404954, deg, 0.000001);
    }
    
}
