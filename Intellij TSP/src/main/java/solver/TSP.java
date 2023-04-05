package solver;// simple exact Solver.TSP solver based on branch-and-bound/Held--Karp

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TSP {
    public boolean verbose = true;
    // upper bound
    Node bestNode = new Node();
    public long visited_node;
    // instance name
    private String name;
    // number of cities
    private int n;
    // city locations
    private double[] x;
    private double[] y;
    // cost matrix
    private double[][] cost;
    // matrix of adjusted costs
    private double[][] costWithPi;
    // matrix of cost without the 1Tree root
    private double[][] subGraph;
    // if constant pi
    private double[] globalPi;
    // nodes to explore
    private Stack<Node> stackNodes = new Stack();
    private PriorityQueue<Node> pqNodes = new PriorityQueue<Node>(11, new NodeComparator());
    // Last conflict
    private int lastConflictNode;
    // True if the currentNode is a direct children of the last one
    private boolean directChildren = false;

    // default solver parameters
    public boolean margCost = true;
    public boolean repCost = true;
    public String branching = "force";
    public String search = "DFS on BFS";
    public boolean lastConflict = true;
    public boolean incrementalPi = true;
    public boolean fixedMultipliers = false;

    public static void main(String[] args) throws IOException {
        System.err.printf("%n");
        TSP tsp = new TSP();
        //tsp.readInput(new InputStreamReader(System.in));
        tsp.xmlReader("../TSPlib/xml files/gr120.xml");

        long start = System.nanoTime();
        tsp.solve();
        if (tsp.verbose) System.out.println("\n Solved in " + (System.nanoTime() - start) / 1e6 + " ms");
    }

    /**
     * EUC_2D .tsp reader
     */
    public void readInput(Reader r) throws IOException {
        BufferedReader in = new BufferedReader(r);
        Pattern specification = Pattern.compile("\\s*([A-Z_]+)\\s*(:\\s*([0-9]+))?\\s*");
        Pattern data = Pattern.compile("\\s*([0-9]+)\\s+([-+.0-9Ee]+)\\s+([-+.0-9Ee]+)\\s*");
        String line;
        while ((line = in.readLine()) != null) {
            Matcher m = specification.matcher(line);
            if (!m.matches()) continue;
            String keyword = m.group(1);
            if (keyword.equals("DIMENSION")) {
                n = Integer.parseInt(m.group(3));
                cost = new double[n][n];
            } else if (keyword.equals("NODE_COORD_SECTION")) {
                x = new double[n];
                y = new double[n];
                for (int k = 0; k < n; k++) {
                    line = in.readLine();
                    m = data.matcher(line);
                    m.matches();
                    int i = Integer.parseInt(m.group(1)) - 1;
                    x[i] = Double.parseDouble(m.group(2));
                    y[i] = Double.parseDouble(m.group(3));
                }
                // TSPLIB distances are rounded to the nearest integer to avoid the sum of square roots problem
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        double dx = x[i] - x[j];
                        double dy = y[i] - y[j];
                        cost[i][j] = Math.rint(Math.sqrt(dx * dx + dy * dy));
                    }
                }
            }
        }
    }

    /**
     * Read Solver.TSP Instance from xml
     * See http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf
     *
     * @param xmlPath path to the file
     */
    public void xmlReader(String xmlPath) {
        String[] elem = xmlPath.split("/");
        this.name = elem[elem.length-1].split("[.]")[0];
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(xmlPath));
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("vertex");

            n = list.getLength();
            this.cost = new double[n][n];

            for (int i = 0; i < n; i++) {
                NodeList edgeList = list.item(i).getChildNodes();
                for (int v = 0; v < edgeList.getLength(); v++) {

                    org.w3c.dom.Node node = edgeList.item(v);
                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String cost = element.getAttribute("cost");
                        String adjacentNode = element.getTextContent();
                        int j = Integer.parseInt(adjacentNode);
                        //distanceMatrix[i][j] = Math.rint(Double.parseDouble(cost)); // Rounded !
                        this.cost[i][j] = Double.parseDouble(cost);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void solve() {
        bestNode.lowerBound = Double.MAX_VALUE;
        Node currentNode = new Node();
        currentNode.excluded = new boolean[n][n];
        currentNode.mandatoryEdges = new int[n][2];
        for (int i = 0; i < n; i++) {
            currentNode.mandatoryEdges[i][0] = -1;
            currentNode.mandatoryEdges[i][1] = -1;
        }
        costWithPi = new double[n][n];
        subGraph = new double[n - 1][n - 1];
        computeHeldKarp(currentNode);
        visited_node = 0;
        do {
            visited_node++;
            int i = -1;
            for (int j = 0; j < n; j++) {
                if (currentNode.degree[j] > 2 && (i < 0 || currentNode.degree[j] < currentNode.degree[i])) i = j;
            }

            if (i < 0) {
                if (currentNode.lowerBound < bestNode.lowerBound) {
                    bestNode = currentNode;
                    if (verbose) System.err.printf("%f", bestNode.lowerBound);
                }
               //break;
            }

            PriorityQueue<Node> children = branch(currentNode);
            currentNode = nextNode(children);
        } while (currentNode != null && currentNode.lowerBound < bestNode.lowerBound);

        if (verbose) System.out.println("---------------------------");
        if (verbose) System.out.println("-> optimum found : " + bestNode.lowerBound);
        if (verbose) System.out.println("-> " + visited_node + " nodes visisted");

        for (int i = 0; i < n; i++) {
            if (verbose) System.out.printf("(%d,%d) ", bestNode.V[i], bestNode.W[i]);
        }
    }

    /**
     * Add the node to the priority queue if it is a potential optimal solution
     * regarding the actual upper bound.
     * @param pq
     * @param node
     */
    private void addPQ(PriorityQueue<Node> pq, Node node) {
        if (!(node.mst.inconsitent) || (node.lowerBound > bestNode.lowerBound && bestNode.lowerBound != Double.MAX_VALUE))
            pq.add(node);
        else if(lastConflict){
            lastConflictNode = node.branchedVertices;
        }
    }

    /**
     * Return the PQ with the children nodes, following the strategy of this.branching.
     * @param currentNode
     * @return A PQ of children Node or NULL if the currentnNode is a Hamiltonian circle
     */
    private PriorityQueue<Node> branch(Node currentNode){
        PriorityQueue<Node> children = new PriorityQueue<Node>(11, new NodeComparator());

        if(this.branching == "exclude") {
            int i = -1;
            for (int j = 0; j < n; j++) {
                if (currentNode.degree[j] > 2 && (i < 0 || currentNode.degree[j] < currentNode.degree[i])) i = j;
            }
            for (int j = 0; j < n; j++) {
                if (currentNode.V[j] == i) addPQ(children, exclude(currentNode, i, currentNode.W[j]));
                else if (currentNode.W[j] == i) addPQ(children, exclude(currentNode, i, currentNode.V[j]));
            }
        }
        if(this.branching == "force") {
            int i = -1;
            if(lastConflict && currentNode.degree[lastConflictNode] < 2 && stillNonExcludeEdge(currentNode.excluded[lastConflictNode])) i = lastConflictNode;
            else{
                for (int j = 0; j < n; j++) {
                    if (currentNode.degree[j] < 2 && stillNonExcludeEdge(currentNode.excluded[j])) i = j;
                }
            }
            if(i == -1) return null; // TSP solution

            int k = -1;
            for(int j = 1; j < n; j++){
                if(i != j && !currentNode.excluded[i][j] && j != currentNode.mandatoryEdges[i][0] && currentNode.mandatoryEdges[j][1] == -1 && (k == -1 || cost[i][k] > cost[i][j])) k=j;
            }
            if(k == -1) return children;
            addPQ(children, exclude(currentNode, i, k));
            addPQ(children, include(currentNode,i,k));
        }

        return children;

    }

    private boolean stillNonExcludeEdge(boolean[] ex){
        int sum = 0;
        for(boolean b : ex){
            if(!b) sum ++;
        }
        return sum >= 2;
    }

    /**
     * Return the next node to explore depending of the strategy.
     * @param children children of the node that we just explored
     * @return the next node to explore.
     */
    private Node nextNode(PriorityQueue<Node> children){
        Node child = null;
        if(search == "DFS") {
            if(children != null) {
                child = children.poll();
                directChildren = true;
                stackNodes.addAll(children);
            }
            while ((child == null || child.lowerBound >= bestNode.lowerBound) && !stackNodes.isEmpty()) {
                child = stackNodes.pop();
                directChildren = false;
            }
            if ((child != null && child.lowerBound >= bestNode.lowerBound)) return null;
            else return child;
        }else if(search == "DFS on BFS"){
            if(children != null) {
                child = children.poll();
                directChildren = true;
                pqNodes.addAll(children);
            }
            if (verbose) System.err.printf(".");
            if (child == null || child.lowerBound >= bestNode.lowerBound) {
                if (verbose) System.err.printf("%n");
                child = pqNodes.poll();
                directChildren = false;
            }
            return child;
        }
        System.out.println("Non valid search strategy");
        return null;
    }

    /**
     * Exclude a edge from the Solver.TSP
     *
     * @param node
     * @param i
     * @param j
     * @return
     */
    private Node exclude(Node node, int i, int j) {
        Node child = new Node();
        child.excluded = node.excluded.clone();
        child.excluded[i] = node.excluded[i].clone();
        child.excluded[j] = node.excluded[j].clone();
        child.excluded[i][j] = true;
        child.excluded[j][i] = true;

        child.clonedExclu = new boolean[n];
        child.clonedExclu[i] = true;
        child.clonedExclu[j] = true;

        child.clonedMandatory = new boolean[n];
        child.mandatoryEdges = node.mandatoryEdges.clone();

        if(!fixedMultipliers) computeHeldKarp(child);
        else computeOneTree(child);

        reduce(child);
        return child; // TODO : don't add suboptimal child to the pq
    }

    private Node include(Node node, int i, int j) {
        Node child = new Node();
        child.excluded = node.excluded.clone();
        child.clonedExclu = new boolean[n];

        child.clonedMandatory = new boolean[n];
        child.mandatoryEdges = node.mandatoryEdges.clone();
        child.mandatoryEdges[i] = node.mandatoryEdges[i].clone();
        child.mandatoryEdges[j] = node.mandatoryEdges[j].clone();
        child.clonedMandatory[i] = true;
        child.clonedMandatory[j] = true;

        if (child.mandatoryEdges[i][1] != -1 || child.mandatoryEdges[j][1] != -1) {
            throw new IllegalArgumentException();
        }

        if (child.mandatoryEdges[i][0] == -1) child.mandatoryEdges[i][0] = j;
        else child.mandatoryEdges[i][1] = j;

        if (child.mandatoryEdges[j][0] == -1) child.mandatoryEdges[j][0] = i;
        else child.mandatoryEdges[j][1] = i;

        if(!fixedMultipliers) computeHeldKarp(child);
        else computeOneTree(child);

        child.branchedVertices = i;

        reduce(child);
        return child; // TODO : don't add suboptimal child to the pq
    }

    /**
     * Use techniques to reduce the problem size.
     * @param node
     */
    private void reduce(Node node) {
        if (bestNode.lowerBound != Double.MAX_VALUE && node.lowerBound <= bestNode.lowerBound) {
            if(margCost) marginalCostFiltering(node);
            if(repCost) replacementCost(node);
        }
    }

    private void computeHeldKarp(Node node) {
        node.init(n);
        if(incrementalPi && directChildren) node.pi = globalPi;
        //if(globalPi == null) globalPi = node.pi;
        else globalPi = node.pi;
        double lambda = 0.1;
        while (lambda > 1e-06) {
            double previousLowerBound = node.lowerBound;
            computeOneTree(node);
            if (node.mst.inconsitent) return;
            if (!(node.lowerBound < bestNode.lowerBound)) return; // TODO : Why does it improve the perf ?
            if (!(node.lowerBound < previousLowerBound)) lambda *= 0.9;
            int denom = 0; // distance from the Solver.TSP
            for (int i = 1; i < n; i++) {
                int d = node.degree[i] - 2;
                denom += d * d;
            }
            if (denom == 0) return; // Solver.TSP found
            double normFactor = node.lowerBound;
            if(normFactor < 0) normFactor = 1;
            double t = lambda * normFactor / denom; // step
            for (int i = 1; i < n; i++) node.pi[i] += t * (node.degree[i] - 2);
        }
    }

    private void computeOneTree(Node node) {
        if(fixedMultipliers && node.pi == null) node.init(n);
        // compute adjusted costs
        node.lowerBound = 0.0;
        Arrays.fill(node.degree, 0);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                if(!fixedMultipliers) costWithPi[i][j] = node.excluded[i][j] ? Double.MAX_VALUE : cost[i][j] + node.pi[i] + node.pi[j];
                else costWithPi[i][j] = node.excluded[i][j] ? Double.MAX_VALUE : cost[i][j] + globalPi[i] + globalPi[j];
        }
        int firstNeighbor;
        int secondNeighbor;
        // find the two cheapest edges from 0
        if (costWithPi[0][2] < costWithPi[0][1]) {
            firstNeighbor = 2;
            secondNeighbor = 1;
        } else {
            firstNeighbor = 1;
            secondNeighbor = 2;
        }
        for (int j = 3; j < n; j++) {
            if (costWithPi[0][j] < costWithPi[0][secondNeighbor]) {
                if (costWithPi[0][j] < costWithPi[0][firstNeighbor]) {
                    secondNeighbor = firstNeighbor;
                    firstNeighbor = j;
                } else {
                    secondNeighbor = j;
                }
            }
        }

        // compute the oneTree
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                subGraph[i][j] = costWithPi[i + 1][j + 1];
            }
        }

        addEdge(node, 0, firstNeighbor);
        addEdge(node, 0, secondNeighbor);

        PrimCCT mst = new PrimCCT(subGraph, node.mandatoryEdges);
        node.mst = mst;
        if (mst.inconsitent) return;

        for (int i = 1; i < n - 1; i++) {
            // shift edges from the MST to consider the root of the 1tree
            int v = i + 1;
            int w = mst.parent[i] + 1;
            node.V[i] = v;
            node.W[i] = w;
            addEdge(node, v, w);
        }
        node.V[0] = 0;
        node.W[0] = firstNeighbor;
        node.V[n - 1] = 0;
        node.W[n - 1] = secondNeighbor;


        //node.lowerBound = Math.rint(node.lowerBound);
    }

    private void addEdge(Node node, int i, int j) {
        node.lowerBound += costWithPi[i][j];
        node.degree[i]++;
        node.degree[j]++;
    }

    /**
     * Apply marginal cost filtering.
     *
     * @param node
     */
    private void marginalCostFiltering(Node node) {
        HashMap<Edge, ArrayList<Edge>> supports = node.mst.computeAllSupport();
        for (Map.Entry<Edge, ArrayList<Edge>> entry : supports.entrySet()) {
            for (Edge nonTreeEdge : entry.getValue()) {
                if (nonTreeEdge.weight - entry.getKey().weight + node.lowerBound > bestNode.lowerBound) {
                    node.exclude(nonTreeEdge);
                }
            }
        }
    }

    /**
     * Force the mandatory edges based on their replacement cost.
     * If an edge is mandatory but one of his vertices as already
     * 2 adjacent mandatory edges, the node is inconsistent.
     * @param node
     */
    private void replacementCost(Node node) {
        double[] repCost = node.mst.computeReplacementCost();
        for (int i = 1; i < repCost.length; i++) {
            if (node.lowerBound + repCost[i] > bestNode.lowerBound) {
                int v = i + 1;
                int w = node.mst.parent[i] + 1;
                if (node.mandatoryEdges[v][1] != -1 && (!node.isMandatoryEdge(v, w) || !node.isMandatoryEdge(w, v))) {
                    //throw new IllegalArgumentException();
                    node.mst.inconsitent = true; // the edge has already a degree 2
                    return;
                }
                if (node.mandatoryEdges[w][1] != -1 && (!node.isMandatoryEdge(v, w) || !node.isMandatoryEdge(w, v))) {
                    //throw new IllegalArgumentException();
                    node.mst.inconsitent = true;
                    return;
                }

                node.forceEdge(i + 1, node.mst.parent[i] + 1);
            }
        }
    }

    // getters

    public double getLB() {
        return bestNode.lowerBound;
    }
}

class Node {
    public boolean[][] excluded;
    public boolean[] clonedExclu; // store the line of exclu that has be cloned O(n^2) !
    // Held--Karp solution
    public double[] pi;
    public double lowerBound;
    public int[] degree;
    //public int[] parent;

    //edges in the 1tree
    public int[] V;
    public int[] W;

    public PrimCCT mst;

    public int[][] mandatoryEdges;
    public boolean[] clonedMandatory;

    public int branchedVertices;

    public void init(int n){
        this.lowerBound = Double.MIN_VALUE;
        this.degree = new int[n];
        this.pi = new double[n];
        //node.parent = new int[n];
        this.V = new int[n];
        this.W = new int[n];
    }

    /**
     * Exclude a edge from the Solver.TSP in the node.
     * /!\ shift all node by one making the supposition that the edge come from the MST.
     * @param edge Solver.Edge coming from the MST (vertices need to be increase by 1)
     */
    public void exclude(Edge edge) {
        int i = edge.v + 1;
        int j = edge.w + 1;

        if (!clonedExclu[i]) {
            this.excluded[i] = this.excluded[i].clone();
            clonedExclu[i] = true;
        }
        if (!clonedExclu[j]) {
            this.excluded[j] = this.excluded[j].clone();
            clonedExclu[j] = true;
        }
        this.excluded[i][j] = true;
        this.excluded[j][i] = true;
    }

    /**
     * Add an edge to the mandatory edges of the node.
     * @param i
     * @param j
     */
    public void forceEdge(int i, int j) {
        if (this.isMandatoryEdge(i, j)) return;

        if (!clonedMandatory[i]) {
            this.mandatoryEdges[i] = this.mandatoryEdges[i].clone();
            clonedMandatory[i] = true;
        }
        if (!clonedMandatory[j]) {
            this.mandatoryEdges[j] = this.mandatoryEdges[j].clone();
            clonedMandatory[j] = true;
        }

        if (this.mandatoryEdges[i][0] == -1) this.mandatoryEdges[i][0] = j;
        else this.mandatoryEdges[i][1] = j;

        if (this.mandatoryEdges[j][0] == -1) this.mandatoryEdges[j][0] = i;
        else this.mandatoryEdges[j][1] = i;
    }

    /**
     * Check if a edge is already mandatory.
     * @param i
     * @param j
     * @return A boolean equals to true if the edge is already mandatory, false otherwise.
     */
    public boolean isMandatoryEdge(int i, int j) {
        if (this.mandatoryEdges[i][0] == j || this.mandatoryEdges[i][1] == j) return true;
        else return false;
    }
}

class NodeComparator implements Comparator<Node> {
    public int compare(Node a, Node b) {
        return Double.compare(a.lowerBound, b.lowerBound);
    }
}
