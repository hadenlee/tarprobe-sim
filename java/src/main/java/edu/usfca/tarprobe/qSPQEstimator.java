package edu.usfca.tarprobe;

import edu.usfca.tarprobe.common.Entity;
import edu.usfca.tarprobe.common.Entity.Type;

public class qSPQEstimator {

  public static void test2(qSPQ.Params params) {
    final int M = 5000;
    final int FLAG_H = 0, FLAG_L = 1;
    for (Type poiType : Entity.Type.values()) {
      final int maxT = params.initTrain + params.GROUP_LENGTH * M;
      final Type antiType = poiType == Entity.Type.Hi ? Entity.Type.Lo : Entity.Type.Hi;

      // [ # packets in Hi ][ # packets in Lo ][ 0 = Hi being processed / 1 = Lo being processed ]
      double[][][] prev = new double[params.Q_CAPACITY + 1][params.Q_CAPACITY + 1][2];

      int cntPOIArrived = 0;
      double expPOIReceived = 0.;
      for (int i = 1; i <= maxT; i++) {

        boolean isPOI = false;
        if (i > params.initTrain && (i - params.initTrain) % params.GROUP_LENGTH == 0) {
          cntPOIArrived++;
          isPOI = true;
        }
        final Type currentType = i <= params.initTrain ? Entity.Type.Hi : (isPOI ? poiType : antiType);
        final double probToHighQ = currentType == Entity.Type.Hi ? params.probH : (1. - params.probL);

        if (i == 1) {
          prev[1][0][FLAG_H] = probToHighQ;
          prev[0][1][FLAG_L] = 1 - probToHighQ;
          continue;
        }

        if (isPOI && i + params.Q_CAPACITY * params.TIME_TO_PROCESS <= maxT) {
          double sumP = 0.0;
          if (i % params.TIME_TO_PROCESS == 1) {
            for (int qL = 0; qL <= params.Q_CAPACITY; qL++)
              sumP += prev[params.Q_CAPACITY][qL][FLAG_L];
          } else {
            for (int qL = 0; qL <= params.Q_CAPACITY; qL++)
              sumP += prev[params.Q_CAPACITY][qL][FLAG_L] + prev[params.Q_CAPACITY][qL][FLAG_H];
          }
          expPOIReceived += probToHighQ * (1.0 - sumP);
        }

        double[][][] next = new double[params.Q_CAPACITY + 1][params.Q_CAPACITY + 1][2];
        double tot = 0.;
        for (int qH = 0; qH <= params.Q_CAPACITY; qH++) {
          for (int qL = 0; qL <= params.Q_CAPACITY; qL++) {
            if (i % params.TIME_TO_PROCESS == 1) {
              // Depending on "flag", one of the queue's packet is going to be processed by the end of this tick.
              next[qH][qL][FLAG_H] = 0.;
              if (qH > 0) { // qH = 0 implies FLAG_H can't happen.
                next[qH][qL][FLAG_H] += probToHighQ * prev[qH][qL][FLAG_H];
                if (qL < params.Q_CAPACITY)
                  next[qH][qL][FLAG_H] += probToHighQ * prev[qH - 1][qL + 1][FLAG_L];
                if (qH == params.Q_CAPACITY && qL < params.Q_CAPACITY)
                  next[qH][qL][FLAG_H] += probToHighQ * prev[qH][qL + 1][FLAG_L];

                next[qH][qL][FLAG_H] += (1 - probToHighQ) * prev[qH][qL][FLAG_L];
                if (qH < params.Q_CAPACITY) {
                  if (qL > 0)
                    next[qH][qL][FLAG_H] += (1 - probToHighQ) * prev[qH + 1][qL - 1][FLAG_H];
                  if (qL == params.Q_CAPACITY) {
                    next[qH][qL][FLAG_H] += (1 - probToHighQ) * prev[qH + 1][qL][FLAG_H];
                  }
                }
              }

              next[qH][qL][FLAG_L] = 0.;
              if (qL > 0) {// qL = 0 implies FLAG_L can't happen.
                // this packet can't be placed to high Q with FLAG_L.
                if (qH == 0) {
                  next[qH][qL][FLAG_L] += (1 - probToHighQ) * prev[qH + 1][qL][FLAG_H];
                  next[qH][qL][FLAG_L] += (1 - probToHighQ) * prev[qH][qL][FLAG_L];
                }
              }
            } else {
              for (int flag = 0; flag < 2; flag++) {
                next[qH][qL][flag] = 0.;

                if (qH > 0) // placed into High Queue
                  next[qH][qL][flag] += probToHighQ * prev[qH - 1][qL][flag];
                if (qH == params.Q_CAPACITY) // High Queue is full and packet is lost.
                  next[qH][qL][flag] += probToHighQ * prev[qH][qL][flag];

                if (qL > 0) // placed into Low Queue
                  next[qH][qL][flag] += (1 - probToHighQ) * prev[qH][qL - 1][flag];
                if (qL == params.Q_CAPACITY) // Low Queue is full and lost.
                  next[qH][qL][flag] += (1 - probToHighQ) * prev[qH][qL][flag];
              }
            }
            tot += next[qH][qL][0] + next[qH][qL][1];
          }
        }

        if (Math.abs(tot - 1.) > 1.e-6) {
          throw new RuntimeException(
            String.format("i = %5d, total prob: %10.8f (i mod TTP = %d)", i, tot, i % params.TIME_TO_PROCESS));
        }

        prev = next;
      }
      System.out
        .format("[%s] arrived: %d  exp.received: %8.2f  loss rate: %6.2f  (prob = %.1f  %.1f)\n", poiType.name(),
          cntPOIArrived, expPOIReceived, 100. - 100. * expPOIReceived / cntPOIArrived, params.probH, params.probL);
    }
  }


  // This is slightly incorrect.
  @Deprecated public static void test(qSPQ.Params params) {
    final int M = 5000;
    for (Type poiType : Entity.Type.values()) {
      final int maxT = params.initTrain + params.GROUP_LENGTH * M;
      final Type antiType = poiType == Entity.Type.Hi ? Entity.Type.Lo : Entity.Type.Hi;
      double[] prev = new double[params.Q_CAPACITY + 1];

      int cntPOIArrived = 0;
      double expPOIReceived = 0.;
      for (int i = 1; i <= maxT; i++) {

        boolean isPOI = false;
        if (i > params.initTrain && (i - params.initTrain) % params.GROUP_LENGTH == 0) {
          cntPOIArrived++;
          isPOI = true;
        }
        final Type currentType = i <= params.initTrain ? Entity.Type.Hi : (isPOI ? poiType : antiType);
        final double probToHighQ = currentType == Entity.Type.Hi ? params.probH : (1. - params.probL);

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
