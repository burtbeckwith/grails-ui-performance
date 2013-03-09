package grails.plugin.uiperformance.postprocess

/**
 * Abstract base class for tag post-processors.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class AbstractTagPostProcessor {

	def grailsApplication
	def uiPerformanceService

	String process(String html, request, boolean quoted = true) {

		if (!uiPerformanceService.isEnabled()) {
			return html
		}

		for (ext in extensions) {
			int indexExt = html.indexOf(".$ext")
			if (indexExt == -1) {
				continue
			}

			String quote = "'"
			int indexQuoteEnd = quoted ? html.indexOf(quote, indexExt) : html.length()
			if (indexQuoteEnd == -1) {
				quote = '"'
				indexQuoteEnd = html.indexOf('"', indexExt)
				if (indexQuoteEnd == -1) {
					continue
				}
			}

			int indexQuoteStart = quoted ? html.length() - html.reverse().indexOf(
					quote, html.length() - indexQuoteEnd + 1) : 0
			String path = html.substring(indexQuoteStart, indexQuoteEnd)

			String name = uiPerformanceService.addVersion(path)
			if (gzip && !uiPerformanceService.isExcluded(path)) {
				String ae = request.getHeader('accept-encoding')
				if (ae && ae.contains('gzip')) {
					name = (name - ".$ext") + ".gz.$ext"
				}
			}

			return html.substring(0, indexQuoteStart) + name + html.substring(indexQuoteEnd)
		}

		return html
	}

	protected String expandBundle(String html) {
		for (bundle in grailsApplication.config.uiperformance.bundles) {
			String ext = extensions[0]
			if (html.contains("/$ext/$bundle.name")) {
				return expandBundle(html, bundle, ext)
			}
		}

		return html
	}

	protected String expandBundle(String html, bundle, String ext) {

		String path = "/$ext/${bundle.name}.$ext"
		int index = html.indexOf(path)
		String start = html.substring(0, index)
		String end = html.substring(index + path.length())

		def sb = new StringBuilder()
		for (file in bundle.files) {
			sb.append(start)
			sb.append('/').append(ext).append('/')
			sb.append(file).append('.').append(ext)
			sb.append(end).append('\n')
		}
		return sb.toString()
	}
}
