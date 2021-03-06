package com.knitml.validation.visitor.instruction.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.common.NoInstructionFoundException;
import com.knitml.core.common.ValidationException;
import com.knitml.core.model.common.Identifiable;
import com.knitml.core.model.common.StitchesOnNeedle;
import com.knitml.core.model.operations.block.Instruction;
import com.knitml.core.model.operations.block.RepeatInstruction;
import com.knitml.core.model.operations.block.RepeatInstruction.Until;
import com.knitml.core.model.operations.expression.Expression;
import com.knitml.core.units.RowGauge;
import com.knitml.core.units.Units;
import com.knitml.engine.Needle;
import com.knitml.engine.common.KnittingEngineException;
import com.knitml.validation.context.KnittingContext;
import com.knitml.validation.visitor.NeedleNotFoundException;
import com.knitml.validation.visitor.instruction.impl.AbstractPatternVisitor;
import com.knitml.validation.visitor.util.NeedleUtils;

@SuppressWarnings("unchecked")
public class RepeatInstructionVisitor extends AbstractPatternVisitor {

	private final static Logger log = LoggerFactory
			.getLogger(RepeatInstructionVisitor.class);

	public void visit(Object element, KnittingContext context)
			throws KnittingEngineException {
		RepeatInstruction operation = (RepeatInstruction) element;
		Identifiable identifiable = operation.getRef();
		Instruction instruction = context.getPatternRepository()
				.getBlockInstruction(identifiable.getId());
		if (instruction == null) {
			throw new NoInstructionFoundException(
					Messages.getString("RepeatInstructionVisitor.NO_INSTRUCTION_FOUND")); //$NON-NLS-1$
		}
		Until until = operation.getUntil();
		Object value = operation.getValue();
		context.getPatternState().setAsCurrent(instruction);
		context.getPatternState().setReplayMode(true);
		switch (until) {
		case ADDITIONAL_TIMES:
			handleAdditionalTimes(getRequiredInteger(value), instruction,
					context);
			break;
		case UNTIL_DESIRED_LENGTH:
			handleUntilDesiredLength(instruction, context);
			break;
		case UNTIL_STITCHES_REMAIN:
			handleUntilStitchesRemain(getRequiredInteger(value), instruction,
					context);
			break;
		case UNTIL_STITCHES_REMAIN_ON_NEEDLES:
			handleUntilStitchesRemainOnNeedles(value, instruction, context);
			break;
		case UNTIL_MEASURES:
			handleUntilMeasures(getRequiredLength(value), instruction, context);
			break;
		case UNTIL_EQUALS:
			handleUntilEquals(value, instruction, context);
			break;
		}
		context.getPatternState().setReplayMode(false);
		context.getPatternState().clearCurrentInstruction();
	}

	private Measurable<Length> getRequiredLength(Object value) {
		if (value == null || !(value instanceof Measure)) {
			throw new ValidationException(
					Messages.getString("RepeatInstructionVisitor.EXPECTING_INTEGER_FOR_VALUE")); //$NON-NLS-1$
		}
		Measure<?, Length> measure = (Measure<?, Length>) value;
		// validate that it's a Measure<Length>
		measure.getUnit().asType(Length.class);
		return measure;
	}

	private Integer getRequiredInteger(Object value) {
		if (value == null || !(value instanceof Integer)) {
			throw new ValidationException(
					Messages.getString("RepeatInstructionVisitor.EXPECTING_INTEGER_FOR_VALUE")); //$NON-NLS-1$
		}
		return (Integer) value;
	}

	private void handleUntilEquals(Object value,
			Instruction instructionToRepeat, KnittingContext context)
			throws KnittingEngineException {

		if (!(value instanceof List)) {
			throw new ValidationException(
					Messages.getString("RepeatInstructionVisitor.NO_EXPRESSIONS_SPECIFIED")); //$NON-NLS-1$
		}
		List<Expression> expressions = (List<Expression>) value;

		EqualsEvaluator evaluator = new EqualsEvaluator(context);
		while (!evaluator.areEqual(expressions)) {
			context.getPatternState().nextRepeatOfCurrentInstruction();
			visitChild(instructionToRepeat, context);
		}
	}

	private void handleUntilStitchesRemainOnNeedles(Object value,
			Instruction instructionToRepeat, KnittingContext context)
			throws KnittingEngineException {

		if (!(value instanceof List)) {
			throw new ValidationException(
					Messages.getString("RepeatInstructionVisitor.NO_NEEDLES_SPECIFIED")); //$NON-NLS-1$
		}
		List<StitchesOnNeedle> stitchesOnNeedles = (List<StitchesOnNeedle>) value;

		// first build a map of Needles and their target stitch counts
		Map<Needle, Integer> needlesToStitches = new HashMap<Needle, Integer>();
		for (StitchesOnNeedle stitchesOnNeedle : stitchesOnNeedles) {
			String id = stitchesOnNeedle.getNeedle().getId();
			Needle engineNeedle = NeedleUtils.lookupNeedle(id, context);
			if (engineNeedle == null) {
				throw new NeedleNotFoundException(id);
			}
			Integer targetNumberOfStitches = stitchesOnNeedle
					.getNumberOfStitches();
			if (targetNumberOfStitches == null) {
				throw new ValidationException(MessageFormat.format(
						Messages.getString("RepeatInstructionVisitor.NO_STITCH_COUNT_SPECIFIED"), //$NON-NLS-1$
						engineNeedle.toString()));
			}
			needlesToStitches.put(engineNeedle, targetNumberOfStitches);
		}

		// start off by assuming we need to repeat the instruction
		boolean shouldRepeat = true;
		// repeat until we determine that we should no longer repeat (i.e. all
		// needles match their target stitch count)
		while (shouldRepeat) {
			for (Needle needle : needlesToStitches.keySet()) {
				// if this needle's stitch count does not equal the target
				// stitch count for this needle, continue repeating
				shouldRepeat = needle.getTotalStitches() != needlesToStitches
						.get(needle);
				if (shouldRepeat) {
					// if we already know we have to repeat, break out of the
					// for loop to optimize
					break;
				}
			}
			if (shouldRepeat) {
				// if we get here, at least one needle indicated that the stitch
				// count did not equal, so we should continue repeating
				context.getPatternState().nextRepeatOfCurrentInstruction();
				visitChild(instructionToRepeat, context);
			}
		}

	}

	private void handleUntilMeasures(Measurable<Length> targetMeasurement,
			Instruction instructionToRepeat, KnittingContext context)
			throws KnittingEngineException {
		Measurable<RowGauge> rowGauge = context.getPatternRepository()
				.getRowGauge();
		if (rowGauge == null) {
			log.warn("The UNTIL_MEASURES specification cannot be fully processed " //$NON-NLS-1$
					+ "because a row gauge was not specified in the pattern"); //$NON-NLS-1$
		} else {
			double untilMeasuresInches = targetMeasurement
					.doubleValue(Units.INCH);
			double rowsPerInch = rowGauge.doubleValue(Units.ROWS_PER_INCH);
			int repeats = (int) Math.ceil((untilMeasuresInches * rowsPerInch)
					/ instructionToRepeat.getRows().size());
			log.info("Repeating instruction {} times", String.valueOf(repeats)); //$NON-NLS-1$
			for (int i = 0; i < repeats - 1; i++) {
				context.getPatternState().nextRepeatOfCurrentInstruction();
				visitChild(instructionToRepeat, context);
			}
		}
	}

	private void handleUntilStitchesRemain(int targetNumberOfStitches,
			Instruction instructionToRepeat, KnittingContext context)
			throws KnittingEngineException {
		while (context.getEngine().getTotalNumberOfStitchesInRow() != targetNumberOfStitches) {
			context.getPatternState().nextRepeatOfCurrentInstruction();
			visitChild(instructionToRepeat, context);
		}
	}

	private void handleUntilDesiredLength(Instruction instructionToRepeat,
			KnittingContext context) {
		log.warn("An UNTIL_DESIRED_LENGTH parameter on a RepeatInstruction was specified. This means that the engine will not replay this instruction."); //$NON-NLS-1$
	}

	private void handleAdditionalTimes(int numberOfRepeats,
			Instruction instructionToRepeat, KnittingContext context)
			throws KnittingEngineException {
		for (int i = 0; i < numberOfRepeats; i++) {
			context.getPatternState().nextRepeatOfCurrentInstruction();
			visitChild(instructionToRepeat, context);
		}
	}

}
