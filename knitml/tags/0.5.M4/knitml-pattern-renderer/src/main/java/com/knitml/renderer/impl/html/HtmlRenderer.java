package com.knitml.renderer.impl.html;

import java.io.Writer;
import java.util.List;

import org.springframework.context.MessageSource;

import com.knitml.core.model.Pattern;
import com.knitml.renderer.chart.stylesheet.StylesheetProvider;
import com.knitml.renderer.context.InstructionInfo;
import com.knitml.renderer.context.RenderingContext;
import com.knitml.renderer.impl.basic.BasicTextRenderer;
import com.knitml.renderer.impl.helpers.HeaderHelper;
import com.knitml.renderer.impl.helpers.OperationSetHelper;

public class HtmlRenderer extends BasicTextRenderer {

	private List<StylesheetProvider> stylesheetProviders;
	private boolean closePreTagBeforePreCraftedInstructions = true;
	private HtmlWriterHelper writerHelper;

	public HtmlRenderer(RenderingContext context, Writer writer,
			MessageSource messageSource, List<StylesheetProvider> stylesheetProviders) {
		super(context, writer, messageSource);
		this.stylesheetProviders = stylesheetProviders;
		if (writer != null) {
			writerHelper = new HtmlWriterHelper(writer);
			setWriterHelper(writerHelper);
		}
		setHeaderHelper(new HeaderHelper(writerHelper, getRenderingContext().getOptions()));
		setOperationSetHelper(new OperationSetHelper(writerHelper,
				getMessageHelper()));
	}
	
	@Override
	public void beginPattern(Pattern pattern) {
		writerHelper.setPreformatted(false);
		writerHelper.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		writerHelper.writeSystemNewLine();
		writerHelper.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\"><head><title>KnitML Pattern</title>");
		writerHelper.writeSystemNewLine();
		for (StylesheetProvider stylesheetProvider : stylesheetProviders) {
			writerHelper.write("<style type=\"");
			writerHelper.write(stylesheetProvider.getMimeType());
			writerHelper.write("\">");
			writerHelper.writeSystemNewLine();
			writerHelper.write(stylesheetProvider.getStylesheet());
			writerHelper.writeSystemNewLine();
			writerHelper.write("</style>");
			writerHelper.writeSystemNewLine();
		}
		writerHelper.write("</head><body>");
		writerHelper.setPreformatted(true);
	}

	@Override
	public void endPattern() {
		writerHelper.setPreformatted(false);
		writerHelper.write("</body></html>");
		super.endPattern();
	}

	@Override
	public void beginInstructionDefinition(InstructionInfo instructionInfo) {
		boolean wasPreformatted = writerHelper.isPreformatted();
		if (wasPreformatted && closePreTagBeforePreCraftedInstructions) {
			writerHelper.setPreformatted(false);
		}
		super.beginInstructionDefinition(instructionInfo);
		if (wasPreformatted && closePreTagBeforePreCraftedInstructions) {
			writerHelper.setPreformatted(true);
		}
	}
	
	@Override
	public void beginInstruction(InstructionInfo instructionInfo) {
		boolean wasPreformatted = writerHelper.isPreformatted();
		if (wasPreformatted && closePreTagBeforePreCraftedInstructions) {
			writerHelper.setPreformatted(false);
		}
		super.beginInstruction(instructionInfo);
		if (wasPreformatted && closePreTagBeforePreCraftedInstructions) {
			writerHelper.setPreformatted(true);
		}
	}
	
}
