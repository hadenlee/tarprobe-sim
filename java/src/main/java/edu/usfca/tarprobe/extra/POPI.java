package edu.usfca.tarprobe.extra;



import edu.usfca.tarprobe.common.Entity.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class POPI {
  static class Packet {
    final Type type;
    final int burstId, orderInBurst;

    private Packet(int burstId, int orderInBurst, Type type) {
      this.burstId = burstId;
      this.orderInBurst = orderInBurst;
      this.type = type;
    }

    @Override public String toString() {
      return String.format("Type [%s] Id [%d] Order [%d]",//
        type, burstId, orderInBurst);
    }
  }

  // Example:
  //K=3, n_r=4
  //ABC BAC CAB ACB
  //E.g., HL LH LH HL for two types of classes (K = 2, n = 4)
  // Each block is a random permutation of K packets (and there are n blocks).


  public static class ConfigPopi {
    final int k, n;
    final Random rnd = new Random(686);

    public ConfigPopi(int k, int n) {
      this.k = k;
      this.n = n;
    }

    public int getK() {
      return k;
    }

    public int getN() {
      return n;
    }

    // In a burst of k packets, k packets (of k different types) will be randomly ordered.
    // Then, one of them will be marked as 'type' (either Hi or Low)
    // and the rest (k-1 types) will be marked as anti-type.
    public List<Packet> getBurst(Type type, int bid) {
      List<Packet> burst = new ArrayList<>();
      Type antiType = type == Type.Hi ? Type.Lo : Type.Hi;
      boolean[] used = new boolean[k];
      for (int i = 0; i < k; i++) {
        int x = rnd.nextInt(k);
        while (used[x]) {
          x = rnd.nextInt(k);
        }
        used[x] = true;
        burst.add(new Packet(bid, i, x == 0 ? type : antiType));
      }
      return burst;
    }
  }

  public static void run(ConfigPopi config) {

    /**
     * Detection engine:
     * <p>
     * In the detection phase,
     * the loss rates for all types of traffic (in our case H and L) in each burst are computed and ordered.
     * The traffic type with the largest loss rate of any given burst is placed first,
     * the traffic type with the second largest loss rate is placed next, and so on.
     * According to the authors, if all types of traffic are treated equally,
     * the positions of different types will be random for the different bursts,
     * since packets of different types are sent randomly in each round.
     * However, if some traffic types have low priority,
     * they will always be in the first positions after the ordering is done because they suffer higher losses.
     * At the end of this step, there will be b (# of independent bursts) sets of types of traffic,
     * ordered according to the loss rate observed for each burst.
     * <p>
     * There is a next phase,
     * where POPI uses Problem of N Rankings to compute the average position of each type of traffic over all burst,
     * but I believe this is only useful when there are more than 2 types of traffic
     * and also if the middlebox is not using “Strict” Priority Queueing.
     */

    for (int i = 0; i < config.getN(); i++) {
      List<Packet> burst = config.getBurst(Type.Hi, i);
      for (Packet p : burst) {
        System.out.format("%s\n", p);
      }
      System.out.format("\n");
    }
  }

}
