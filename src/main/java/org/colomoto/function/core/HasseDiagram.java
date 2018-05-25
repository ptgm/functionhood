package org.colomoto.function.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hasse Diagram
 * 
 * @author Pedro T. Monteiro
 * @author José R. Cury
 *
 */
public class HasseDiagram {
	// n=2 -> 2 nodes
	// n=3 -> 9 nodes
	// n=4 -> 114 nodes
	// n=5 -> 6.894 nodes
	// n=6 -> 7.785.062 nodes
	// n=7 -> 2.414.627.396.434 nodes
	// n=8 -> 56.130.437.209.370.320.359.966 nodes
	private int nvars;
	private PowerSetGraph powerSet;
	private static Formula fBottom;

	public HasseDiagram(int nvars) {
		this.nvars = nvars;
		this.powerSet = new PowerSetGraph(nvars);
	}

	public int getSize() {
		return this.nvars;
	}

	public Set<Formula> getAncestors(Formula fInit, boolean degenerated) {
		Set<Formula> sExplored = new HashSet<Formula>();
		Set<Formula> sToExplore = new HashSet<Formula>();
		sToExplore.add(fInit);
		while (!sToExplore.isEmpty()) {
			Formula f = sToExplore.iterator().next();
			sToExplore.remove(f);
			sToExplore.addAll(this.getFormulaParents(f, degenerated));
			sExplored.add(f);
		}
		return sExplored;
	}

	public Set<Formula> getFormulaParents(Formula f, boolean degenerated) {
		Set<Clause> sfClauses = f.getClauses();
		Set<Formula> sFprime = new HashSet<Formula>();

		// from the 1st rule
		Set<Clause> sID = this.powerSet.getIndependentClauses(f.getClauses());
		for (Clause c : sID) {
			Formula fprime = f.cloneAdd(c);
			sFprime.add(fprime);
		}

		// from 2nd rule
		Set<Clause> sAllParents = new HashSet<Clause>();
		for (Clause c : sfClauses) {
			sAllParents.addAll(this.powerSet.getComputedFathers(c));
		}

		// get minimal parents that do not dominate sID
		Set<Clause> sMinParents = this.powerSet.getMinimal(sAllParents);
		List<Clause> lR2cand = new ArrayList<Clause>();
		for (Clause c : sMinParents) {
			if (!c.dominates(sID)) {
				lR2cand.add(c);
			}
		}

		HashSet<Clause> sCand = new HashSet<Clause>();
		for (int i = 0; i < lR2cand.size(); i++) {
			Clause ci = lR2cand.get(i);
			sCand.add(ci);
			Formula fPrime = this.getConsistentFormula(sfClauses, sCand);
			if (fPrime.isConsistent()) {
				sFprime.add(fPrime);
			} else {
				if (degenerated) {
					sFprime.add(fPrime);
				} else {
					for (int j = i + 1; j < lR2cand.size(); j++) {
						Clause cj = lR2cand.get(j);
						sCand.add(cj);
						fPrime = this.getConsistentFormula(sfClauses, sCand);
						if (fPrime.isConsistent()) {
							sFprime.add(fPrime);
						}
						sCand.remove(cj);
					}
				}
			}
			sCand.remove(ci);
		}

		return sFprime;
	}

	public Set<Formula> getDescendants(Formula fInit, boolean degenerated) {
		Set<Formula> sExplored = new HashSet<Formula>();
		Set<Formula> sToExplore = new HashSet<Formula>();
		sToExplore.add(fInit);
		while (!sToExplore.isEmpty()) {
			Formula f = sToExplore.iterator().next();
			sToExplore.remove(f);
			sToExplore.addAll(this.getFormulaChildren(f, degenerated));
			sExplored.add(f);
		}
		return sExplored;
	}

	public Set<Formula> getFormulaChildren(Formula f, boolean degenerated) {
		Set<Formula> sFSons = new HashSet<Formula>();
		Set<Clause> sFClauses = f.getClauses();
		List<Clause> lNonConsis = new ArrayList<Clause>();
		Map<Clause, Formula> mCiFMinus = new HashMap<Clause, Formula>();

		for (Clause ci : sFClauses) {
			// 1.
			Set<Clause> sCfMinus = new HashSet<Clause>(sFClauses);
			sCfMinus.remove(ci);
			Formula fMinus = new Formula(this.nvars, sCfMinus);

			// 1.2
			Set<Clause> sRi = new HashSet<Clause>();
			for (Clause c : this.powerSet.getComputedSons(ci)) {
				if (c.isIndependent(sCfMinus)) {
					sRi.add(c);
				}
			}

			// 1.3
			if (!sRi.isEmpty()) {
				Set<Clause> sFPrime = new HashSet<Clause>(sCfMinus);
				sFPrime.addAll(sRi);
				Formula fPrime = new Formula(this.nvars, sFPrime);
				sFSons.add(fPrime);
				continue;
			}

			// 2.1
			if (fMinus.isConsistent()) {
				sFSons.add(fMinus);
			} else {
				// 2.2
				lNonConsis.add(ci);
				mCiFMinus.put(ci, fMinus);
			}
		}
		if (degenerated) {
			for (Formula fDegen : mCiFMinus.values()) {
				sFSons.add(fDegen);
			}
		} else {
			for (int i = 0; i < (lNonConsis.size() - 1); i++) {
				Clause ci = lNonConsis.get(i);
				for (int j = i + 1; j < lNonConsis.size(); j++) {
					Clause cj = lNonConsis.get(j);
					Clause cMeet = this.powerSet.meet(ci, cj);
					if (cMeet != null) {
						Set<Clause> sFPrime = new HashSet<Clause>(mCiFMinus.get(ci).getClauses());
						sFPrime.remove(cj);
						sFPrime.add(cMeet);
						Formula fPrime = new Formula(this.nvars, sFPrime);
						sFSons.add(fPrime);
					}
				}
			}
		}
		return sFSons;
	}

	private Formula getConsistentFormula(Set<Clause> sfClauses, Set<Clause> set) {
		Set<Clause> sCandSons = new HashSet<Clause>();
		for (Clause c : set) {
			sCandSons.addAll(powerSet.getComputedSons(c));
		}
		// remove dominated clauses
		Set<Clause> sPrime = new HashSet<Clause>(sfClauses);
		sPrime.removeAll(sCandSons);
		sPrime.addAll(set);

		return new Formula(nvars, sPrime);
	}

	public Formula genBottomFormula() {
		if (fBottom == null) {
			Set<Clause> fClauses = new HashSet<Clause>();
			BitSet bs = new BitSet(this.nvars);
			bs.set(0, this.nvars, true);
			fClauses.add(new Clause(this.nvars, bs));
			fBottom = new Formula(this.nvars, fClauses);
		}
		return fBottom;
	}
}
