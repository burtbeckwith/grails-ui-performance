package grails.plugin.uiperformance.taglib

import grails.plugin.uiperformance.postprocess.AbstractTagPostProcessor
import grails.plugin.uiperformance.postprocess.CssTagPostProcessor
import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.junit.Before

/**
 * Integration tests for {@link CssTagLib}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@TestFor(CssTagLib)
class CssTagLibTests extends AbstractTaglibTest {

	@Before
	void setup() {
		CssTagLib taglib = applicationContext.getBean(CssTagLib)
		def processor = new CssTagPostProcessor()
		taglib.cssTagPostProcessor = processor

		init processor, taglib
		taglib.cssTagPostProcessor.uiPerformanceService = uiPerformanceService
	}

	void testMissingName() {
		shouldFail(GrailsTagException) {
			applyTemplate "<p:css src='foo'/>"
		}
	}

	void testAbsolute() {
		assert applyTemplate("<p:css name='/ab/so/lute/foo.css' absolute='true'/>") ==
			'<link rel="stylesheet" type="text/css" href="/ab/so/lute/foo__v123.css" />'
	}

	void testRelative() {
		assert applyTemplate("<p:css name='foo'/>") ==
		'<link rel="stylesheet" type="text/css" href="/css/foo__v123.css" />'
	}

	void testRel() {
		assert applyTemplate("<p:css name='foo' rel='bar'/>") ==
			'<link rel="bar" type="text/css" href="/css/foo__v123.css" />'
	}
}
