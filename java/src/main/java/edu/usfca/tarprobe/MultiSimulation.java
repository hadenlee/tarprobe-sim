package edu.usfca.tarprobe;

import edu.usfca.tarprobe.common.Entity;
import edu.usfca.tarprobe.qSPQ.MultiSummary;

import java.util.ArrayList;
import java.util.List;

public class MultiSimulation {
  public static void run(qSPQ.Params params) {

    qSPQ ins = new qSPQ();

    double probH = params.probH, probL = params.probL;
    final int numRepeats = probH == 1.0 && probL == 1.0 ? 1 : 200;

    List<String> expResult = new ArrayList<>();
    // plot No = 1  (but only up to 5K packets)
    for (int M : new int[] {5000}) {
      MultiSummary ms1 =
        ins.simulateMultiple(params, Entity.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, numRepeats);
      MultiSummary ms2 =
        ins.simulateMultiple(params, Entity.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, numRepeats);

      System.out.format("[Hi Summary]: %s\n", ms1.toString());
      System.out.format("[Lo Summary]: %s\n", ms2.toString());
      System.out.format("\n");
    }
    System.out.format("------------------------- done for pH = %.2f  pL = %.2f\n\n", probH, probL);
  }
}
