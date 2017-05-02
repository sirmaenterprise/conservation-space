import {
  AnnotationListUpdatedEvent, AnnotationShapeCreatedEvent, AnnotationCreatedEvent, AnnotationUpdatedEvent,
  AnnotationDeletedEvent, AnnotationListLoadedEvent, WindowSlotAddedEvent, WindowRemovedEvent, WindowUpdatedEvent,
  MiradorChangeViewTypeEvent, RemoveAnnotationsDialogEvent, AnnotationEditModeEvent, UpdateMiradorAnnotationListEvent, AnnotationsRenderedEvent,
  SlotActivatedEvent
} from 'idoc/widget/image-widget/mirador-integration/mirador-events';

export const ANNOTATIONS_RENDERED = ['annotationsRendered', AnnotationsRenderedEvent];
export const ANNOTATIONS_LIST_UPDATED = ['ANNOTATIONS_LIST_UPDATED', AnnotationListUpdatedEvent];
export const ANNOTATION_SHAPE_CREATED = ['annotationShapeCreated', AnnotationShapeCreatedEvent];
export const ANNOTATION_CREATED = ['annotationCreated', AnnotationCreatedEvent];
export const ANNOTATION_UPDATED = ['annotationUpdated', AnnotationUpdatedEvent];
export const ANNOTATION_DELETED = ['annotationDeleted', AnnotationDeletedEvent];
export const ANNOTATION_LIST_LOADED = ['annotationListLoaded', AnnotationListLoadedEvent];
export const WINDOW_SLOT_ADDED = ['windowSlotAdded', WindowSlotAddedEvent];
export const WINDOW_REMOVED = ['windowRemoved', WindowRemovedEvent];
export const WINDOW_UPDATED = ['windowUpdated', WindowUpdatedEvent];
export const REMOVE_DIALOG = ['SET_STATE_MACHINE_POINTER', RemoveAnnotationsDialogEvent];
export const ANNOTATION_IN_EDIT_MODE = ['annotationInEditMode', AnnotationEditModeEvent];
export const UPDATE_ANNOTATION_LIST = ['updateAnnotationList', UpdateMiradorAnnotationListEvent];
export const SLOT_ACTIVATED = ['slotActivated', SlotActivatedEvent];

const CANCEL_ACTIVE_ANNOTATIONS = 'CANCEL_ACTIVE_ANNOTATIONS';

const IMAGE_VIEW = 'ImageView';
const ANNO_OFF = 'annoOff';
const ANNO_ON_CREATE_OFF = 'annoOnCreateOff';

export class MiradorEventsAdapter {

  constructor(eventbus, eventsEmitter, widgetId) {
    this.eventbusEvents = [];
    this.eventbus = eventbus;
    this.eventsEmitter = eventsEmitter;
    this.widgetId = widgetId;
    this.slotToImageId = new Map();
    this.subscribe();
  }

  destroy() {
    this.unsubscribeEventbusEvents();
  }

  unsubscribeEventbusEvents() {
    for (let event of this.eventbusEvents) {
      event.unsubscribe();
    }
  }

  /**
   * Subscribes for the all necessary Mirador events for every added slot in the given eventsEmitter system.
   * Then publishes them using the eventbus.
   * When the slot is removed it unsubscribes from the events.
   */
  subscribe() {
    this.eventsEmitter.subscribe(WINDOW_SLOT_ADDED[0], (event, options)=> {
      let slotId = options.id;

      this.eventsEmitter.subscribe(ANNOTATION_SHAPE_CREATED[0] + '.' + slotId, this.handleAnnoShapeCreated.bind(this));

      this.eventsEmitter.subscribe(ANNOTATION_IN_EDIT_MODE[0] + '.' + slotId, (event, id)=> {
        this.currentSlotId = slotId;
        this.publishEventbusEvent(ANNOTATION_IN_EDIT_MODE[1], id);
      });

      this.eventsEmitter.subscribe(ANNOTATION_LIST_LOADED[0] + '.' + slotId, (event, options)=> {
        this.publishEventbusEvent(ANNOTATION_LIST_LOADED[1], options);
      });

      this.eventsEmitter.subscribe(REMOVE_DIALOG[0] + '.' + slotId, ()=> {
        this.publishEventbusEvent(REMOVE_DIALOG[1]);
      });

      this.eventsEmitter.subscribe(CANCEL_ACTIVE_ANNOTATIONS + '.' + slotId, ()=> {
        this.publishEventbusEvent(REMOVE_DIALOG[1]);
      });

      this.eventsEmitter.subscribe(ANNOTATIONS_RENDERED[0] + '.' + slotId, ()=> {
        this.publishEventbusEvent(ANNOTATIONS_RENDERED[1], options);
      });

      this.eventsEmitter.subscribe(WINDOW_UPDATED[0] + '.' + slotId, this.handleWindowUpdatedEvent.bind(this));

      this.publishEventbusEvent(WINDOW_SLOT_ADDED[1], options);

      this.eventsEmitter.subscribe(SLOT_ACTIVATED[0] + '.' + slotId, (event, options)=> {
        this.currentSlotId = slotId;
        this.lastSelectedSlotView = options.view;
        this.publishEventbusEvent(SLOT_ACTIVATED[1], {
          id: slotId
        });
      });
    });

    this.eventsEmitter.subscribe(ANNOTATIONS_LIST_UPDATED[0], (event, options)=> {
      this.publishEventbusEvent(ANNOTATIONS_LIST_UPDATED[1], options);
    });

    //When a slot is removed unsubscribes from the events for it.
    this.eventsEmitter.subscribe(WINDOW_REMOVED[0], this.unsubscribeSlotEvents.bind(this));

    this.eventbusEvents.push(this.eventbus.subscribe(UPDATE_ANNOTATION_LIST[1], this.handleUpdateAnnotationListEvent.bind(this)));
  }

  unsubscribeSlotEvents(event, slotId) {
    this.slotToImageId.delete(slotId);
    this.eventsEmitter.unsubscribe(ANNOTATION_SHAPE_CREATED[0] + '.' + slotId);
    this.eventsEmitter.unsubscribe(ANNOTATION_LIST_LOADED[0] + '.' + slotId);
    this.eventsEmitter.unsubscribe(WINDOW_UPDATED[0] + '.' + slotId);
    this.eventsEmitter.unsubscribe(ANNOTATION_IN_EDIT_MODE[0] + '.' + slotId);
    this.eventsEmitter.unsubscribe(CANCEL_ACTIVE_ANNOTATIONS + '.' + slotId);
    this.eventsEmitter.unsubscribe(REMOVE_DIALOG[0] + '.' + slotId);
    this.publishEventbusEvent(WINDOW_REMOVED[1], slotId);
  }

  handleUpdateAnnotationListEvent(event) {
    if (event[0] === this.widgetId) {
      this.publishMiradorEvent(UPDATE_ANNOTATION_LIST[0] + '.' + this.getCurrentSlotId());
    }
  }

  handleWindowUpdatedEvent(event, options) {
    if (this.currentSlotId !== options.id) {
      this.currentSlotId = options.id;
      this.publishEventbusEvent(WindowUpdatedEvent, options);
      return;
    }
    if (options.annotationState === ANNO_OFF || options.annotationState === ANNO_ON_CREATE_OFF) {
      this.publishEventbusEvent(RemoveAnnotationsDialogEvent);
    }
    if (options.viewType && this.lastSelectedSlotView !== options.viewType) {
      this.lastSelectedSlotView = options.viewType;
      this.publishEventbusEvent(MiradorChangeViewTypeEvent);
      this.publishEventbusEvent(WindowUpdatedEvent, options);
    }

    if (options.canvasID && this.slotToImageId.get(this.currentSlotId) !== options.canvasID) {
      this.slotToImageId.set(options.id, options.canvasID);
      this.publishEventbusEvent(RemoveAnnotationsDialogEvent);
    }
  }

  handleAnnoShapeCreated(event, options) {
    if (this.currentSlotId !== options.windowId) {
      this.currentSlotId = options.windowId;
      this.publishEventbusEvent(WindowUpdatedEvent, options);
    }
    this.imageView = true;
    this.publishEventbusEvent(ANNOTATION_SHAPE_CREATED[1], options);
  }

  /**
   * Publishes the given event via the eventbus system.
   *
   * @param event the event
   * @param options the options
   */
  publishEventbusEvent(event, options) {
    this.eventbus.publish(new event(this.widgetId, options));
  }

  /**
   * Publishes the event to the mirador's jquery publish/subscribe system.
   *
   * @param event the event's name
   * @param options the options of the event
   * if it is annotationCreated the options represent an array with the [annotation, the last shape|null]
   */
  publishMiradorEvent(event, options) {
    this.eventsEmitter.publish(event, options);
  }

  getCurrentImageId() {
    return this.getImageIdBySlot(this.currentSlotId);
  }

  getImageIdBySlot(slotId){
    return this.slotToImageId.get(slotId);
  }

  getCurrentSlotId() {
    return this.currentSlotId;
  }

  isLastSelectedSlotImageView() {
    return this.lastSelectedSlotView === IMAGE_VIEW;
  }
}