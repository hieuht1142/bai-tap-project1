package weightedloadexperiment.pairstrategies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatuples.* ;

import common.RandomGenerator;
import custom.fattree.Address;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;

public class InterPodIncoming extends OverSubscription {

    private int [][] adjMx;
    
    public InterPodIncoming(FatTreeRoutingAlgorithm routing, FatTreeGraph G) {
    	super();
    	this.routing = routing;
        this.G = G;
    }
    
    @Override
    public void setAllHosts(Integer[] allHosts) {
    	super.setAllHosts(allHosts);
    	this.k =  (int) Math.cbrt(4 * allHosts.length); // number of hosts = k*k*k/4
        
        int numOfHosts = allHosts.length; // number of hosts
        
        adjMx = new int[numOfHosts][numOfHosts];
        for (int i = 0; i < numOfHosts; i++) {
            for (int j = 0; j < numOfHosts; j++) {
                if (i / (k * k / 4) != j / (k * k / 4)) {
                    int src = allHosts[i];
                    int dst = allHosts[j];
                    int core = getCoreSwitch(src, dst);
                    adjMx[i][j] = core;
                } else {
                    adjMx[i][j] = 0;
                }
            }
        }
    	
    }

    @Override
    public void pairHosts() {
        List<Integer> sources = getSources();
        List<Integer> destinations = getDestinations();

        Integer[] allHosts = this.getAllHosts();
        int delta = RandomGenerator.nextInt(0, k*k/4);
        
        startPairHosts(sources, destinations, allHosts, delta);
        
        this.setSources(sources);
        this.setDestinations(destinations);
    }
    
    /**
     *  This method is used to start pair hosts
     *  
     * @param sources list of source hosts
     * @param destinations list of destination hosts
     * @param allHosts array of all hosts
     * @param delta
     */
    private void startPairHosts(List<Integer> sources, List<Integer> destinations, Integer[] allHosts, int delta) {
    	
    	int numOfHosts = allHosts.length;
        int sizeOfPod = k*k/4;
    	int currPod = 0, prePod = 0; // current pod, previous pod
    	for(int i = 0; i < numOfHosts; i++) {
            int dst = allHosts[i]; // destination host
            prePod = currPod;
            if (!destinations.contains(dst)) {
                int index = getIndex(i, delta, numOfHosts, sizeOfPod, prePod, dst);
                currPod = findSourceAndDest(i, sources, destinations, allHosts, currPod, dst, index);
            } else {
                currPod = i / sizeOfPod;
            }
        }
    }
    
    /**
     * This method is used to find satisfiable source and destination hosts 
     * and add them into source list and destination list
     * 
     * @param i
     * @param sources list of source hosts
     * @param destinations list of destination hosts
     * @param allHosts array of all hosts
     * @param currPod current pod
     * @param dst destination host
     * @param index
     * @return current pod
     */
    private int findSourceAndDest(int i, List<Integer> sources, List<Integer> destinations, 
    								Integer[] allHosts, int currPod, int dst, int index) {    	
    	int numOfHosts = allHosts.length;
        int sizeOfPod = k*k/4;
        int count = 0;
        int expectedSrc = allHosts[index];
        boolean found = false; 
        while (!found && count < k) {
            if (sources.contains(expectedSrc)) {
                for (int j = index + 1; j < (index/sizeOfPod + 1)*sizeOfPod; j++) {
                    expectedSrc = allHosts[j];
                    if (!sources.contains(expectedSrc) && ((expectedSrc / sizeOfPod) != (dst / sizeOfPod))) {
                        found = true;
                        currPod = addSourceAndDest(sizeOfPod, sources, destinations, currPod, index, expectedSrc, dst);
                        break;
                    }
                }
            } else if (expectedSrc / sizeOfPod != dst / sizeOfPod) {
                    found = true;
                    currPod = addSourceAndDest(sizeOfPod, sources, destinations, currPod, index, expectedSrc, dst);                           
                    break;
            }
            if (!found) {
                count++;
                index = (index + sizeOfPod) % numOfHosts;
            }
        }
        return currPod;
    }
    
    /**
     * This method is used to pair source and destination hosts
     * 
     * @param i
     * @param sources List of source hosts
     * @param destinations List of destination hosts
     * @param currPod current pod
     * @param index
     * @param expectedSrc expected source
     * @param dst destination host
     * @return current pod
     */
    private int addSourceAndDest(int i, List<Integer> sources, List<Integer> destinations, 
    									int currPod, int index, int expectedSrc, int dst) {
    	
    	int sizeOfPod = k*k/4;
    	sources.add(expectedSrc);
        destinations.add(dst); 
        
        if((i + 1) % sizeOfPod == 0) {
            currPod = (i + 1) / sizeOfPod;
        } else {
            currPod = index / sizeOfPod;
        }  
        sources.add(dst);
        destinations.add(expectedSrc);
        
        return currPod;
    }
    
    /**
     * 
     * @param i
     * @param delta
     * @param numOfHosts the number of hosts
     * @param sizeOfPod size of pod
     * @param prePod previous pod
     * @param dst destination host
     * @return index
     */
    private int getIndex(int i, int delta, int numOfHosts, int sizeOfPod, int prePod, int dst) {
    	int index = (i + sizeOfPod + delta) % numOfHosts;
        if (index / sizeOfPod == prePod) {
            index = (index + sizeOfPod) % numOfHosts;
            if (index / sizeOfPod == dst / sizeOfPod) {
                index = (index + sizeOfPod) % numOfHosts;
            }
        }
        return index;
    }

    @Override
    public void checkValid() {
        List<Integer> sources = getSources();
        List<Integer> destinations = getDestinations();
        
        handleNotEnoughPair(sources, destinations);
        
        handleEnoughPair(sources, destinations);
   
    }
    
    /**
     * This method is used to handle if there are not enough pairs
     * 
     * @param sources List of source hosts
     * @param destinations List of destination hosts
     */
    private void handleNotEnoughPair(List<Integer> sources, List<Integer> destinations) {    	
    	if(sources.size() != k * k * k / 4) {
            System.out.println("Not enough pair! Just " + sources.size());
            for (int i = 0; i < sources.size(); i++) {
                int realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
                System.out.println("From " + sources.get(i) + " through " +
                        getCoreSwitch(sources.get(i), destinations.get(i))
                        + "/" + realCore
                        + " to "
                        + destinations.get(i)
                );
            }
            System.exit(0);
        }

    }
    
    /**
     * This method is used to handle if there are enough pairs
     * 
     * @param sources List of source hosts
     * @param destinations List of destination hosts
     */
    private void handleEnoughPair(List<Integer> sources, List<Integer> destinations) {
    	int sizeOfPod = k * k / 4;
    	
    	for (int i = 0; i < sources.size(); i++) {
            int realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
            System.out.println("From " + sources.get(i) + " through " +
                    getCoreSwitch(sources.get(i), destinations.get(i))
                    + "/" + realCore
                    + " to "
                    + destinations.get(i)
            );

            if (sources.get(i) / sizeOfPod == destinations.get(i) / sizeOfPod) {
                System.out.print("Source and destination are in the same pod. INVALID!!!!");
                System.exit(0);
                break;
            }
        }
    }

    public void transform(int[][] M, int length) {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (i > j) {
                    int temp = M[j][i];
                    M[j][i] = M[i][j];
                    M[i][j] = temp;
                }
            }
        }
    }
    
    /**
     * Gets the core switch on the path from source to destination
     *
     * @param source the source host
     * @param destination the destination host
     * @return the core switch
     */
    public int getCoreSwitch(int source, int destination) {
        int edge = G.adj(source).get(0);
        int agg = G.adj(edge).get(k/2);
        int core = G.adj(agg).get(k/2);
        return core;
    }

    public int getRealCoreSwitch(int source, int destination) {
        int edge = G.adj(source).get(0);
        Address address = G.getAddress(destination);
        Map<Integer, Map<Integer, Integer>> suffixTables = routing.getSuffixTables();
        Map<Integer, Map<Triplet<Integer, Integer, Integer>, Integer>> prefixTables = routing.getPrefixTables();

        Map<Integer, Integer> suffixTable = suffixTables.get(edge);
        int suffix = address._4;
        int agg = suffixTable.get(suffix);

        Triplet<Integer, Integer, Integer> prefix
                = new Triplet<>(address._1, address._2, address._3);

        Map<Triplet<Integer, Integer, Integer>, Integer> prefixTable =
                prefixTables.get(agg);
        suffixTable = suffixTables.get(agg);

        if (prefixTable.containsKey(prefix)) {
            return prefixTable.get(prefix);
        } else {
            return suffixTable.get(suffix);
        }
    }


}
