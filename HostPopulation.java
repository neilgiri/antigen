/* A population of host individuals */

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class HostPopulation {

	// fields
	private int deme;
	private int cases;	
	private List<Host> susceptibles = new ArrayList<Host>();
	private List<Host> infecteds = new ArrayList<Host>();	
	private List<Host> recovereds = new ArrayList<Host>();		// this is the transcendental class, immune to all forms of virus  
	private double diversity;
	private double tmrca;
	private double netau;	

	// construct population, using Virus v as initial infection
	public HostPopulation(int d) {
	
		// basic parameters
		deme = d;
		int initialR = (int) ((double) Parameters.initialNs[deme] * Parameters.initialPrR);
	
		// fill population with susceptibles
		int initialS = Parameters.initialNs[deme] - initialR;
		if (deme == 0) {
			initialS -= Parameters.initialI;
		}
		for (int i = 0; i < initialS; i++) {
			Host h = new Host();			
			susceptibles.add(h);
		}
		
		// fill population with recovereds
		for (int i = 0; i < initialR; i++) {
			Host h = new Host();			
			recovereds.add(h);
		}		
		
		if (deme == 0) {
		
			// infect some individuals
			for (int i = 0; i < Parameters.initialI; i++) {
				Virus v = new Virus(Parameters.urVirus, deme);
				Host h = new Host(v);
				infecteds.add(h);
			}	
		
		}
		
	}
		
	// accessors
	public int getN() {
		return susceptibles.size() + infecteds.size() + recovereds.size();
	}
	public int getS() {
		return susceptibles.size();
	}
	public int getI() {
		return infecteds.size();
	}
	public int getR() {
		return recovereds.size();
	}	
	public double getPrS() {
		return (double) getS() / (double) getN();
	}
	public double getPrI() {
		return (double) getI() / (double) getN();
	}
	public double getPrR() {
		return (double) getR() / (double) getN();
	}	
	public int getRandomN() {
		return Random.nextInt(0,getN()-1);
	}
	public int getRandomS() {
		return Random.nextInt(0,getS()-1);
	}
	public int getRandomI() {
		return Random.nextInt(0,getI()-1);
	}
	public int getRandomR() {
		return Random.nextInt(0,getR()-1);
	}
	
	public Host getRandomHost() {
		// figure out whether to pull from S, I or R
		Host h = null;
		double n = Random.nextDouble(0.0,1.0);
		if (n < getPrS()) {
			h = getRandomHostS();
		}
		else if (n > getPrS() && n < getPrS() + getPrI()) {
			h = getRandomHostI();
		}
		else if (n > getPrS() + getPrI()) {
			h = getRandomHostR();
		}
		return h;
	}
	
	public Host getRandomHostS() {
		int index = Random.nextInt(0,getS()-1);
		return susceptibles.get(index);
	}
	public Host getRandomHostI() {
		Host h = null;
		if (getI() > 0) {
			int index = Random.nextInt(0,getI()-1);
			h = infecteds.get(index);
		}
		return h;
	}
	public Host getRandomHostR() {
		Host h = null;
		if (getR() > 0) {	
			int index = Random.nextInt(0,getR()-1);
			h = recovereds.get(index);
		}
		return h;
	}	
	
	public Virus getRandomInfection() {
		Virus v = null;
		Host h = getRandomHostI();
		if (h != null) {
			v = h.getInfection();
		}
		return v;
	}	
	
	public void resetCases() {
		cases = 0;
	}
	public int getCases() {
		return cases;
	}	

	public double getDiversity() {
		return diversity;
	}	
	
	public double getTmrca() {
		return tmrca;
	}		
	
	public double getNetau() {
		return netau;
	}			
	
	public void removeSusceptible(int i) {
		int lastIndex = getS() - 1;
		Host lastHost = susceptibles.get(lastIndex);
		susceptibles.set(i,lastHost);
		susceptibles.remove(lastIndex);
	}	
	public void removeInfected(int i) {
		int lastIndex = getI() - 1;
		Host lastHost = infecteds.get(lastIndex);
		infecteds.set(i,lastHost);
		infecteds.remove(lastIndex);
	}
	public void removeRecovered(int i) {
		int lastIndex = getR() - 1;
		Host lastHost = recovereds.get(lastIndex);
		recovereds.set(i,lastHost);
		recovereds.remove(lastIndex);
	}	
	
	public void stepForward() {
	
	//	resetCases();
		if (Parameters.swapDemography) {
			swap();
		} else {
			grow();
			decline();
		}
		contact();
		recover();
		loseImmunity(); 
		sample();
	
	}
	
	// draw a Poisson distributed number of births and add these hosts to the end of the population list
	public void grow() {
		double totalBirthRate = getN() * Parameters.birthRate;
		int births = Random.nextPoisson(totalBirthRate);
		for (int i = 0; i < births; i++) {
			Host h = new Host();
			susceptibles.add(h);
		}
	}
	
	// draw a Poisson distributed number of deaths and remove random hosts from the population list
	public void decline() {
		// deaths in susceptible class
		double totalDeathRate = getS() * Parameters.deathRate;
		int deaths = Random.nextPoisson(totalDeathRate);
		for (int i = 0; i < deaths; i++) {
			if (getS()>0) {
				int sndex = getRandomS();
				removeSusceptible(sndex);
			}
		}		
		// deaths in infectious class		
		totalDeathRate = getI() * Parameters.deathRate;
		deaths = Random.nextPoisson(totalDeathRate);
		for (int i = 0; i < deaths; i++) {
			if (getI()>0) {
				int index = getRandomI();
				removeInfected(index);
			}
		}		
	}
	
	// draw a Poisson distributed number of births and reset these individuals
	public void swap() {
		// draw random individuals from susceptible class
		double totalBirthRate = getS() * Parameters.birthRate;
		int births = Random.nextPoisson(totalBirthRate);
		for (int i = 0; i < births; i++) {
			if (getS()>0) {
				int index = getRandomS();
				Host h = susceptibles.get(index);
				h.reset();
			}
		}		
		// draw random individuals from infected class
		totalBirthRate = getI() * Parameters.birthRate;
		births = Random.nextPoisson(totalBirthRate);
		for (int i = 0; i < births; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				h.reset();
				removeInfected(index);
				susceptibles.add(h);
			}
		}	
		// draw random individuals from recovered class
		totalBirthRate = getR() * Parameters.birthRate;
		births = Random.nextPoisson(totalBirthRate);
		for (int i = 0; i < births; i++) {
			if (getR()>0) {
				int index = getRandomR();
				Host h = recovereds.get(index);
				h.reset();
				removeRecovered(index);
				susceptibles.add(h);
			}
		}			
	}

	// draw a Poisson distributed number of contacts and move from S->I based upon this
	public void contact() {

		// each infected makes I->S contacts on a per-day rate of beta * S/N
		double totalContactRate = getI() * getPrS() * Parameters.beta * Parameters.getSeasonality(deme);
		int contacts = Random.nextPoisson(totalContactRate);
		for (int i = 0; i < contacts; i++) {
			if (getS()>0 && getI()>0) {
		
				// get indices and objects
				int index = getRandomI();
				int sndex = getRandomS();
				Host iH = infecteds.get(index);
				Host sH = susceptibles.get(sndex);			
				Virus v = iH.getInfection();
				
				// infection
				sH.infect(v,deme);
				removeSusceptible(sndex);
				infecteds.add(sH);
				cases++;
			
			}
		}		
		
	}
	
	// draw a Poisson distributed number of contacts and move from S->I based upon this
	// this deme is susceptibles and other deme is infecteds
	public void betweenDemeContact(HostPopulation hp) {

		// each infected makes I->S contacts on a per-day rate of beta * S/N
		double totalContactRate = hp.getI() * getPrS() * Parameters.beta * Parameters.betweenDemePro * Parameters.getSeasonality(deme);
		int contacts = Random.nextPoisson(totalContactRate);
		for (int i = 0; i < contacts; i++) {
			if (getS()>0 && hp.getI()>0) {
		
				// get indices and objects
				Host iH = hp.getRandomHostI();
				int sndex = getRandomS();
				Host sH = susceptibles.get(sndex);			
				Virus v = iH.getInfection();
				
				// infection
				sH.infect(v,deme);
				removeSusceptible(sndex);
				infecteds.add(sH);
				cases++;
			
			}
		}		
		
	}	
	
	// draw a Poisson distributed number of recoveries and move from I->R based upon this
	public void recover() {
		// each infected recovers at a per-day rate of nu
		double totalRecoveryRate = getI() * Parameters.nu;
		int recoveries = Random.nextPoisson(totalRecoveryRate);
		for (int i = 0; i < recoveries; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				h.clearInfection();
				removeInfected(index);
				recovereds.add(h);
			}
		}			
	}
	
	// draw a Poisson distributed number of R->S 
	public void loseImmunity() {
		// each recovered regains immunity at a per-day rate
		double totalReturnRate = getR() * Parameters.immunityLoss;
		int returns = Random.nextPoisson(totalReturnRate);
		for (int i = 0; i < returns; i++) {
			if (getR()>0) {
				int index = getRandomR();
				Host h = recovereds.get(index);
				removeRecovered(index);
				susceptibles.add(h);
			}
		}			
	}	
		
	// draw a Poisson distributed number of samples and add them to the VirusSample
	// only sample after burnin is completed
	public void sample() {
		if (getI()>0 && Parameters.day >= Parameters.burnin) {
		
			double totalSamplingRate = Parameters.tipSamplingRate;
			if (Parameters.tipSamplingProportional) {
				totalSamplingRate *= getI();
			} 
			
			int samples = Random.nextPoisson(totalSamplingRate);
			for (int i = 0; i < samples; i++) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				Virus v = h.getInfection();
				VirusTree.add(v);
			}	
		}
	}
		
	// through current infected population assigning ancestry as trunk
	public void makeTrunk() {
		for (int i = 0; i < getI(); i++) {
			Host h = infecteds.get(i);
			Virus v = h.getInfection();
			v.makeTrunk();
			while (v.getParent() != null) {
				v = v.getParent();
				if (v.isTrunk()) {
					break;
				} else {
					v.makeTrunk();
				}
			}
		}
	}	
	
	public void updateDiversity() {
		diversity = 0.0;
		tmrca = 0.0;		
		int sampleCount = 0;
		for (int i = 0; i < Parameters.diversitySamplingCount; i++) {
			Virus vA = getRandomInfection();
			Virus vB = getRandomInfection();
			if (vA != null && vB != null) {
				double dist = vA.distance(vB);
				diversity += dist;
				if (dist > tmrca) {
					tmrca = dist;
				}				
				sampleCount += 1;
			}
		}	
		if (sampleCount > 0) {
			diversity /= (double) sampleCount;
		}
		tmrca /= 2.0;
	}		
	
	public void updateNetau() {
		double coalCount = 0.0;	
		double coalOpp = 0.0;
		double coalWindow = Parameters.netauWindow / 365.0;
		int sampleCount = 0;		
		for (int i = 0; i < Parameters.netauSamplingCount; i++) {
			Virus vA = getRandomInfection();
			Virus vB = getRandomInfection();
			if (vA != null && vB != null) {
				coalOpp += coalWindow;
				coalCount += vA.coalescence(vB, coalWindow);
				sampleCount += 1;
			}
		}	
		if (sampleCount > 0) {		
			netau = coalOpp / coalCount;
		}
	}			
	
	public void printState(PrintStream stream) {
		if (Parameters.day > Parameters.burnin) {
			updateDiversity();
			updateNetau();
			stream.printf("\t%.4f\t%.4f\t%.4f\t%d\t%d\t%d\t%d\t%d", getDiversity(), getTmrca(), getNetau(), getN(), getS(), getI(), getR(), getCases());
		}
	}	
	
	// reset population to factory condition
	public void reset() {
	
		// clearing lists
		susceptibles.clear();
		infecteds.clear();
		recovereds.clear();
		
		int initialR = (int) ((double) Parameters.initialNs[deme] * Parameters.initialPrR);
	
		// fill population with susceptibles
		int initialS = Parameters.initialNs[deme] - Parameters.initialI - initialR;
		for (int i = 0; i < initialS; i++) {
			Host h = new Host();			
			susceptibles.add(h);
		}
		
		// fill population with recovereds
		for (int i = 0; i < initialR; i++) {
			Host h = new Host();			
			recovereds.add(h);
		}		
		
		if (deme == 0) {
		
			// infect some individuals
			for (int i = 0; i < Parameters.initialI; i++) {
				Virus v = new Virus(Parameters.urVirus, deme);
				Host h = new Host(v);
				infecteds.add(h);
			}	
		
		}
		
	}
				
}