grails.project.work.dir = 'target'
grails.project.source.level = 1.6
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		runtime 'com.yahoo.platform.yui:yuicompressor:2.4.7', {
			excludes 'js'
		}
		runtime 'rhino:js:1.7R2'

		runtime 'com.carrotsearch:smartsprites:0.2.8', {
			excludes 'ant', 'args4j', 'commons-io', 'commons-lang', 'commons-math', 'fest-assert', 'google-collections', 'junit'
		}
		runtime 'com.google.collections:google-collections:1.0', {
			excludes 'jsr305'
		}
		runtime 'net.sourceforge.pjl-comp-filter:pjl-comp-filter:1.7', {
			excludes 'commons-logging', 'servlet-api'
		}

		build 'net.java.dev.jets3t:jets3t:0.9.0', {
			excludes 'commons-codec', 'commons-logging', 'httpclient', 'httpcore', 'java-xmlbuilder'
		}
		build 'com.jamesmurty.utils:java-xmlbuilder:0.4', {
			excludes 'junit'
		}
	}

	plugins {
    build ':release:3.0.1', {
      export = false
    }

    compile ':webxml:1.4.1'
	}
}
