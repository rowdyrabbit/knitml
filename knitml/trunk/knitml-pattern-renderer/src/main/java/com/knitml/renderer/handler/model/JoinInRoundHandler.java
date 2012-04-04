package com.knitml.renderer.handler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knitml.renderer.Renderer;
import com.knitml.renderer.common.RenderingException;
import com.knitml.renderer.event.impl.AbstractEventHandler;

public class JoinInRoundHandler extends AbstractEventHandler {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(JoinInRoundHandler.class);

	public boolean begin(Object element, Renderer renderer)
			throws RenderingException {
		renderer.renderJoinInRound();
		return true;
	}

}