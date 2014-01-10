package grails.plugin.uiperformance;

import grails.util.Environment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds 'Expires' and 'Cache-Control' headers plus handles necessary headers when
 * the resource is gzipped. Only applied when in enabled for the current environment.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class CacheFilter extends OncePerRequestFilter implements GrailsApplicationAware {

  private static final Log log = LogFactory.getLog(CacheFilter.class);

	protected static final List<String> DEFAULT_IMAGE_EXTENSIONS = Arrays.asList("gif", "jpg", "png", "ico");
	protected static final long SECONDS_IN_DAY = 60 * 60 * 24;
	protected static final long TEN_YEARS_SECONDS = SECONDS_IN_DAY * 365 * 10;
	protected static final long TEN_YEARS_MILLIS = TEN_YEARS_SECONDS * 1000;
	protected static final String MAX_AGE = "public, max-age=" + TEN_YEARS_SECONDS;
	protected static final Set<String> EXTENSIONS = new HashSet<String>(Arrays.asList("js", "css"));

	protected boolean processImages;
	protected boolean processCSS;
	protected boolean processJS;
	protected AntPathMatcher pathMatcher = new AntPathMatcher();
	protected List<String> exclusions;
	protected GrailsApplication grailsApplication;
  protected UiPerformanceService uiPerformanceService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

		String uri = request.getRequestURI();

    log.debug("Filtering request URI : " + uri);
		if (uiPerformanceService.isEnabled() && isCacheable(uri)) {
      log.debug("UI-Performance plugin is enabled and uri is cacheable");
			response.setDateHeader("Expires", System.currentTimeMillis() + TEN_YEARS_MILLIS);
			response.setHeader("Cache-Control", MAX_AGE);
			if (uri.endsWith(".gz.css") || uri.endsWith(".gz.js")) {
        log.debug("Set gzip content encoding");
        response.addHeader("Content-Encoding", "gzip");
				response.addHeader("Vary", "Accept-Encoding");
			}
		}

		chain.doFilter(request, response);
	}

	protected boolean isCacheable(final String uri) {

		if (!uri.contains("__v")) {
			return false;
		}

		if (isExcluded(uri)) {
			return false;
		}

		int index = uri.lastIndexOf('.');
		if (index == -1) {
			return false;
		}

		String extension = uri.substring(index + 1).toLowerCase();
		if (!EXTENSIONS.contains(extension)) {
			return false;
		}

		if (extension.equals("css")) {
			if (!processCSS) {
				return false;
			}
		}
		else if (extension.equals("js")) {
			if (!processJS) {
				return false;
			}
		}
		else if (!processImages) {
			return false;
		}

		return true;
	}

	protected boolean isExcluded(final String uri) {
		String testedUri = uri.startsWith("/") ? uri.substring(1) : uri;
		for (String pattern : exclusions) {
			if (pathMatcher.match(pattern, testedUri)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initFilterBean() throws ServletException {

		processImages = getConfigBoolean("processImages");
		processCSS = getConfigBoolean("processCSS");
		processJS = getConfigBoolean("processJS");

		initImageExtensions();
		findExclusions();
	}

	protected boolean getConfigBoolean(final String name) {
		Boolean value = (Boolean)getConfigProperty(name);
		return value == null ? true : value;
	}

	@SuppressWarnings("unchecked")
	protected void findExclusions() {
		exclusions = (List<String>)getConfigProperty("exclusions");
		if (exclusions == null) {
			exclusions = Collections.emptyList();
		}
	}

	protected Object getConfigProperty(final String name) {
		return grailsApplication.getFlatConfig().get("uiperformance." + name);
	}

	@SuppressWarnings("unchecked")
	protected void initImageExtensions() {
		List<String> imageExtensions = (List<String>)getConfigProperty("imageExtensions");
		if (imageExtensions == null) {
			imageExtensions = DEFAULT_IMAGE_EXTENSIONS;
		}
		EXTENSIONS.addAll(imageExtensions);
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware#setGrailsApplication(org.codehaus.groovy.grails.commons.GrailsApplication)
	 */
	public void setGrailsApplication(GrailsApplication app) {
		grailsApplication = app;
	}

  public void setUiPerformanceService(UiPerformanceService uiPerformanceService) {
    this.uiPerformanceService = uiPerformanceService;
  }
}
