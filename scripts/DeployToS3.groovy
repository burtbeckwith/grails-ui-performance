import grails.util.BuildSettings
import groovy.time.TimeCategory

import org.jets3t.service.S3Service
import org.jets3t.service.security.AWSCredentials
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Bucket
import org.jets3t.service.model.S3Object
import org.jets3t.service.multithread.S3ServiceSimpleMulti
import org.jets3t.service.acl.GroupGrantee
import org.jets3t.service.acl.Permission
import org.jets3t.service.acl.AccessControlList
import org.jets3t.service.CloudFrontService
import org.jets3t.service.model.cloudfront.Distribution

import org.apache.commons.io.FilenameUtils

includeTargets << grailsScript('_GrailsBootstrap')

target(deployToS3: 'Uploads processed resources to S3') {
	depends(bootstrap)

	String className = 'grails.plugin.uiperformance.ResourceVersionHelper'
	def helper = Class.forName(className, true, classLoader).newInstance()

	// copy resources to temp dir
	File tempDir = File.createTempFile('uiPerformance', '')
	tempDir.delete()
	tempDir.mkdirs()
	ant.copy(todir: tempDir) {
		fileset dir: new File(basedir, 'web-app')
	}

	// need to make a WEB-INF/classes directory or the helper will fail when trying to create the uiperformance.properties file
	File classesDir = new File(tempDir.absolutePath, '/WEB-INF/classes')
	classesDir.mkdirs()

	// version resources
	helper.version tempDir, basedir, config // TODO load config

	String version = helper.determineVersion(basedir, config)

	// upload to s3
	S3Helper.deployToS3 config.uiperformance.cdn, tempDir, version
}

setDefaultTarget deployToS3

// helper class for Amazon s3 functionality
private class S3Helper {

	// keeps track of all uploaded objects
	private static filesToUpload = []

	static deployToS3(cdnConfig, deployDir, version) {
		if (!cdnConfig.s3.enabled) {
			return
		}

		println 'uiPerformance : starting to deploy to Amazon CDN'

		String awsAccessKey = cdnConfig.s3.accessKey
		String awsSecretKey = cdnConfig.s3.secretKey
		String bucketName = cdnConfig.s3.bucketName

		AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey)
		S3Service s3Service = new RestS3Service(awsCredentials)
		S3Bucket bucket = s3Service.getOrCreateBucket(bucketName)
		S3ServiceSimpleMulti simpleMulti = new S3ServiceSimpleMulti(s3Service)

		// make the bucket publicly accessible
		AccessControlList bucketAcl = s3Service.getBucketAcl(bucket)
		bucketAcl.grantPermission GroupGrantee.ALL_USERS, Permission.PERMISSION_READ
		bucket.setAcl bucketAcl
		s3Service.putBucketAcl bucket

		new File(deployDir.absolutePath).eachFile { topDir ->
			if (topDir.name == 'js' || topDir.name == 'css' || topDir.name == 'images') {
				addToBucket topDir, deployDir, version, bucketAcl
			}
		}

		println "uiPerformance : uploading $filesToUpload.size files"

		simpleMulti.putObjects bucket, filesToUpload as S3Object[]

		println 'uiPerformance : deployment to Amazon CDN finished'

		if (!cdnConfig.cloudFront.enabled) {
			return
		}

		println 'uiPerformance : checking CloudFront distribution'

		CloudFrontService cloudFrontService = new CloudFrontService(awsCredentials)
		Distribution[] bucketDistributions = cloudFrontService.listDistributions(bucketName)

		boolean distributionExists = false
		String cloudFrontCName = cdnConfig.cloudFront.CName as String

		for (int i = 0; i < bucketDistributions.length; i++) {
			for (cname in bucketDistributions[i].CNAMEs) {
				if (cname == cloudFrontCName) {
					println 'uiPerformance : CloudFront distribution found - domain name is '
					println "${bucketDistributions[i].domainName}"
					println 'uiPerformance : CloudFront deployment done'
					distributionExists = true
				}
			}
		}

		if (!distributionExists) {
			println "uiPerformance : CloudFront distribution does not exist for CNAME '$cloudFrontCName', creating"

			Distribution newDistribution = cloudFrontService.createDistribution(
				bucketName,
				System.currentTimeMillis().toString(), // Caller reference - a unique string value
				[cloudFrontCName] as String[], // CNAME aliases for distribution
				'Grails Ui Performance Assets', // Comment
				true,  // Distribution is enabled?
				null  // Logging status of distribution (null means disabled)
			)

			println 'uiPerformance : CloudFront distribution created - domain name is'
			println "${bucketDistributions[i].domainName}"
			println 'uiPerformance : CloudFront deployment done'
		}
	}

	static addToBucket(file, directoryRoot, version, accessControl) {

		Date now = new Date()

		if (file.isDirectory()) {
			file.eachFile { childFile ->
				addToBucket childFile, directoryRoot, version, accessControl
			}
		}
		else {
			String path = file.absolutePath - (directoryRoot.absolutePath + File.separator)
			path = path.replaceAll('\\\\', '/')
			S3Object s3file = new S3Object(file)
			s3file.setKey(version + '/' + path)
			s3file.setAcl(accessControl)

			// set metadata
			s3file.setLastModifiedDate(now)

			use([TimeCategory]) {
				s3file.addMetadata 'Expires', now + 10.years
			}

			switch (FilenameUtils.getExtension(file.name).toLowerCase()) {
				case 'jpg': s3file.setContentType('image/jpeg');      break
				case 'png': s3file.setContentType('image/png');       break
				case 'gif': s3file.setContentType('image/gif');       break
				case 'css': s3file.setContentType('text/css');        break
				case 'js':  s3file.setContentType('text/javascript'); break
			}

			if (file.name.contains('.gz.')) {
				s3file.setContentEncoding('gzip')
			}

			filesToUpload << s3file
		}
	}
}
