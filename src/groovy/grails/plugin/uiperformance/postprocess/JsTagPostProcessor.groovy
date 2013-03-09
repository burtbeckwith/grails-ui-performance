package grails.plugin.uiperformance.postprocess

/**
 * JavaScript tag post-processor.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class JsTagPostProcessor extends AbstractTagPostProcessor {

	List extensions = ['js']
	boolean gzip = true

	@Override
	String process(String html, request) {

		if (!uiPerformanceService.getConfigBoolean('processJS') || !uiPerformanceService.isEnabled()) {
			return expandBundle(html)
		}

		return super.process(html, request)
	}
}
