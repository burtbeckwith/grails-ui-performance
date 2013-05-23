import grails.plugin.uiperformance.CacheFilter
import grails.plugin.uiperformance.ResourceVersionHelper
import grails.plugin.uiperformance.postprocess.CssTagPostProcessor
import grails.plugin.uiperformance.postprocess.ImageTagPostProcessor
import grails.plugin.uiperformance.postprocess.JsTagPostProcessor
import grails.util.Environment

import com.planetj.servlet.filter.compression.CompressingFilter
import org.springframework.web.filter.DelegatingFilterProxy

class UiPerformanceGrailsPlugin {

	String version = '2.0'
	String grailsVersion = '2.0 > *'
	List pluginExcludes = [
		'scripts/CreateUiPerformanceTestApps.groovy',
		'src/groovy/**/AbstractTaglibTest.groovy'
	]

	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String title = 'Grails UI Performance Plugin'
	String description = "Taglibs and Filter to implement some of the Yahoo performance team's 14 rules"
	String documentation = 'http://grails.org/plugin/ui-performance'

	def doWithSpring = {
		// register the three post-processors
		imageTagPostProcessor(ImageTagPostProcessor) { bean ->
			bean.autowire = 'byName'
		}
		cssTagPostProcessor(CssTagPostProcessor) { bean ->
			bean.autowire = 'byName'
		}
		jsTagPostProcessor(JsTagPostProcessor) { bean ->
			bean.autowire = 'byName'
		}

		resourceVersionHelper(ResourceVersionHelper) {
			uiPerformanceService = ref('uiPerformanceService')
		}

		cacheFilter(CacheFilter)
	}

	def doWithWebDescriptor = { xml ->

		if (!isEnabled(application)) {
			return
		}

		def contextParam = xml.'context-param'
		contextParam[contextParam.size() - 1] + {
			'filter' {
				'filter-name'('cacheFilter')
				'filter-class'(DelegatingFilterProxy.name)
			}
		}

		def htmlConfig = application.config.uiperformance.html

		if (htmlConfig.compress) {

			if (!htmlConfig.containsKey('includeContentTypes') &&
					!htmlConfig.containsKey('excludeContentTypes')) {
				// set default to text types only if there's no config set
				htmlConfig.includeContentTypes = ['text/html', 'text/xml', 'text/plain']
			}

			contextParam[contextParam.size() - 1] + {
				'filter' {
					'filter-name'(CompressingFilter.name)
					'filter-class'(CompressingFilter.name)

					['debug', 'statsEnabled'].each { name ->
						def value = htmlConfig[name]
						if (value) {
							'init-param' {
								'param-name'(name)
								'param-value'('true')
							}
						}
					}

					['includePathPatterns', 'excludePathPatterns',
					 'includeContentTypes', 'excludeContentTypes',
					 'includeUserAgentPatterns', 'excludeUserAgentPatterns'].each { name ->
						def value = htmlConfig[name]
						if (value) {
							'init-param' {
								'param-name'(name)
								'param-value'(value.join(','))
							}
						}
					}

					['compressionThreshold', 'javaUtilLogger', 'jakartaCommonsLogger'].each { name ->
						def value = htmlConfig[name]
						if (value) {
							'init-param' {
								'param-name'(name)
								'param-value'(value.toString())
							}
						}
					}
				}
			}
		}

		def filter = xml.'filter'
		if (htmlConfig.compress) {
			filter[filter.size() - 1] + {
				if (!htmlConfig.urlPatterns) {
					htmlConfig.urlPatterns = ['/*']
				}

				for (pattern in htmlConfig.urlPatterns) {
					'filter-mapping' {
						'filter-name'(CompressingFilter.name)
						'url-pattern'(pattern)
					}
				}
			}
		}
    filter[filter.size() - 1] + {
      'filter-mapping' {
        'filter-name'('cacheFilter')
        'url-pattern'('/*')
      }
    }
	}

	private boolean isEnabled(application) {
		def value = application.config.uiperformance.enabled
		value instanceof Boolean ? value : Environment.PRODUCTION == Environment.current
	}
}
