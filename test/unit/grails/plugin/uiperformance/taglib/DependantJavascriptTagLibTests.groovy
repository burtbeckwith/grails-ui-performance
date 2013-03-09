package grails.plugin.uiperformance.taglib

import org.springframework.web.context.request.RequestContextHolder as RCH

/**
 * Integration tests for {@link DependantJavascriptTagLib}.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class DependantJavascriptTagLibTests extends AbstractTaglibTest {

	void testDependantJavascript() {
		String template = "<p:dependantJavascript>foo1</p:dependantJavascript>" +
		                  "<p:dependantJavascript>foo2</p:dependantJavascript>" +
		                  "<p:dependantJavascript>foo3</p:dependantJavascript>"
		assertOutputEquals "", template
		assertEquals 3, RCH.currentRequestAttributes().request.jsBlocks.size()
	}

	void testRenderDependantJavascript() {
		String template = "<p:dependantJavascript>foo1</p:dependantJavascript>" +
		                  "<p:dependantJavascript>foo2</p:dependantJavascript>" +
		                  "<p:dependantJavascript>foo3</p:dependantJavascript>" +
		                  "<p:renderDependantJavascript />"
		assertOutputEquals "foo1\nfoo2\nfoo3\n", template
	}
}
