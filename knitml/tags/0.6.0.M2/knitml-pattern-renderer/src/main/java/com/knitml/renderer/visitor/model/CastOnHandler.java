package com.knitml.renderer.visitor.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.model.directions.CompositeOperation;
import com.knitml.core.model.directions.Operation;
import com.knitml.core.model.directions.block.CastOn;
import com.knitml.core.model.directions.block.UseNeedles;
import com.knitml.core.model.header.Needle;
import com.knitml.renderer.Renderer;
import com.knitml.renderer.common.RenderingException;
import com.knitml.renderer.event.impl.AbstractEventHandler;

public class CastOnHandler extends AbstractEventHandler {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(CastOnHandler.class);

	public boolean begin(Object element, Renderer renderer)
			throws RenderingException {
		CastOn castOn = (CastOn) element;
		
		if (!renderer.getRenderingContext().getPatternState().getOperationTree().empty()) {
			CompositeOperation parent = renderer.getRenderingContext().getPatternState()
					.getOperationTree().peek();
			List<? extends Operation> siblings = parent.getOperations();
			int position = siblings.indexOf(castOn);
			if (position > 0) {
				Operation previousOperation = siblings.get(position - 1);
				if (previousOperation instanceof UseNeedles
						&& ((UseNeedles) previousOperation).isSilentRendering()) {
					List<Needle> needles = ((UseNeedles) previousOperation)
							.getNeedles();
					renderer.renderUsingNeedlesCastOn(needles,
							castOn);
				} else {
					renderer.renderCastOn(castOn);
				}
			} else {
				renderer.renderCastOn(castOn);
			}
		} else {
			renderer.renderCastOn(castOn);
		}
		return true;
	}
}
