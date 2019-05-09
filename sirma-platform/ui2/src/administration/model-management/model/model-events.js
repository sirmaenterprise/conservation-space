/**
 * A collection of model events. Used to communicate events
 * between different model components.
 *
 * @author Svetlozar Iliev
 */
export class ModelEvents {
}

/**
 * Event triggered when a model is changed from one to another. Most likely
 * due to user look up inside the model tree or direct URI model change
 */
ModelEvents.MODEL_CHANGED_EVENT = 'ModelChangedEvent';
/**
 * Event triggered when a model attribute is changed. This event is exclusively
 * triggered only when an attribute belonging to a given model is changed. Change
 * is considered every action which modifies the value of the attributes
 */
ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT = 'ModelAttributeChangedEvent';
/**
 * Event triggered when the state of a model is changed from dirty
 * to not dirty or back to dirty again. Ideally this event should
 * be triggered only two times when the state of the model toggles
 * between the states: dirty and clean and from clean to dirty
 */
ModelEvents.MODEL_STATE_CHANGED_EVENT = 'ModelStateChangedEvent';