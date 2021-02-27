package edu.usfca.tarprobe;



import edu.usfca.tarprobe.SPQ.Params;
import edu.usfca.tarprobe.SPQ.Summary;

/**
 * Follow the instructions on github.
 */
public class Main {
  public static void main(String[] args) {
    System.out.format("ta-da!\n");
    SPQ ins = new SPQ();

    final int plotNo = 3;

    if (plotNo == 1) {
      // plot 1
      Params params = new Params(150, 75, 4, 5);
      for (int M : new int[] {10, 20, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000,
        10000}) {
        Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
        Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      }
    }

    if (plotNo == 2) {
      // plot 2
      final int M = 500;
      for (int np = 1; np <= 20; np++) {
        Params params = new Params(150, 75, np + 1, 5);

        Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
        Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
      }
    }

    if (plotNo == 3) {
      // plot 3
      final int M = 500;
      for (int np = 2; np <= 6; np++) {
        System.out.format("N' = %d\n", np);
        for (int ttp = 10; ttp >= 1; ttp--) {
          Params params = new Params(150, 75, np + 1, ttp);

          Summary s1 = ins.simulate(params, SPQ.Type.Hi, params.initTrain + M * params.GROUP_LENGTH, false);
          Summary s2 = ins.simulate(params, SPQ.Type.Lo, params.initTrain + M * params.GROUP_LENGTH, false);
        }
        System.out.format("================[Done N' = %d]\n", np);
      }
    }

  }
}
