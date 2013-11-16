/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repairmansim;

/**
 * A simple class to represent a machine in the machine
 * repairman discrete-event simulation.
 * 
 * @author Zac Chenaille
 */
public class Machine {
    
    private boolean broken = false;
    
    private double nextEventTime = 0.00;
    
    public void fail() {
	broken = true;
    }
    
    public void fixed() {
	broken = false;
    }
    
    public boolean isBroken() {
	return broken;
    }
    
    public void setNextFailureTime(double nextEventTime) {
	this.nextEventTime = nextEventTime;
    }
    
    public double getNextFailureTime() {
	return this.nextEventTime;
    }
}
