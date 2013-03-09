package grails.plugin.uiperformance

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class UiPerformanceServiceTests extends AbstractUiPerformanceTest {

	void testAddVersionNotWar() {

		config.uiperformance.enabled = false
		flattenConfig()

		String url = '/foo/bar/images/theurl.gif'
		assertEquals url, uiPerformanceService.addVersion(url)
	}

	void testAddVersionWar() {
		assertEquals '/foo/bar/images/theurl__v123.gif',
			uiPerformanceService.addVersion('/foo/bar/images/theurl.gif')
	}

	void testGetCdnPath() {
		uiPerformanceService.setCdnPath null
		config.uiperformance = [
			enabled: true,
			cdn: [
				enabled: true,
				location: 'http:////www.foo.bar'
			] as ConfigObject
		] as ConfigObject
		flattenConfig()

		assertEquals 'http:////www.foo.bar/', uiPerformanceService.getCdnPath()
	}

	void testGetCdnPathS3() {
		uiPerformanceService.setCdnPath null
		config.uiperformance = [
			enabled: true,
			cdn: [
				enabled: true,
				s3: [
					enabled: true,
					bucketName: 'subbucket.bucket',
					domain: 's3.amazonaws.com'
				] as ConfigObject
			] as ConfigObject
		] as ConfigObject
		flattenConfig()

		assertEquals 'http:////s3.amazonaws.com/subbucket.bucket/123/', uiPerformanceService.getCdnPath()
	}

	void testGetCdnPathCloudFront() {
		uiPerformanceService.setCdnPath null
		config.uiperformance = [
			enabled: true,
			cdn: [
				enabled: true,
				s3: [
					enabled: true,
					bucketName: 'subbucket.bucket',
					domain: 's3.amazonaws.com'
				] as ConfigObject,
				cloudFront: [
					enabled: true,
					CName: 'cloudfront.myhome.com'
				] as ConfigObject
			] as ConfigObject
		] as ConfigObject
		flattenConfig()

		assertEquals 'http:////cloudfront.myhome.com/123/', uiPerformanceService.getCdnPath()
	}
}
