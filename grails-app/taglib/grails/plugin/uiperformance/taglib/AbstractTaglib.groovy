package grails.plugin.uiperformance.taglib

/**
 * Abstract base class for taglibs.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class AbstractTaglib {

	def pluginManager
	def uiPerformanceService

	/**
	 * Generates html for attributes not explicitly handled.
	 * <p/>
	 * Remove all handled attributes before calling this, e.g.
	 * <code>attrs.remove 'border'</code>
	 *
	 * @param attrs  the attribute map
	 * @return  html for extra attributes
	 */
	protected String generateExtraAttributes(attrs) {
		StringBuilder extra = new StringBuilder()
		attrs.each { key, value ->
			extra.append ' '
			extra.append key
			extra.append '="'
			extra.append value
			extra.append '"'
		}
		extra.toString()
	}

	protected String generateRelativePath(dir, name, extension, plugin, absolute, base) {

		boolean cdn = uiPerformanceService.cdnIsEnabled()

		StringBuilder path
		if (cdn) {
			path = new StringBuilder(uiPerformanceService.getCdnPath())
		}
		else {
			if ('true' == absolute) {
				return name
			}
		}

		if (!cdn) {
      String baseUri = base ?: grailsAttributes.getApplicationUri(request)
			path = new StringBuilder(baseUri)
			if (!baseUri.endsWith('/')) {
				path.append '/'
			}
		}

		String requestPluginContext = plugin ? pluginManager.getPluginPath(plugin) : ''
		if (requestPluginContext) {
			path.append (requestPluginContext.startsWith('/') ? requestPluginContext.substring(1) : requestPluginContext)
			path.append '/'
		}
		if (dir) {
      path.append (dir.startsWith('/') ? dir.substring(1) : dir)
			path.append '/'
		}
		path.append (name.startsWith('/') ? name.substring(1) : name)
		if (extension) {
			path.append extension
		}

		path.toString()
	}
}
