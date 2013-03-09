package grails.plugin.uiperformance.postprocess

import grails.plugin.uiperformance.AbstractUiPerformanceTest

/**
 * Unit tests for {@link CssTagPostProcessor}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class CssTagPostProcessorTests extends AbstractUiPerformanceTest {

	private CssTagPostProcessor processor = new CssTagPostProcessor()

	@Override
	protected void setUp() {
		super.setUp()
		processor.uiPerformanceService = uiPerformanceService
	}

	void testProcessGzip() {

		def request = [getHeader: { String name -> 'gzip' }]

		assertEquals "<link rel='stylesheet' type='text/css' href='/foo__v123.gz.css' />",
			processor.process("<link rel='stylesheet' type='text/css' href='/foo.css' />", request)
	}

	void testProcessNotGzip() {

		def request = [getHeader: { String name -> null }]

		assertEquals "<link rel='stylesheet' type='text/css' href='/foo__v123.css' />",
			processor.process("<link rel='stylesheet' type='text/css' href='/foo.css' />", request)
	}

	void testProcessNotSupported() {

		def request = [getHeader: { String name -> null }]

		String html = "<script src='foo.js'></script>"
		assertEquals html, processor.process(html, request)
	}
}
