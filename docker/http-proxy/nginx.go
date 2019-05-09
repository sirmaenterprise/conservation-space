package proxy

import (
	"bytes"
	"io/ioutil"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"
	"time"

	log "github.com/sirupsen/logrus"
)

var lock = sync.RWMutex{}

type NginxConfigurator struct {
	DestConfig string
	Servers    map[string]*Server
}

type Server struct {
	ID        string
	Template  string
	Locations map[string]*Location
}

func (s Server) ToConfig() string {
	buf := new(bytes.Buffer)
	if err := ExecTemplate("server-"+s.ID, s.Template, s, buf); err != nil {
		log.WithError(err).WithField("server", s.ID).Error("unable to generate server config")
	}
	return string(buf.Bytes())
}

type Location struct {
	ID        string
	Template  string
	Match     string
	ProxyPass string
	Service   *ProxiedService
	Vars      map[string]string
}

func (l Location) ToConfig() string {
	buf := new(bytes.Buffer)
	if err := ExecTemplate("location-"+l.ID, l.Template, l, buf); err != nil {
		log.WithError(err).WithField("location", l.ID).Error("unable to generate location config")
	}
	return string(buf.Bytes())
}

func (n *NginxConfigurator) Generate(e *Event) {
	lock.Lock()
	defer lock.Unlock()

	var hasUnreachable bool
	services, err := ListServices(Config.Stacks)
	if err != nil {
		log.WithError(err).Error("unable to retrieve list of docker services")
		return
	}

	for _, server := range n.Servers {
		server.Locations = make(map[string]*Location)
	}

	for _, service := range services {
		// server_template_id:location_template_id:match:proxy_pass
		for key, def := range service.Env {
			if !strings.HasPrefix(key, "PROXY_SERVICE_DEF_") {
				continue
			}

			if !service.Reachable() {
				hasUnreachable = true
				continue
			}

			split := strings.SplitN(def, ":", 4)

			var data []byte
			data, err = ioutil.ReadFile(filepath.Join(Config.Templates.Locations, split[1]+".conf"))
			if err != nil {
				log.WithError(err).WithField("proxy_service_def", def).Error("unable to read location template")
				continue
			}

			server := n.Servers[split[0]]
			server.Locations[def] = &Location{
				ID:        def,
				Match:     split[2],
				ProxyPass: split[3],
				Template:  string(data),
				Service:   &service,
			}
		}
	}

	if hasUnreachable {
		time.AfterFunc(30*time.Second, func() {
			log.WithField("after", "30s").Debug("there are unreachable services - will retry")
			n.Generate(nil)
		})
	}

	buf := new(bytes.Buffer)
	if err = ExecTemplate("nginx-root-template", "{{range $key, $server := .Servers}}{{.ToConfig}}{{end}}", n, buf); err != nil {
		log.WithError(err).Error("unable to generate proxy configuration")
		return
	}

	if err = os.MkdirAll(filepath.Dir(Config.DestConfig), 0755); err != nil {
		log.WithError(err).Error("unable to create directory for destination config")
		return
	}
	file, err := os.OpenFile(Config.DestConfig, os.O_RDWR|os.O_CREATE|os.O_TRUNC, 0666)
	if err != nil {
		log.WithError(err).Error("unable to write destination configuration")
		return
	}
	defer file.Close()

	if _, err = file.Write(buf.Bytes()); err != nil {
		return
	}

	if err = file.Sync(); err != nil {
		log.WithError(err).Error("unable to sync configuration to disk")
	}

	cmd := exec.Command("nginx", "-s", "reload")
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Run()
}

// NewNginxConfigurator creates and initializes a new NginxConfigurator.
func NewNginxConfigurator() (*NginxConfigurator, error) {
	servers := make(map[string]*Server)
	for _, s := range Config.Servers {
		data, err := ioutil.ReadFile(filepath.Join(Config.Templates.Servers, s+".conf"))
		if err != nil {
			return nil, err
		}

		servers[s] = &Server{
			ID:       s,
			Template: string(data),
		}
	}

	return &NginxConfigurator{
		Servers:    servers,
		DestConfig: Config.DestConfig,
	}, nil
}

func loadTemplates(dest map[string]string, path string) error {
	infos, err := ioutil.ReadDir(path)
	if err != nil {
		return err
	}

	for _, info := range infos {
		name := info.Name()
		data, err := ioutil.ReadFile(filepath.Join(path, name))
		if err != nil {
			return err
		}

		dest[strings.TrimSuffix(name, filepath.Ext(name))] = string(data)
	}
	return nil
}
