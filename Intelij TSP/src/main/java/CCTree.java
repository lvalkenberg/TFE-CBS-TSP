/**
 * Connected componnent tree
 */
public class CCTree {
    int elementCounter;
    int size;
    int[] inorder;
    int[] pos;
    int[] height;
    CCTNode[] value;
    int[] p; // pointer the root node of the connected component (index of vallue[])
    int globalCounter = 0;

    int[] log2Array;
    int[] pow2Array;
    int[][] M;

    // numberOfLeaf = number of vertices in the graph
    public CCTree(int numberOfLeaf) {
        if (numberOfLeaf < 2) throw new IllegalArgumentException();

        this.elementCounter = numberOfLeaf - 1;
        this.size = numberOfLeaf * 2 - 1;
        this.inorder = new int[size];
        this.pos = new int[size];
        this.height = new int[size];
        this.value = new CCTNode[size];
        this.p = new int[numberOfLeaf];
        this.log2Array = new int[size + 1];
        this.pow2Array = new int[(int) (Math.log(size) / Math.log(2)) + 1];
        this.M = new int[size][];

        for (int i = 0; i < numberOfLeaf; i++) {
            value[i] = new CCTNode(i, i);
            p[i] = i;
        }

        for (int i = 0; i < size; i++) {
            this.M[i] = new int[pow2Array.length]; // TODO : optimise -> mem/2 because the lower diag is not used.
        }

        for (int i = 0; i < size + 1; i++) {
            this.log2Array[i] = (int) (Math.log(i) / Math.log(2)); // ! log 0 = -inf
        }

        for (int i = 0; i <= Math.log(size) / Math.log(2); i++) {
            this.pow2Array[i] = (int) Math.pow(2, i);
        }
    }

    /**
     * Update the CCTree when a new edge is added to the KruskalCCT.
     *
     * @param rv      canonical element of the connected component containing v
     * @param rw      canonical element of the connected component containing w
     * @param newEdge The new edge.
     */
    public void updateCCTree(int rv, int rw, Edge newEdge) {
        elementCounter++;

        CCTNode left = value[p[rv]];
        CCTNode right = value[p[rw]];
        CCTNode newNode = new CCTNode(left, right, newEdge, elementCounter);

        p[rv] = elementCounter;
        p[rw] = elementCounter; // rv or rw will be the new canonical element
        value[elementCounter] = newNode;
    }

    /**
     * Travel the CCTree to setup inorder, pos and height vectors.
     */
    public void travelCCTree() {
        globalCounter = 0;
        CCTNode root = value[elementCounter]; // last added value is the root of the tree
        inorderTravelling(root, 0);
    }

    /**
     * Inorder visit of the ccTree.
     *
     * @param node node to visit.
     * @param h    height of the node.
     */
    private void inorderTravelling(CCTNode node, int h) {
        if (node.left != null) inorderTravelling(node.left, h + 1);
        inorder[globalCounter] = node.index;
        height[globalCounter] = h;
        pos[node.index] = globalCounter;
        globalCounter++;
        if (node.right != null) inorderTravelling(node.right, h + 1);

    }

    /**
     * Precompute the RMQ.
     */
    public void precomputeRMQ() {
        for (int i = 0; i < size; i++) {
            M[i][0] = i;
        }

        for (int j = 1; j <= log2Array[size - 1]; j++) {
            for (int i = 0; i <= size - pow2Array[j]; i++) {
                int minL = M[i][j - 1];
                int minR = M[i + pow2Array[j - 1]][j - 1];
                M[i][j] = height[minL] <= height[minR] ? minL : minR;
            }
        }
    }

    /**
     * Compute the range min query with the precomputed RMQ.
     *
     * @param i lower bound
     * @param j upper bound
     * @return the index of the RMQ inorder.
     */
    public int RMQ(int i, int j) {
        int logWidth = log2Array[j - i + 1];
        int minL = M[i][logWidth];
        int minR = M[j - pow2Array[logWidth] + 1][logWidth];
        return height[minL] <= height[minR] ? minL : minR;
    }

    /**
     * Return the last common ancestor between i and j in the ccTree.
     *
     * @param i lower bound of the interval.
     * @param j upper bound of the interval.
     * @return index of the lca node in the ccTree.
     */
    public int LCA(int i, int j) {
        int posi = pos[i];
        int posj = pos[j];
        return posi <= posj ? inorder[RMQ(posi, posj)] : inorder[RMQ(posj, posi)];
    }

}

class CCTNode {
    public boolean isLeaf;
    public CCTNode left;
    public CCTNode right;
    public CCTNode parent;
    public Edge edge; // if not a leaf
    public int vertice; // if leaf
    public int index; // position of the node in de value array

    /**
     * Leaf constructor
     *
     * @param vetice
     */
    public CCTNode(int vetice, int index) {
        this.isLeaf = true;
        this.vertice = vetice;
        this.index = index;
    }

    /**
     * Intermediate node constructor
     */
    public CCTNode(CCTNode left, CCTNode right, Edge edge, int index) {
        this.left = left;
        this.right = right;
        this.edge = edge;
        this.index = index;

        right.parent = this;
        left.parent = this;
    }
}

class Edge implements Comparable<Edge> {
    int v;
    int w;
    double weight;

    public Edge(int v, int w, double weight) {
        this.v = v;
        this.w = w;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge o) {
        if (this.weight < o.weight) return -1;
        else if (this.weight > o.weight) return +1;
        else return 0;
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Edge)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Edge e = (Edge) o;

        // Compare the data members and return accordingly
        return (Integer.compare(v, e.v) == 0 && Integer.compare(w, e.w) == 0) || (Integer.compare(v, e.w) == 0 && Integer.compare(w, e.v) == 0);
    }
}