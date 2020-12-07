package network.entities;

import config.Constant;
import infrastructure.entity.Device;
import infrastructure.entity.Node;
import network.elements.UnidirectionalWay;
import weightedloadexperiment.pairstrategies.OverSubscription;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dandoh on 6/27/17.
 */
public class Link extends Device {
   
	private Map<Integer, UnidirectionalWay> ways;
    private long bandwidth;
    
    public long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}

	private double length;

    public Link(Node u, Node v) {
        super(0);
        this.ways = new HashMap<>();
        ways.put(u.getId() ,new UnidirectionalWay(u, v, this));
        ways.put(v.getId() ,new UnidirectionalWay(v, u, this));

        this.bandwidth = Constant.LINK_BANDWIDTH;
        this.length = Constant.DEFAULT_LINK_LENGTH;
    }

    public Link(Node u, Node v, double length) {
        this(u, v);
        this.length = length;
    }

    /**
     * This method is used to get other node into the link
     */
    public Node getOtherNode(Node node) {
        return ways.get(node.getId()).getToNode();
    }

    /**
     * This method is used to get way to other node
     */
    public UnidirectionalWay getWayToOtherNode(Node node) {
        return ways.get(node.getId());
    }

    public Map<Integer, UnidirectionalWay> Ways() {
        return ways;
    }

    /**
     * This method is used to calculate the serial latency
     * @param packetSize the size of packet
     * @return the serial latency
     */
    public long serialLatency(int packetSize) {
    	if (OverSubscription.isOversubscriptedLink(this, 35, 32)) {
    		if(this.bandwidth != OverSubscription.OVERSUBSCRIPTION_BANDWIDTH) {
    			System.exit(0);
    		}
    	} else {
    		if (this.bandwidth != OverSubscription.NORMAL_BANDWIDTH) {
    			System.exit(0);
    		}
    	}
        return (long) (1e9 * packetSize / this.bandwidth);
    }
    
    /**
     * @return the propagation latency
     */
    public long propagationLatency() {
        return (long) (length / Constant.PROPAGATION_VELOCITY);
    }

    /**
     * @param packetSize the size of packet
     * @return total latency
     */
    public long getTotalLatency(int packetSize) {
        return serialLatency(packetSize) + propagationLatency();
    }

    public double getLength() {
        return this.length;
    }
}
