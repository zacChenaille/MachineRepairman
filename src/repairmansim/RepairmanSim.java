
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
    private static final int stopAtMachinesFixed = 100000;
    //Machine failure rate
    private static final double lambda = 0.4;
    //Repairman fix rate
    private static final double mu = 0.6;
    //Number of machines
    private static final int c = 4;
    //Number of repairmen
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
    
    //Holds the list of repairmen to use in the simulation
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
	
	//Records how much time the system spends in each state
	HashMap<Integer, Double> stateTimeMap = new HashMap<Integer, Double>();
	for (int state = 0; state <= c; state++) {
	    stateTimeMap.put(state, 0.0);
	}
	
	//Records how much time a certain number of repairmen have been utilized
	HashMap<Integer, Double> repairmenUsedMap = new HashMap<Integer, Double>();
	for (int menUsed = 0; menUsed <= r; menUsed++) {
	    repairmenUsedMap.put(menUsed, 0.0);
	}

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
	    
	    //Capture the data from the previous event time
	    int previousSystemState = getSystemState();
	    int previousMenUsed = repairmen.size() - idleRepairmen.size();

	    double previousSimTime = currentTime;
	    
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
	    //Update our record of system states (working machines and utilized repairmen)
	    stateTimeMap.put(previousSystemState, stateTimeMap.get(previousSystemState) + (currentTime - previousSimTime));
	    repairmenUsedMap.put(previousMenUsed, repairmenUsedMap.get(previousMenUsed) + (currentTime - previousSimTime));
	}
	
	//Calculate the average number of working machines as well as steady-state probabilities
	double averageWorking = 0.0;
	HashMap<Integer, Double> steadyStateProbs = new HashMap<Integer, Double>();
	//Calculate steady-state probabilities
	for (Integer state : stateTimeMap.keySet()) {
	    steadyStateProbs.put(state, stateTimeMap.get(state)/currentTime);
	    averageWorking += state*steadyStateProbs.get(state);
	}

	//Calculate average repairmen utilization
	double averageUtilization = 0.0;
	HashMap<Integer, Double> steadyRepairmenUsedProbs = new HashMap<Integer, Double>();
	for (Integer menUsed : repairmenUsedMap.keySet()) {
	    if (repairmenUsedMap.get(menUsed) > 0) {
		steadyRepairmenUsedProbs.put(menUsed, repairmenUsedMap.get(menUsed)/currentTime);
		averageUtilization += menUsed*steadyRepairmenUsedProbs.get(menUsed);
	    }
	}

	System.out.println(numMachinesFixed + " machines fixed in " + currentTime);
	System.out.println("Average number working machines: " + averageWorking);
	System.out.println("Average repairmen being utilized: " + averageUtilization);
	
	System.out.println("Steady-State probabilities:");
	for (Integer state : steadyStateProbs.keySet()) {
	    System.out.println("    " + state + ": " + steadyStateProbs.get(state));
	}
    }
    
    /**
     * Determines the system state based on the number of machines that are currently working.
     * 
     * @return The number of machines that are currently working
     */
    private static int getSystemState() {
	int systemState = 0;
	for (Machine machine : machines) {
	    if (!machine.isBroken()) {
		systemState++;
	    }
	}
	return systemState;
    }
    
    private static double generateExponentialRV(double rate) {
	return -Math.log(Math.random())/rate;
    }
}
