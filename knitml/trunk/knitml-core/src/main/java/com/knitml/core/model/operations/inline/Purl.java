package com.knitml.core.model.operations.inline;

import java.util.ArrayList;
import java.util.List;

import com.knitml.core.common.LoopToWork;
import com.knitml.core.model.operations.StitchNature;

public class Purl extends Knit {

	public Purl(Integer numberOfTimes, LoopToWork loopToWork, String yarnIdRef) {
		super(numberOfTimes, loopToWork, yarnIdRef);
	}

	public Purl(Integer numberOfTimes, Integer rowsBelow, LoopToWork loopToWork, String yarnIdRef) {
		super(numberOfTimes, rowsBelow, loopToWork, yarnIdRef);
	}

	@Override
	public String toString() {
		return "Purl"
				+ (numberOfTimes != null ? " " + numberOfTimes : "")
				+ (rowsBelow != null ? " into " + rowsBelow + " rows below": "")
				+ (yarnIdRef != null ? " with yarn " + yarnIdRef : "")
				+ (loopToWork != null ? " through " + loopToWork + " loop" : "");
	}

	public List<Purl> canonicalize() {
		int size = numberOfTimes == null ? 1 : numberOfTimes;
		Integer numberOfRowsBelow = (rowsBelow != null && rowsBelow > 0) ? rowsBelow : null;
		List<Purl> newOps = new ArrayList<Purl>(size);
		for (int i = 0; i < size; i++) {
			newOps.add(new Purl(null, numberOfRowsBelow, loopToWork == null ? LoopToWork.LEADING
					: loopToWork, null));
		}
		return newOps;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((loopToWork == null) ? 0 : loopToWork.hashCode());
		result = prime * result
				+ ((numberOfTimes == null) ? 0 : numberOfTimes.hashCode());
		result = prime * result
				+ ((rowsBelow == null) ? 0 : rowsBelow.hashCode());
		result = prime * result
				+ ((yarnIdRef == null) ? 0 : yarnIdRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Purl other = (Purl) obj;
		if (loopToWork != other.loopToWork)
			return false;
		if (numberOfTimes == null) {
			if (other.numberOfTimes != null)
				return false;
		} else if (!numberOfTimes.equals(other.numberOfTimes))
			return false;
		if (rowsBelow == null) {
			if (other.rowsBelow != null)
				return false;
		} else if (!rowsBelow.equals(other.rowsBelow))
			return false;
		if (yarnIdRef == null) {
			if (other.yarnIdRef != null)
				return false;
		} else if (!yarnIdRef.equals(other.yarnIdRef))
			return false;
		return true;
	}

	
	@Override
	public StitchNature getStitchNatureProduced() {
		return StitchNature.PURL;
	}

}
