import util.UF;
import util.UFTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PrimCCT {
    double[][] distanceMatrix;
    int[][] mandatoryEdges;
    boolean[][] edgesAdded;
    int size;
    //ArrayList<Edge> mst;
    double cost;
    int[] nodesDegree;
    CCTree ccTree;
    int[] parent;
    double[] minCost;
    boolean inconsitent = false;

    public PrimCCT(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.size = distanceMatrix.length;
        this.nodesDegree = new int[size];
        //this.mst = new ArrayList<>();
        this.cost = 0;
        this.parent = new int[size];
        computeMST();
    }

    public PrimCCT(double[][] distanceMatrix, int[][] mandatoryEdges) {
        this.distanceMatrix = distanceMatrix;
        this.mandatoryEdges = mandatoryEdges;
        this.size = distanceMatrix.length;
        this.edgesAdded = new boolean[size][2];
        this.nodesDegree = new int[size];
        //this.mst = new ArrayList<>();
        this.cost = 0;
        this.parent = new int[size];
        computeMST();
    }

    /**
     * Compute the mst based on the distance matrix, including
     * the mandatory edges.
     */
    public void computeMST() {
        //add fist node
        this.minCost = distanceMatrix[0].clone();
        for (int i = 0; i < size; i++) {
            parent[i] = 0;
        }

        addMandatoryEdgesFrom(0);
        if (inconsitent) return;

        for (int k = 1; k < size; k++) {
            int i;
            for (i = 1; i < size; i++) {
                if (nodesDegree[i] == 0) break;
            }
            if (i == size) return; // MST completed with the mandatory edges
            for (int j = i + 1; j < size; j++) {
                if (nodesDegree[j] == 0 && minCost[j] < minCost[i]) i = j;
            }
            addEdge(parent[i], i);
            for (int j = 1; j < size; j++) {
                if (nodesDegree[j] == 0 && distanceMatrix[i][j] < minCost[j]) {
                    minCost[j] = distanceMatrix[i][j];
                    parent[j] = i;
                }
            }
            addMandatoryEdgesFrom(i);
            if (inconsitent) return;
        }
    }

    /**
     * Add mandatory (store in mandatoryEdges) edges adjacent with node i.
     *
     * @param i vertice
     */
    private void addMandatoryEdgesFrom(int i) {
        int o = -1;
        for (int k : mandatoryEdges[i + 1]) {
            o++;
            if (k == -1) return;
            if (k == 0 || edgesAdded[i][o]) continue;
            if (nodesDegree[k - 1] != 0 && !edgesAdded[i][o]) {
                inconsitent = true;
                return;
            }
            k--; // shift from the 1tree

            addEdge(i, k);
            parent[k] = i;

            edgesAdded[i][o] = true;
            if (mandatoryEdges[k + 1][0] == i + 1) edgesAdded[k][0] = true;
            else edgesAdded[k][1] = true;

            for (int j = 1; j < size; j++) {
                if (nodesDegree[j] == 0 && distanceMatrix[k][j] < minCost[j]) {
                    minCost[j] = distanceMatrix[k][j];
                    parent[j] = k;
                }
            }

            addMandatoryEdgesFrom(k);
        }
    }

    private void addEdge(int i, int j) {
        cost += distanceMatrix[i][j];
        nodesDegree[i]++;
        nodesDegree[j]++;
        //mst.add(new Edge(i, j, distanceMatrix[i][j]));
    }

    /**
     * Compute the support edge for each non tree edge.
     *
     * @return Hashmap : support -> non tree edge
     */
    public HashMap<Edge, ArrayList<Edge>> computeAllSupport() {
        // construct the CCT (add  edges in increasing weight order)
        ccTree = new CCTree(distanceMatrix.length);
        UF uf = new UF(size);
        ArrayList<Edge> mst = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            mst.add(new Edge(i, parent[i], distanceMatrix[i][parent[i]]));
        }
        Collections.sort(mst);
        for (Edge e : mst) {
            ccTree.updateCCTree(uf.find(e.v), uf.find(e.w), e);
            uf.union(e.v, e.w);
        }

        // compute the the support
        ccTree.travelCCTree();
        ccTree.precomputeRMQ();
        HashMap<Edge, ArrayList<Edge>> supports = new HashMap<>(); // ! only contain edges (i,j) st i<j

        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = i + 1; j < distanceMatrix.length; j++) {
                if (distanceMatrix[i][j] == Double.MAX_VALUE) continue;
                int lca = ccTree.LCA(i, j);
                Edge sup = ccTree.value[lca].edge;
                if (sup.equals(new Edge(i, j, distanceMatrix[i][j])))
                    continue; // the support of a MST edge is not useful
                if (sup == null) throw new IllegalArgumentException();
                if (!supports.containsKey(sup)) supports.put(sup, new ArrayList<>());
                supports.get(sup).add(new Edge(i, j, distanceMatrix[i][j]));
            }
        }

        return supports;
    }

    public double[] computeReplacementCost() {
        // get all non tree edges ordered depending of the pi O(n log(n))
        ArrayList<Edge> nonTreeEdges = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (distanceMatrix[i][j] < Double.MAX_VALUE && parent[i] != j && parent[j] != i)
                    nonTreeEdges.add(new Edge(i, j, distanceMatrix[i][j]));
            }
        }
        Collections.sort(nonTreeEdges);

        // tree rooted in 0
        int[] depth = new int[size];
        for (int i = 1; i < size; i++) {
            int j = i;
            int d = 1;
            while (depth[i] == 0) { // TODO : optimise
                if (depth[parent[j]] != 0 || parent[j] == 0) depth[i] = depth[parent[j]] + d;
                d++;
                j = parent[j];
            }
        }

        // compute the replacement edges
        double[] replacementCost = new double[size];
        boolean[] makred = new boolean[size];
        UFTree uft = new UFTree(size, depth);
        for (Edge e : nonTreeEdges) {
            if (uft.count() == 1) break;

            int i = e.v;
            int j = e.w;
            int cci = uft.find(i);
            int ccj = uft.find(j);
            i = uft.find(i);
            j = uft.find(j);
            while (i != j) {
                if (depth[i] >= depth[j]) {
                    replacementCost[i] = e.weight - distanceMatrix[i][parent[i]];
                    uft.union(i, parent[i]);
                    //i = parent[i];
                    i = uft.find(i);
                }
                if (depth[i] < depth[j]) {
                    replacementCost[j] = e.weight - distanceMatrix[j][parent[j]];
                    uft.union(j, parent[j]);
                    //j = parent[j];
                    j = uft.find(j);
                }
            }
        }

        return replacementCost;
    }
}
