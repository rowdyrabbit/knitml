/**
 * 
 */
package com.knitml.core.model.directions.inline

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static test.support.JiBXTestUtils.unmarshalXml
import static test.support.JiBXTestUtils.marshalXmlAndCompare

import org.custommonkey.xmlunit.XMLUnit

import org.junit.Test
import org.junit.Ignore
import org.junit.BeforeClass
import org.junit.internal.runners.JUnit4ClassRunner
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith

import com.knitml.core.model.Pattern
import com.knitml.core.common.Side
import com.knitml.core.common.Wise
import com.knitml.core.common.LoopToWork
import com.knitml.core.common.YarnPosition
import com.knitml.core.common.IncreaseType

@RunWith(JUnit4ClassRunner)
class IncreaseTests {
	@BeforeClass
	static void setUp() {
		XMLUnit.ignoreWhitespace = true
	}
	
	@Test
	void increase() {
		def xml = '''
			<pattern xmlns="http://www.knitml.com/schema/pattern">
			  <directions>
					<row>
						<increase yarn-ref="yarn0" type="m1a"/>
						<increase>5</increase>
					</row>
				  </directions>
			</pattern>'''
		Pattern pattern = unmarshalXml(xml)
		def element = pattern.directions.operations[0].operations[0]
		element.with {
			assertThat yarnIdRef, is ('yarn0')
			assertThat type, is (IncreaseType.M1A)
			assertThat numberOfTimes, is (null)
		}
		element = pattern.directions.operations[0].operations[1]
		element.with {
			assertThat yarnIdRef, is (null)
			assertThat type, is (null)
			assertThat numberOfTimes, is (5)
		}
		marshalXmlAndCompare(pattern,xml)
	}
	
	@Test
	void increaseIntoNextStitch() {
		def xml = '''
			<pattern xmlns="http://www.knitml.com/schema/pattern">
			  <directions>
					<row>
						<increase-into-next-stitch>
							<knit>2</knit>
							<purl>2</purl>
						</increase-into-next-stitch>
					</row>
				  </directions>
			</pattern>'''
		Pattern pattern = unmarshalXml(xml)
		def element = pattern.directions.operations[0].operations[0]
		assertThat ((element instanceof IncreaseIntoNextStitch), is (true))
		element.operations[0].with {
			assertThat (it instanceof Knit, is (true))
			assertThat numberOfTimes, is (2)
		}
		element.operations[1].with {
			assertThat (it instanceof Purl, is (true))
			assertThat numberOfTimes, is (2)
		}
		marshalXmlAndCompare(pattern,xml)
	}
	
	@Test
	void pickUpStitches() {
		def xml = '''
			<pattern xmlns="http://www.knitml.com/schema/pattern">
			  <directions>
					<row>
						<inline-pick-up-stitches>10</inline-pick-up-stitches>
						<inline-pick-up-stitches type="purlwise" yarn-ref="yarn0">15</inline-pick-up-stitches>
					</row>
				  </directions>
			</pattern>'''
		Pattern pattern = unmarshalXml(xml)
		def row = pattern.directions.operations[0]
		row.operations[0].with {
			assertThat (it instanceof InlinePickUpStitches, is (true))
			assertThat numberOfTimes, is (10)
		}
		row.operations[1].with {
			assertThat (it instanceof InlinePickUpStitches, is (true))
			assertThat type, is (Wise.PURLWISE)
			assertThat yarnIdRef, is ('yarn0')
			assertThat numberOfTimes, is (15)
		}
		marshalXmlAndCompare(pattern,xml)
	}
	
	@Test
	void castOn() {
		def xml = '''
				<pattern xmlns="http://www.knitml.com/schema/pattern">
				  <directions>
				  	<inline-cast-on yarn-ref="yarn1" style="backwards-loop">10</inline-cast-on>
				  	<inline-cast-on>5</inline-cast-on>
				  </directions>
				</pattern>'''
		Pattern pattern = unmarshalXml(xml)
		def element = pattern.directions.operations[0]
		element.with {
			assertThat yarnIdRef, is ('yarn1')
			assertThat style, is ('backwards-loop')
			assertThat numberOfStitches, is (10)
		}
		element = pattern.directions.operations[1]
		element.with {
			assertThat yarnIdRef, is (null)
			assertThat style, is (null)
			assertThat numberOfStitches, is (5)
		}
		marshalXmlAndCompare(pattern,xml)
	}
	
	
	static void main(args) {
		JUnitCore.main(IncreaseTests.name)
	}
	
}
