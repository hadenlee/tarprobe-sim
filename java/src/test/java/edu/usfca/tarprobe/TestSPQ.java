package edu.usfca.tarprobe;

import edu.usfca.tarprobe.SPQ.Params;
import edu.usfca.tarprobe.SPQ.Summary;
import edu.usfca.tarprobe.SPQ.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSPQ {
  @Test public void testSample() {
    // Using the following parameters:
    // r = 1, Z = 1/2 (hence r/Z = time-to-process = 2)
    // Q = 2 (both queues)
    // N' = 2 (hence each group length is 3)
    // n_I = 3 (number of packets in init packet train)
    SPQ instance = new SPQ();
    Params params = new Params(3, 2, 3, 2);

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 25, false);
      assertEquals(7, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(6, summaryHi.proecssed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 25, false);
      assertEquals(7, summaryLo.arrived);
      assertEquals(5, summaryLo.lost);
      assertEquals(0, summaryLo.proecssed);
      // --------------------------- The values above had been verified manually.
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 250, false);
      assertEquals(82, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(82, summaryHi.proecssed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 250, false);
      assertEquals(82, summaryLo.arrived);
      assertEquals(80, summaryLo.lost);
      assertEquals(0, summaryLo.proecssed);
      // ---------------------------
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 252, false);
      assertEquals(83, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(82, summaryHi.proecssed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 252, false);
      assertEquals(83, summaryLo.arrived);
      assertEquals(81, summaryLo.lost);
      assertEquals(0, summaryLo.proecssed);
      // ---------------------------
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 255, false);
      assertEquals(84, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(83, summaryHi.proecssed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 255, false);
      assertEquals(84, summaryLo.arrived);
      assertEquals(82, summaryLo.lost);
      assertEquals(0, summaryLo.proecssed);
      // ---------------------------
    }
  }
}
