package net.nosegrind.webhook

import grails.converters.JSON

class WebhookService {

	def xmlService
	def jsonService
	def grailsApplication
	
    static transactional = false
    static scope = "prototype"

    def postToURL(String service, String data, String state) { 
		// set attempts number in config.properties so we can override
		def hooks = grailsApplication.getClassForName(grailsApplication.config.webhook.domain).findAll("from Webhook where service='${service}' and attempts<5")
		//def hooks = Webhook.findAll("from Webhook where service='${service}' and attempts<${grailsApplication.config.webhook.attempts}")
		hooks.each { hook ->
			try{
				def conn = hook.url.toURL().openConnection()
				conn.setRequestMethod("POST")
				conn.doOutput = true
				def queryString = []
				queryString << "state=${state}&data=${data}"
				def writer = new OutputStreamWriter(conn.outputStream)
				writer.write(queryString)
				writer.flush()
				writer.close()
				conn.connect()
				if(conn.content.text!='connected'){
					hook.attempts+=1
					hook.save(flush: true)
				}
			}catch(Exception e){
				hook.attempts+=1
				hook.save(flush: true)
				log.info("[Webhook] WebhookService: No Url ${hook.url} found :"+e)
			}
		}
	}

	boolean checkProtocol(String url){
		if(url.size()>=4){
			if(url[0..3]=='http'){
				return true
			}else{
				return false
			}
		}else{
			return false
		}
	}
}
