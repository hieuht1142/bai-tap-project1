package network.elements;

import config.Constant;
import infrastructure.element.Buffer;



//import network.states.packet.StateP1;
import network.states.sourcequeue.*;


public class SourceQueue  extends Buffer{
    protected int sourceId;
    protected int destinationId;
    protected long numGeneratedPacket; // the number of generated packet
     
    public SourceQueue(int sourceId) {
        this.id = sourceId;
    	this.sourceId = sourceId;
    	this.destinationId = sourceId;
    	this.numGeneratedPacket = -1;
        setState( new Sq1(this));
    }

    public SourceQueue(int sourceId, int destinationId){
        this.id = sourceId;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.numGeneratedPacket = -1; // the number of generated packet, initialize -1
        setState( new Sq1(this));
    }


    public int getSourceId() {
        return sourceId;
    }

    public void setDestinationID(int destinationID) {
    	this.destinationId = destinationID;
    }
    
    public int getDestinationID() {
    	return this.destinationId;
    }

    public boolean hasOnlyOnePacket() {
        return allPackets.size() == 1;
    }

    /**
     * This method is used to generate packet and insert it into the queue of all packets
     * 
     * @param currentTime the current time
     * @return generated packet
     */
    public Packet generatePacket(long currentTime) {
        if (this.isDelayed(currentTime)) return null;

        numGeneratedPacket++;
        double timeSent = numGeneratedPacket * Constant.HOST_DELAY;
        Packet p = new Packet(0, sourceId, destinationId, timeSent);
        insertPacket(p);
        return p;
    }

    /**
     * This method is used to remove an packet from the queue of all packets
     * @return the removed packet
     */
    public Packet removePacket() {
        if(allPackets.size() == 1) {
            if(state instanceof Sq2) {
                state = new Sq1(this);
                state.act();
            }
        }
        return allPackets.dequeue();
    }

    /**
     * This method is used to insert an packet into the queue of all packets
     * @param p the packet
     */
    public void insertPacket(Packet p) {
        allPackets.enqueue(p);
        if(state instanceof Sq1){
            state = new Sq2(this);
        }
    }

    /**
     * @param currentTime the current time
     * @return true if delaying generating next packet
     */
    public boolean isDelayed(long currentTime) {
        long r = currentTime/Constant.HOST_DELAY; // HOST_DELAY denotes the required time to generate next packet 
        return r <= numGeneratedPacket;
    }

    /**
     * @return time of the next packet
     */
    public double getNextPacketTime(){
        return (double)(numGeneratedPacket +1) * Constant.HOST_DELAY;
    }

   
}
