package weightedloadexperiment.pairstrategies;

import java.util.ArrayList;
import java.util.List;

import common.RandomGenerator;
import custom.fattree.Address;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;

public class SameIDOutgoing extends OverSubscription {
	
	public SameIDOutgoing(FatTreeGraph G, FatTreeRoutingAlgorithm routing) {
		super();
		this.G = G;
		this.routing = routing;
	}
    
	public SameIDOutgoing() {
	}

	public SameIDOutgoing(Integer[] allHosts) {
		super(allHosts);
	}
	
	/**
	 * This method is used to pair hosts
	 */
	@Override
	public void pairHosts() {
		setTypeOfAddresss();
		List<Integer> sources = getSources();
		List<Integer> destinations = getDestinations();
		Integer[] allHosts = this.getAllHosts();
		int numOfHosts = allHosts.length;
		int delta = RandomGenerator.nextInt(0, k*k*k/4);
		int count = 0;
		int i = 0;
		while (i < numOfHosts && count < numOfHosts * 1000) {
			List<Integer> allTempDsts = new ArrayList<>();
			List<Integer> allTempSrcs = new ArrayList<>();
            
			pairing(i, allHosts, delta, destinations, allTempDsts, allTempSrcs);
            
			if (allTempDsts.size() == k/2) {
				handleAllTempDsts(sources, destinations, allTempSrcs, allTempDsts);
			} else {
				delta = RandomGenerator.nextInt(0, k*k*k/4);
			}
			count++;
		}
	}
	
	/**
	 * This method is used to handle if size of allTempDsts == k/2
	 */
	private void handleAllTempDsts(List<Integer> sources, List<Integer> destinations,
										List<Integer> allTempSrcs, List<Integer> allTempDsts) {
		
		System.out.print("\n");
		sources.addAll(allTempSrcs);
		destinations.addAll(allTempDsts);
		for (int m = 0; m < allTempDsts.size(); m++) {
			System.out.print(allTempDsts.get(m) + "(" + getHostID(allTempDsts.get(m)) + ") ");
			int id = allTempDsts.get(m);
			Address host = G.getAddress(id);
			System.out.print("Addr: " + host._1 + "." + host._2 + "." + host._3 + "." + host._4);
			System.out.println();
		}
		System.out.print("\n");
	}
	
	/**
	 * This method is used to pair hosts
	 * 
	 * @param i
	 * @param allHosts list of all hosts
	 * @param delta
	 * @param destinations list of destination hosts
	 * @param allTempDsts list of all temporary destination hosts
	 * @param allTempSrcs list of all temporary source hosts
	 */
	private void pairing(int i, Integer[] allHosts, int delta, List<Integer> destinations, List<Integer> allTempDsts, List<Integer> allTempSrcs) {		
		int sameHostID = -1;
		int numOfHosts = allHosts.length;
		for (int j = i; j < i + (k/2); j++) {
			int src =  allHosts[j];
			boolean found = false;
			for (int k = 0; k < numOfHosts; k++) {
				int dst = allHosts[(k + delta) % numOfHosts];
				if (dst != src && !destinations.contains(dst) && !allTempDsts.contains(dst)) {
					if (sameHostID == -1) {
						sameHostID = getHostID(dst);
						allTempDsts.add(dst);
						found = true;
						break;
					} else if (sameHostID == getHostID(dst)) {
						allTempDsts.add(dst);
						found = true;
						break;
					}
				}
			}
			if(found) {
				allTempSrcs.add(src);
			} else {
				break;
			}
		}
	}
	
	@Override
	public void setAllHosts(Integer[] allHosts) {
		super.setAllHosts(allHosts);
    	this.k =  (int)Math.cbrt(4.0 * allHosts.length);
	}
	
	/**
	 * 
	 * @param id id of host
	 * @return host id of host address
	 */
	public int getHostID(int id) {
		Address host = G.getAddress(id);
		int lastPart = host._4;
		int hostID = 0;
		if (lengthOfHostID == 8) {
			hostID = (lastPart << 24) >> 24;	
		}
		
		if (lengthOfHostID == 16) {
			hostID = (lastPart << 16) >> 16;
		}
		
		if (lengthOfHostID == 24) {
			hostID = (lastPart << 8) >> 8;
		}
		
		return hostID;
	}
	
	private int lengthOfHostID = 8;
	
	/**
	 * This method is used to set type of address
	 */
	private void setTypeOfAddresss() {
		Address one = G.getAddress(0);
		int firstPart = one._1;
		int firstBit = firstPart >> 31;
		int firstTwoBits = firstPart >> 30;
		int firstThreeBits = firstPart >> 29;
		if (firstBit == 0) {
			lengthOfHostID = 24;
			return;
		}
		if (firstTwoBits == 1) {
			lengthOfHostID = 16;
			return ;
		}
		if (firstThreeBits == 5) { 
			lengthOfHostID = 8;
			return ;
		}		
	}
	
	/**
	 * This method is used to check the valid of pairs of hosts
	 */
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
		if (sources.size() != k * k * k / 4) {
			System.out.println("Not enough pair! Just " + sources.size());
			for(int i = 0; i < sources.size(); i++) {
				int realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
				System.out.println("From " + sources.get(i) + " through " +
						getCoreSwitch(sources.get(i), destinations.get(i))
						+ "/" + realCore
						+ " to "
						+ destinations.get(i) + "(HostID = " + getHostID(destinations.get(i)) + ")");
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
		for (int i = 0; i < sources.size(); i++) {
			int realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
			System.out.println("From " + sources.get(i) + " through " +
					getCoreSwitch(sources.get(i), destinations.get(i))
					+ "/" + realCore
					+ " to "
					+ destinations.get(i) + "(HostID = " + getHostID(destinations.get(i)) + ")");           
		}     
	}
}


