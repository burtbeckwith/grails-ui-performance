package grails.plugin.uiperformance.postprocess

import grails.plugin.uiperformance.AbstractUiPerformanceTest

/**
 * Unit tests for {@link JsTagPostProcessor}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class JsTagPostProcessorTests extends AbstractUiPerformanceTest {

	private JsTagPostProcessor processor = new JsTagPostProcessor()

	@Override
	protected void setUp() {
		super.setUp()
		processor.uiPerformanceService = uiPerformanceService
	}

	void testProcessGzip() {

		def request = [getHeader: { String name -> 'gzip' }]

		assertEquals "<script src='foo__v123.gz.js'></script>",
			processor.process("<script src='foo.js'></script>", request)
	}

	void testProcessNotGzip() {

		def request = [getHeader: { String name -> null }]

		assertEquals "<script src='foo__v123.js'></script>",
			processor.process("<script src='foo.js'></script>", request)
	}

	void testProcessNotSupported() {

		def request = [getHeader: { String name -> null }]

		String html = "<img src='foo.gif'></script>"
		assertEquals html, processor.process(html, request)
	}
}
