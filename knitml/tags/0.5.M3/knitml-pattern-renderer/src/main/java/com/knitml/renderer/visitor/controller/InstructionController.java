package com.knitml.renderer.visitor.controller;

import java.util.List;

import com.knitml.core.model.directions.CompositeOperation;
import com.knitml.core.model.directions.InlineOperation;
import com.knitml.core.model.directions.Operation;
import com.knitml.core.model.directions.block.Instruction;
import com.knitml.core.model.directions.block.Row;
import com.knitml.core.model.directions.inline.InlineInstruction;
import com.knitml.renderer.Renderer;
import com.knitml.renderer.event.EventHandler;
import com.knitml.renderer.event.EventHandlerFactory;

public class InstructionController {
	private EventHandlerFactory eventHandlerFactory;

	public InstructionController(EventHandlerFactory eventHandlerFactory) {
		this.eventHandlerFactory = eventHandlerFactory;
	}

	public void visitInstruction(Instruction instruction,
			Renderer renderer) {
		if (instruction.hasRows()) {
			for (Row row : instruction.getRows()) {
				visit(row, renderer);
			}
		} else {
			visit(instruction.getForEachRowInInstruction(), renderer);
		}
	}

	public void visitInlineInstruction(InlineInstruction instruction,
			Renderer renderer) {
		List<InlineOperation> subOperations = instruction.getOperations();
		for (Operation subOperation : subOperations) {
			visit(subOperation, renderer);
		}
	}

	protected void visit(Operation operation, Renderer renderer) {
		EventHandler eventHandler = eventHandlerFactory
				.findEventHandlerFromClassName(operation);
		boolean result = eventHandler.begin(operation, renderer);
		if (result) {
			if (operation instanceof CompositeOperation) {
				List<? extends Operation> subOperations = ((CompositeOperation) operation)
						.getOperations();
				for (Operation subOperation : subOperations) {
					visit(subOperation, renderer);
				}
			}
		}
		eventHandler.end(operation, renderer);
	}
}
