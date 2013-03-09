package grails.plugin.uiperformance

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.util.ReflectionUtils

/**
 * Abstract base class for unit tests.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class AbstractUiPerformanceTest extends GroovyTestCase {

	protected Map flatConfig
	protected ConfigObject config = [:]
	protected DefaultGrailsApplication grailsApplication = new DefaultGrailsApplication()
	protected UiPerformanceService uiPerformanceService = new UiPerformanceService()
	protected ResourceVersionHelper resourceVersionHelper = new ResourceVersionHelper()

	/**
	 * {@inheritDoc}
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {
		super.setUp()

		uiPerformanceService.applicationVersion  = '123'
		uiPerformanceService.grailsApplication = grailsApplication

		uiPerformanceService.resourceVersionHelper = resourceVersionHelper
		resourceVersionHelper.uiPerformanceService = uiPerformanceService

		grailsApplication.config = config
		config.clear()

		config.uiperformance.enabled = true
		flattenConfig()
	}

	protected void flattenConfig() {
		flatConfig = config.flatten()
		grailsApplication.flatConfig = flatConfig
	}
}
