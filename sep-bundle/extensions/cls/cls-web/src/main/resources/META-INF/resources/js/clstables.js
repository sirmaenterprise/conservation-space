$(function() {
	/*
	 * Adds an editable row in the table, meaning a normal row with all its
	 * fields' contentEnable property set to true and the extra column to remove
	 * the row visible. To be used only when editing the table !
	 */
	$(".addRow img").click(
			function() {
				var newInputId = CLS.descriptionIdCounter;
				var row = CLS.generateDescriptionTableRow("", "", "", true,
						false);
				var tableBody = $(this).parent().parent().parent().parent()
						.find("tbody");
				tableBody.append(row);
				
				var form = $('input[name="description_' + newInputId + '"]').closest("form");
				// Initialize form validator
				var validator = form.validate();
				validator.addRules('input[name="description_' + newInputId + '"]', { required: true });
				validator.addRules('input[name="language_' + newInputId + '"]', { required: true });
			});

	/*
	 * Live event that follows any .removeRow element in the codelist table and
	 * removes the entire row when clicked.
	 */
	$("tbody").on('click', 'tr td.removeRow img', function() {
		var form = $(this).closest("form");
		
		$(this).parent().parent().remove();
		
		// Initialize form validator
		var validator = form.validate();
		validator.silentValid();
	});

	/*
	 * Makes the currently focused cell in the codelist table glow. The cell
	 * must be editable!
	 */
	$("tbody").on('focus', 'tr td', function() {
		$(this).addClass('highlightedRow');
	});
	/*
	 * Activated when a td element in the codelist table is deselected. Removes
	 * the glow.
	 */
	$("tbody ").on('blur', 'tr td', function() {
		$(this).removeClass('highlightedRow');
	});

	$("tbody").on('blur', '#language', function() {
		var row = $(this).parent().parent();
		var language = $(this).prop("value");
		row.find("td input").each(function() {
			var id = $(this).prop("id");
			id = id.replace(new RegExp("_.*"), "_" + language);
			$(this).prop("id", id);
		});
	});
});

var CLS = CLS || {};
/**
 * Toggles the state of the table between editable and not editable. Editable -
 * all the tds in the table have their contentEnabled property set to true and
 * the div that enables the removal of the row is shown. Not editable - the tds
 * are not editable and the div in the end is not shown.
 * 
 * @param state
 *            the state of the table
 * @param table
 *            the table to be toggled
 */
CLS.toggleTable = function(state, table) {
	var tableObj = $(table);
	if (!state) {
		tableObj.find('.addRow').show();
	} else {
		tableObj.find('.addRow').hide();
	}
	tableObj.find("tbody tr").each(function() {
		$(this).find('td').each(function() {
			$(this).find('input').prop("readonly", state);
		})
		if (!state) {
			$(this).find(".removeRow").show();
		} else {
			$(this).find(".removeRow").hide();
		}
	});
}

CLS.generateDescriptionTableRow = function(description, comment, language,
		editable, required) {
	var readonly = "";
	var disabled = "";
	if (!editable) {
		readonly = "readonly";
	}
	if (required) {
		disabled = "disabled";
	}
	
	var descrId = CLS.descriptionIdCounter++;
	
	var requiredLanguageClass =  (language && language.length > 0)?"":"required-field";
	var requiredDescriptionClass =  (description && description.length > 0)?"":"required-field";
	
	var row = "<tr><td><input class='form-control input-sm " + requiredLanguageClass +  "' " + readonly + " "
			+ disabled + " id='language_" + descrId + "'"
			+ " name='language_" + descrId + "' value='" + language + "' /></td>"
			+ "<td><input class='form-control input-sm " + requiredDescriptionClass +  "' " + readonly
			+ " id='description_" + descrId + "' name='description_" + descrId 
			+ "' value='" + description
			+ "' /></td><td><input class='form-control input-sm' " + readonly
			+ " id='comment_" + language + "' value='" + comment + "' /></td>";
	if (!required) {
		if (editable) {
			row += "<td class='removeRow'><img class='removeImg' src='graphics/remove.png' /></td>";
		} else {
			row += "<td class='removeRow' style='display: none;'><img class='removeImg' src='graphics/remove.png' /></td>";
		}
	} else {
		if (editable) {
			row += "<td class='removeRow'></td>"
		} else {
			row += "<td class='removeRow' style='display: none;'></td>"
		}
	}
	row += "</tr>"
	return row;
}