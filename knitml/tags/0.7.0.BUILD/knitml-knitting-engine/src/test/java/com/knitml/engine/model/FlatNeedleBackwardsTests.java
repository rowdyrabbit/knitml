/**
 * 
 */
package com.knitml.engine.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.knitml.core.model.operations.StitchNature;
import com.knitml.engine.Stitch;
import com.knitml.engine.common.NeedlesInWrongDirectionException;
import com.knitml.engine.impl.DefaultStitch;

/**
 * @author Jonathan Whitall (fiddlerpianist@gmail.com)
 * 
 */
public class FlatNeedleBackwardsTests extends FlatNeedleTests {

	@Override
	public void onSetUp() {
		nextRow();
	}

	@Test(expected = NeedlesInWrongDirectionException.class)
	public void removeNStitchesFromBeginning() throws Exception {
		needle.removeNStitchesFromBeginning(5);
	}

	@Test(expected = NeedlesInWrongDirectionException.class)
	public void removeNStitchesFromEnd() throws Exception {
		needle.removeNStitchesFromEnd(5);
	}

	@Test
	@Override
	public void addStitchesToBeginning() throws Exception {
		List<Stitch> startingStitchesOnNeedle = new ArrayList<Stitch>(
				needle.getStitches());
		List<Stitch> stitchesToAdd = new ArrayList<Stitch>();
		stitchesToAdd.add(new DefaultStitch("AA")); //$NON-NLS-1$
		stitchesToAdd.add(new DefaultStitch("BB")); //$NON-NLS-1$
		stitchesToAdd.add(new DefaultStitch("CC")); //$NON-NLS-1$
		Stitch lastStitch = new DefaultStitch("DD"); //$NON-NLS-1$
		stitchesToAdd.add(lastStitch);

		// perform the operation the needle
		needle.addStitchesToBeginning(stitchesToAdd);

		// addStitchesToBeginning() will reverse the ordering of the stitches to
		// add wrt the needle
		Collections.reverse(stitchesToAdd);

		List<Stitch> expectedStitchesOnNeedle = new ArrayList<Stitch>();
		expectedStitchesOnNeedle.addAll(stitchesToAdd);
		expectedStitchesOnNeedle.addAll(startingStitchesOnNeedle);

		// expectedStitchesOnNeedle is [DD,CC,BB,AA,J,I,H,G,F,E,D,C,B,A] (don't
		// think about this too hard... it's right)
		assertEquals(expectedStitchesOnNeedle, needle.getStitches());

		// make sure the stitch cursor is set to return the first stitch (i.e.
		// "DD")
		needle.startAtBeginning();
		assertEquals(lastStitch, needle.peekAtNextStitch());
	}

	@Test
	@Override
	public void addStitchesToEnd() throws Exception {
		List<Stitch> startingStitchesOnNeedle = new ArrayList<Stitch>(
				needle.getStitches());
		List<Stitch> stitchesToAdd = new ArrayList<Stitch>();
		stitchesToAdd.add(new DefaultStitch("K")); //$NON-NLS-1$
		stitchesToAdd.add(new DefaultStitch("L")); //$NON-NLS-1$
		stitchesToAdd.add(new DefaultStitch("M")); //$NON-NLS-1$
		Stitch lastStitch = new DefaultStitch("N"); //$NON-NLS-1$
		stitchesToAdd.add(lastStitch);

		// perform the operation on the needle
		needle.addStitchesToEnd(stitchesToAdd);

		// addStitchesToEnd() will reverse the ordering of the stitches to add
		// wrt the needle
		Collections.reverse(stitchesToAdd);

		List<Stitch> expectedStitchesOnNeedle = new ArrayList<Stitch>();
		expectedStitchesOnNeedle.addAll(startingStitchesOnNeedle);
		expectedStitchesOnNeedle.addAll(stitchesToAdd);

		// expectedStitchesOnNeedle is [J,I,H,G,F,E,D,C,B,A,N,M,L,K] (don't
		// think about this too hard... it's right)
		assertEquals(expectedStitchesOnNeedle, needle.getStitches());

		// make sure the stitch cursor is set to return the first stitch (i.e.
		// "AA")
		needle.startAtBeginning();
		knit(10);
		assertEquals(lastStitch, needle.peekAtNextStitch());
	}

	@Test
	public void castOnInBackwardsDirection() throws Exception {
		assertTrue(needle.isBeginningOfNeedle());
		knit(5);
		needle.increase(5);
		knit(5);
		assertTrue(needle.isEndOfNeedle());
	}

	@Override
	public void crossStitches() throws Exception {
		knit(3);
		needle.cross(2, 3, 0);
		knit(7);
		assertThat(needle.isEndOfNeedle(), is(true));

		List<Stitch> stitches = needle.getStitches();
		List<String> stitchNames = new ArrayList<String>(stitches.size());
		for (Stitch stitch : stitches) {
			stitchNames.add(stitch.getId());
		}
		String[] expectedStitchArray = new String[] {
				"J", "I", "H", "E", "D", "C", "G", "F", "B", "A" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		assertThat(stitchNames.toArray(new String[0]), is(expectedStitchArray));
	}

	@Override
	public void crossStitchesWithSkip() throws Exception {
		knit(1);
		needle.cross(2, 3, 4);
		knit(9);
		assertThat(needle.isEndOfNeedle(), is(true));

		List<Stitch> stitches = needle.getStitches();
		List<String> stitchNames = new ArrayList<String>(stitches.size());
		for (Stitch stitch : stitches) {
			stitchNames.add(stitch.getId());
		}
		String[] expectedStitchArray = new String[] { "J", "C", "B", "A", "G", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"F", "E", "D", "I", "H" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		assertThat(stitchNames.toArray(new String[0]), is(expectedStitchArray));
	}

	@Test
	@Override
	public void verifyLastOperation() throws Exception {
		knit(10); // this is on the backward side
		nextRow();
		while (needle.getStitchesRemaining() > 0) {
			assertThat(needle.peekAtNextStitch().getCurrentNature(),
					is(StitchNature.PURL));
			needle.knit();
		}
	}

	@Test
	@Override
	public void verifyLastOperationAcrossMultipleRows() throws Exception {
		// backward side
		knit(10);
		nextRow();
		// forward side
		knit(10);
		nextRow();
		// backward side
		while (needle.getStitchesRemaining() > 0) {
			assertThat(needle.peekAtNextStitch().getCurrentNature(),
					is(StitchNature.KNIT));
			needle.knit();
		}
	}

}
