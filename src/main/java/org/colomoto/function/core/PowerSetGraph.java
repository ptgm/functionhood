package org.colomoto.function.core;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For clauses of size n, it generates the complete Power Set graph containing
 * the set of all subsets up until a set with n elements. It does not contain
 * the empty set, thus having 2^n - 1 subsets, as well as their inclusion 
 * relationships.
 * 
 * @author Pedro T. Monteiro
 * @author José R. Cury
 *
 */
public class PowerSetGraph {
	private int nvars;
	private Map<Clause, Set<Clause>> fathers;
	private Map<Clause, Set<Clause>> sons;
	private Set<Clause> all;

	public PowerSetGraph(int nvars) {
		this.nvars = nvars;
		this.fathers = new HashMap<Clause, Set<Clause>>();
		this.sons = new HashMap<Clause, Set<Clause>>();
		this.all = new HashSet<Clause>();
		Clause c = new Clause(nvars);
		if (this.getComputedSons(c) == null) {
			this.sons.put(c, new HashSet<Clause>());
			this.all.add(c);
		}
		this.buildGraph(c);
	}

	/**
	 * Recursively computes the powerset graph using a bottom-up approach. From
	 * a given clause c, it computes its fathers, keeping their father-son
	 * relations. Then if the a father is not known, it calls itself to build
	 * the graph of the father.
	 * 
	 * @param c
	 */
	private void buildGraph(Clause c) {
		if (this.getComputedFathers(c) == null) {
			this.fathers.put(c, new HashSet<Clause>());
		}
		if (c.order() == 1) {
			// Cannot have a father (it'd be empty)
			return;
		}
		for (Clause cFather : this.computeFathers(c)) {
			if (this.getComputedSons(cFather) == null) {
				this.sons.put(cFather, new HashSet<Clause>());
			}
			this.all.add(cFather);
			this.addRelation2Graph(cFather, c);
			// Optimization: only if not computed yet
			if (this.getComputedFathers(cFather) == null
					|| this.getComputedFathers(cFather).isEmpty()) {
				this.buildGraph(cFather);
			}
		}
	}

	/**
	 * Computes the fathers of a given Clause c. Fathers will be more generic in
	 * the sense that each will have one position less set to true than Clause
	 * c. This yields a small size list of clauses instead of a set, since it's
	 * most of the times < 10 elements.
	 * 
	 * @param c
	 *            A clause c to compute all possible fathers
	 * @return the list of all possible fathers
	 */
	private List<Clause> computeFathers(Clause c) {
		List<Clause> lFathers = new ArrayList<Clause>();
		BitSet bs = c.getSignature();
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
			BitSet clone = (BitSet) bs.clone();
			clone.clear(i);
			lFathers.add(new Clause(this.nvars, clone));
		}
		return lFathers;
	}

	/**
	 * Adds a father-son relationship between two clauses in the powerset graph.
	 * 
	 * @param cFather
	 *            the father clause
	 * @param cSon
	 *            the son clause
	 */
	private void addRelation2Graph(Clause cFather, Clause cSon) {
		// Fathers
		Set<Clause> sFathers = this.getComputedFathers(cSon);
		sFathers.add(cFather);
		this.fathers.put(cSon, sFathers);

		// Sons
		Set<Clause> sSons = this.getComputedSons(cFather);
		sSons.add(cSon);
		this.sons.put(cFather, sSons);
	}

	/**
	 * Gets the computed fathers of clause c present in the powerset graph.
	 * 
	 * @param c
	 * @return
	 */
	public Set<Clause> getComputedFathers(Clause c) {
		return this.fathers.get(c);
	}

	/**
	 * Gets the computed sons of clause c present in the powerset graph.
	 * 
	 * @param c
	 * @return
	 */
	public Set<Clause> getComputedSons(Clause c) {
		return this.sons.get(c);
	}

	/**
	 * The meet operator, returns a clause that is both a son of clause ci and
	 * clause cj, if possible. If no such clause exists, it returns null.
	 * 
	 * @param ci
	 * @param cj
	 * @return
	 */
	public Clause meet(Clause ci, Clause cj) {
		Set<Clause> sMeet = new HashSet<Clause>(this.getComputedSons(ci));
		sMeet.retainAll(this.getComputedSons(cj));
		// Has at most 1 element!
		return (sMeet.isEmpty() ? null : sMeet.iterator().next());
	}

	/**
	 * Given a set of clauses, it finds the set of independent clauses from a
	 * given set of clauses it checks the powerset graph to find the set of all
	 * clauses that are independent
	 * 
	 * @param sClauses
	 * @return
	 */
	public Set<Clause> getIndependentClauses(Set<Clause> sClauses) {
		// Get Dependent
		Set<Clause> sDependent = new HashSet<Clause>();
		for (Clause c : sClauses) {
			sDependent.addAll(this.getAncestors(c, sDependent));
			sDependent.addAll(this.getDescendants(c, sDependent));
		}

		// Get Independent
		Set<Clause> sDiff = new HashSet<Clause>(this.all);
		sDiff.removeAll(sDependent);
		// Remove self
		sDiff.removeAll(sClauses);

		// Compute minimal in sDiff
		return this.getMinimal(sDiff);
	}

	// TODO: some optimizations needed
	public Set<Clause> getMinimal(Set<Clause> sClauses) {
		List<Clause> lClauses = new ArrayList<Clause>(sClauses);
		Set<Clause> sMinimal = new HashSet<Clause>();
		for (int i = 0; i < lClauses.size(); i++) {
			boolean dominates = false;
			for (int j = 0; j < lClauses.size(); j++) {
				if (i != j && lClauses.get(i).isLargerOrEqual(lClauses.get(j))) {
					dominates = true;
					break;
				}
			}
			if (!dominates) {
				sMinimal.add(lClauses.get(i));
			}
		}
		return sMinimal;
	}

	private Set<Clause> getAncestors(Clause c, Set<Clause> sSeen) {
		for (Clause cFather : this.fathers.get(c)) {
			if (!sSeen.contains(cFather)) {
				sSeen.add(cFather);
				sSeen.addAll(this.getAncestors(cFather, sSeen));
			}
		}
		return sSeen;
	}

	private Set<Clause> getDescendants(Clause c, Set<Clause> sSeen) {
		for (Clause cSon : this.sons.get(c)) {
			if (!sSeen.contains(cSon)) {
				sSeen.add(cSon);
				sSeen.addAll(this.getDescendants(cSon, sSeen));
			}
		}
		return sSeen;
	}
}
