package net.nosegrind.webhook

import grails.converters.JSON
import grails.converters.XML

class WebhookService {

	def grailsApplication

    static transactional = false

    def postToURL(String service, Map data, String state) {
		// set attempts number in config.properties so we can override
		def hooks = grailsApplication.getClassForName(grailsApplication.config.webhook.domain).findAll("from Webhook where service='${service}' and attempts<5")
		hooks.each { hook ->
			try{
				String hookData
				switch(hook.format.toLowerCase()){
					case 'xml':
						hookData = (data as XML).toString()
						break
					case 'json':
					default:
						hookData = (data as JSON).toString()
						break
				}

				def conn = hook.url.toURL().openConnection()
				conn.setRequestMethod("POST")
				conn.doOutput = true
				def queryString = []
				queryString << "state=${state}&data=${hookData}"
				def writer = new OutputStreamWriter(conn.outputStream)
				writer.write(queryString)
				writer.flush()
				writer.close()
				conn.connect()
				println("hookData = ${hookData}")
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

	Map formatData(Object data){
	    def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass"]
	    def newMap = [:]
	    data.getProperties().each { property ->
	        if (!nonPersistent.contains(property.key)) {
	            newMap.put property.key, property.value
	        }
	    }
		return newMap
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
