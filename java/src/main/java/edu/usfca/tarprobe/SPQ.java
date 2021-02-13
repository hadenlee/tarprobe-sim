package edu.usfca.tarprobe;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SPQ {
  public enum Type {
    Lo, Hi;
  }

  class Packet {
    public Type type;
    Integer arrivalTime, timeToProcess;
    boolean ofInterest;

    private Packet() {}

    public Packet(int t, int ttp) {
      arrivalTime = t;
      timeToProcess = ttp;
      ofInterest = false;
    }
  }

  public void simulate(Type typePOI, int maxT) {
    Map<Type, PriorityQueue<Packet>> pq = new HashMap<>();
    for (Type type : Type.values()) {
      pq.put(type, new PriorityQueue<>(10, (a, b) -> (a.arrivalTime).compareTo(b.arrivalTime)));
    }

    final int initTrain = 3;
    final int CAPACITY = 2;
    final int GROUP_LENGTH = 3;
    final int TIME_TO_PROCESS = 2;
    final Type typeAntiPOI = typePOI == Type.Hi ? Type.Lo : Type.Hi;

    Packet current = null;
    int cntReceivedPOI = 0, cntLostPOI = 0, cntProcessedPOI = 0;
    for (int t = 1; t <= maxT; t++) {
      // -------------------------------------------------------------------
      // A new packet arrives at time t.
      Packet p = new Packet(t, TIME_TO_PROCESS);
      if (t <= initTrain) {
        p.type = Type.Hi; // Initial train is always of high type.
      } else {
        if ((t - initTrain) % GROUP_LENGTH == 0) {
          p.type = typePOI;
          p.ofInterest = true; // <- This is a "tagged" packet, we want to calculate the loss rate of.
          cntReceivedPOI++;
        } else {
          p.type = typeAntiPOI;
        }
      }
      System.out.format("Time: %2d -> Packet (%s): ", p.arrivalTime, p.type);
      // -------------------------------------------------------------------
      if (pq.get(p.type).size() == CAPACITY) {
        System.out.format("  This packet is lost.\n");
        if (p.ofInterest)
          cntLostPOI++;
      } else {
        pq.get(p.type).add(p);
        System.out.format("  This packet is added to the queue (now queue size = %d).\n", pq.get(p.type).size());
      }
      if (current == null) {
        if (pq.get(Type.Hi).size() > 0) { // peek High packet.
          current = pq.get(Type.Hi).peek();
        } else if (pq.get(Type.Lo).size() > 0) { // peek Low packet.
          current = pq.get(Type.Lo).peek();
        }
      }

      if (current != null) {
        current.timeToProcess--;
        if (current.timeToProcess == 0) { // Remove packet from queue.
          if (current.ofInterest)
            cntProcessedPOI++;
          pq.get(current.type).poll();
          System.out.format("Packet (%s at %2d) has been processed. Removed from queue.\n", current.type,
              current.arrivalTime);
          current = null;
        } else {
          System.out.format("Packet (%s at %2d) needs %d more ticks to process.\n", current.type, current.arrivalTime,
              current.timeToProcess);
        }
      }
      System.out.format("\n");
    }
    System.out.format("POI Summary: %d received, %d lost, %d processed, %d still in queue (loss rate %3d%% - %3d%%)\n",
        cntReceivedPOI, cntLostPOI, cntProcessedPOI, cntReceivedPOI - cntLostPOI - cntProcessedPOI, //
        cntLostPOI * 100 / cntReceivedPOI, (cntReceivedPOI - cntProcessedPOI) * 100 / cntReceivedPOI);

  }
}
