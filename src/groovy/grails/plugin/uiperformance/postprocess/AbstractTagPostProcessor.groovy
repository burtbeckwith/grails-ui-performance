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
      def matcher = html =~  /((?:[^'\s=]*\.$ext[^'\s]*)|(?:[^"\s=]*\.$ext[^"\s]*))/
      if (!matcher.size()) {
        continue
      }
      String path = matcher[0][0]
      if (!uiPerformanceService.isExcluded(path)) {
        String name = uiPerformanceService.addVersion(path)
        if (gzip) {
          String ae = request.getHeader('accept-encoding')
          if (ae && ae.contains('gzip') && !ae.contains('gzip;q=0')) {
            name = name.replace(".$ext", ".gz.$ext")
          }
        }
        return html.replace(path, name)
      }
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
