import static org.junit.jupiter.api.Assertions.*;

import solver.TSP;
import org.junit.jupiter.api.Test;
import other.soTSP;

public class GlobalTests {

    @Test
    void outputSmall() {
        // Symetric instances of the TSPlib
        String[] instances = {"burma14", "ulysses16", "gr17", "gr21", "gr24", "fri26", "bayg29", "bays29"}; // , "ulysses22"

        double simpleTSPTime = 0; // ms
        double filterTSPTime = 0; // ms
        long start;

        for (String instance : instances) {
            soTSP trueTSP = new soTSP();
            trueTSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            trueTSP.verbose = false;
            start = System.currentTimeMillis();
            trueTSP.solve();
            simpleTSPTime += (System.currentTimeMillis() - start);

            TSP TSP = new TSP();
            TSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            TSP.verbose = false;
            start = System.currentTimeMillis();
            TSP.solve();
            filterTSPTime += (System.currentTimeMillis() - start);

//            System.out.println("true :" + trueTSP.getLB() );
//            System.out.println("enhenced :" + TSP.getLB() );
            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
        }

//        System.out.println("Sum solving time for simple Solver.TSP [ms] : " + simpleTSPTime);
//        System.out.println("Sum solving time for enhenced Solver.TSP [ms] : " + filterTSPTime);
    }


    @Test
    void outputMedium() {
        // Symetric instances of the TSPlib
        String[] instances = {"swiss42", "hk48", "berlin52", "st70", "eil76"}; // , "gr48", "brazil58"

        double simpleTSPTime = 0; // ms
        long simpleNode = 0;
        double filterTSPTime = 0; // ms
        long filterNode = 0;
        long start;

        for (String instance : instances) {
            soTSP trueTSP = new soTSP();
            trueTSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            trueTSP.verbose = false;
            start = System.currentTimeMillis();
            trueTSP.solve();
            simpleTSPTime += (System.currentTimeMillis() - start);
            simpleNode += trueTSP.visited_node;

            TSP TSP = new TSP();
            TSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            TSP.verbose = false;
            start = System.currentTimeMillis();
            TSP.solve();
            filterTSPTime += (System.currentTimeMillis() - start);
            filterNode += TSP.visited_node;

            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
            //System.out.println("compare : " + instance);
        }

//        System.out.println("Sum solving time for simple Solver.TSP [ms] : " + simpleTSPTime);
//        System.out.println("visited node for simple Solver.TSP [ms] : " + simpleNode);
//        System.out.println("Sum solving time for enhenced Solver.TSP [ms] : " + filterTSPTime);
//        System.out.println("visited node for enhenced Solver.TSP [ms] : " + filterNode);
    }

    @Test
    void outputBig() {
        // Symetric instances of the TSPlib
        String[] instances = {"dantzig42", "att48", "gr48", "rat99"}; // , "gr48", "brazil58"

        double simpleTSPTime = 0; // ms
        long simpleNode = 0;
        double filterTSPTime = 0; // ms
        long filterNode = 0;
        long start;

        for (String instance : instances) {
            soTSP trueTSP = new soTSP();
            trueTSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            trueTSP.verbose = false;
            start = System.currentTimeMillis();
            trueTSP.solve();
            simpleTSPTime += (System.currentTimeMillis() - start);
            simpleNode += trueTSP.visited_node;

            TSP TSP = new TSP();
            TSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            TSP.verbose = false;
            start = System.currentTimeMillis();
            TSP.solve();
            filterTSPTime += (System.currentTimeMillis() - start);
            filterNode += TSP.visited_node;

            System.out.println("Compare on instance : " + instance);
            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
            //System.out.println("compare : " + instance);
        }
    }

    @Test
    void outputSmallRandom() {
        // Symetric instances of the TSPlib
        String[] instances = {"random/rd020_mmhrP", "random/rd021_iAW5s", "random/rd022_cM9gb", "random/rd023_5tp4r", "random/rd024_mki7C", "random/rd025_g60Yb", "random/rd026_7XDE1", "random/rd027_IE0Vj", "random/rd028_6akpg", "random/rd029_Sfk9O", "random/rd030_wRKSe"}; // , "ulysses22"

        double simpleTSPTime = 0; // ms
        double filterTSPTime = 0; // ms
        long start;

        for (String instance : instances) {
            soTSP trueTSP = new soTSP();
            trueTSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            trueTSP.verbose = false;
            start = System.currentTimeMillis();
            trueTSP.solve();
            simpleTSPTime += (System.currentTimeMillis() - start);

            TSP TSP = new TSP();
            TSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            TSP.verbose = false;
            start = System.currentTimeMillis();
            TSP.solve();
            filterTSPTime += (System.currentTimeMillis() - start);

//            System.out.println("true :" + trueTSP.getLB() );
//            System.out.println("enhenced :" + TSP.getLB() );
            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
        }

//        System.out.println("Sum solving time for simple Solver.TSP [ms] : " + simpleTSPTime);
//        System.out.println("Sum solving time for enhenced Solver.TSP [ms] : " + filterTSPTime);
    }
}
