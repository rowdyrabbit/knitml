package com.knitml.tools.runner;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.knitml.renderer.RendererProgram;
import com.knitml.renderer.context.RenderingContextFactory;
import com.knitml.renderer.context.impl.SpringRenderingContextFactory;
import com.knitml.renderer.event.EventFactory;
import com.knitml.renderer.event.impl.DefaultEventFactory;
import com.knitml.validation.ValidationProgram;
import com.knitml.validation.context.KnittingContextFactory;
import com.knitml.validation.context.impl.DefaultKnittingContextFactory;
import com.knitml.validation.visitor.instruction.impl.SpringVisitorFactory;

public abstract class RunnerTests {

	// protected static final String APP_CTX_VALIDATION =
	// "applicationContext-validation.xml";
	protected static final String APP_CTX_RENDERER = "applicationContext-patternRenderer.xml";

	protected static ValidationProgram validator;
	protected static RendererProgram renderer;
	protected static KnittingContextFactory knittingContextFactory;
	protected static com.knitml.validation.visitor.instruction.VisitorFactory knittingVisitorFactory;
	protected static RenderingContextFactory renderingContextFactory;
	protected static EventFactory renderingVisitorFactory;

	@BeforeClass
	public static void configureContextFactories() {
		knittingContextFactory = new DefaultKnittingContextFactory();
		knittingVisitorFactory = new SpringVisitorFactory();
		renderingContextFactory = new SpringRenderingContextFactory(
				APP_CTX_RENDERER);
		renderingVisitorFactory = new DefaultEventFactory();
		validator = new ValidationProgram(knittingContextFactory,
				knittingVisitorFactory);
		renderer = new RendererProgram(renderingContextFactory,
				renderingVisitorFactory, knittingContextFactory,
				knittingVisitorFactory);
	}

	@AfterClass
	public static void shutdownContextFactories() {
		knittingContextFactory.shutdown();
		renderingContextFactory.shutdown();
		validator = null;
		renderer = null;
	}

}
