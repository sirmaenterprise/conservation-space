package proxy

import (
	"time"

	"golang.org/x/net/context"

	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/filters"
	log "github.com/sirupsen/logrus"
)

// Watch listens for docker events on services and publishes them to events.
func Watch(events *Eventbus) {
	ctx := context.Background()
	dockerEvents, errs := cli.Events(ctx, types.EventsOptions{
		Filters: filters.NewArgs(filters.Arg("type", "service"), filters.Arg("since", time.Now().String())),
	})

	for {
		select {
		case event := <-dockerEvents:
			name := EventServiceModified
			if event.Action == "remove" {
				name = EventServiceRemoved
			}

			events.Publish(&Event{
				Name:        name,
				DockerEvent: event,
			})
		case err := <-errs:
			log.WithError(err).Fatal("unable to listen on docker events")
		}
	}
}
