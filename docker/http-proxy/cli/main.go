package main

import (
	log "github.com/sirupsen/logrus"

	"git.sirmaplatform.com/stash/seip/http-proxy"
)

func main() {
	var err error
	var nginx *proxy.NginxConfigurator

	if nginx, err = proxy.NewNginxConfigurator(); err != nil {
		log.WithError(err).Fatal("unable to create nginx handler")
	}

	events := proxy.NewEventbus()

	events.Subscribe(proxy.EventServiceModified, nginx.Generate)
	events.Subscribe(proxy.EventServiceRemoved, nginx.Generate)
	go proxy.Watch(events)

	nginx.Generate(nil)

	// wait until SIGINT is sent
	events.Wait()
}
