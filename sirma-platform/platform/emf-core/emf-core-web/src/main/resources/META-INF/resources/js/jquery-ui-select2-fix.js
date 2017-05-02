/**
 * Fixes a problem with ui.dialog and select2 controls. The method is moved into a separate class in
 * order to solve the problem with drop-down menus which did not disappear after click out of the
 * menu or select another action.
 *
 * @see http://stackoverflow.com/questions/16966002/select2-plugin-works-fine-when-not-inside-a-jquery-modal-dialog
 */
(function() {
	$.ui.dialog.prototype._allowInteraction = function(e) {
		var result = !!$(e.target).closest('.ui-dialog, .ui-datepicker, .select2-input, .select2-chosen').length;
		if ($(e.target).closest('.select2-input, .select2-chosen').length === 0) {
			$('div.select2-dropdown-open').select2('close');
		}
		return result;
	};
}());