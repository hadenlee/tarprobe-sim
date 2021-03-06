package edu.usfca.tarprobe;

import edu.usfca.tarprobe.SPQ.Params;
import edu.usfca.tarprobe.SPQ.Summary;
import edu.usfca.tarprobe.extra.DiffProbe;
import edu.usfca.tarprobe.extra.DiffProbe.ConfigDiffProbe;

import java.util.ArrayList;
import java.util.List;

/**
 * Follow the instructions on github.
 */
public class Main {

  public static void main(String[] args) {
    System.out.format("ta-da!\n");
    ConfigDiffProbe diff;

    for (int r = 1; r <= 4; r++) {
      diff = new ConfigDiffProbe(100, r, 2, 75);
      DiffProbe.run(diff);
    }

    //    ConfigPopi popi = new ConfigPopi(2, 4);
    //    POPI.run(popi);

    //    tarprobeDPSimulation(); // Apr 13


  }

  static void tarprobeDPSimulation() {
    qSPQ.Params params;

    for (int gl = 3; gl <= 6; gl++) {
      for (int offset = 0; offset <= 5; offset++) {
        for (int pp = 100; pp >= 50; pp -= 10) {
          double prob = pp / 100.;
          params = new qSPQ.Params(150 + offset, 75, gl, 5, prob, prob);
          qSPQEstimator.test(params);
          System.out.format("\n");
          qSPQEstimator.test2(params);
          System.out.format("\n");
          System.out.format("================= prob = %.2f (%s) ================\n", prob, params.toString());
          //      MultiSimulation.run(params);
        }
      }
    }

    //    params = new qSPQ.Params(200, 75, 4, 5, 0.5, 0.5);
    //    qSPQEstimator.test(params, Type.Hi, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    qSPQEstimator.test(params, Type.Lo, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    System.out.format("\n");
    //
    //    params = new qSPQ.Params(200, 75, 5, 5, 0.5, 0.5);
    //    qSPQEstimator.test(params, Type.Hi, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    qSPQEstimator.test(params, Type.Lo, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    System.out.format("\n");
    //
    //    params = new qSPQ.Params(200, 75, 6, 5, 0.5, 0.5);
    //    qSPQEstimator.test(params, Type.Hi, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    qSPQEstimator.test(params, Type.Lo, params.initTrain + 5000 * params.GROUP_LENGTH);
    //    System.out.format("\n");


    //    MultiSimulation.run(0.9, 0.9);
    //    MultiSimulation.run(0.8, 0.8);
    //    MultiSimulation.run(0.7, 0.7);
    //    MultiSimulation.run(0.6, 0.6);
    //    MultiSimulation.run(0.5, 0.5);
  }


  public static void plots() { // static SPQ, to match plots from NS-3 simulations.
    SPQ ins = new SPQ();

    final int plotNo = 3;

    if (plotNo == 1) {
      // plot 1
      Params params = new Params(150, 75, 4, 5);
      List<String> expResult = new ArrayList<>();
      for (int M : new int[] {10, 20, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000,
        10000}) {
        Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
        Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
        expResult.add(String.format("%d,%d,%d,%d,%d", M, s1.getLossRate().getLeft(), s2.getLossRate().getLeft(),//
          s1.getLossRate().getRight(), s2.getLossRate().getRight()));
      }
      System.out.format("%s\n\n", String.join("\n", expResult));
    }

    if (plotNo == 2) {
      // plot 2
      final int M = 500;
      List<String> expResult = new ArrayList<>();
      for (int np = 1; np <= 20; np++) {
        Params params = new Params(150, 75, np + 1, 5);

        Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
        Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
        expResult.add(String.format("%d,%d,%d,%d,%d", np, s1.getLossRate().getLeft(), s2.getLossRate().getLeft(),//
          s1.getLossRate().getRight(), s2.getLossRate().getRight()));
      }
      System.out.format("%s\n\n", String.join("\n", expResult));
    }

    if (plotNo == 3) {
      // plot 3
      final int M = 500;
      for (int np = 2; np <= 6; np++) {
        List<String> expResult = new ArrayList<>();
        System.out.format("N' = %d\n", np);
        for (int ttp = 10; ttp >= 1; ttp--) {
          Params params = new Params(150, 75, np + 1, ttp);

          Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
          Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
          expResult.add(String
            .format("%.1f,%d,%d,%d,%d", (double) 10. / ttp, s1.getLossRate().getLeft(), s2.getLossRate().getLeft(),//
              s1.getLossRate().getRight(), s2.getLossRate().getRight()));
        }
        System.out.format("%s\n\n", String.join("\n", expResult));
        System.out.format("================[Done N' = %d]\n", np);
      }
    }
  }
}
