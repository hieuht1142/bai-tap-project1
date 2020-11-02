package weightedloadexperiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.StdOut;
import config.Constant;
import custom.fattree.FatTreeFlowClassifier;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;
import infrastructure.event.Event;
import network.Topology;

import network.entities.Host;
import network.entities.Switch;
import network.entities.TypeOfHost;
import simulator.DiscreteEventSimulator;
import weightedloadexperiment.pairstrategies.ForcePair;
import weightedloadexperiment.pairstrategies.InterPodIncoming;
import weightedloadexperiment.pairstrategies.PairGenerator;
import weightedloadexperiment.pairstrategies.SameIDOutgoing;
import weightedloadexperiment.pairstrategies.StrideIndex;
import weightedloadexperiment.pairstrategies.interpod.MinimalCoreSwitches;


public class ThroughputExperiment {
	private Topology topology;

	public ThroughputExperiment(Topology network) {
        this.topology = network;
    }
	
    /**
     * This method is used to calculate throughput.
     */
    public double[][] calThroughput(Map<Integer, Integer> trafficPattern, boolean verbose) {
    	long start = System.currentTimeMillis();
        System.out.println("Start:");
        
        DiscreteEventSimulator.Initialize(true, Constant.MAX_TIME, verbose);
        DiscreteEventSimulator simulator = DiscreteEventSimulator.getInstance();
        setSimulator(trafficPattern, verbose, simulator);

        List<Double> scores = new ArrayList<Double>();
        double[][] points = calcThroughput(trafficPattern, simulator, scores);
        
        calExecTime(start);
        GraphPanel.createAndShowGui(scores);

        return points;
    }
    
    /**
     * This method is used to calculate throughput and print it at the console window.
     */
    private double[][] calcThroughput(Map<Integer, Integer> trafficPattern,
    									DiscreteEventSimulator simulator, List<Double> scores) {
    	double interval = 1e7;
        int nPoint = (int) (simulator.getTimeLimit() / interval + 1);
        double[][] points = new double[2][nPoint];
        for (int i = 0; i < nPoint; i++) {
            points[0][i] = i * interval; // convert to millisecond
            points[1][i] = simulator.receivedPacketPerUnit[i];
        }

        double throughput = 0;
        for (int i = 0; i < nPoint; i++) {
            points[1][i] = 100 * points[1][i] * Constant.PACKET_SIZE /
                    (trafficPattern.size() * Constant.LINK_BANDWIDTH * interval / 1e9);
        }
        for (int i = 0; i < nPoint; i++) {
        	scores.add(points[1][i]);
        }
        throughput = points[1][nPoint - 1];
        StdOut.printf("Throughput : %.2f\n", throughput);

        return points;
    }
    
    /**
     * This method is used to calculate execution time and print it at the console window
     *
     * @param start the time when application starts
     */
    private void calExecTime(long start) {
    	long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
    }
    
    /**
     * This method is used to build a Discrete Event Simulator.
     */
    private void setSimulator(Map<Integer, Integer> trafficPattern, boolean verbose, DiscreteEventSimulator simulator) {
        topology.clear();
        topology.setSimulator(simulator);

        for (Integer source : trafficPattern.keySet()) {
            Integer destination = trafficPattern.get(source);
            ((Host) topology.getHostById(source)).generatePacket(destination);
        }
        simulator.start();
    }
    
    /**
     * This method is used to calculate throughput
     */
    public void calTp() {
    	int rxPacket = 0;
        double thp = 0, privateThp = 0;
        for (int i = 0; i < topology.getHosts().size(); i++) {
            Host host = topology.getHosts().get(i);
            if(host.type == TypeOfHost.Destination || host.type == TypeOfHost.Mix) {
                Host destinationNode = host;
                if (destinationNode.getReceivedPacketInNode() != 0) {
                    
                    rxPacket += destinationNode.getReceivedPacketInNode();
                    privateThp = destinationNode.getReceivedPacketInNode()
                            * Constant.PACKET_SIZE / (destinationNode.getLastRx() - destinationNode.getFirstTx());
                    thp += privateThp;
                    
                }
            }
        }
    }
    
    /**
     * This method is used to print the capacity of flow from node to node.
     */
    private void printCapacity() {
    	 for (int i = 0; i < topology.getSwitches().size(); i++) {
             Switch nodeSwitch = topology.getSwitches().get(i);
             System.out.print("\nSwitch has id: " + nodeSwitch.getId() + " \n");

             if(nodeSwitch.getNetworkLayer().routingAlgorithm instanceof FatTreeFlowClassifier) {
                 FatTreeFlowClassifier ftfc = (FatTreeFlowClassifier)nodeSwitch.getNetworkLayer().routingAlgorithm;
                 Map<Integer, Long> outgoingTraffic = ftfc.outgoingTraffic;
                 
                 for(Integer key: outgoingTraffic.keySet()) {
                 	System.out.println("\tFlow to node: " + key + " has capacity: " + outgoingTraffic.get(key));
                 }
             }
         }
    }

    public static void main(String[] args) {

    	FatTreeGraph G = new FatTreeGraph(4);
        FatTreeRoutingAlgorithm ra = //new FatTreeRoutingAlgorithm(G, false);
        							new FatTreeFlowClassifier(G, false);
        
        PairGenerator pairGenerator = //new StrideIndex(1);
        								new StrideIndex(8);
        								//new InterPodIncoming(ra, G);
        								//new ForcePair(ra, G, 13);
        								//new MinimalCoreSwitches(ra, G);
        								//new SameIDOutgoing(G, ra);
        Topology topology = new Topology(G, ra, pairGenerator);    
		//new StaggeredProb(hosts, 4, 1, 0);
		//new InterPodIncoming(hosts, k, ra, G);
 
        ThroughputExperiment experiment = new ThroughputExperiment(topology);  

        Map<Integer, Integer> traffic = new HashMap<>();

        List<Integer> sourceNodeIDs = topology.getSourceNodeIDs();
        List<Integer> destinationNodeIDs = topology.getDestinationNodeIDs();

        int sizeOfFlow = sourceNodeIDs.size();

        for (int i = 0; i < sizeOfFlow; i++) {
            traffic.put(sourceNodeIDs.get(i), destinationNodeIDs.get(i));
        }

        experiment.calThroughput(traffic, false);
        experiment.calTp();
        experiment.printCapacity();
    }

}
