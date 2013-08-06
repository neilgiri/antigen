/* Stores a list of Viruses that have sampled during the course of the simulation */

import java.util.*;
import java.io.*;
import static java.lang.Math.*;

public class VirusTree {

	// fields
	private static Virus root = Parameters.urVirus;	
	private static List<Virus> tips = new ArrayList<Virus>();
		
	static final Comparator<Virus> descendantOrder = new Comparator<Virus>() {
		public int compare(Virus v1, Virus v2) {
			Integer descendantsV1 = new Integer(getNumberOfDescendants(v1));
			Integer descendantsV2 = new Integer(getNumberOfDescendants(v2));
			return descendantsV1.compareTo(descendantsV2);
		}
	};	
		
	// static methods
	public static void add(Virus v) {		
		tips.add(v);
	}
	public static void clear() {
		tips.clear();
	}
	public static List<Virus> getTips() {
		return tips;
	}
	public static Virus getRoot() {
		return root;
	}
	
	// return a random tip that lies between year from and year to
	public static Virus getRandomTipFromTo(double from, double to) {
	
		// fill temporary list
		List<Virus> select = new ArrayList<Virus>();
		for (Virus v : tips) {
			double x = v.getBirth();
			if (x >= from && x < to) {
				select.add(v);
			}
		}
		
		// pull random virus from this list
		Virus rV = null;
		if (select.size() > 0) {	
			int index = Random.nextInt(0,select.size()-1);
			rV = select.get(index);
		}
		return rV;
		
	}
	
	public static int getDemeCount(int d) {
		int count = 0;
		for (Virus v : tips) {
			if (v.getDeme() == d) {
				count++;
			}
		}
		return count;
	}	
		
	// work backwards for each sample filling the children lists
	public static void fillBackward() {
	
		for (Virus child : tips) {
			Virus parent = child.getParent();
			while (parent != null) {
				parent.addChild(child);
				parent.incrementCoverage();
				child = parent;
				parent = child.getParent();
			}
		}
	
	}
	
	public static void dropTips() {
	
		List<Virus> reducedTips = new ArrayList<Virus>();
		for (Virus v : tips) {
			if (Random.nextBoolean(Parameters.treeProportion)) {
				reducedTips.add(v);
			}
		}
		tips = reducedTips;
	
	}

	// marking to by time, not proportional to prevalence
	public static void markTips() {
			
		for (double i = 0; i < Parameters.getDate(); i+=0.1) {
			Virus v = getRandomTipFromTo(i,i+0.1);
			if (v != null) {
				while (v.getParent() != null) {
					v.mark();
					v = v.getParent();
				}
			}
		}
		
	}
		
	// prune tips
	public static void pruneTips() {
	
		List<Virus> reducedTips = new ArrayList<Virus>();
		for (int d = 0; d < Parameters.demeCount; d++) {
			double keepProportion = (double) Parameters.tipSamplesPerDeme / (double) getDemeCount(d);
			for (Virus v : tips) {
				if (Random.nextBoolean(keepProportion) && v.getDeme() == d) {
					reducedTips.add(v);
				}
			}
		}
		tips = reducedTips;
	
	}
	
	// returns virus v and all its descendents via a depth-first traversal
	public static List<Virus> postOrderNodes(Virus v) {
		List<Virus> vNodes = new ArrayList<Virus>();
		vNodes.add(v);
		vNodes = postOrderChildren(vNodes);
		return vNodes;
	}
	
	public static List<Virus> postOrderNodes() {
		return postOrderNodes(root);
	}	
	
	// returns virus v and all its descendents via a depth-first traversal
	public static List<Virus> postOrderChildren(List<Virus> vNodes) {
	
		Virus last = vNodes.get(vNodes.size()-1);
	
		for (Virus child : last.getChildren()) {
			vNodes.add(child);
			postOrderChildren(vNodes);
		}
		
		return vNodes;
	
	}


	// Count total descendents of a Virus, working through its children and its children's children
	public static int getNumberOfDescendants(Virus v) {
	
		int numberOfDescendants = v.getNumberOfChildren();
		
		for (Virus child : v.getChildren()) {
			numberOfDescendants += getNumberOfDescendants(child);
		}
		
		return numberOfDescendants;
		
	}
	
	public static int getNumberOfDescendants() {
		return getNumberOfDescendants(root);
	}
		
	// sorts children lists so that first member is child with more descendents than second member
	public static void sortChildrenByDescendants(Virus v) {
		
		List<Virus> children = v.getChildren();
		Collections.sort(children, descendantOrder);
		
		for (Virus child : children) {
			sortChildrenByDescendants(child);
		}
				
	}	
	
	public static void sortChildrenByDescendants() {
		sortChildrenByDescendants(root);
	}
	
	// sets Virus layout based on a postorder traversal
	public static void setLayoutByDescendants() {
	
		List<Virus> vNodes = postOrderNodes();
		
		// set layout of tips based on traversal
		double y = 0;
		for (Virus v : vNodes) {
//			if (tips.contains(v)) {
			if (v.isTip()) {
				v.setLayout(y);
				y++;
			}
		}
		
		// update layout of internal nodes
		Collections.reverse(vNodes);
		for (Virus v : vNodes) {
			if (v.getNumberOfChildren() > 0) {
				double mean = 0;
				for (Virus child : v.getChildren()) {
					mean += child.getLayout();
				}
				mean /= v.getNumberOfChildren();
				v.setLayout(mean);
			}
		}
		
	}	
	
	// looks at a virus and its grandparent, if traits are identical and there is no branching
	// then make virus child rather than grandchild
	// returns v.parent after all is said and done
	public static Virus collapse(Virus v) {
	
		Virus vp = null;
		Virus vgp = null;
		if (v.getParent() != null) {
			vp = v.getParent();
			if (vp.getParent() != null) {
				vgp = vp.getParent();
			}
		}

		if (vp != null && vgp != null) {
		
			if (vp.getNumberOfChildren() == 1) {
		
				List<Virus> vgpChildren = vgp.getChildren();
				int vpIndex =  vgpChildren.indexOf(vp);
				
				if (vpIndex >= 0) {
				
					// replace virus as child of grandparent
					vgpChildren.set(vpIndex, v);
				
					// replace grandparent as parent of virus
					v.setParent(vgp);
				
					// erase parent
					vp = null;
				
				}
		
			}
		}
		
		return v.getParent();

	}
	
	// walks backward using the list of tips, collapsing where possible
	public static void streamline() {
		
		for (Virus v : tips) {
			Virus vp = v;
			while (vp != null) {
				vp = collapse(vp);
			}
		}
		
	}
		
	public static void printTips() {
		
		try {
			File tipFile = new File("out.tips");
			tipFile.delete();
			tipFile.createNewFile();
			PrintStream tipStream = new PrintStream(tipFile);
			tipStream.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n", "name", "year", "trunk", "tip", "mark", "location", "layout");
			for (int i = 0; i < tips.size(); i++) {
				Virus v = tips.get(i);			
				tipStream.printf("\"%s\",%.4f,%d,%d,%d,%d,%.4f\n", v, v.getBirth(), v.isTrunk()?1:0, v.isTip()?1:0, v.isMarked()?1:0, v.getDeme(), v.getLayout());
			}
			tipStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}
		
	}
	
	public static void printBranches() {
		
		try {
			File branchFile = new File("out.branches");
			branchFile.delete();
			branchFile.createNewFile();
			PrintStream branchStream = new PrintStream(branchFile);
			for (Virus v : postOrderNodes()) {
				if (v.getParent() != null) {
					Virus vp = v.getParent();
					branchStream.printf("{\"%s\",%.4f,%d,%d,%d,%d,%.4f}\t", v, v.getBirth(), v.isTrunk()?1:0, v.isTip()?1:0, v.isMarked()?1:0, v.getDeme(), v.getLayout());
					branchStream.printf("{\"%s\",%.4f,%d,%d,%d,%d,%.4f}\t", vp, vp.getBirth(), vp.isTrunk()?1:0, vp.isTip()?1:0, v.isMarked()?1:0, vp.getDeme(), vp.getLayout());
					branchStream.printf("%d\n", vp.getCoverage());
				}
			}
			branchStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}
		
	}
	
	// assess node in building Newick string
	public static Virus assessNode(Virus v, List<Virus> visited, PrintStream treeStream) {
	
		Virus returnVirus = null;
		boolean printHeight = false;
	
		// if virus has multiple children, return first child that has not been visited
		if (v.getNumberOfChildren() > 1) {
			boolean childrenVisited = true;
			for (int i = 0; i < v.getNumberOfChildren(); i++) {
				Virus vc = v.getChildren().get(i);
				if (!visited.contains(vc)) {
					if (i == 0) {
						treeStream.print("(");
					}
					else {
						treeStream.print(",");
					}
					childrenVisited = false;
					returnVirus = vc;
					break;
				}
			}
			// failure, all children visited, return to parent
			if (childrenVisited) {
				treeStream.print(")");	
				printHeight = true;
				returnVirus = v.getParent();
			}
		}
		
		// if tip is encountered, print tip, return to parent
		if (v.getNumberOfChildren() == 0) {
			treeStream.print(v.toString());
			printHeight = true;
			returnVirus = v.getParent();		
		}			
		
		// walk down (or up) branches 
		if (v.getNumberOfChildren() == 1) {
			Virus vc = v.getChildren().get(0);
			if (!visited.contains(vc)) {
				returnVirus = vc;
			}
			else {
				returnVirus = v.getParent();
			}
		}
		
		// find height, walk back until a parent with a split occurs
		if (printHeight && v.getParent() != null) {

			Virus vp = v.getParent();
			while (vp.getNumberOfChildren() == 1 && vp.getParent() != null) {
				vp = vp.getParent();
			}
			double height = v.getBirth() - vp.getBirth();
			treeStream.printf(":%.4f", height);	

		}
		
		return returnVirus;
	
	}
	
	public static void printNewick() {
	
		try {
			File treeFile = new File("out.trees");
			treeFile.delete();
			treeFile.createNewFile();
			PrintStream treeStream = new PrintStream(treeFile);
			
			List<Virus> visited = new ArrayList<Virus>();
				
			// start at root
			Virus v = root;
			visited.add(v);			
			
			while (v != null) {
				
				v = assessNode(v, visited, treeStream);		
				visited.add(v);
							
			}
			treeStream.println();
			
			treeStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}
	
	}
		
}