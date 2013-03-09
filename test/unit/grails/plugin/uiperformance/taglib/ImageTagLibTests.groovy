package grails.plugin.uiperformance.taglib

import grails.plugin.uiperformance.postprocess.ImageTagPostProcessor
import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.plugins.DefaultGrailsPlugin
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.junit.Before
import org.springframework.core.io.FileSystemResource
import org.springframework.web.context.request.RequestContextHolder as RCH

/**
 * Integration tests for {@link ImageTagLib}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@TestFor(ImageTagLib)
class ImageTagLibTests extends AbstractTaglibTest {

	private ImageTagLib taglib

	@Before
	void setup() {
		taglib = applicationContext.getBean(ImageTagLib)
		def processor = new ImageTagPostProcessor()
		taglib.imageTagPostProcessor = processor

		init processor, taglib
		taglib.imageTagPostProcessor.uiPerformanceService = uiPerformanceService
	}

	void testImageMissingSrc() {
		shouldFail(GrailsTagException) {
			applyTemplate "<p:image source='foo'/>"
		}
	}

	void testImageAbsolute() {
		String template = "<p:image src='/ab/so/lute/foo.gif' absolute='true'/>"
		assertOutputEquals '<img src="/ab/so/lute/foo__v123.gif" border="0"/>', template
	}

	void testImageRelative() {
		String template = "<p:image src='foo.gif'/>"
		assertOutputEquals '<img src="/images/foo__v123.gif" border="0"/>', template
	}

	void testImageFromPlugin() {

		GroovyClassLoader cl = new GroovyClassLoader()
		cl.addURL(new File('target/classes').toURI().toURL())

		DefaultGrailsPlugin uiPerformance = new DefaultGrailsPlugin(
			cl.loadClass('UiPerformanceGrailsPlugin'),
			new FileSystemResource(new File('UiPerformanceGrailsPlugin.groovy')),
			taglib.grailsApplication)
		uiPerformance.version = '1.2.3'

		taglib.pluginManager.plugins['uiPerformance'] = uiPerformance

		String template = "<p:image src='foo.gif' plugin='ui-performance'/>"
		assertOutputEquals """<img src="/plugins/ui-performance-1.2.3/images/foo__v123.gif" border="0"/>""", template
	}

	void testInputImageMissingSrc() {
		shouldFail(GrailsTagException) {
			applyTemplate "<p:inputImage source='foo'/>"
		}
	}

	void testInputImageAbsolute() {
		String template = "<p:inputImage src='/ab/so/lute/foo.gif' absolute='true'/>"
		assertOutputEquals '<input type="image" src="/ab/so/lute/foo__v123.gif" border="0"/>', template
	}

	void testInputImageRelative() {
		String template = "<p:inputImage src='foo.gif'/>"
		assertOutputEquals '<input type="image" src="/images/foo__v123.gif" border="0"/>', template
	}

	void testImageLinkMissingSrc() {
		shouldFail(GrailsTagException) {
			applyTemplate "<p:imageLink source='foo'/>"
		}
	}

	void testImageLinkAbsolute() {
		String template = "<p:imageLink src='/ab/so/lute/foo.gif' absolute='true'/>"
		assertOutputEquals "/ab/so/lute/foo__v123.gif", template
	}

	void testImageLinkRelative() {
		String template = "<p:imageLink src='foo.gif'/>"
		assertOutputEquals "/images/foo__v123.gif", template
	}

	void testFavicon() {
		String template = "<p:favicon src='/images/myfavicon'/>"
		assertOutputEquals '<link rel="shortcut icon" href="/images/myfavicon__v123.ico" type="image/x-icon"/>', template
	}

	void testFaviconDefault() {
		String template = "<p:favicon />"
		assertOutputEquals '<link rel="shortcut icon" href="/favicon__v123.ico" type="image/x-icon"/>', template
	}

	void testFaviconPng() {
		String template = "<p:favicon src='/foo.png' />"
		assertOutputEquals '<link rel="shortcut icon" href="/foo__v123.png" type="image/png"/>', template
	}

	void testFaviconWithContext() {
		String template = "<p:favicon src='/images/myfavicon'/>"
		RCH.currentRequestAttributes().request.contextPath = '/the_context'

		assertOutputEquals '<link rel="shortcut icon" href="/the_context/images/myfavicon__v123.ico" type="image/x-icon"/>', template
	}
}
