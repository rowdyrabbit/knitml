package com.knitml.validation.visitor.instruction.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.model.operations.block.Instruction;
import com.knitml.core.model.operations.block.InstructionRef;
import com.knitml.engine.common.KnittingEngineException;
import com.knitml.validation.context.KnittingContext;
import com.knitml.validation.context.PatternRepository;
import com.knitml.validation.visitor.instruction.impl.AbstractPatternVisitor;

public class InstructionRefVisitor extends AbstractPatternVisitor {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(InstructionRefVisitor.class);

	public void visit(Object element, KnittingContext context)
			throws KnittingEngineException {
		InstructionRef instructionRef = (InstructionRef) element;
		PatternRepository repository = context.getPatternRepository();
		// Visit the referenced instruction in the repository, NOT the one in the model
		Instruction instruction = repository.getBlockInstruction(instructionRef.getRef().getId());
		context.getPatternState().setAsCurrent(instruction);
		context.getPatternState().setReplayMode(true);
		visitChild(instruction, context);
		context.getPatternState().setReplayMode(false);
		context.getPatternState().clearCurrentInstruction();
	}

}
