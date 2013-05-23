package grails.plugin.uiperformance

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

/**
 * Unit tests for {@link CacheFilter}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class CacheFilterTests extends AbstractUiPerformanceTest {

	private CacheFilter filter

	private static final List EXTENSIONS = ['js', 'css',  'gif', 'jpg', 'png', 'ico']

	/**
	 * {@inheritDoc}
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {
		super.setUp()

		filter = new CacheFilter()
		filter.exclusions = []
		filter.processImages = true
		filter.processCSS = true
		filter.processJS = true
		filter.EXTENSIONS.clear()
		filter.EXTENSIONS.addAll EXTENSIONS
		filter.grailsApplication = grailsApplication
    filter.uiPerformanceService = uiPerformanceService
	}

	void testDoFilter() {

		def request = new MockHttpServletRequest()
		def response = new MockHttpServletResponse()

		int doFilterCount = 0
		FilterChain filterChain = new FilterChain() {
			void doFilter(ServletRequest req, ServletResponse res) {
				doFilterCount++
			}
		}

		// gzipped CSS
		request.requestURI = '/css/bar__v123.gz.css'
		filter.doFilter request, response, filterChain

		assertEquals 'public, max-age=315360000', response.getHeader('Cache-Control')
		assertEquals 'gzip', response.getHeader('Content-Encoding')
		checkExpires response.getHeader('Expires')

		// non-gzipped CSS
		request.requestURI = '/css/bar__v123.css'
		response.reset()
		filter.doFilter request, response, filterChain

		assertEquals 'public, max-age=315360000', response.getHeader('Cache-Control')
		assertNull response.getHeader('Content-Encoding')
		checkExpires response.getHeader('Expires')

		// not cached
		request.requestURI = '/css/bar'
		response.reset()
		filter.doFilter request, response, filterChain

		assertNull response.getHeader('Cache-Control')
		assertNull response.getHeader('Content-Encoding')
		assertNull response.getHeader('Expires')

		// not prod
		flatConfig['uiperformance.enabled'] = false
    def newUiPerformanceService = new UiPerformanceService()
    newUiPerformanceService.grailsApplication = grailsApplication
    newUiPerformanceService.resourceVersionHelper = resourceVersionHelper
    filter.uiPerformanceService = newUiPerformanceService
		request.requestURI = '/css/bar__v123.gz.css'
		response.reset()
		filter.doFilter request, response, filterChain

		assertNull response.getHeader('Cache-Control')
		assertNull response.getHeader('Content-Encoding')
		assertNull response.getHeader('Expires')

		assertEquals 4, doFilterCount
	}

	void testIsCacheable() {

		assertFalse filter.isCacheable('/foo/bar')
		assertFalse filter.isCacheable('/foo/bar.css')
		assertFalse filter.isCacheable('/foo/bar__v123.flv')

		assertTrue filter.isCacheable('/foo/bar__v123.jpg')
	}

	private void checkExpires(expires) {
		long time = expires.toLong() - 315360000000L
		assertTrue Math.abs(time - System.currentTimeMillis()) < 1000
	}
}
