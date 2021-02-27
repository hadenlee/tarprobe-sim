package edu.usfca.tarprobe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public class SPQ {
  public enum Type {
    Lo, Hi;
  }


  class Packet {
    public Type type;
    Integer arrivalTime, timeToProcess;
    boolean ofInterest;

    private Packet() {
    }

    public Packet(int t, int ttp) {
      arrivalTime = t;
      timeToProcess = ttp;
      ofInterest = false;
    }
  }


  class State {
    int position, hq_size, lq_size, timeToProcess;
    boolean newPacketPOI;
    Type currentType, newPacketType;

    public State(int position, Type newPacketType, boolean newPacketPOI, int hq_size, int lq_size, int timeToProcess,
      Type currentType) {
      this.position = position;
      this.newPacketType = newPacketType;
      this.newPacketPOI = newPacketPOI;
      this.hq_size = hq_size;
      this.lq_size = lq_size;
      this.timeToProcess = timeToProcess;
      this.currentType = currentType;
    }

    @Override public String toString() {
      return String.format("Position (%2d) Packet (%s %d) HQ (%2d) LQ (%2d) Current(%2d / %s)",//
        position, newPacketType.name(), newPacketPOI ? 1 : 0, hq_size, lq_size, timeToProcess,
        timeToProcess >= 0 ? currentType.name() : "--");
    }

    @Override public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      State state = (State) o;
      return position == state.position && hq_size == state.hq_size && lq_size == state.lq_size
        && timeToProcess == state.timeToProcess && newPacketPOI == state.newPacketPOI
        && currentType == state.currentType && newPacketType == state.newPacketType;
    }

    @Override public int hashCode() {
      return com.google.common.base.Objects
        .hashCode(position, hq_size, lq_size, timeToProcess, newPacketPOI, currentType, newPacketType);
    }
  }


  static class Summary {
    final int arrived, lost, proecssed, inQueue;

    public Summary(int arrived, int lost, int processed, int inQueue) {
      this.arrived = arrived;
      this.lost = lost;
      this.proecssed = processed;
      this.inQueue = inQueue;
    }
  }


  static class Params {
    final int initTrain; // n_I: Initial # of High packets
    final int Q_CAPACITY; // Q: Capacity of each queue
    final int GROUP_LENGTH; // This is equal to (N' + 1).
    final int TIME_TO_PROCESS; // This is (1 / Z) assuming that r = 1.

    public Params(int initTrain, int qCap, int groupLen, int ttp) {
      this.initTrain = initTrain;
      this.Q_CAPACITY = qCap;
      this.GROUP_LENGTH = groupLen;
      this.TIME_TO_PROCESS = ttp;
    }

  }

  public Summary simulate(Params params, Type typePOI, int maxT, boolean verbose) {
    Map<Type, PriorityQueue<Packet>> pq = new HashMap<>();
    for (Type type : Type.values()) {
      pq.put(type, new PriorityQueue<>(10, (a, b) -> (a.arrivalTime).compareTo(b.arrivalTime)));
    }

    final Type typeAntiPOI = typePOI == Type.Hi ? Type.Lo : Type.Hi;

    Packet current = null;
    int cntArrivedPOI = 0, cntLostPOI = 0, cntProcessedPOI = 0;

    Map<State, List<Integer>> history = new HashMap<>();
    Set<Integer> periods = new TreeSet<>();
    for (int t = 1; t <= maxT; t++) {
      // -------------------------------------------------------------------
      // A new packet arrives at time t.
      Packet p = new Packet(t, params.TIME_TO_PROCESS);
      if (t <= params.initTrain) {
        p.type = Type.Hi; // Initial train is always of high type.
      } else {
        if ((t - params.initTrain) % params.GROUP_LENGTH == 0) {
          p.type = typePOI;
          p.ofInterest = true; // <- This is a "tagged" packet, we want to calculate the loss rate of.
          cntArrivedPOI++;
        } else {
          p.type = typeAntiPOI;
        }
      }
      State st = new State(t <= params.initTrain ? -t : (t - params.initTrain) % params.GROUP_LENGTH, //
        p.type, p.ofInterest,//
        pq.get(Type.Hi).size(),//
        pq.get(Type.Lo).size(),//
        current == null ? -1 : current.timeToProcess, current == null ? null : current.type);
      if (verbose) {
        history.putIfAbsent(st, new ArrayList<>());
        List<Integer> list = history.get(st);
        list.add(t);
        if (list.size() > 1) {
          periods.add(list.get(list.size() - 1) - list.get(list.size() - 2));
        }
        System.out
          .format("Time: %2d -> Packet (%s): %s  [%s]", t, p.type, st, Arrays.toString(history.get(st).toArray()));
      }
      // -------------------------------------------------------------------
      if (pq.get(p.type).size() == params.Q_CAPACITY) {
        if (verbose)
          System.out.format("  This packet is lost.\n");
        if (p.ofInterest)
          cntLostPOI++;
      } else {
        pq.get(p.type).add(p);
        if (verbose)
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
          if (verbose)
            System.out.format("Packet (%s at %2d) has been processed. Removed from queue.\n", current.type,
              current.arrivalTime);
          current = null;
        } else {
          if (verbose)
            System.out.format("Packet (%s at %2d) needs %d more ticks to process.\n", current.type, current.arrivalTime,
              current.timeToProcess);
        }
      }
      if (verbose)
        System.out.format("\n");
    }
    System.out
      .format("POI Summary: %3d arrived, %3d lost, %3d processed, %3d still in queue (loss rate %3d%% - %3d%%)\t",
        cntArrivedPOI, cntLostPOI, cntProcessedPOI, cntArrivedPOI - cntLostPOI - cntProcessedPOI, //
        cntLostPOI * 100 / cntArrivedPOI, (cntArrivedPOI - cntProcessedPOI) * 100 / cntArrivedPOI);
    System.out.format("   Period(s) found: %s\n\n", Arrays.toString(periods.toArray()));
    return new Summary(cntArrivedPOI, cntLostPOI, cntProcessedPOI, cntArrivedPOI - cntLostPOI - cntProcessedPOI);
  }
}
