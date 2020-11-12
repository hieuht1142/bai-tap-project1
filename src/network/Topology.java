package network;


import config.Constant;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;
import graph.Coordination;
import graph.Graph;
import network.entities.Host;
import network.elements.EntranceBuffer;
import network.elements.ExitBuffer;
import network.entities.*;

import network.layers.PhysicalLayer;
import simulator.DiscreteEventSimulator;
import weightedloadexperiment.pairstrategies.PairGenerator;

import java.util.*;

/**
 * Created by Dandoh on 6/27/17.
 */
public class Topology {
    private Graph graph;
    private List<Host> hosts;
    private List<Switch> switches;
    private Map<Integer, Host> hostById;
    private Map<Integer, Switch> switchById;
    private List<Integer> sourceNodes;
    private List<Integer> destinationNodes;
    private long bandwidthToHost = 0;
    public Map<Integer, String> cordOfNodes; // new property added by ThanhNT 14/10 
    public PairGenerator pairGenerator;
    
    /**
     * this method is used to construct hosts, switches and links and routing algorithm
     * 
     * @param graph Graph
     * @param routingAlgorithm fat-tree routing algorithm
     * @param pair Pair Generator
     */
    public Topology(FatTreeGraph graph, FatTreeRoutingAlgorithm routingAlgorithm, PairGenerator pair) {
        this.graph = graph;
        hosts = new ArrayList<>();
        switches = new ArrayList<>();
        hostById = new HashMap<>();
        switchById = new HashMap<>();
        sourceNodes = new ArrayList<>();
        destinationNodes = new ArrayList<>();
        cordOfNodes = new HashMap<>(); // the new statement added by ThanhNT 14/10
        this.pairGenerator = pair;
        buildTopology(graph, routingAlgorithm, pair);
    }
    
    /**
     * This method is used to construct topology
     * 
     * @param graph Graph
     * @param routingAlgorithm fat-tree routing algorithm
     * @param pair Pair Generator
     */
    private void buildTopology(FatTreeGraph graph, FatTreeRoutingAlgorithm routingAlgorithm, PairGenerator pair) {
    	initSwitch(graph, routingAlgorithm);
        Coordination C = new Coordination(graph);
        linkSwToSw(C);     
        
        // initiate host and add host to list
        Integer[] hostIDList = graph.hosts().toArray(new Integer[0]);
        pair.setAllHosts(hostIDList);
        
        pairGenerator.pairHosts();
        pairGenerator.checkValid();     

        List<Integer> sourceNodeIDs = new ArrayList<>();  
        List<Integer> destinationNodeIDs = new ArrayList<>(); 

        sourceNodeIDs = pairGenerator.getSources();
        destinationNodeIDs = pairGenerator.getDestinations();
        
        sourceNodes.addAll(sourceNodeIDs);
        buildSourceNode(routingAlgorithm);
        destinationNodes.addAll(destinationNodeIDs);
        buildDestinationNode(routingAlgorithm);
       
        linkSwToHost(C);        
        pairGenerator.setUpBandwidth(this);
    }
    
    
    /**
     * This method is used to initiate switches and add them to list.
     *
     * @param graph the graph used to build fat-tree graph
     * @param routingAlgorithm the the fat-tree routing algorithm
     */
    private void initSwitch(FatTreeGraph graph, FatTreeRoutingAlgorithm routingAlgorithm) {
    	for (int sid : graph.switches()) {
            Switch sw = new Switch(sid);
            switches.add(sw);
            switchById.put(sid, sw);
            cordOfNodes.put(sid, "");
            sw.physicalLayer = new PhysicalLayer(sw, graph.getK());         
            sw.setNetworkLayer(routingAlgorithm, sw);
        }
    }
    
    /**
     * This method is used to create link from switch to switch
     * 
     * @param C Coordination
     */
    private void linkSwToSw(Coordination C) { 
        for (Switch sw : switches) {
            for (int nextNodeID : graph.adj(sw.getId())) {
                if (graph.isSwitchVertex(nextNodeID)) {
                    Switch otherSwitch = switchById.get(nextNodeID);
                    if (!otherSwitch.physicalLayer.links.containsKey(sw.getId())) {
                        // create new link
                        double distance =  C.distanceBetween(sw.getId(), otherSwitch.getId());
                        Link link = new Link(sw, otherSwitch, distance);
                        sw.physicalLayer.links.put(otherSwitch.getId(), link);
                        otherSwitch.physicalLayer.links.put(sw.getId(), link);

                        buildExbEnbSw(sw, otherSwitch);
                        buildExbEnbOSw(sw, otherSwitch);

                        cordOfNodes.put(sw.getId(), C.getCoordOfSwitch(sw.getId()));
                        cordOfNodes.put(otherSwitch.getId(), C.getCoordOfSwitch(otherSwitch.getId()));
                    }
                }
            }
        }
    }
    
    /**
     * This method is used to build the entrance buffer and exit buffer of switch.
     */
    private void buildExbEnbSw(Switch sw, Switch otherSwitch) {
    	
        EntranceBuffer entranceBuffer = new EntranceBuffer(sw, otherSwitch, Constant.QUEUE_SIZE);
        ExitBuffer exitBuffer = new ExitBuffer(sw, otherSwitch, Constant.QUEUE_SIZE);
        
        entranceBuffer.physicalLayer = sw.physicalLayer;
        exitBuffer.physicalLayer = sw.physicalLayer;
        sw.physicalLayer.entranceBuffers.put(otherSwitch.getId(), entranceBuffer);
        sw.physicalLayer.exitBuffers.put(otherSwitch.getId(), exitBuffer);
    }
    
    /**
     * This method is used to build the exit buffer and exit buffer of other switch.
     */
    private void buildExbEnbOSw(Switch sw, Switch otherSwitch) {
    	
    	EntranceBuffer entranceBuffer = new EntranceBuffer(otherSwitch, sw, Constant.QUEUE_SIZE);
    	ExitBuffer exitBuffer = new ExitBuffer(otherSwitch, sw, Constant.QUEUE_SIZE);
    	
        entranceBuffer.physicalLayer = otherSwitch.physicalLayer;
        exitBuffer.physicalLayer = otherSwitch.physicalLayer;
        otherSwitch.physicalLayer.entranceBuffers.put(sw.getId(), entranceBuffer);
        otherSwitch.physicalLayer.exitBuffers.put(sw.getId(), exitBuffer);
    }
    
    /**
     * This method is used to build source node id for topology.
     *
     * @param routingAlgorithm the fat-tree routing algorithm
     */
    private void buildSourceNode(FatTreeRoutingAlgorithm routingAlgorithm) {
    	for (int sourceNodeID : sourceNodes) {
            Host sourceNode = new Host(sourceNodeID);
            sourceNode.type = TypeOfHost.Source;
            sourceNode.physicalLayer = new PhysicalLayer(sourceNode);
            sourceNode.setNetworkLayer(routingAlgorithm, sourceNode);
            hosts.add(sourceNode);
            hostById.put(sourceNodeID, sourceNode);
            cordOfNodes.put(sourceNodeID, "");
        }
    }
    
    /**
     * This method is used to build destination node id for topology.
     *
     * @param routingAlgorithm the fat-tree routing algorithm
     */
    private void buildDestinationNode(FatTreeRoutingAlgorithm routingAlgorithm) {
    	 for (int destinationNodeID : destinationNodes) {
         	Host destinationNode = null;
         	if(hostById.containsKey(destinationNodeID)) {
         		destinationNode = hostById.get(destinationNodeID);
         		destinationNode.type = TypeOfHost.Mix;
         	} else {
         		destinationNode = new Host(destinationNodeID);
         		destinationNode.type = TypeOfHost.Destination;
         		hosts.add(destinationNode);
         		hostById.put(destinationNodeID, destinationNode);
         		destinationNode.physicalLayer = new PhysicalLayer(destinationNode);
         		destinationNode.setNetworkLayer(routingAlgorithm, destinationNode);
         	}
             cordOfNodes.put(destinationNodeID, "");
         }
    }
    
    /**
     * This method is used to create link from switch to host
     * 
     * @param C Coordination
     */
    private void linkSwToHost(Coordination C) {
        for (Host host : hosts) {
            // get switch
            int switchID = graph.adj(host.getId()).get(0);
            Switch sw = switchById.get(switchID);

            // create new link
            Link link = new Link(host, sw, Constant.HOST_TO_SWITCH_LENGTH);
           
            host.physicalLayer.links.put(host.getId(), link);
            sw.physicalLayer.links.put(host.getId(), link);

            initPhsLayerProp(host, sw);

            // build exit buffer of switch to destination node
            if(host.isDestinationNode()){
                ExitBuffer exitBuffer = new ExitBuffer(sw, host, Constant.QUEUE_SIZE);
                exitBuffer.physicalLayer = sw.physicalLayer;
                sw.physicalLayer.exitBuffers.put(host.getId(), exitBuffer);
            }
            
            cordOfNodes.put(host.getId(), C.getCoordOfHost(sw.getId(), Constant.HOST_TO_SWITCH_LENGTH));
        }
    }
    
    /**
     * This method is used to initiate property in Physical Layer
     * 
     * @param host Host
     * @param sw Switch
     */
    private void initPhsLayerProp(Host host, Switch sw) {
        if(host.isSourceNode()){
            //exb of host
            ExitBuffer exitBuffer = new ExitBuffer(host, sw, Constant.QUEUE_SIZE);
            exitBuffer.physicalLayer = host.physicalLayer;
            host.physicalLayer.exitBuffers.put(sw.getId(), exitBuffer);

            //enb of switch to host
            EntranceBuffer entranceBuffer = new EntranceBuffer(sw, host, Constant.QUEUE_SIZE);
            entranceBuffer.physicalLayer = sw.physicalLayer;
            sw.physicalLayer.entranceBuffers.put(host.getId(), entranceBuffer);
        }
    }

    public List<Integer> getSourceNodeIDs(){
        return sourceNodes;
    }
    public List<Integer> getDestinationNodeIDs(){
        return destinationNodes;
    }

    public Graph getGraph() {
        return graph;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public List<Switch> getSwitches() {
        return switches;
    }

    public Host getHostById(int id) {
        return hostById.get(id);
    }

    public void clear() {
        for (Host host : hosts) {
            host.clear();
        }

        for (Switch sw: switches) {
            sw.clear();
        }
    }
    
    public void setSimulator(DiscreteEventSimulator sim) {
    	for (Host host : hosts) {
            host.physicalLayer.simulator = sim;
        }

        for (Switch sw: switches) {
            sw.physicalLayer.simulator = sim;
        }
        sim.topology = this;
    }

    public boolean checkDeadlock(){
    	return false;
    }
}
