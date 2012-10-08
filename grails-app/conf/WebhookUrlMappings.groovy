class WebhookUrlMappings {

	static mappings = {
		"/api/webhook/$format/$id" (controller:'webhook',action:'api', parseRequest: true)
		"/api/webhook/$format" (controller:'webhook',action:'api', parseRequest: true)
	}
}
