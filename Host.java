/* A human individual that harbors viruses and immunity */

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Host {

	// fields
	private Virus infection;		
	
	// naive host
	public Host() {
		infection = null;		
	}
	
	// initial infected host
	public Host(Virus v) {
		infection = v;
	}
				
	// infection methods
	public void reset() {
		infection = null;
	}
	
	public boolean isInfected() {
		boolean infected = false;
		if (infection != null) {
			infected = true;
		}
		return infected;
	}
	public Virus getInfection() {
		return infection;
	}
	public void infect(Virus pV, int d) {
		Virus nV = new Virus(pV, d);
		infection = nV;
	}
	public void clearInfection() {
		infection = null;
	}
		
	public String toString() {
		return Integer.toHexString(this.hashCode());
	}	
	
}