package org.colomoto.function.core;

import java.util.BitSet;
import java.util.Set;

/**
 * Clause is represented as a bit vector (true/false) of size n. By default all
 * bits are set to true (representing a more specific clause). Clauses can be
 * compared against other clauses.
 * 
 * @author Pedro T. Monteiro
 * @author Jose' R. Cury
 *
 */
public class Clause {
	private int size;
	private BitSet signature;

	public Clause(int nvars, BitSet signature) {
		this.size = nvars;
		this.signature = signature;
	}

	public Clause(int nvars) {
		this(nvars, new BitSet(nvars));
		this.signature.set(0, nvars);
	}

	public BitSet getSignature() {
		return this.signature;
	}

	public int hashCode() {
		return this.signature.hashCode();
	}

	public int order() {
		return this.signature.cardinality();
	}

	public boolean equals(Object o) {
		Clause co = (Clause) o;
		for (int i = 0; i < this.size; i++) {
			if (this.signature.get(i) != co.signature.get(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean dominatesStrictly(Set<Clause> sClauses) {
		boolean bDominates = false;
		for (Clause c : sClauses) {
			if (this.isLargerOrEqual(c) && !this.equals(c)) {
				bDominates = true;
				break;
			}
		}
		return bDominates;
	}

	public boolean dominates(Set<Clause> sClauses) {
		boolean bDominates = false;
		for (Clause c : sClauses) {
			if (this.isLargerOrEqual(c) && !this.equals(c)) {
				bDominates = true;
				break;
			}
		}
		return bDominates;
	}

	public boolean isLargerOrEqual(Clause c) {
		for (int i = 0; i < this.size; i++) {
			if (this.signature.get(i) && !c.signature.get(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean isSmallerOrEqual(Clause c) {
		for (int i = 0; i < this.size; i++) {
			if (c.signature.get(i)) {
				if (!this.signature.get(i)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isSet(int pos) {
		return (pos < this.size) && (pos >= 0) && this.signature.get(pos);
	}

	public boolean isIndependent(Clause c) {
		return !this.equals(c) && !this.isLargerOrEqual(c)
				&& !this.isSmallerOrEqual(c);
	}

	public boolean isIndependent(Set<Clause> sClauses) {
		for (Clause c : sClauses) {
			if (!this.isIndependent(c)) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		String s = "{";
		boolean first = true;
		for (int i = 0; i < this.size; i++) {
			if (this.signature.get(i)) {
				if (!first)
					s += ",";
				first = false;
				s+= i+1;
			}
//			s += this.signature.get(i) ? i+1 : "";
		}
		return s + "}";
	}
}
