package proxy

import (
	"os"

	"github.com/docker/docker/client"
	log "github.com/sirupsen/logrus"
)

const (
	// DefaultDockerAPIVersion is the default api version to use if DOCKER_API_VERSION environment variable is not set.
	DefaultDockerAPIVersion = "1.37"
)

var cli *client.Client

// DockerClient creates a new docker client.
// If the DOCKER_API_VERSION is not set the values of DefaultDockerAPIVersion is used.
func DockerClient() (*client.Client, error) {
	if _, ok := os.LookupEnv("DOCKER_API_VERSION"); !ok {
		os.Setenv("DOCKER_API_VERSION", DefaultDockerAPIVersion)
	}
	return client.NewClientWithOpts(client.FromEnv)
}

func init() {
	var err error
	if cli, err = DockerClient(); err != nil {
		log.WithError(err).Fatal("unable to create docker client")
	}
}
