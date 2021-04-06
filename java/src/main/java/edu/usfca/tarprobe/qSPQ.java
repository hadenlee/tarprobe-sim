package edu.usfca.tarprobe;

import com.google.common.base.Objects;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class qSPQ {
  public enum Type {
    Hi, Lo;
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


  static class QueueState {
    final List<Boolean> isPOI;
    final List<Type> packetTypes;
    final int n;

    public QueueState(Queue<Packet> qu) {
      n = qu.size();
      isPOI = new ArrayList<>();
      packetTypes = new ArrayList<>();
      for (Packet p : qu) {
        isPOI.add(p.ofInterest);
        packetTypes.add(p.type);
      }
    }

    @Override public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof QueueState))
        return false;
      QueueState that = (QueueState) o;
      return n == that.n && Objects.equal(isPOI, that.isPOI) && Objects.equal(packetTypes, that.packetTypes);
    }

    @Override public int hashCode() {
      return Objects.hashCode(isPOI, packetTypes, n);
    }
  }


  static class State {
    int position, hq_size, lq_size, timeToProcess;
    QueueState hQ, lQ;
    boolean newPacketPOI;
    Type currentType, newPacketType;

    public State(int position, Type newPacketType, boolean newPacketPOI, Queue<Packet> hq, Queue<Packet> lq,
      int timeToProcess, Type currentType) {
      this.position = position;
      this.newPacketType = newPacketType;
      this.newPacketPOI = newPacketPOI;
      this.hq_size = hq.size();
      this.hQ = new QueueState(hq);
      this.lq_size = lq.size();
      this.lQ = new QueueState(lq);
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
      if (!(o instanceof State))
        return false;
      State state = (State) o;
      return position == state.position && hq_size == state.hq_size && lq_size == state.lq_size
        && timeToProcess == state.timeToProcess && newPacketPOI == state.newPacketPOI && Objects.equal(hQ, state.hQ)
        && Objects.equal(lQ, state.lQ) && currentType == state.currentType && newPacketType == state.newPacketType;
    }

    @Override public int hashCode() {
      return Objects
        .hashCode(position, hq_size, lq_size, timeToProcess, hQ, lQ, newPacketPOI, currentType, newPacketType);
    }
  }


  static class Summary {
    final int arrived, lost, processed, inQueue;
    final Set<Integer> periods;

    public Summary(int arrived, int lost, int processed, int inQueue, Set<Integer> periods) {
      this.arrived = arrived;
      this.lost = lost;
      this.processed = processed;
      this.inQueue = inQueue;
      this.periods = new TreeSet<>(periods);
    }

    Pair<Integer, Integer> getLossRate() {
      return Pair.of(lost * 100 / arrived, (arrived - processed) * 100 / arrived);
    }

    int getSmallestPeriod() {
      if (periods.iterator().hasNext())
        return periods.iterator().next();
      return -1;
    }
  }


  static class Params {
    final int initTrain; // n_I: Initial # of High packets
    final int Q_CAPACITY; // Q: Capacity of each queue
    final int GROUP_LENGTH; // This is equal to (N' + 1).
    final int TIME_TO_PROCESS; // This is (1 / Z) assuming that r = 1.
    final double probH; // This defines probability of "sticking to the plan" for H packets
    final double probL; // This defines probability of "sticking to the plan" for L packets

    public Params(int initTrain, int qCap, int groupLen, int ttp) {
      this.initTrain = initTrain;
      this.Q_CAPACITY = qCap;
      this.GROUP_LENGTH = groupLen;
      this.TIME_TO_PROCESS = ttp;
      this.probH = this.probL = 1.0;
    }

    public Params(int initTrain, int qCap, int groupLen, int ttp, double pH, double pL) {
      this.initTrain = initTrain;
      this.Q_CAPACITY = qCap;
      this.GROUP_LENGTH = groupLen;
      this.TIME_TO_PROCESS = ttp;
      this.probH = pH;
      this.probL = pL;
    }

    public double getProb(Type t) {
      switch (t) {
        case Hi:
          return probH;
        case Lo:
          return probL;
        default:
          return 0;
      }
    }

    @Override public String toString() {
      return String
        .format("n_i = %3d  Q = %3d  N' = %d  TTP = %d", initTrain, Q_CAPACITY, GROUP_LENGTH - 1, TIME_TO_PROCESS);
    }
  }

  static Type getAntiType(Type t) {
    return t == Type.Hi ? Type.Lo : Type.Hi;
  }

  private Packet getNext(int t, Params params, Type typePOI) {
    final Type typeAntiPOI = getAntiType(typePOI);
    // A new packet arrives at time t.
    Packet p = new Packet(t, params.TIME_TO_PROCESS);
    if (t <= params.initTrain) {
      p.type = Type.Hi; // Initial train is always of high type.
    } else {
      if ((t - params.initTrain) % params.GROUP_LENGTH == 0) {
        p.type = typePOI;
        p.ofInterest = true; // <- This is a "tagged" packet, we want to calculate the loss rate of.
      } else {
        p.type = typeAntiPOI;
      }
    }
    return p;
  }

  private State getCurrentState(int t, Packet p, Params params, Map<Type, Queue<Packet>> pq, Queue<Packet> currentQ) {
    State st = new State(t <= params.initTrain ? -t : (t - params.initTrain) % params.GROUP_LENGTH, //
      p.type, p.ofInterest,//
      pq.get(Type.Hi),//
      pq.get(Type.Lo),//
      currentQ == null ? -1 : currentQ.peek().timeToProcess, //
      currentQ == null ? null : currentQ.peek().type //
    );
    return st;
  }

  private void checkPeriod(int t, State st, Packet p, Map<State, List<Integer>> history, Set<Integer> periods,
    boolean verbose) {
    history.putIfAbsent(st, new ArrayList<>());
    List<Integer> list = history.get(st);
    list.add(t);
    if (list.size() > 1) {
      periods.add(list.get(list.size() - 1) - list.get(list.size() - 2));
    }
    if (verbose) {
      System.out
        .format("Time: %2d -> Packet (%s): %s  [%s]", t, p.type, st, Arrays.toString(history.get(st).toArray()));
    }
  }

  // ------------------------------------------------------------------------------------------------------
  // This runs the simulation once.
  public Summary simulateOnce(Params params, Type typePOI, int maxT, boolean verbose) {
    return simulateOnce(params, typePOI, maxT, verbose, false);
  }

  public Summary simulateOnce(Params params, Type typePOI, int maxT, boolean verbose, boolean suppress) {
    Map<Type, Queue<Packet>> pq = new HashMap<>();
    for (Type type : Type.values()) {
      pq.put(type, new LinkedList<>());
    }


    Queue<Packet> currentQ = null;
    int cntArrivedPOI = 0, cntLostPOI = 0, cntProcessedPOI = 0;

    Map<State, List<Integer>> history = new HashMap<>();
    Set<Integer> periods = new TreeSet<>();

    Random rnd = new Random();
    for (int t = 1; t <= maxT; t++) {
      // -------------------------------------------------------------------
      final Packet p = getNext(t, params, typePOI);
      if (p.ofInterest)
        cntArrivedPOI++;

      // -------------------------------------------------------------------
      final State st = getCurrentState(t, p, params, pq, currentQ);
      if (t > params.initTrain) {
        checkPeriod(t, st, p, history, periods, verbose);
      }
      // -------------------------------------------------------------------
      Type targetType;
      final double prob = params.getProb(p.type);
      if (rnd.nextDouble() <= prob) {
        targetType = p.type;
      } else {
        targetType = getAntiType(p.type);
      }
      Queue<Packet> targetQu = pq.get(targetType);

      if (targetQu.size() == params.Q_CAPACITY) {
        if (verbose)
          System.out.format("  This packet is lost.\n");
        if (p.ofInterest)
          cntLostPOI++;
      } else {
        targetQu.add(p);
        if (verbose)
          System.out.format("  This packet is added to the queue (now queue size = %d).\n", targetQu.size());
      }

      // Process next packet.
      if (currentQ == null) {
        if (pq.get(Type.Hi).size() > 0) { // peek High packet.
          currentQ = pq.get(Type.Hi);
        } else if (pq.get(Type.Lo).size() > 0) { // peek Low packet.
          currentQ = pq.get(Type.Lo);
        }
      }

      if (currentQ != null) {
        final Packet thisPacket = currentQ.peek();
        thisPacket.timeToProcess--;
        if (thisPacket.timeToProcess == 0) { // Remove packet from queue.
          if (thisPacket.ofInterest)
            cntProcessedPOI++;
          if (verbose)
            System.out.format("Packet (%s at %2d) has been processed. Removed from queue.\n", thisPacket.type,
              thisPacket.arrivalTime);
          currentQ.poll();
          currentQ = null;
        } else {
          if (verbose)
            System.out
              .format("Packet (%s at %2d) needs %d more ticks to process.\n", thisPacket.type, thisPacket.arrivalTime,
                thisPacket.timeToProcess);
        }
      }
      if (verbose)
        System.out.format("\n");
    }

    if (!suppress) {
      System.out
        .format("POI Summary: %3d arrived, %3d lost, %3d processed, %3d still in queue (loss rate %3d%% - %3d%%)\t",
          cntArrivedPOI, cntLostPOI, cntProcessedPOI, cntArrivedPOI - cntLostPOI - cntProcessedPOI, //
          cntLostPOI * 100 / cntArrivedPOI, (cntArrivedPOI - cntProcessedPOI) * 100 / cntArrivedPOI);
      System.out.format("   Period(s) found: %s\n\n", Arrays.toString(periods.toArray()));
    }
    return new Summary(cntArrivedPOI, cntLostPOI, cntProcessedPOI, cntArrivedPOI - cntLostPOI - cntProcessedPOI,
      periods);
  }

  // ------------------------------------------------------------------------------------------------------
  static class MultiSummary {
    final Params params;
    final Type typePOI;
    final int maxT;
    final int numRepeats;
    final List<Summary> summaries;

    public MultiSummary(Params params, Type typePOI, int maxT, int numRepeats) {
      this.params = params;
      this.typePOI = typePOI;
      this.maxT = maxT;
      this.numRepeats = numRepeats;
      this.summaries = new ArrayList<>();
    }

    public void add(Summary sum) {
      summaries.add(sum);
    }

    @Override public String toString() {
      int minLR = summaries.stream().map(sum -> sum.getLossRate().getRight()).min(Integer::compare).orElse(200);
      int maxLR = summaries.stream().map(sum -> sum.getLossRate().getRight()).max(Integer::compare).orElse(-1);
      long sumLR = summaries.stream().map(sum -> (long) sum.getLossRate().getRight()).reduce(Long::sum).orElse(0L);

      return String.format(
        "[Loss Rate: min (%3d) max (%3d) avg (%6.2f) over %5d runs] [Params: n_I = %6d  Q_cap = %3d  N' = %2d  (r/Z) = %2d  M = %5d  probH = %5.2f  probL = %5.2f]",
        minLR, maxLR, (double) sumLR / summaries.size(), numRepeats, //
        params.initTrain, params.Q_CAPACITY, params.GROUP_LENGTH - 1, params.TIME_TO_PROCESS,
        (maxT - params.initTrain) / params.GROUP_LENGTH, //
        params.probH, params.probL);
    }
  }

  public MultiSummary simulateMultiple(Params params, Type typePOI, int maxT, int numRepeats) {
    MultiSummary multiSummary = new MultiSummary(params, typePOI, maxT, numRepeats);
    for (int i = 0; i < numRepeats; i++) {
      Summary sum = simulateOnce(params, typePOI, maxT, false, true);
      multiSummary.add(sum);
    }
    return multiSummary;
  }

}
