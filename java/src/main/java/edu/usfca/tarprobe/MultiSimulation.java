package edu.usfca.tarprobe;

import edu.usfca.tarprobe.qSPQ.MultiSummary;

import java.util.ArrayList;
import java.util.List;

public class MultiSimulation {
  public static void run() {

    qSPQ ins = new qSPQ();

    final int numRepeats = 100;

    qSPQ.Params params = new qSPQ.Params(3, 2, 3, 2, 0.9, 0.9);
    List<String> expResult = new ArrayList<>();
    // plot No = 1  (but only up to 5K packets)
    for (int M : new int[] {10, 20, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000}) {
      MultiSummary ms1 =
        ins.simulateMultiple(params, qSPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, numRepeats);
      MultiSummary ms2 =
        ins.simulateMultiple(params, qSPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, numRepeats);

      System.out.format("[Hi Summary]: %s\n", ms1.toString());
      System.out.format("[Lo Summary]: %s\n", ms2.toString());
      System.out.format("\n");
    }
  }
}
