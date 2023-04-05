package executable;

import other.soTSP;
import solver.TSP;

public class Solver {

    public static void main(String[] args) {

        if (args.length < 2) System.out.println("Not enough agrs");

        String instance = args[args.length - 1];


        if (args[0].equals("stackoverflow")) {
            //System.out.println("SO version");

            soTSP TSP = new soTSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;
            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);


        } else if (args[0].equals("no filtering")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = false;
            TSP.repCost = false;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        } else if (args[0].equals("repCost")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = false;
            TSP.repCost = true;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("margCost")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = false;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("margCost + repCost")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("force branching")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;
            TSP.branching = "force";

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("force DFS")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;
            TSP.branching = "force";
            TSP.search = "DFS";

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("LCS")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;
            TSP.branching = "force";
            TSP.lastConflict = true;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }else if (args[0].equals("incremental Lagrangian")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;
            TSP.branching = "force";
            TSP.lastConflict = false;
            TSP.incrementalPi = true;

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }
        else if (args[0].equals("incremental Lagrangian + LCS")) {
            TSP TSP = new TSP();
            TSP.xmlReader(instance);
            TSP.verbose = false;

            TSP.margCost = true;
            TSP.repCost = true;
            TSP.branching = "force";
            TSP.lastConflict = true;
            TSP.incrementalPi = true;
            TSP.search = "DFS on BFS";

            Long time = System.currentTimeMillis();
            TSP.solve();
            System.out.println(System.currentTimeMillis() - time);
        }
        else{
            System.out.println("Not valid arguments");
        }
    }

}
