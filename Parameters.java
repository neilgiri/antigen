/* Stores parameters for use across simulation */
/* Start with parameters in source, implement input file later */
/* A completely static class.  */

import static java.lang.Math.*;

public class Parameters {

	// simulation parameters
	public static int day = 0;
	public static final int burnin = 0; // 0
	public static final int endDay = 3*365; // 20*365 = 7300
	public static final int printStep = 10;								// print to out.timeseries every day
	public static final double tipSamplingRate = 1;						// in samples per deme per day
	public static final int tipSamplesPerDeme = 2000;
	public static final boolean tipSamplingProportional = false;		// whether to sample proportional to prevalance
	public static final double treeProportion = 1;						// proportion of tips to use in tree reconstruction
	public static final int	diversitySamplingCount = 1000;				// how many samples to draw to calculate diversity
	public static final int	netauSamplingCount = 1000000;				// how many samples to draw to calculate Ne*tau	
	public static final double	netauWindow = 10;						// window in days to calculate Ne*tau	
	public static final int	serialIntervalSamplingCount = 1000000;		// how many samples to draw to calculate serial interval	
	public static final boolean repeatSim = false;						// repeat simulation until endDay is reached?
	public static final boolean memoryProfiling = false;				// requires -javaagent:classmexer.jar to run

	public static Virus urVirus = new Virus();

	// metapopulation parameters
	public static final int demeCount = 1;
	public static final String[] demeNames = {"world"};
	public static final int[] initialNs = {1000000};	
	
	// host parameters
	public static final double birthRate = 0.000091;				// in births per individual per day, 1/30 years = 0.000091
	public static final double deathRate = 0.000091;				// in deaths per individual per day, 1/30 years = 0.000091
	public static final boolean swapDemography = true;				// whether to keep overall population size constant
		
	// epidemiological parameters
	public static final int initialI = 1;							// in individuals
	public static final double initialPrR = 0.0; 					// as proportion of population
	public static final double beta = 0.3; 							// in contacts per individual per day
	public static final double nu = 0.2; 							// in recoveries per individual per day
	public static final double immunityLoss = 0.00274;				// in R->S per individual per day
	public static final double betweenDemePro = 0.0005;				// relative to within-deme beta		
	
	// seasonal betas
	public static final double[] demeBaselines = {1};
	public static final double[] demeAmplitudes = {0};
	public static final double[] demeOffsets = {0};					// relative to the year
			
	// measured in years, starting at burnin
	public static double getDate() {
		return ((double) day - (double) burnin ) / 365.0;
	}
	
	public static double getSeasonality(int index) {
		double baseline = demeBaselines[index];
		double amplitude = demeAmplitudes[index];
		double offset = demeOffsets[index];
		return baseline + amplitude * Math.cos(2*Math.PI*getDate() + 2*Math.PI*offset);
	}

}