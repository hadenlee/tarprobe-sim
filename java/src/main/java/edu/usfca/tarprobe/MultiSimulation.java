package edu.usfca.tarprobe;

import edu.usfca.tarprobe.qSPQ.MultiSummary;

import java.util.ArrayList;
import java.util.List;

public class MultiSimulation {
  public static void run(double probH, double probL) {

    qSPQ ins = new qSPQ();

    final int numRepeats = probH == 1.0 && probL == 1.0 ? 1 : 200;

    qSPQ.Params params = new qSPQ.Params(150, 75, 4, 5, probH, probL);
    List<String> expResult = new ArrayList<>();
    // plot No = 1  (but only up to 5K packets)
    for (int M : new int[] {1000, 2000, 3000, 4000, 5000}) {
      MultiSummary ms1 =
        ins.simulateMultiple(params, qSPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, numRepeats);
      MultiSummary ms2 =
        ins.simulateMultiple(params, qSPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, numRepeats);

      System.out.format("[Hi Summary]: %s\n", ms1.toString());
      System.out.format("[Lo Summary]: %s\n", ms2.toString());
      System.out.format("\n");
    }
    System.out.format("------------------------- done for pH = %.2f  pL = %.2f\n\n", probH, probL);
  }
}
