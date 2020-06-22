package com.sirma.sep.content.idoc.extensions.widgets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.handler.RevertContentNodeHandler;
import com.sirma.sep.content.idoc.handler.VersionContentNodeHandler;

/**
 * Contains default methods for simple revert of widgets in instance view content. <br>
 * The handler will restore the original configuration of the widget by using the stored diff when the widget was
 * versioned. All of the information that is stored by the {@link VersionContentNodeHandler}s will be removed.
 *
 * @param <W>
 *            the type of the widget
 * @author A. Kunchev
 */
public abstract class AbstractWidgetRevertHandler<W extends Widget> implements RevertContentNodeHandler<W> {

	@Override
	public HandlerResult handle(W node, HandlerContext context) {
		WidgetConfiguration configuration = node.getConfiguration();
		if (!configuration.getConfiguration().has(AbstractWidgetVersionHandler.ORIGINAL_CONFIGURATION_DIFF)) {
			return new HandlerResult(node);
		}

		JsonObject originlaConfigurationDiff = configuration
				.getConfiguration()
					.remove(AbstractWidgetVersionHandler.ORIGINAL_CONFIGURATION_DIFF)
					.getAsJsonObject();

		JsonElement originalSelectedMode = originlaConfigurationDiff.get("selectObjectMode");
		if (originalSelectedMode != null && WidgetSelectionMode.AUTOMATICALLY
				.equals(WidgetSelectionMode.getMode(originalSelectedMode.getAsString()))) {
			// remove all selected objects added from the version properties
			configuration.cleanUpAllSelectedObjects();
		}

		removeAdditionalChangesIfAny(configuration, originlaConfigurationDiff);
		originlaConfigurationDiff.entrySet().forEach(e -> configuration.addNotNullProperty(e.getKey(), e.getValue()));
		return new HandlerResult(node);
	}

	/**
	 * Removes the additional changes done by the handles. This changes are added to the diff explicitly and should be
	 * removed from the widget configuration when restoring it back to the original state. We remove the whole element
	 * from the diff to ensure that it would not be detected as change and put in to the original configuration when the
	 * other changes are transferred back.
	 */
	private static void removeAdditionalChangesIfAny(WidgetConfiguration configuration,
			JsonObject originlaConfigurationDiff) {
		if (originlaConfigurationDiff.has(AbstractWidgetVersionHandler.ADDITIONAL_CHANGES_DONE_BY_HANDLERS)) {
			originlaConfigurationDiff
					.remove(AbstractWidgetVersionHandler.ADDITIONAL_CHANGES_DONE_BY_HANDLERS)
						.getAsJsonArray()
						.forEach(element -> configuration.removeProperty(element.getAsString()));
		}
	}

}
