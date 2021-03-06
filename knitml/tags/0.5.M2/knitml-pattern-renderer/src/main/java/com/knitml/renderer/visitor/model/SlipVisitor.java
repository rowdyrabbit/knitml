package com.knitml.renderer.visitor.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.core.model.directions.inline.Slip;
import com.knitml.renderer.common.RenderingException;
import com.knitml.renderer.context.RenderingContext;
import com.knitml.renderer.event.impl.AbstractRenderingEvent;

public class SlipVisitor extends AbstractRenderingEvent {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(SlipVisitor.class);

	public boolean begin(Object element, RenderingContext context)
			throws RenderingException {
		Slip slip = (Slip) element;
		context.getRenderer().renderSlip(slip);
		return true;
	}
}
