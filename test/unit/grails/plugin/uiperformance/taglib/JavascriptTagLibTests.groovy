package grails.plugin.uiperformance.taglib

import grails.plugin.uiperformance.postprocess.JsTagPostProcessor
import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.junit.Before

/**
 * Integration tests for {@link JavascriptTagLib}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@TestFor(JavascriptTagLib)
class JavascriptTagLibTests extends AbstractTaglibTest {

	@Before
	void setup() {
		JavascriptTagLib taglib = applicationContext.getBean(JavascriptTagLib)
		def processor = new JsTagPostProcessor()
		taglib.jsTagPostProcessor = processor

		init processor, taglib
		taglib.jsTagPostProcessor.uiPerformanceService = uiPerformanceService
	}

	void testMissingSrc() {
		shouldFail(GrailsTagException) {
			applyTemplate "<p:javascript source='foo'/>"
		}
	}

	void testOk() {
		String template = "<p:javascript src='foo'/>"
		assertOutputEquals '<script type="text/javascript" src="/js/foo__v123.js"></script>', template
	}

	void testExtraAttributes() {
		String template = "<p:javascript src='foo' defer='defer'/>"
		assertOutputEquals '<script type="text/javascript" src="/js/foo__v123.js" defer="defer"></script>', template
	}
}
