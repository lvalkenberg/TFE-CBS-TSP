import static org.junit.jupiter.api.Assertions.*;

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

            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
        }

        System.out.println("Sum solving time for simple TSP [ms] : " + simpleTSPTime);
        System.out.println("Sum solving time for enhenced TSP [ms] : " + filterTSPTime);
    }


    @Test
    void outputMedium() {
        // Symetric instances of the TSPlib
        String[] instances = {"dantzig42", "swiss42", "att48", "hk48", "berlin52"}; // , "gr48", "brazil58"

        double simpleTSPTime = 0; // ms
        double filterTSPTime = 0; // ms
        long start;

        for (String instance : instances) {
            soTSP trueTSP = new soTSP();
            trueTSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            trueTSP.verbose = false;
            start = System.nanoTime();
            trueTSP.solve();
            simpleTSPTime += (System.nanoTime() - start) / 10e6;

            TSP TSP = new TSP();
            TSP.xmlReader("../TSPlib/xml files/" + instance + ".xml");
            TSP.verbose = false;
            start = System.nanoTime();
            TSP.solve();
            filterTSPTime += (System.nanoTime() - start) / 10e6;

            assertEquals(trueTSP.getLB(), TSP.getLB(), 10e-6);
            //System.out.println("compare : " + instance);
        }

        System.out.println("Sum solving time for simple TSP [ms] : " + simpleTSPTime);
        System.out.println("Sum solving time for enhenced TSP [ms] : " + filterTSPTime);
    }

}
