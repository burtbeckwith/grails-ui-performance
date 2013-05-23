package grails.plugin.uiperformance

import grails.util.Environment

import java.awt.Dimension

import javax.imageio.ImageIO
import javax.imageio.ImageReader

import org.springframework.beans.factory.InitializingBean
import org.springframework.util.AntPathMatcher

/**
 * Utility methods.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class UiPerformanceService implements InitializingBean {

	protected AntPathMatcher exclusionMatcher = new AntPathMatcher()

	protected String applicationVersion
	protected List exclusions
	protected String cdnPath
	protected Properties properties

	public static final List<String> DEFAULT_IMAGE_EXTENSIONS = ['gif', 'jpg', 'png', 'ico']

  @Lazy
  volatile boolean enabled = {
    grailsApplication.getFlatConfig().containsKey("uiperformance.enabled") ?
      grailsApplication.getFlatConfig().get("uiperformance.enabled") :
      Environment.PRODUCTION == Environment.getCurrent()
  }()

  def grailsApplication
	def resourceVersionHelper

	static transactional = false

	void afterPropertiesSet() {
		def stream = Thread.currentThread().contextClassLoader.getResourceAsStream('uiperformance.properties')
		if (stream) {
			properties = new Properties()
			properties.load(stream)
			applicationVersion = properties.getProperty('version')
		}

		def value = grailsApplication.config?.uiperformance?.exclusions
		if (value instanceof List) {
			exclusions = value
		}
		if (!exclusions) {
			exclusions = []
		}
		exclusions << '**/CVS'
		exclusions << '**/.svn'
	}

	/**
	 * Add in '__vXXX' where XXX is the current application version, unless the url is excluded
	 * by a configuration rule.
	 * @param url  the url
	 * @return  the url with version embedded
	 */
 	String addVersion(String url) {

 		if (!isEnabled() || isExcluded(url)) {
 			return url
 		}

 		int index = url.lastIndexOf('.')
		return url.substring(0, index) + '__v' + getApplicationVersion() + url.substring(index)
 	}

	/**
	 * Get the current application version.
	 */
	String getApplicationVersion() {
		if (applicationVersion == null) {
			String basePath = new File('').absolutePath
			applicationVersion = resourceVersionHelper.determineVersion(basePath, grailsApplication.config)
		}

		isEnabled() ? applicationVersion : ''
	}

 	/**
 	 * Check if the path is excluded by a configuration rule.
 	 * @param relativePath  the path
 	 * @return <code>true</code> if the path matches one of the rules
 	 */
	boolean isExcluded(String relativePath) {
		relativePath = fixRelativePath(relativePath)

		for (pattern in exclusions) {
			if (exclusionMatcher.match(pattern, relativePath)) {
				return true
			}
		}

		return false
	}

	protected String fixRelativePath(String relativePath) {
		int index = relativePath.indexOf('staging/')
		if (index == -1) {
			index = relativePath.indexOf('stage/')
		}

		if (index > -1) {
			index = relativePath.indexOf('/', index)
			relativePath = relativePath.substring(index + 1)
		}

		return relativePath.startsWith('/') ? relativePath.substring(1) : relativePath
	}

	/**
	 * Check if a given file src is included in a sprite bundle.
	 * @param src  the image src
	 * @return true if it's in a bundle
	 */
	boolean isIncludedInSprite(String src) {
		for (entry in properties?.entrySet()) {
			if (entry.key.startsWith('sprite-')) {
				for (path in entry.value.split(',')) {
					if (src.endsWith(path) || path.endsWith(src)) {
						return true
					}
				}
			}
		}
		return false
	}

	/**
	 * Calculate an image's width and height.
	 * @param file  the image file
	 * @return  the dimensions
	 */
	Dimension calculateImageDimension(File file) {

		String extension = file.name.substring(file.name.lastIndexOf('.') + 1)

		def readers = ImageIO.getImageReadersBySuffix(extension)
		ImageReader reader = null
		if (readers.hasNext()) {
			reader = readers.next()
		}

		if (!reader) {
			return null
		}

		try {
			byte[] bytes = file.readBytes()
			reader.setInput ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)), true
			return new Dimension(reader.getWidth(0), reader.getHeight(0))
		}
		catch (IOException e) {
			e.printStackTrace()
			return null
		}
	}

	boolean getConfigBoolean(String name, boolean defaultIfMissing = true) {
		def value = grailsApplication.config.uiperformance[name]
		return value instanceof Boolean ? value : defaultIfMissing
	}

	def getConfigValue(String name, defaultIfMissing = null) {
		def value = grailsApplication.config.uiperformance[name]
		return value ?: defaultIfMissing
	}

  boolean isEnabled() {
    getEnabled()
  }

	boolean cdnIsEnabled() {
		def cdn = grailsApplication.config.uiperformance.cdn
		cdn.enabled || cdn.s3.enabled || cdn.cloudFront.enabled
	}

	String getCdnPath() {
		if (!cdnPath) {
			if (cdnIsEnabled()) {
				def cdn = grailsApplication.config.uiperformance.cdn
				if (cdn.s3.enabled || cdn.cloudFront.enabled) {
					if (cdn.cloudFront.enabled) {
						String cname = cdn.cloudFront.CName ?: 'ERROR : uiperformance.cdn.cloudFront.CName not set'
						cdnPath = "http://$cname/"
					}
					else {
						String bucketName = cdn.s3.bucketName ?: 'ERROR : uiperformance.cdn.s3.bucketname not set'
						String domain = cdn.s3.domain ?: 'ERROR : uiperformance.cdn.s3.domain not set'
//						cdnPath = "http://${bucketName}.$domain/"
						cdnPath = "http://$domain/$bucketName/"
					}
					cdnPath += "${getApplicationVersion()}/"
				}
				else {
					cdnPath = cdn.location ?: 'ERROR : uiperformance.cdn.location not set'
				}

				if (!cdnPath.endsWith('/')) {
					cdnPath += '/'
				}
			}
		}

		cdnPath
	}

	void setCdnPath(String path) {
		cdnPath = path
	}
}
