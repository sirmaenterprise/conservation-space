package proxy

import (
	"bytes"
	"flag"
	"io/ioutil"

	log "github.com/sirupsen/logrus"
	"gopkg.in/yaml.v2"
)

var Config HttpProxyConfig

type HttpProxyConfig struct {
	LogLevel   string            `yaml:"log_level"`
	Stacks     []string          `yaml:"stacks"`
	DestConfig string            `yaml:"dest_config"`
	Servers    []string          `yaml:"servers"`
	Templates  Templates         `yaml:"templates"`
	Vars       map[string]string `yaml:"vars"`
}

type Templates struct {
	Servers   string `yaml:"servers"`
	Locations string `yaml:"locations"`
}

func (h *HttpProxyConfig) ServerEnabled(server string) bool {
	for _, s := range h.Servers {
		if s == server {
			return true
		}
	}
	return false
}

func init() {
	var config string

	flag.StringVar(&config, "config", "./http-proxy.yaml", "Path to the http-proxy configuration file. Default: ./http-proxy.yaml")
	flag.Parse()

	var err error
	var data []byte
	data, err = ioutil.ReadFile(config)
	if err != nil {
		log.WithError(err).WithField("file", config).Fatal("unable to read configuration file")
	}

	out := new(bytes.Buffer)
	if err = ExecTemplate("http-proxy-config", string(data), nil, out); err != nil {
		log.WithError(err).WithField("file", config).Fatal("unable to execute configuration template")
	}
	if err = yaml.Unmarshal(out.Bytes(), &Config); err != nil {
		log.WithError(err).WithField("file", config).Fatal("unable to parse configuration file")
	}

	switch Config.LogLevel {
	case "TRACE":
		log.SetLevel(log.TraceLevel)
	case "DEBUG":
		log.SetLevel(log.DebugLevel)
	case "WARN":
		log.SetLevel(log.WarnLevel)
	case "ERROR":
		log.SetLevel(log.ErrorLevel)
	case "FATAL":
		log.SetLevel(log.FatalLevel)
	case "INFO":
		log.SetLevel(log.InfoLevel)
	default:
		log.SetLevel(log.ErrorLevel)
	}
}
