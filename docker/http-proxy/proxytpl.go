package main

import (
	"io/ioutil"
	"log"
	"os"
	"path"
	"strings"
	"text/template"

	"gopkg.in/yaml.v2"
)

func main() {
	if len(os.Args) != 3 {
		log.Fatalln("usage: proxytpl <path to template file> <path to variables directory>")
	}

	tplfile := os.Args[1]
	vars := merge(os.Args[2])

	tpldata, err := ioutil.ReadFile(tplfile)
	if err != nil {
		log.Fatalf("unable to read template (%s): %s", tplfile, err.Error())
	}

	tmpl, err := template.New("nginx proxy template").Parse(string(tpldata))
	if err != nil {
		log.Fatalf("unable to parse template: %s", err.Error())
	}

	err = tmpl.Execute(os.Stdout, vars)
	if err != nil {
		log.Fatalf("unable to execute template: %s", err.Error())
	}
}

func merge(dir string) map[string]interface{} {
	infos, err := ioutil.ReadDir(dir)
	if err != nil {
		log.Fatalf("unable to read vars dir (%s): %s", dir, err.Error())
	}

	dest := make(map[string]interface{})
	for _, info := range infos {

		if info.IsDir() || (!strings.HasSuffix(info.Name(), ".yaml") && !strings.HasSuffix(info.Name(), ".yml")) {
			continue
		}

		file := path.Join(dir, info.Name())
		log.Printf("reading variables from %s", file)
		data, err := ioutil.ReadFile(file)
		if err != nil {
			log.Fatalf("unable to read vars file (%s): %s", file, err.Error())
		}

		src := make(map[string]interface{})
		if err = yaml.Unmarshal(data, src); err != nil {
			log.Fatalf("unable to parse yaml file (%s): %s", file, err.Error())
		}

		for k, v := range src {
			dest[k] = v
		}
	}
	return dest
}
