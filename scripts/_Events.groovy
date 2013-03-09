import grails.util.Environment

eventCreateWarStart = { name, stagingDir ->
	def value = config.uiperformance.enabled
	def enabled = value instanceof Boolean ? value : Environment.PRODUCTION == Environment.current
	if (!enabled) {
		println "\nUiPerformance not enabled, not processing resources\n"
		return
	}

	println "\nUiPerformance: versioning resources ...\n"

	def helper = classLoader.loadClass('grails.plugin.uiperformance.ResourceVersionHelper').newInstance()
	def service = classLoader.loadClass('grails.plugin.uiperformance.UiPerformanceService').newInstance()
	service.grailsApplication = grailsApp
	service.resourceVersionHelper = helper
	service.afterPropertiesSet()
	helper.uiPerformanceService = service
	helper.version stagingDir, basedir, config
}
