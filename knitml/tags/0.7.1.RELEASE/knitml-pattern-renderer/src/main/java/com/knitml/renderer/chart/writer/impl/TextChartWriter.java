package com.knitml.renderer.chart.writer.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.knitml.core.common.KnittingShape;
import com.knitml.core.common.Side;
import com.knitml.renderer.chart.Chart;
import com.knitml.renderer.chart.ChartElement;
import com.knitml.renderer.chart.legend.LegendOperationRenderer;
import com.knitml.renderer.chart.symbol.Symbol;
import com.knitml.renderer.chart.symbol.SymbolProvider;
import com.knitml.renderer.chart.symbol.SymbolResolutionException;
import com.knitml.renderer.chart.writer.ChartWriter;
import com.knitml.renderer.context.RenderingContext;

public class TextChartWriter implements ChartWriter {

	private SymbolProvider symbolProvider;
	private LegendOperationRenderer legendOperationRenderer;
	private boolean writeLineNumbers = true;
	private String suffix = ":";
	private String rowDelimiter = "|";
	private static final String LINE_BREAK = System
			.getProperty("line.separator");

	@Inject
	public TextChartWriter(SymbolProvider symbolProvider,
			LegendOperationRenderer legendOperationRenderer) {
		super();
		this.symbolProvider = symbolProvider;
		this.legendOperationRenderer = legendOperationRenderer;
	}

	public void writeChart(Chart chart, Writer writer, RenderingContext context)
			throws SymbolResolutionException {

		List<List<ChartElement>> graph = chart.getGraph();
		int currentLineNumber = chart.getStartingRowNumber() + graph.size() - 1;
		int fillCount = 2; // one for the row number digit, one for the space
		if (currentLineNumber >= 10) {
			fillCount++;
		}
		if (currentLineNumber >= 100) {
			fillCount++;
		}

		Set<ChartElement> elementsUsed = new TreeSet<ChartElement>();
		ListIterator<List<ChartElement>> graphIt = graph.listIterator(graph
				.size());
		try {
			if (chart.getTitle() != null) {
				writer.write(chart.getTitle());
				writer.write(suffix);
				writer.write(LINE_BREAK);
			}
			while (graphIt.hasPrevious()) {
				List<ChartElement> row = graphIt.previous();
				ListIterator<ChartElement> rowIt = row.listIterator(row.size());

				if (renderLeftSideRowNumber(currentLineNumber, chart)) {
					writer.write(StringUtils.rightPad(
							String.valueOf(currentLineNumber), fillCount));
				} else {
					writer.write(StringUtils.rightPad("", fillCount));
				}
				writer.write(rowDelimiter);
				String symbolSetId = null;
				while (rowIt.hasPrevious()) {
					ChartElement element = rowIt.previous();
					elementsUsed.add(element);
					Symbol symbol = symbolProvider.getSymbol(element);
					if (symbolSetId != null
							&& !symbolSetId.equals(symbol.getSymbolSetId())) {
						throw new CannotRenderSymbolException(
								"A TextChartWriter cannot render more than one symbol set from a given SymbolProvider",
								symbolProvider, element);
					}
					symbolSetId = symbol.getSymbolSetId();
					writer.write(symbol.getSymbol());
				}

				writer.write(rowDelimiter);
				if (renderRightSideRowNumber(currentLineNumber, chart)) {
					writer.write(StringUtils.leftPad(
							String.valueOf(currentLineNumber), fillCount));
				} else {
					writer.write(StringUtils.leftPad("", fillCount));
				}
				currentLineNumber--;
				writer.write(LINE_BREAK);
			}
			if (this.legendOperationRenderer != null) {
				writer.write("Legend");
				writer.write(LINE_BREAK);
				for (ChartElement element : elementsUsed) {
					writer.write(symbolProvider.getSymbol(element).getSymbol()
							+ suffix + " ");
					writer.write(legendOperationRenderer.resolveLegendFor(element, chart
							.getLegend().get(element)));
					writer.write(LINE_BREAK);
				}
				writer.write(LINE_BREAK);
			}

		} catch (IOException ex) {
			throw new RuntimeException("Could not write to writer", ex);
		}

	}

	private boolean renderLeftSideRowNumber(int currentLineNumber, Chart chart) {
		if (!writeLineNumbers) {
			return false;
		}
		if (chart.getShape() == KnittingShape.ROUND) {
			return false;
		}
		if ((currentLineNumber - chart.getStartingRowNumber()) % 2 == 0) {
			// same direction as first row
			if (chart.getStartingSide() == Side.RIGHT) {
				return false;
			}
			return true;
		}
		// different direction from first row
		if (chart.getStartingSide() == Side.RIGHT) {
			return true;
		}
		return false;
	}

	private boolean renderRightSideRowNumber(int currentLineNumber, Chart chart) {
		return writeLineNumbers
				&& !renderLeftSideRowNumber(currentLineNumber, chart);
	}

}
