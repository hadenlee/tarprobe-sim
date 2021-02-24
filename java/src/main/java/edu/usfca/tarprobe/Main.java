package edu.usfca.tarprobe;



/**
 * Follow the instructions on github.
 */
public class Main {
  public static void main(String[] args) {
    System.out.format("ta-da!\n");
    SPQ ins = new SPQ();

    ins.simulate(SPQ.Type.Hi, 55, true);
    ins.simulate(SPQ.Type.Lo, 55, true);
  }
}
