package com.knitml.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.list.TreeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.common.NeedleStyle;
import com.knitml.engine.KnittingFactory;
import com.knitml.engine.Marker;
import com.knitml.engine.Needle;
import com.knitml.engine.Stitch;
import com.knitml.engine.common.CannotPutMarkerOnEndOfNeedleException;
import com.knitml.engine.common.CannotWorkThroughMarkerException;
import com.knitml.engine.common.NeedlesInWrongDirectionException;
import com.knitml.engine.common.NoMarkerFoundException;
import com.knitml.engine.common.NotEnoughStitchesException;
import com.knitml.engine.settings.Direction;
import com.knitml.engine.settings.MarkerBehavior;

public class DefaultNeedle implements Needle {

	private KnittingFactory knittingFactory;

	private static final Logger log = LoggerFactory
			.getLogger(DefaultNeedle.class);

	@Override
	public String toString() {
		return "Needle [" + getId() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DefaultNeedle
				&& ((DefaultNeedle) obj).getId().equals(this.getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	private String id;
	private NeedleStyle needleType;
	private Direction direction = Direction.FORWARDS;
	// FIXME when Collections generics for 3.2 comes out
	@SuppressWarnings("unchecked")
	private List<Stitch> stitches = new TreeList();
	private ListIterator<Stitch> stitchCursor = stitches.listIterator();
	// the marker falls BEFORE the index specified by its key on the needles
	// (when knitting forward)
	private SortedMap<Integer, Marker> markers = new TreeMap<Integer, Marker>();

	// the gap falls BEFORE the index specified by its key on the needles
	// (when knitting forward)
	private SortedMap<Integer, Marker> gaps = new TreeMap<Integer, Marker>();

	private int lastStitchIndexReturned = -1;

	public DefaultNeedle(String id, NeedleStyle needleType,
			KnittingFactory knittingFactory) {
		this.id = id;
		this.needleType = needleType;
		this.knittingFactory = knittingFactory;
	}

	public void restore(Object mementoObj) {
		if (!(mementoObj instanceof DefaultNeedleMemento)) {
			throw new IllegalArgumentException(
					"Type to restore must be of type DefaultNeedleMemento");
		}
		DefaultNeedleMemento memento = (DefaultNeedleMemento) mementoObj;
		this.direction = memento.getDirection();
		this.stitches = memento.getStitches();
		this.stitchCursor = this.stitches.listIterator(memento
				.getNextStitchIndex());
		this.markers = memento.getMarkers();
		this.gaps = memento.getGaps();
		this.lastStitchIndexReturned = memento.getLastStitchIndexReturned();
	}	

	public Object save() {
		List<Stitch> stitchesCopy = new ArrayList<Stitch>(this.stitches);
		SortedMap<Integer, Marker> markersCopy = new TreeMap<Integer, Marker>(
				this.markers);
		SortedMap<Integer, Marker> gapsCopy = new TreeMap<Integer, Marker>(
				this.gaps);
		Object memento = new DefaultNeedleMemento(this.direction, stitchesCopy,
				stitchCursor.nextIndex(), markersCopy, gapsCopy,
				this.lastStitchIndexReturned);
		return memento;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#setDirection(com
	 * .knitml.validation.validation.engine.Direction)
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#knit()
	 */
	public void knit() throws NotEnoughStitchesException {
		advanceCursorOne();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#purl()
	 */
	public void purl() throws NotEnoughStitchesException {
		advanceCursorOne();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#slip()
	 */
	public void slip() throws NotEnoughStitchesException {
		advanceCursorOne();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#reverseSlip()
	 */
	public void reverseSlip() throws NotEnoughStitchesException {
		retreatCursorOne();
	}

	public Stitch removeNextStitch() throws NotEnoughStitchesException {
		Stitch stitchToRemove;
		if (direction == Direction.FORWARDS) {
			stitchToRemove = stitchCursor.next();
			stitchCursor.remove();
			lastStitchIndexReturned = stitchCursor.nextIndex() - 1;
		} else {
			stitchToRemove = stitchCursor.previous();
			stitchCursor.remove();
			lastStitchIndexReturned = stitchCursor.previousIndex() + 1;
		}
		adjustMarkersAfterDecrease(1);
		return stitchToRemove;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#placeMarker(com.
	 * knitml.validation.validation.engine.model.Marker)
	 */
	public void placeMarker(Marker marker)
			throws CannotPutMarkerOnEndOfNeedleException {
		placeMarker(marker, this.markers);
	}

	protected void signalGap() {
		try {
			// setting the behavior to REMOVE will take care of any decreases
			placeMarker(getKnittingFactory().createMarker(null,
					MarkerBehavior.REMOVE), this.gaps);
		} catch (CannotPutMarkerOnEndOfNeedleException ex) {
			log
					.warn("Request was made to signal a gap at the end of a needle. Ignoring");
		}
	}

	private void placeMarker(Marker marker, SortedMap<Integer, Marker> markerMap)
			throws CannotPutMarkerOnEndOfNeedleException {
		if (marker == null) {
			throw new IllegalArgumentException(
					"A marker must be supplied as an argument");
		}
		if (isEndOfNeedle()) {
			throw new CannotPutMarkerOnEndOfNeedleException();
		}
		if (direction == Direction.FORWARDS) {
			markerMap.put(stitchCursor.nextIndex(), marker);
		} else {
			markerMap.put(stitchCursor.previousIndex() + 1, marker);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#removeMarker()
	 */
	public Marker removeMarker() throws NoMarkerFoundException {
		Marker result = removeMarker(this.markers);
		if (result == null) {
			throw new NoMarkerFoundException();
		}
		return result;
	}

	protected void unsignalGap() {
		removeMarker(this.gaps);
	}

	private Marker removeMarker(SortedMap<Integer, Marker> markerMap) {
		Marker result;
		if (direction == Direction.FORWARDS) {
			result = markerMap.remove(stitchCursor.nextIndex());
		} else {
			result = markerMap.remove(stitchCursor.previousIndex() + 1);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#turn()
	 */
	public void turn() {
		if (getStitchesRemaining() > 0) {
			signalGap();
		}
		if (direction == Direction.BACKWARDS) {
			setDirection(Direction.FORWARDS);
		} else {
			setDirection(Direction.BACKWARDS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#knitTwoTogether()
	 */
	public void knitTwoTogether() throws NotEnoughStitchesException,
			CannotWorkThroughMarkerException {
		decrease(1);
	}

	private void doDecrease(int numberToDecrease, boolean betweenGap)
			throws NotEnoughStitchesException {
		if (betweenGap) {
			int stitchesToGap = 0;
			while (getStitchesToGap() > 0) {
				slip();
				stitchesToGap++;
			}
			unsignalGap();
			for (int i = 0; i < stitchesToGap; i++) {
				reverseSlip();
			}
			assert numberToDecrease <= stitchesToGap;
		}
		if (getStitchesRemaining() < (numberToDecrease + 1)) {
			throw new NotEnoughStitchesException("Need at least "
					+ (numberToDecrease + 1)
					+ " stitches on the needle to decrease " + numberToDecrease
					+ "; found " + getStitchesRemaining() + " stitch(es) left");
		}
		if (direction == Direction.FORWARDS) {
			for (int i = 0; i < numberToDecrease; i++) {
				stitchCursor.next();
				stitchCursor.remove();
			}
			lastStitchIndexReturned = stitchCursor.nextIndex();
			stitchCursor.next();
		} else {
			for (int i = 0; i < numberToDecrease; i++) {
				stitchCursor.previous();
				stitchCursor.remove();
			}
			lastStitchIndexReturned = stitchCursor.previousIndex();
			stitchCursor.previous();
		}
		adjustMarkersAfterDecrease(numberToDecrease);
	}

	private boolean isMarkerBetweenNextNStitches(int numberOfStitches) {
		try {
			return areMarkersRemaining() && getStitchesToNextMarker() >= 1
					&& getStitchesToNextMarker() <= numberOfStitches - 1;
		} catch (NoMarkerFoundException ex) {
			// this should not happen under this scenario
			throw new RuntimeException("An unexpected internal error occurred",
					ex);
		}
	}

	private boolean isGapBetweenNextNStitches(int numberOfStitches) {
		return hasGaps() && getStitchesToGap() >= 1
				&& getStitchesToGap() <= numberOfStitches - 1;
	}

	private void advanceCursorOne() throws NotEnoughStitchesException {
		try {
			if (hasGaps() && getStitchesToGap() == 0) {
				unsignalGap();
			}
			if (direction == Direction.FORWARDS) {
				lastStitchIndexReturned = stitchCursor.nextIndex();
				stitchCursor.next();
			} else {
				lastStitchIndexReturned = stitchCursor.previousIndex();
				stitchCursor.previous();
			}
		} catch (NoSuchElementException ex) {
			throw new NotEnoughStitchesException(
					"You have reached the end of the row");
		}
	}

	public boolean hasGaps() {
		return gaps.size() > 0;
	}

	public boolean hasMarkers() {
		return markers.size() > 0;
	}

	private void retreatCursorOne() throws NotEnoughStitchesException {
		try {
			if (direction == Direction.FORWARDS) {
				lastStitchIndexReturned = stitchCursor.previousIndex();
				stitchCursor.previous();
			} else {
				lastStitchIndexReturned = stitchCursor.nextIndex();
				stitchCursor.next();
			}
		} catch (NoSuchElementException ex) {
			throw new NotEnoughStitchesException(
					"You have reached the beginning of the row");
		}
	}

	protected void adjustMarkersAfterDecrease(int numberDecreased) {
		this.markers = adjustMarkersAfterDecrease(numberDecreased, this.markers);
		this.gaps = adjustMarkersAfterDecrease(numberDecreased, this.gaps);
	}

	private SortedMap<Integer, Marker> adjustMarkersAfterDecrease(
			int numberDecreased, SortedMap<Integer, Marker> markerMap) {
		if (markerMap.size() > 0) {
			SortedMap<Integer, Marker> newMarkers = new TreeMap<Integer, Marker>();
			int targetIndex;
			if (getDirection() == Direction.FORWARDS) {
				targetIndex = getNextStitchIndex() + 1;
			} else {
				// add 2 to the next stitch index because of the way markers are
				// represented internally (the index of a marker you arrive at
				// is one higher than the index of the stitch just knit, but
				// only when knitting forwards).
				targetIndex = getNextStitchIndex() + 2;
			}
			newMarkers.putAll(markerMap.headMap(targetIndex));
			for (int index : markerMap.tailMap(targetIndex).keySet()) {
				Marker marker = markerMap.get(index);
				// offset each marker's index by the number that was decreased
				newMarkers.put(index - numberDecreased, marker);
			}
			return newMarkers;
		}
		return markerMap;
	}

	protected void adjustMarkersAfterIncrease(int numberIncreased) {
		this.markers = adjustMarkersAfterIncrease(numberIncreased, this.markers);
		this.gaps = adjustMarkersAfterIncrease(numberIncreased, this.gaps);
	}

	private SortedMap<Integer, Marker> adjustMarkersAfterIncrease(
			int numberIncreased, SortedMap<Integer, Marker> markerMap) {
		if (markerMap.size() > 0) {
			SortedMap<Integer, Marker> newMarkers = new TreeMap<Integer, Marker>();
			int targetIndex;
			if (getDirection() == Direction.FORWARDS) {
				targetIndex = lastStitchIndexReturned + 1;
			} else {
				targetIndex = lastStitchIndexReturned;
			}
			newMarkers.putAll(markerMap.headMap(targetIndex));
			for (int index : markerMap.tailMap(targetIndex).keySet()) {
				Marker marker = markerMap.get(index);
				// offset each marker's index by the number that was increased
				newMarkers.put(index + numberIncreased, marker);
			}
			return newMarkers;
		}
		return markerMap;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see com.knitml.validation.validation.engine.model.Needle#castOn(int)
	// */
	// public void castOn(int numberOfStitches) {
	// for (int i = 0; i < numberOfStitches; i++) {
	// Stitch stitch = knittingFactory.createStitch();
	// if (getDirection() == Direction.FORWARDS) {
	// stitchCursor.add(stitch);
	// } else {
	// stitchCursor.add(stitch);
	// stitchCursor.previous();
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#getTotalStitches()
	 */
	public int getTotalStitches() {
		return stitches.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#getStitchesRemaining
	 * ()
	 */
	public int getStitchesRemaining() {
		if (direction == Direction.FORWARDS) {
			return stitches.size() - stitchCursor.nextIndex();
		} else {
			return 1 + stitchCursor.previousIndex();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#areMarkersRemaining
	 * ()
	 */
	public boolean areMarkersRemaining() {
		if (markers.size() == 0) {
			return false;
		}
		int nextStitch = getNextStitchIndex();
		if (direction == Direction.FORWARDS) {
			return !(markers.tailMap(nextStitch)).isEmpty();
		} else {
			return !(markers.headMap(nextStitch + 2)).isEmpty();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#isEndOfNeedle()
	 */
	public boolean isEndOfNeedle() {
		return getStitchesRemaining() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#isBeginningOfNeedle
	 * ()
	 */
	public boolean isBeginningOfNeedle() {
		return getStitchesRemaining() == getTotalStitches();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#startAtBeginning()
	 */
	public void startAtBeginning() {
		if (direction == Direction.FORWARDS) {
			stitchCursor = stitches.listIterator();
		} else {
			stitchCursor = stitches.listIterator(stitches.size());
		}
	}

	public void startAtEnd() {
		if (direction == Direction.FORWARDS) {
			stitchCursor = stitches.listIterator(stitches.size());
		} else {
			stitchCursor = stitches.listIterator();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#getNextStitchIndex()
	 */
	public int getNextStitchIndex() {
		if (direction == Direction.FORWARDS) {
			return stitchCursor.nextIndex();
		} else {
			return stitchCursor.previousIndex();
		}
	}

	public int getPreviousStitchIndex() {
		if (direction == Direction.FORWARDS) {
			return stitchCursor.previousIndex();
		} else {
			return stitchCursor.nextIndex();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#peekAtNextStitch()
	 */
	public Stitch peekAtNextStitch() {
		if (direction == Direction.FORWARDS) {
			return stitches.get(stitchCursor.nextIndex());
		} else {
			return stitches.get(stitchCursor.previousIndex());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#getStitchesToNextMarker
	 * ()
	 */
	public int getStitchesToNextMarker() throws NoMarkerFoundException {
		int result = getStitchesToNextMarker(this.markers);
		if (result == -1) {
			throw new NoMarkerFoundException();
		}
		return result;
	}

	public int getStitchesToGap() {
		return getStitchesToNextMarker(this.gaps);
	}

	private int getStitchesToNextMarker(SortedMap<Integer, Marker> markerMap) {
		int nextStitch = getNextStitchIndex();
		boolean ignoreImmediateMarker = false;
		if (lastStitchIndexReturned == nextStitch) {
			ignoreImmediateMarker = true;
		}
		int markerIndex;
		if (direction == Direction.FORWARDS) {
			try {
				if (ignoreImmediateMarker) {
					markerIndex = markerMap.tailMap(nextStitch + 1).firstKey();
				} else {
					markerIndex = markerMap.tailMap(nextStitch).firstKey();
				}
				return markerIndex - nextStitch;
			} catch (NoSuchElementException ex) {
				return -1;
			}
		} else {
			try {
				// since headMap() is exclusive of the number provided AND
				// the marker index precedes the stitch number (when knitting
				// forwards), we have to add 2 to the next stitch to achieve
				// the desired result.
				if (ignoreImmediateMarker) {
					markerIndex = markerMap.headMap(nextStitch + 1).lastKey();
				} else {
					markerIndex = markerMap.headMap(nextStitch + 2).lastKey();
				}
				return nextStitch - markerIndex + 1;
			} catch (NoSuchElementException ex) {
				return -1;
			}
		}
	}

	private Marker getNextMarker() throws NoMarkerFoundException {
		int nextStitch = getNextStitchIndex();
		Marker result = null;
		if (direction == Direction.FORWARDS) {
			result = markers.get(markers.tailMap(nextStitch).firstKey());
		} else {
			result = markers.get(markers.headMap(nextStitch + 2).lastKey());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#purlTwoTogether()
	 */
	public void purlTwoTogether() throws NotEnoughStitchesException,
			CannotWorkThroughMarkerException {
		decrease(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#addStitchesToBeginning
	 * (java.util.List)
	 */
	public void addStitchesToBeginning(List<Stitch> stitchesToAdd) {
		if (getDirection() == Direction.FORWARDS) {
			stitches.addAll(0, stitchesToAdd);
		} else {
			stitches.addAll(stitchesToAdd);
		}
		// reset the list iterator for the stitches
		startAtBeginning();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#addStitchesToEnd
	 * (java.util.List)
	 */
	public void addStitchesToEnd(List<Stitch> stitchesToAdd) {
		if (getDirection() == Direction.FORWARDS) {
			stitches.addAll(stitchesToAdd);
		} else {
			stitches.addAll(0, stitchesToAdd);
		}
		// reset the list iterator for the stitches
		startAtEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#getStitches()
	 */
	public List<Stitch> getStitches() {
		List<Stitch> result;
		if (getDirection() == Direction.BACKWARDS) {
			result = new ArrayList<Stitch>();
			result.addAll(this.stitches);
			Collections.reverse(result);
		} else {
			result = this.stitches;
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.knitml.validation.validation.engine.model.Needle#
	 * removeNStitchesFromBeginning(int)
	 */
	public List<Stitch> removeNStitchesFromBeginning(int number)
			throws NeedlesInWrongDirectionException {
		assertForwardsDirection();
		@SuppressWarnings("unchecked")
		List<Stitch> result = new TreeList(stitches.subList(0, number));
		stitchCursor = stitches.listIterator(number);
		while (stitchCursor.hasPrevious()) {
			lastStitchIndexReturned = stitchCursor.previousIndex();
			stitchCursor.previous();
			stitchCursor.remove();
		}
		// reset list iterator
		stitchCursor = stitches.listIterator();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.knitml.validation.validation.engine.model.Needle#removeNStitchesFromEnd
	 * (int)
	 */
	@SuppressWarnings("unchecked")
	public List<Stitch> removeNStitchesFromEnd(int number)
			throws NeedlesInWrongDirectionException {
		assertForwardsDirection();
		List<Stitch> result = new TreeList(stitches.subList(stitches.size()
				- number, stitches.size()));
		stitchCursor = stitches.listIterator(stitches.size() - number);
		while (stitchCursor.hasNext()) {
			lastStitchIndexReturned = stitchCursor.nextIndex();
			stitchCursor.next();
			stitchCursor.remove();
		}
		// reset list iterator
		stitchCursor = stitches.listIterator();
		return result;
	}

	private void assertForwardsDirection()
			throws NeedlesInWrongDirectionException {
		if (direction == Direction.BACKWARDS) {
			throw new NeedlesInWrongDirectionException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.knitml.validation.validation.engine.model.Needle#getDirection()
	 */
	public Direction getDirection() {
		return direction;
	}

	public NeedleStyle getNeedleType() {
		return needleType;
	}

	public String getId() {
		return id;
	}

	public KnittingFactory getKnittingFactory() {
		return knittingFactory;
	}

	public void passPreviousStitchOver() throws NotEnoughStitchesException {
		retreatCursorOne();
		retreatCursorOne();
		doDecrease(1, false);
	}

	public void knitThreeTogether() throws NotEnoughStitchesException,
			CannotWorkThroughMarkerException {
		// doing this rather than decrease(2) allows multiple markers to be
		// handled, though technically it knits the same stitch twice.
		knitTwoTogether();
		reverseSlip();
		knitTwoTogether();

		// This used to be done by the following method call:
		// decrease(2);
	}

	private void decrease(int numberToDecrease)
			throws NotEnoughStitchesException, CannotWorkThroughMarkerException {
		boolean betweenGap = false;
		if (isGapBetweenNextNStitches(numberToDecrease + 1)) {
			betweenGap = true;
		}
		try {
			if (isMarkerBetweenNextNStitches(numberToDecrease + 1)) {
				MarkerBehavior behavior = getNextMarker()
						.getWhenWorkedThrough();
				Marker removedMarker = null;
				if (behavior == MarkerBehavior.THROW_EXCEPTION) {
					throw new CannotWorkThroughMarkerException();
				} else {
					int stitchesToMarker = 0;
					while (getStitchesToNextMarker() > 0) {
						slip();
						stitchesToMarker++;
					}
					removedMarker = removeMarker();
					for (int i = 0; i < stitchesToMarker; i++) {
						reverseSlip();
					}
				}
				if (behavior == MarkerBehavior.REMOVE) {
					log
							.info("A knit two together was executed between a stitch marker; removing marker");
				} else if (behavior == MarkerBehavior.PLACE_BEFORE_STITCH_WORKED) {
					placeMarker(removedMarker);
				}
				doDecrease(numberToDecrease, betweenGap);
				if (behavior == MarkerBehavior.PLACE_AFTER_STITCH_WORKED) {
					placeMarker(removedMarker);
				}
			} else {
				doDecrease(numberToDecrease, betweenGap);
			}
		} catch (NoMarkerFoundException ex) {
			// should not happen
			throw new RuntimeException("An unexpected internal error occurred",
					ex);
		} catch (CannotPutMarkerOnEndOfNeedleException ex) {
			// should not happen
			throw new RuntimeException("An unexpected internal error occurred",
					ex);
		}
	}

	public void increase(int numberToIncrease) {
		if (direction == Direction.FORWARDS) {
			for (int i = 0; i < numberToIncrease; i++) {
				Stitch stitch = knittingFactory.createStitch();
				// this inserts the stitch before the cursor
				stitchCursor.add(stitch);
			}
			lastStitchIndexReturned = stitchCursor.previousIndex();
		} else {
			for (int i = 0; i < numberToIncrease; i++) {
				Stitch stitch = knittingFactory.createStitch();
				stitchCursor.add(stitch);
				stitchCursor.previous();
			}
			lastStitchIndexReturned = stitchCursor.nextIndex();
		}
		adjustMarkersAfterIncrease(numberToIncrease);
	}
	
	public void addStitch(Stitch stitchToAdd) {
		if (direction == Direction.FORWARDS) {
			stitchCursor.add(stitchToAdd);
			lastStitchIndexReturned = stitchCursor.previousIndex();
		} else {
			stitchCursor.add(stitchToAdd);
			stitchCursor.previous();
			lastStitchIndexReturned = stitchCursor.nextIndex();
		}
		adjustMarkersAfterIncrease(1);
	}

	public void cross(int first, int next) throws NotEnoughStitchesException,
			CannotWorkThroughMarkerException {
		int stitchesAffected = first + next;
		if (stitchesAffected > getStitchesRemaining()) {
			throw new NotEnoughStitchesException("Need " + (first + next)
					+ " but only found " + getStitchesRemaining());
		}
		try {
			if (hasMarkers() && getStitchesToNextMarker() > 0
					&& getStitchesToNextMarker() < stitchesAffected) {
				throw new CannotWorkThroughMarkerException(
						"Cannot cross stitches through a marker");
			}
		} catch (NoMarkerFoundException ex) {
			// won't happen since we're checking whether there are markers to
			// begin with
			throw new RuntimeException(ex);
		}
		int nextStitchIndex = getNextStitchIndex();
		if (getDirection() == Direction.FORWARDS) {
			List<Stitch> unaffectedPartHead = stitches.subList(0,
					nextStitchIndex);
			List<Stitch> firstPart = stitches.subList(nextStitchIndex,
					nextStitchIndex + first);
			List<Stitch> nextPart = stitches.subList(nextStitchIndex + first,
					nextStitchIndex + stitchesAffected);
			List<Stitch> unaffectedPartTail = stitches.subList(nextStitchIndex
					+ stitchesAffected, stitches.size());
			List<Stitch> result = new ArrayList<Stitch>(stitches.size());
			result.addAll(unaffectedPartHead);
			result.addAll(nextPart);
			result.addAll(firstPart);
			result.addAll(unaffectedPartTail);
			this.stitches = result;
			this.stitchCursor = stitches.listIterator(nextStitchIndex);
		} else {
			int baseIndex = nextStitchIndex + 1;
			List<Stitch> unaffectedPartHead = stitches.subList(baseIndex,
					stitches.size());
			List<Stitch> firstPart = stitches.subList(baseIndex - first,
					baseIndex);
			List<Stitch> nextPart = stitches.subList(baseIndex
					- stitchesAffected, baseIndex - first);
			List<Stitch> unaffectedPartTail = stitches.subList(0, baseIndex
					- stitchesAffected);
			List<Stitch> result = new ArrayList<Stitch>(stitches.size());
			result.addAll(unaffectedPartTail);
			result.addAll(firstPart);
			result.addAll(nextPart);
			result.addAll(unaffectedPartHead);
			this.stitches = result;
			this.stitchCursor = stitches.listIterator(nextStitchIndex + 1);
		}
	}

}
