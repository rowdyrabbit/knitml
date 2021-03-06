package com.knitml.validation.visitor.instruction.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.model.directions.block.InstructionGroup;
import com.knitml.engine.common.KnittingEngineException;
import com.knitml.validation.context.KnittingContext;
import com.knitml.validation.visitor.instruction.impl.AbstractPatternVisitor;

public class InstructionGroupVisitor extends AbstractPatternVisitor {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(InstructionGroupVisitor.class);

	public void visit(Object element, KnittingContext context)
			throws KnittingEngineException {
		InstructionGroup instructionGroup = (InstructionGroup)element;
		if (instructionGroup.getResetRowCount()) {
			context.getEngine().resetRowNumber();
			context.getPatternRepository().clearLocalInstructions();
		}
//		int rowsCompletedBeforeInstruction = context.getEngine().getTotalRowsCompleted();
		visitChildren(instructionGroup, context);
//		int rowsCompletedAfterInstruction = context.getEngine().getTotalRowsCompleted();
//		String id = instructionGroup.getId();
//		if (id != null) {
//			if (repository.getInstructionInfo(id) == null) {
//				repository.addInstructionInfo(new InstructionInfo(element,rowsCompletedAfterInstruction - rowsCompletedBeforeInstruction));
//				log.info("Added instruction ID [{}] to the store", id);
//			}
//		}
	}

}
