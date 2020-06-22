/**
 * Set the content from the specified template id to the given node.
 * @param node the node to update
 * @param templateId the template id to load
 */
function setContentFromTemplate(node, templateId) {
	if (!templateId) {
		return;
	}
	var data = content.getTemplateContent(templateId);
	if (data && !node.setTextContent(data)) {
		// if the template was not set or the view was not found then just
		// set the template id as default template so the next loading the
		// template will be loaded
		node.getProperties().put('defaultTemplate', templateId);
	}
}