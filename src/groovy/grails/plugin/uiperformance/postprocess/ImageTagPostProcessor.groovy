package grails.plugin.uiperformance.postprocess

/**
 * Image tag post-processor.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class ImageTagPostProcessor extends AbstractTagPostProcessor {

	boolean gzip = false

	@Override
	String process(String html, request) {

		if (!uiPerformanceService.getConfigBoolean('processImages') || !uiPerformanceService.isEnabled()) {
			return html
		}

		return super.process(html, request)
	}

	protected List getExtensions() {
		return uiPerformanceService.getConfigValue('imageExtensions', uiPerformanceService.DEFAULT_IMAGE_EXTENSIONS)
	}
}
