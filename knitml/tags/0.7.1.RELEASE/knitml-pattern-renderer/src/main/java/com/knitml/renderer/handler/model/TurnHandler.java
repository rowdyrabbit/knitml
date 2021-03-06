package com.knitml.renderer.handler.model;

import com.knitml.core.model.operations.inline.Turn;
import com.knitml.renderer.Renderer;
import com.knitml.renderer.common.RenderingException;
import com.knitml.renderer.event.impl.AbstractEventHandler;

public class TurnHandler extends AbstractEventHandler {

	public boolean begin(Object element, Renderer renderer)
			throws RenderingException {
		Turn turn = (Turn) element;
		renderer.renderTurn();
		if (turn.getStitchesLeft() != null) {
			renderer.renderUnworkedStitches(turn.getStitchesLeft());
		}
		return true;
	}
}
