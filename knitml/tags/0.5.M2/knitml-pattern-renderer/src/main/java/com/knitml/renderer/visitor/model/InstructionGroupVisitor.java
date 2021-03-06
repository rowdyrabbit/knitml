package com.knitml.renderer.visitor.model;

import static com.knitml.renderer.context.ContextUtils.resetLastExpressedRowNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.model.directions.block.InstructionGroup;
import com.knitml.renderer.common.RenderingException;
import com.knitml.renderer.context.PatternRepository;
import com.knitml.renderer.context.RenderingContext;
import com.knitml.renderer.context.ContextUtils;
import com.knitml.renderer.event.impl.AbstractRenderingEvent;

public class InstructionGroupVisitor extends AbstractRenderingEvent {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(InstructionGroupVisitor.class);

	public boolean begin(Object element, RenderingContext context)
			throws RenderingException {
		InstructionGroup instructionGroup = (InstructionGroup) element;
		PatternRepository repository = context.getPatternRepository();
		String message = ContextUtils.deriveLabel(instructionGroup,
				repository);
		if (message != null) {
			context.getRenderer().beginInstructionGroup(message);
		} else {
			context.getRenderer().beginInstructionGroup();
		}
		if (instructionGroup.getResetRowCount()) {
			resetLastExpressedRowNumber(context);
		}
		return true;
	}
	
	public void end(Object element, RenderingContext context) {
		context.getRenderer().endInstructionGroup();
	}

}
