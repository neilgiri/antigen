/* Acts as constructor for Phenotype objects */
/* A completely static class */

public class PhenotypeFactory {

	// returns newly instantiated Phenotype objects of type according to Parameters.phenotypeSpace
	public static Phenotype makeVirusPhenotype() {
	
		Phenotype p = null;
		if (Parameters.phenotypeSpace == "geometric") { p = new GeometricPhenotype(); }			
		return p;
	
	}
	
	// returns newly instantiated Phenotype objects of type according to Parameters.phenotypeSpace
	public static Phenotype makeHostPhenotype() {
	
		Phenotype p = null;
		if (Parameters.phenotypeSpace == "geometric") { p = new GeometricPhenotype(Parameters.initialTraitA, Parameters.initialTraitB); }	
		return p;
	
	}	
	
	// returns newly instantiated Phenotype objects of type according to Parameters.phenotypeSpace
	public static Phenotype makeArbitaryPhenotype(double x, double y) {
	
		Phenotype p = null;
		if (Parameters.phenotypeSpace == "geometric") { p = new GeometricPhenotype(x, y); }	
		return p;
	
	}		

}