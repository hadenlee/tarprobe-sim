package edu.usfca.tarprobe.extra;

import edu.usfca.tarprobe.common.Entity.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DiffProbe {
  static class Packet {
    final Type type;
    final int groupId, orderInGroup, sentAt;
    int timeToProcess, finishedAt;

    private Packet(int groupId, int orderInGroup, Type type, int ttp, int sentAt) {
      this.groupId = groupId;
      this.orderInGroup = orderInGroup;
      this.type = type;
      this.sentAt = sentAt;
      this.timeToProcess = ttp;
    }

    @Override public String toString() {
      return String.format("Type [%s] Group Id [%d] Order [%d]", //
        type, groupId, orderInGroup);
    }
  }


  // n := # of groups (repeating patterns)
  // r := # of H's between each L
  // Example:
  //   n = 4, r = 1: HL HL HL HL
  //   n = 4, r = 2: HHL HHL HHL HHL
  public static class ConfigDiffProbe {
    final int n, r, TTP, Q_CAP;

    public ConfigDiffProbe(int n, int r, int TTP, int Q_CAP) {
      this.n = n;
      this.r = r;
      this.TTP = TTP;
      this.Q_CAP = Q_CAP;
    }
  }

  public static void run(ConfigDiffProbe config) {
    Map<Type, Queue<Packet>> pq = new HashMap<>();
    Map<Type, Queue<Packet>> received = new HashMap<>();
    for (Type type : Type.values()) {
      pq.put(type, new LinkedList<>());
      received.put(type, new LinkedList<>());
    }

    Packet current = null;
    int currentTimestamp = 0;

    for (int i = 0; i < config.n; i++) {
      for (int j = 0; j <= config.r; j++, currentTimestamp++) {
        final Type t = j == config.r ? Type.Lo : Type.Hi;
        final Packet p = new Packet(i, j, t, config.TTP, currentTimestamp);

        if (pq.get(p.type).size() == config.Q_CAP) { // Q is full; lost.

        } else { // Add to Queue
          pq.get(p.type).add(p);
        }

        if (current == null) {
          if (pq.get(Type.Hi).size() > 0) {
            current = pq.get(Type.Hi).peek();
          } else if (pq.get(Type.Lo).size() > 0) {
            current = pq.get(Type.Lo).peek();
          }
        }

        if (current != null) {
          current.timeToProcess--;
          if (current.timeToProcess == 0) {
            current.finishedAt = currentTimestamp + 1;
            pq.get(current.type).poll();
            received.get(current.type).add(current);
            current = null;
          }
        }
      }
    }

    System.out.format("[Status] No more new packets. %d (H) and %d (L) received.\n", received.get(Type.Hi).size(),
      received.get(Type.Lo).size());

    for (; pq.get(Type.Hi).size() > 0 || pq.get(Type.Lo).size() > 0; currentTimestamp++) {
      if (current == null) {
        if (pq.get(Type.Hi).size() > 0) {
          current = pq.get(Type.Hi).peek();
        } else if (pq.get(Type.Lo).size() > 0) {
          current = pq.get(Type.Lo).peek();
        }
      }

      if (current != null) {
        current.timeToProcess--;
        if (current.timeToProcess == 0) {
          current.finishedAt = currentTimestamp;
          pq.get(current.type).poll();
          received.get(current.type).add(current);
          current = null;
        }
      }
    }

    long delayHi = 0, delayLo = 0;
    for (Packet p : received.get(Type.Hi)) {
      delayHi += p.finishedAt - p.sentAt - config.TTP;
    }
    for (Packet p : received.get(Type.Lo)) {
      delayLo += p.finishedAt - p.sentAt - config.TTP;
    }

    System.out.format("[Status] Completed. %d (H) and %d (L) received. (%.2f vs %.2f)\n",//
      received.get(Type.Hi).size(), received.get(Type.Lo).size(),//
      (double) delayHi / received.get(Type.Hi).size(),//
      (double) delayLo / received.get(Type.Lo).size()//
    );

    System.out.format("==========================================\n");
  }
}
