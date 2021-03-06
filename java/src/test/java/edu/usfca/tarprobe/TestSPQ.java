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
      assertEquals(6, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 25, false);
      assertEquals(7, summaryLo.arrived);
      assertEquals(5, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // --------------------------- The values above had been verified manually.
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 250, false);
      assertEquals(82, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(82, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 250, false);
      assertEquals(82, summaryLo.arrived);
      assertEquals(80, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 252, false);
      assertEquals(83, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(82, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 252, false);
      assertEquals(83, summaryLo.arrived);
      assertEquals(81, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      Summary summaryHi = instance.simulate(params, Type.Hi, 255, false);
      assertEquals(84, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(83, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, 255, false);
      assertEquals(84, summaryLo.arrived);
      assertEquals(82, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }
  }

  @Test public void testPlot1() {
    // Using the following parameters (plot 1):
    // r = 10, Z = 2 (hence r/Z = time-to-process = 5)
    // Q = 75 (both queues)
    // N' = 3 (hence each group length is 4)
    // n_I = 150 (number of packets in init packet train)
    SPQ instance = new SPQ();
    Params params = new Params(150, 75, 4, 5);

    {
      int M = 100;
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(100, summaryHi.arrived);
      assertEquals(20, summaryHi.lost);
      assertEquals(6, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(100, summaryLo.arrived);
      assertEquals(25, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      int M = 1000;
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(1000, summaryHi.arrived);
      assertEquals(200, summaryHi.lost);
      assertEquals(726, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(1000, summaryLo.arrived);
      assertEquals(925, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      int M = 5000;
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(5000, summaryHi.arrived);
      assertEquals(1000, summaryHi.lost);
      assertEquals(3926, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(5000, summaryLo.arrived);
      assertEquals(4925, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      int M = 10000;
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(10000, summaryHi.arrived);
      assertEquals(2000, summaryHi.lost);
      assertEquals(7926, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(10000, summaryLo.arrived);
      assertEquals(9925, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }
  }

  @Test public void testPlot2() {
    // Using the following parameters (plot 2):
    // r = 10, Z = 2 (hence r/Z = time-to-process = 5)
    // Q = 75 (both queues)
    // N' = 1, 2, 5, 10
    // n_I = 150 (number of packets in init packet train)
    // M = 500 (# of groups)
    SPQ instance = new SPQ();
    final int M = 500;

    {
      Params params = new Params(150, 75, 1 + 1, 5);
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryHi.arrived);
      assertEquals(300, summaryHi.lost);
      assertEquals(126, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryLo.arrived);
      assertEquals(425, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      Params params = new Params(150, 75, 2 + 1, 5);
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryHi.arrived);
      assertEquals(200, summaryHi.lost);
      assertEquals(226, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryLo.arrived);
      assertEquals(425, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      Params params = new Params(150, 75, 5 + 1, 5);
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(499, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryLo.arrived);
      assertEquals(425, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }

    {
      Params params = new Params(150, 75, 10 + 1, 5);
      Summary summaryHi = instance.simulate(params, Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryHi.arrived);
      assertEquals(0, summaryHi.lost);
      assertEquals(499, summaryHi.processed);

      Summary summaryLo = instance.simulate(params, Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      assertEquals(500, summaryLo.arrived);
      assertEquals(425, summaryLo.lost);
      assertEquals(0, summaryLo.processed);
      // ---------------------------
    }
  }
}
