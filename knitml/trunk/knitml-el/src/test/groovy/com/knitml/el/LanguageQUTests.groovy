package com.knitml.el

import static com.knitml.el.KelUtils.*
import static org.custommonkey.xmlunit.XMLAssert.*

import org.custommonkey.xmlunit.XMLUnit
import org.junit.BeforeClass
import org.junit.Test

class LanguageQUTests {
	
	@BeforeClass
	static void setUp() {
		initXMLUnit()
	}
	
	@Test
	void rowAndAllEvenRows() {
		String actual = toXml ("row 2 and all even rows")
		String expected = '''
			<row number="2" subsequent="even"/>
		'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void roundAndAllEvenRounds() {
		String actual = toXml ("round 2 and all even rounds")
		String expected = '''
			<row type="round" number="2" subsequent="even"/>
		'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void rowAndFollowingOddRows() {
		String actual = toXml ("row 2 and following odd rows")
		String expected = '''
			<row number="2" subsequent="odd"/>
		'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void rowAndFollowingEvenRows() {
		String actual = toXml ("row 2 and following even rows")
		String expected = '''
			<row number="2" subsequent="even"/>
		'''
        assertXMLEqual expected, actual
	}

	@Test
	void rowAndEvenRows() {
		String actual = toXml ("row 2 and even rows")
		String expected = '''
			<row number="2" subsequent="even"/>
		'''
		assertXMLEqual expected, actual
	}

	@Test
	void rowAndFollowingRows() {
		// note that this won't do anything; either 'even' or 'odd' is required
		String actual = toXml ("row 2 and following rows")
		String expected = '''
			<row number="2"/>
		'''
		assertXMLEqual expected, actual
	}

	@Test
	void rowWithRsFacing() {
		String actual = toXml ("row with RS facing")
		String expected = '''
			<row side="right"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnRightSide() {
		String actual = toXml ("row on rightSide")
		String expected = '''
			<row side="right"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnRS() {
		String actual = toXml ("row on RS")
		String expected = '''
			<row side="right"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnRs() {
		String actual = toXml ("row on rs")
		String expected = '''
			<row side="right"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnWrongSide() {
		String actual = toXml ("row on wrongSide")
		String expected = '''
			<row side="wrong"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnWS() {
		String actual = toXml ("row on WS")
		String expected = '''
			<row side="wrong"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void rowOnWs() {
		String actual = toXml ("row on ws")
		String expected = '''
			<row side="wrong"/>
		'''
		assertXMLEqual expected, actual
	}
	
	@Test
	void repeatInstructionFor3Inches() {
		String actual = toXml ("repeat 'blah' for 3.5 in")
		String expected = '''
			<repeat-instruction ref="blah">
				<until-measures unit="in">3.5</until-measures>
			</repeat-instruction>
		'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void stitchHolder() {
		String actual = toXml ("stitchHolder 'sh1' withKey withLabel 'Stitch Holder 1'")
		String expected = '''
			<stitch-holder id="sh1" message-key="stitch-holder.sh1" label="Stitch Holder 1"/>
		'''
        assertXMLEqual expected, actual
	}

	@Test
	void s2kp() {
		String actual = toXml ("s2kp")
		String expected = '''<double-decrease type="cdd"/>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sk2p() {
		String actual = toXml ("sk2p")
		String expected = '''<double-decrease type="sk2p"/>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sssk() {
		String actual = toXml ("sssk")
		String expected = '''<double-decrease type="sssk"/>'''
        assertXMLEqual expected, actual
	}
	
	
	@Test
	void s2kpWithArg() {
		String actual = toXml ("s2kp 3")
		String expected = '''<double-decrease type="cdd">3</double-decrease>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sk2pWithArg() {
		String actual = toXml ("sk2p 3")
		String expected = '''<double-decrease type="sk2p">3</double-decrease>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void ssskWithArg() {
		String actual = toXml ("sssk 3")
		String expected = '''<double-decrease type="sssk">3</double-decrease>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void slReverse() {
		String actual = toXml ("sl reverse 2 sts")
		String expected = '''
			<slip direction="reverse">2</slip>
		'''
        assertXMLEqual expected, actual
	}
	@Test
	void slInReverse() {
		String actual = toXml ("sl inReverse 2 sts")
		String expected = '''
			<slip direction="reverse">2</slip>
		'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sl2Wyib() {
		String actual = toXml ("sl 2 wyib")
		String expected = '''<slip yarn-position="back">2</slip>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sl2Wyif() {
		String actual = toXml ("sl 2 wyif")
		String expected = '''<slip yarn-position="front">2</slip>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void slFromHolder() {
		String actual = toXml ("sl 2 sts from holder 'sh1'")
		String expected = '''
			<from-stitch-holder ref="sh1">
				<slip>2</slip>
			</from-stitch-holder>'''
        assertXMLEqual expected, actual
	}
	@Test
	void slPurlwiseFromHolder() {
		String actual = toXml ("sl2 purlwise from holder 'sh1'")
		String expected = '''
			<from-stitch-holder ref="sh1">
				<slip type="purlwise">2</slip>
			</from-stitch-holder>'''
        assertXMLEqual expected, actual
	}
	@Test
	void sl2ToHolder() {
		String actual = toXml ("sl next 2 sts to holder 'sh1'")
		String expected = '''<slip-to-stitch-holder ref="sh1">2</slip-to-stitch-holder>'''
        assertXMLEqual expected, actual
	}
	
	@Test
	void sl1ToHolder() {
		String actual = toXml ("sl next st to holder 'sh1'")
		String expected = '''<slip-to-stitch-holder ref="sh1">1</slip-to-stitch-holder>'''
        assertXMLEqual expected, actual
	}

}