/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repairmansim;

/**
 * A simple class to represent a repairman in the machine
 * repairman discrete-event simulation
 * 
 * @author Zac Chenaille
 */
public class Repairman {
    
    private Machine machineFixing;
    
    private double nextEventTime = 0.0;
    
    public boolean isWorking() {
	return machineFixing != null;
    }
    
    public void setMachineFixing(Machine machineFixing) {
	this.machineFixing = machineFixing;
    }
    
    public Machine getMachineFixing() {
	return machineFixing;
    }
    
    public void setNextFixTime(double nextEventTime) {
	this.nextEventTime = nextEventTime;
    }
    
    public double getNextFixTime() {
	return nextEventTime;
    }
}
