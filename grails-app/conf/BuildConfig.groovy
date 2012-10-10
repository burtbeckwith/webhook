grails.project.work.dir = 'target'
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	plugins {
		compile ':spring-security-core:1.2.7.3'

		build(':release:2.0.4', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
