package edu.usfca.tarprobe;

import edu.usfca.tarprobe.qSPQ.Type;

public class qSPQEstimator {

  public static void test(qSPQ.Params params) {
    final int M = 5000;
    for (Type poiType : Type.values()) {
      final int maxT = params.initTrain + params.GROUP_LENGTH * M;
      final Type antiType = poiType == Type.Hi ? Type.Lo : Type.Hi;
      double[] prev = new double[params.Q_CAPACITY + 1];

      int cntPOIArrived = 0;
      double expPOIReceived = 0.;
      for (int i = 1; i <= maxT; i++) {

        boolean isPOI = false;
        if (i > params.initTrain && (i - params.initTrain) % params.GROUP_LENGTH == 0) {
          cntPOIArrived++;
          isPOI = true;
        }
        final Type currentType = i <= params.initTrain ? Type.Hi : (isPOI ? poiType : antiType);
        final double probToHighQ = currentType == Type.Hi ? params.probH : (1. - params.probL);

        if (i == 1) {
          prev[1] = probToHighQ;
          prev[0] = 1 - probToHighQ;
          continue;
        }

        if (isPOI && i + params.Q_CAPACITY * params.TIME_TO_PROCESS <= maxT) {
          if (i % params.TIME_TO_PROCESS == 1) {
            expPOIReceived += probToHighQ;
          } else {
            expPOIReceived += probToHighQ * (1.0 - prev[params.Q_CAPACITY]);
          }
        }


        double[] next = new double[params.Q_CAPACITY + 1];
        double tot = 0.;
        for (int q = 0; q <= params.Q_CAPACITY; q++) {
          if (i % params.TIME_TO_PROCESS == 1) { // It's guaranteed that there is a space in the High queue.
            if (q == 0) {
              next[q] = prev[q] + prev[q + 1] * (1. - probToHighQ);
            } else {
              next[q] = probToHighQ * prev[q] + (1. - probToHighQ) * (q == params.Q_CAPACITY ? 0 : prev[q + 1]);
            }
          } else {
            if (q == params.Q_CAPACITY) {
              next[q] = prev[q] + prev[q - 1] * probToHighQ;
            } else {
              next[q] = probToHighQ * (q == 0 ? 0 : prev[q - 1]) + (1 - probToHighQ) * prev[q];
            }
          }
          tot += next[q];
        }
        if (Math.abs(tot - 1.) > 1.e-6) {
          throw new RuntimeException(String.format("i = %5d, total prob: %10.8f", i, tot));
        }
        prev = next;
      }
      System.out
        .format("[%s] arrived: %d  exp.received: %8.2f  loss rate: %6.2f  (prob = %.1f  %.1f)\n", poiType.name(),
          cntPOIArrived, expPOIReceived, 100. - 100. * expPOIReceived / cntPOIArrived, params.probH, params.probL);
    }
  }

}
