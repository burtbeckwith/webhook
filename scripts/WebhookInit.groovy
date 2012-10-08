import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")

includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")

USAGE = """
Usage: grails webhook-init <domain-class-package> <user-class-name>

Takes two arguments of package name and the spring-security user classname
and creates webhook domain, controller and views.

Example: grails webhook-init com.yourapp User
"""

packageName = ''
userClassName = ''
templateDir = "$webhookPluginDir/src/templates"
appDir = "$basedir/grails-app"

target('webhookInit': 'Creates artifacts for the Webhook plugin') {
	if (!configure()) {
		return 1
	}
	
	createDomains()
	copyControllersAndViews()
	updateURLMappings()
	updateConfig()
	
	printMessage """
	*************************************************************
	* SUCCESS! Created domain classes, controllers, and GSPs.   *
	* Webhook Plugin is now installed. Please see documentation *
	* page on implementation details.                           *
	*************************************************************
	"""
}

private boolean configure() {
	def argValues = parseArgs()
	if (!argValues) {
		return false
	}

	if (argValues.size() == 2) {
		(packageName, userClassName) = argValues
	}else {
		return false
	}

	templateAttributes = [packageName: packageName,userClassName: userClassName]

	true
}

private void createDomains() {
	String dir = packageToDir(packageName)
	generateFile "$templateDir/Webhook.groovy.template", "$appDir/domain/${dir}Webhook.groovy"
	printMessage "Domain created..."
}
	
private void copyControllersAndViews() {
	ant.mkdir dir: "$appDir/views/webhook"
	// add default views for webhooks administration
	copyFile "$templateDir/create.gsp.template", "$appDir/views/webhook/create.gsp"
	copyFile "$templateDir/edit.gsp.template", "$appDir/views/webhook/edit.gsp"
	copyFile "$templateDir/list.gsp.template", "$appDir/views/webhook/list.gsp"
	copyFile "$templateDir/show.gsp.template", "$appDir/views/webhook/show.gsp"
	
	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/WebhookController.groovy.template", "$appDir/controllers/${dir2}WebhookController.groovy"
	printMessage "Controller / Views created..."
}

private void updateURLMappings() {
	def mapFile = new File(appDir, 'conf/URLMappings.groovy')
	if (mapFile.exists()) {
		mapFile.withWriterAppend {
			it.writeLine '\n// Added by the Webhook plugin:'
			it.writeLine "\"/api/webhook/$format/$id\"(controller:'webhook',action:'api', parseRequest: true)"
			it.writeLine "\"/api/webhook/$format\"(controller:'webhook',action:'api', parseRequest: true)"
		}
	}
}

private void updateConfig() {
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Webhook plugin:'
			it.writeLine "webhook.attempts = 5"
			it.writeLine "webhook.services = []"
			it.writeLine "webhook.formats = ['JSON']"
			it.writeLine "webhook.domain = 'com.geezo.Webhook'"
			it.writeLine "webhook.controller = 'com.geezo.WebhookController'"
		}
	}
}

private parseArgs() {
	def args = argsMap.params

	if (2 == args.size()) {
		printMessage "Creating classes in package ${args[0]}..."
		return args
	}

	errorMessage USAGE
	null
}

setDefaultTarget('webhookInit')
