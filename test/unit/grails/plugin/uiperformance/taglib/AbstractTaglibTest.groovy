package grails.plugin.uiperformance.taglib

import grails.plugin.uiperformance.UiPerformanceService
import grails.plugin.uiperformance.postprocess.AbstractTagPostProcessor

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

/**
 * Abstract base class for taglib tests.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class AbstractTaglibTest {

	protected UiPerformanceService uiPerformanceService = new UiPerformanceService()

	protected void init(AbstractTagPostProcessor processor, AbstractTaglib taglib) {
		taglib.uiPerformanceService = uiPerformanceService
		DefaultGrailsApplication grailsApplication = new DefaultGrailsApplication()
		def config = new ConfigObject()
		processor.grailsApplication = grailsApplication
		uiPerformanceService.grailsApplication = grailsApplication
		uiPerformanceService.applicationVersion = '123'
		config.uiperformance.enabled = true
		grailsApplication.config = config
	}
}
