package graph;

public class Coordination {
    private Graph G;
    private int nCol; // number of columns
    private int nRow; // number of rows

    public Coordination(Graph G) {
        this.G = G;

        init();
    }

    private void init() {
        int nSwitch = G.switches().size(); // number of switches in graph G
        nCol = (int) Math.sqrt(nSwitch);
        nRow = nSwitch % nCol == 0 ? nCol : nCol + 1;
    }

    /**
     * This method is used to get manhattan distance between 2 vertexes
     * @param u vertex u
     * @param v vertex y
     * @return manhattan distance between u and v
     */
    public double distanceBetween(int u, int v) {
        if (!G.isSwitchVertex(u) || !G.isSwitchVertex(v)) {
            throw new RuntimeException("Node must be switch");
        }

        return manhattanDistance(u, v);
    }

    /**
     * This method is used to calculate manhattan distance between 2 vertexes
     * @param u vertex u
     * @param v vertex y
     * @return manhattan distance between u and v
     */
    public int manhattanDistance(int u, int v) {
        int ux = u % nCol; // u(ux, uy)
        int uy = u / nCol;
        int vx = v % nCol; // v(vx, vy)
        int vy = v / nCol;
        return Math.abs(ux - vx) + Math.abs(uy - vy); // manhattan distance = |ux-vx| + |uy-vy|
    }

    /**
     * This method is used to get coordination of switch
     * @param u switch u
     * @return coordination of u
     */
    public String getCoordOfSwitch(int u) {
        int ux = u % nCol;
        int uy = u / nCol;
        return ux + "\t" + uy;
    }

    /**
     * This method is used to get coordination of host
     * @param s host s
     * @param bias
     * @return coordination of host s
     */
    public String getCoordOfHost(int s, double bias) {
        int hx = s % nCol;
        int sy = s / nCol;
        double hy = sy + bias;
        return hx + "\t" + hy;
    }

    /**
     * This method is used to calculate total cable length
     * @return total cable length
     */
    public double totalCableLength() {
        double totalLength = 0;
        for (int sw1: G.switches()) {
            for (int sw2: G.adj(sw1)) {
                if (G.isSwitchVertex(sw2)) {
                    totalLength += distanceBetween(sw1, sw2);
                }
            }
        }

        return totalLength / 2;
    }
}
