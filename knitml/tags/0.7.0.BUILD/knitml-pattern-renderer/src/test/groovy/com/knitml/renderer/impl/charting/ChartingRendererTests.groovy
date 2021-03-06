package com.knitml.renderer.impl.charting

import static com.knitml.renderer.chart.ChartElement.*
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.not
import static org.junit.Assert.assertThat
import static test.support.JiBXUtils.parseXml

import org.junit.Test

import test.support.AbstractRenderingContextTests
import test.support.LogicalChartTestModule
import test.support.GuiceJUnit4Runner.GuiceModules


@GuiceModules( LogicalChartTestModule )
class ChartingRendererTests extends AbstractRenderingContextTests {

	@Test
	public void flatChart() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="flat"> 
						<row> 
							<purl>2</purl>
							<knit loop-to-work="trailing">2</knit>
						</row>
						<row>
							<knit loop-to-work="trailing">2</knit>
							<purl>2</purl>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[P, P, K_TW, K_TW],
			[K, K, P_TW, P_TW]
		])
	}

	@Test
	public void flatChartStartingOnWrongSide() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="flat">
						<row side="wrong">
							<purl>2</purl>
							<knit loop-to-work="trailing">2</knit>
						</row>
						<row>
							<knit loop-to-work="trailing">2</knit>
							<purl>2</purl>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[P_TW, P_TW, K, K],
			[K_TW, K_TW, P, P]
		])
	}


	@Test
	public void flatChartWithWorkEven() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="flat"> 
						<row> 
							<knit>2</knit>
							<purl>2</purl>
						</row>
						<row>
							<work-even>4</work-even>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([[K, K, P, P], [K, K, P, P]])
	}

	@Test
	public void flatChartWithWorkEvenWithRepeat() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="flat"> 
						<row> 
							<knit>2</knit>
							<purl>2</purl>
						</row>
						<row>
							<repeat until="times" value="4">
								<work-even/>
							</repeat>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([[K, K, P, P], [K, K, P, P]])
	}


	@Test
	public void roundChart() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="round"> 
						<row> 
							<knit>4</knit>
						</row>
						<row>
							<knit>4</knit>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([[K, K, K, K], [K, K, K, K]])
	}

	@Test
	public void roundChartWithWorkEven() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="Stockinette Stitch" shape="round"> 
						<row> 
							<knit>2</knit>
							<purl>2</purl>
						</row>
						<row>
							<work-even>4</work-even>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([[K, K, P, P], [K, K, P, P]])
	}


	@Test
	public void asymmetricFlatChart() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="inst1" label="2x2 Ribbing" shape="flat"> 
						<row>
							<repeat until="times" value="2">
								<knit>2</knit>
								<purl>2</purl>
							</repeat>
						</row>
						<row>
							<repeat until="end">
								<knit>2</knit>
								<purl>2</purl>
							</repeat>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[K, K, P, P, K, K, P, P],
			[K, K, P, P, K, K, P, P]
		]);
	}

	@Test
	public void laceWithEqualRows() {
		// reminiscient of the nutkin2 sample
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="nutkin2" label="Nutkin 2" shape="round"> 
						<row>
							<purl>2</purl>
							<increase type="yo" />
							<knit>3</knit>
							<decrease type="ssk" />
							<knit>8</knit>
							<decrease type="k2tog" />
							<knit>3</knit>
							<increase type="yo" />
							<purl>2</purl>
						</row>
						<row>
							<purl>2</purl>
							<knit>1</knit>
							<increase type="yo" />
							<knit>3</knit>
							<decrease type="ssk" />
							<knit>6</knit>
							<decrease type="k2tog" />
							<knit>3</knit>
							<increase type="yo" />
							<knit>1</knit>
							<purl>2</purl>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[
				P,
				P,
				YO,
				K,
				K,
				K,
				SSK,
				K,
				K,
				K,
				K,
				K,
				K,
				K,
				K,
				K2TOG,
				K,
				K,
				K,
				YO,
				P,
				P
			],
			[
				P,
				P,
				K,
				YO,
				K,
				K,
				K,
				SSK,
				K,
				K,
				K,
				K,
				K,
				K,
				K2TOG,
				K,
				K,
				K,
				YO,
				K,
				P,
				P
			]
		]);
	}

	@Test
	public void basic2StCable() {
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="chart" label="Chart" shape="flat"> 
						<row>
							<purl>4</purl>
								<group size="2">
									<cross-stitches first="1" next="1" type="front"/>
									<knit>2</knit>
								</group>
							<purl>4</purl>
						</row>
						<row>
							<knit>4</knit>
							<purl>2</purl>
							<knit>4</knit>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[P, P, P, P, CBL_1_1_LC, P, P, P, P],
			[P, P, P, P, K, K, P, P, P, P]
		]);
	}
	@Test
	public void custom2StCable() {
		// use a stitch set not found in the group operations map
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="chart" label="Chart" shape="flat"> 
						<row>
							<purl>4</purl>
								<group size="2">
									<cross-stitches first="1" next="1" type="front"/>
									<knit loop-to-work="trailing">1</knit>
									<purl loop-to-work="trailing">1</purl>
								</group>
							<purl>4</purl>
						</row>
						<row>
							<knit>4</knit>
							<purl>2</purl>
							<knit>4</knit>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[
				P,
				P,
				P,
				P,
				CBL_2ST_CUSTOM,
				P,
				P,
				P,
				P
			],
			[P, P, P, P, K, K, P, P, P, P]
		]);
	}
	@Test
	public void kpkIntoNextStitch() {
		// use a stitch set not found in the group operations map
		processXml PATTERN_START_TAG + '''
			<pattern:directives>
				<pattern:instruction-definitions>
					<instruction id="chart" label="Chart" shape="round"> 
						<row>
							<knit>1</knit>
						</row>
						<row>
							<increase-into-next-stitch>
								<knit/>
								<purl/>
								<knit/>
							</increase-into-next-stitch>
						</row>
						<row>
							<knit>3</knit>
						</row>
					</instruction>
				</pattern:instruction-definitions>
			</pattern:directives>
		 </pattern:pattern>'''

		assertThat renderer.graph, is ([
			[K, NS, NS],
			[KPK_NEXT_ST, NS, NS],
			[K, K, K]
		]);

	}
}
