package grails.plugin.uiperformance.postprocess

import grails.plugin.uiperformance.AbstractUiPerformanceTest

/**
 * Unit tests for {@link ImageTagPostProcessor}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class ImageTagPostProcessorTests extends AbstractUiPerformanceTest {

	private ImageTagPostProcessor processor = new ImageTagPostProcessor()

	@Override
	protected void setUp() {
		super.setUp()
		processor.uiPerformanceService = uiPerformanceService
	}

	void testProcessGzip() {

		def request = [getHeader: { String name -> 'gzip' }]

		assertEquals "<img src='foo__v123.gif' />",
			processor.process("<img src='foo.gif' />", request)
	}

	void testProcessNotGzip() {

		def request = [getHeader: { String name -> null }]

		assertEquals "<img src='foo__v123.gif' />",
			processor.process("<img src='foo.gif' />", request)
	}

	void testProcessNotSupported() {

		def request = [getHeader: { String name -> null }]

		String html = "<script src='foo.js'></script>"
		assertEquals html, processor.process(html, request)
	}
}
