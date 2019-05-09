package proxy

import (
	"fmt"
	"strings"

	"golang.org/x/net/context"

	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/swarm"
)

// ListServices lists all services in the given stacks.
// If stacks is empty - all services are returned.
func ListServices(stacks []string) (proxied map[string]ProxiedService, err error) {
	var services []swarm.Service
	ctx := context.Background()

	if services, err = cli.ServiceList(ctx, types.ServiceListOptions{}); err != nil {
		return
	}

	proxied = make(map[string]ProxiedService)
	for _, service := range services {
		env := proxyEnv(service.Spec.TaskTemplate.ContainerSpec.Env)
		if !keepService(stacks, &service) || len(env) == 0 {
			continue
		}

		stack := service.Spec.Labels["com.docker.stack.namespace"]
		proxied[service.Spec.Name] = ProxiedService{
			Stack: stack,
			Name:  strings.TrimPrefix(service.Spec.Name, fmt.Sprintf("%s_", stack)),
			Env:   env,
		}
	}
	return
}

func proxyEnv(envVars []string) map[string]string {
	vars := make(map[string]string)
	for _, env := range envVars {
		if !strings.HasPrefix(env, "PROXY_") {
			continue
		}

		split := strings.SplitN(env, "=", 2)
		key := split[0]
		var val string
		if len(split) == 2 {
			val = split[1]
		}
		vars[key] = val
	}
	return vars
}

func keepService(stacks []string, s *swarm.Service) bool {
	if stacks == nil || len(stacks) == 0 {
		return true
	}

	ns, ok := s.Spec.Labels["com.docker.stack.namespace"]
	if !ok {
		return false
	}

	for _, stack := range stacks {
		if ns == stack {
			return true
		}
	}
	return false
}
