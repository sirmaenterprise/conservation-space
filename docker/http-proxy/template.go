package proxy

import (
	"io"
	"os"
	"strings"
	"text/template"
)

var funcs template.FuncMap = template.FuncMap{
	"Env":     env,
	"StrList": strList,
}

// ExecTemplate parses and executes tmpl using name.
// The result is written to out and data is used for template variables.
// Returns error if the template could not be parsed or if an error occurs durring processing.
func ExecTemplate(name, tmpl string, data interface{}, out io.Writer) error {
	t, err := template.New(name).Funcs(funcs).Parse(tmpl)
	if err != nil {
		return err
	}
	return t.Execute(out, data)
}

// env returns the value of an environment variable name.
// Returns empty string if the variable is not set.
func env(name string) string {
	return os.Getenv(name)
}

// srtList splits string s using a single space as a separator.
// Empty strings are not added to the list.
func strList(s string) []string {
	var list []string
	for _, s := range strings.Split(s, " ") {
		if s == "" {
			continue
		}

		list = append(list, s)
	}
	return list
}
