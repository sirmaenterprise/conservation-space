package proxy

import (
	"net"
	"net/url"
	"os"
	"os/signal"
	"strings"

	"github.com/docker/docker/api/types/events"
	log "github.com/sirupsen/logrus"
)

const (
	EventServiceModified = "service-mod"
	EventServiceRemoved  = "service-removed"
)

// Event represents an event fired from the utility.
type Event struct {
	Name        string
	DockerEvent events.Message
}

// EventHandler is the function signature of an event handler.
type EventHandler func(event *Event)

// Eventbus is responsible for dispatching events to the subscribed handler and for firing events.
type Eventbus struct {
	quit     chan os.Signal
	events   chan *Event
	handlers map[string][]EventHandler
}

// Subscribe adds a new handler to the list of handler for event.
func (e *Eventbus) Subscribe(event string, handler EventHandler) {
	h := e.handlers[event]
	h = append(h, handler)
	e.handlers[event] = h
}

// Publish notifies all subscribed event handlers for the occured event.
func (e *Eventbus) Publish(event *Event) {
	e.events <- event
}

func (e *Eventbus) dispatch() {
	for {
		event := <-e.events
		h := e.handlers[event.Name]
		if h == nil || len(h) == 0 {
			return
		}

		for _, handler := range h {
			handler(event)
		}
	}
}

// Wait blocks until a shutdown event is received.
func (e *Eventbus) Wait() {
	signal.Notify(e.quit, os.Interrupt)
	<-e.quit
	log.Info("received interrupt signal - bye bye")
}

// ProxiedService holds the information to create a proxy configuration for the docker service.
type ProxiedService struct {
	Stack  string
	Name   string
	TypeID string

	// Env holds only the PROXY_* prefixed environment variables of the service
	Env map[string]string
}

func (p *ProxiedService) Reachable() bool {
	var def string
	for key, val := range p.Env {
		if strings.HasPrefix(key, "PROXY_SERVICE_DEF_") {
			def = val
			break
		}
	}

	split := strings.SplitN(def, ":", 4)
	if len(split) != 4 {
		return false
	}

	serviceURL, err := url.Parse(split[3])
	if err != nil {
		log.WithError(err).WithFields(log.Fields{
			"definition": def,
			"service":    p.Name,
			"stack":      p.Stack,
		}).Error("unable to parse url from service definition")
		return false
	}

	addr := serviceURL.Host
	if serviceURL.Port() == "" {
		if serviceURL.Scheme == "https" {
			addr += ":443"
		} else {
			addr += ":80"
		}
	}
	conn, err := net.Dial("tcp", addr)
	if err != nil {
		log.WithError(err).WithFields(log.Fields{
			"definition": def,
			"address":    addr,
			"service":    p.Name,
			"stack":      p.Stack,
		}).Error("service unreachable")
		return false
	}
	conn.Close()
	return true
}

// NewEventbus creates a new Eventbus instance.
func NewEventbus() *Eventbus {
	bus := &Eventbus{
		quit:     make(chan os.Signal),
		events:   make(chan *Event),
		handlers: make(map[string][]EventHandler),
	}
	go bus.dispatch()

	return bus
}
