
package repairmansim;

import java.util.*;

/**
 * A discrete event simulation of the traditional machine repairman problem.
 * 
 * In the machine repairman problem, there are c machines, all of which
 * may fail at rate lambda. When this happens, a single repairman
 * will go around (on a first-come first-serve basis) and fix all broken machines
 * at a rate mu.
 * 
 * @author Zac Chenaille
 */
public class RepairmanSim {

    //The number of machines to fix before ending the simulation
    private static final int stopAtMachinesFixed = 100;
    //Machine failure rate
    private static final double lambda = 0.4;
    //Repairman fix rate
    private static final double mu = 0.6;
    //Number of machines
    private static final int c = 4;
    //Number of repairmen (for scalability purposes only)
    private static final int r = 1;

    //The current simulation time
    private static double currentTime = 0.0;
    
    //Holds all of the machines we will be using
    private static final List<Machine> machines = new ArrayList<Machine>();
    static {
	//Initialize the machines to use in the simulation
	for (int m = 0; m < c; m++) {
	    machines.add(new Machine());
	}
    }
    
    private static final List<Repairman> repairmen = new ArrayList<Repairman>();
    static {
	//Initialize the repairmen to use in the simulation
	for (int i = 0; i < r; i++) {
	    repairmen.add(new Repairman());
	}
    }
    
    //Holds the list of pending events (machines and repairmen) in the order in which they will happen
    private static final PriorityQueue eventList = new PriorityQueue(c, new Comparator() {
	@Override
	public int compare(Object o1, Object o2) {
	    Double eventTime1 = null;
	    Double eventTime2 = null;
	    
	    if (o1 instanceof Machine) {
		eventTime1 = ((Machine)o1).getNextFailureTime();
	    } else if (o1 instanceof Repairman) {
		eventTime1 = ((Repairman)o1).getNextFixTime();
	    }
	    if (o2 instanceof Machine) {
		eventTime2 = ((Machine)o2).getNextFailureTime();
	    } else if (o2 instanceof Repairman) {
		eventTime2 = ((Repairman)o2).getNextFixTime();
	    }
	    
	    return eventTime1.compareTo(eventTime2);
	}
    });
    
    /**
     * Runs the discrete-event machine repairman simulation
     * @param args 
     */
    public static void main(String[] args) {
	
	//Tracks the number of machines that have been fixed in this simulation
	int numMachinesFixed = 0;
	
	//Holds currently failed machines in the order in which they failed
	ArrayDeque<Machine> failedMachines = new ArrayDeque<Machine>();
	
	//Holds currently idle repairmen in the order in which they became idle
	ArrayDeque<Repairman> idleRepairmen = new ArrayDeque<Repairman>();
	
	//Initialize the event list
	for (Machine machine : machines) {
	    machine.setNextFailureTime(currentTime + generateExponentialRV(lambda));
	    eventList.offer(machine);
	}
	
	//Put all repairmen into our idle queue
	for (Repairman repairman : repairmen) {
	    idleRepairmen.offer(repairman);
	}
	
	//Continue the simulation until we have fixed the specified number of machines...
	while (numMachinesFixed < stopAtMachinesFixed) {
	    Object eventObject = eventList.poll();
	    if (eventObject instanceof Machine) {
		//This event is a machine failure
		Machine machine = (Machine)eventObject;
		currentTime = machine.getNextFailureTime();
		
		//Fail the machine
		machine.fail();
		
		if (!idleRepairmen.isEmpty()) {
		    //Check and see if there is a free repairman...If there is, get them started on this machine
		    Repairman nextRepairman = idleRepairmen.poll();
		    nextRepairman.setMachineFixing(machine);
		    nextRepairman.setNextFixTime(currentTime + generateExponentialRV(mu));
		    
		    eventList.offer(nextRepairman);
		}
		else {
		    //If there is no repairman to work on this machine, add it to the failure queue
		    failedMachines.offer(machine);
		}
	    }
	    else if (eventObject instanceof Repairman) {
		//This event is the repairman fixing a machine
		Repairman repairman = (Repairman)eventObject;
		currentTime = repairman.getNextFixTime();

		//Schedule a time for the recently fixed machine to fail again
		Machine fixedMachine = repairman.getMachineFixing();
		fixedMachine.fixed();
		fixedMachine.setNextFailureTime(currentTime + generateExponentialRV(lambda));
		eventList.offer(fixedMachine);

		//If there are no idle repairmen, check if there are any other failed machines waiting to be fixed
		if (idleRepairmen.isEmpty() && !failedMachines.isEmpty()) {
		    Machine nextMachine = failedMachines.poll();
		    repairman.setMachineFixing(nextMachine);
		    repairman.setNextFixTime(currentTime + generateExponentialRV(mu));
		    
		    eventList.offer(repairman);
		}
		else {
		    //No other machines to fix right now
		    repairman.setMachineFixing(null);
		    idleRepairmen.offer(repairman);
		}
		
		numMachinesFixed++;
	    }
	}
	System.out.println(numMachinesFixed + " machines fixed in " + currentTime);
	
    }
    
    /**
     * Determines the system state based on the number of machines that are currently in a state of failure.
     * 
     * @return The number of machines that are currently broken
     */
    private static int getSystemState() {
	int systemState = 0;
	for (Machine machine : machines) {
	    if (machine.isBroken()) {
		systemState++;
	    }
	}
	return systemState;
    }
    
    private static double generateExponentialRV(double rate) {
	return -Math.log(Math.random())/rate;
    }
}
