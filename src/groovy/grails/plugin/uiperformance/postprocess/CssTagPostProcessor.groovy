package grails.plugin.uiperformance.postprocess

/**
 * CSS tag post-processor.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class CssTagPostProcessor extends AbstractTagPostProcessor {

	List extensions = ['css']
	boolean gzip = true

	@Override
	String process(String html, request) {

		if (!uiPerformanceService.getConfigBoolean('processCSS') || !uiPerformanceService.isEnabled()) {
			return expandBundle(html)
		}

		return super.process(html, request)
	}
}
