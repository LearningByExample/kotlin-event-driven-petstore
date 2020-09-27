package k8ssetup

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"regexp"

	"gopkg.in/yaml.v3"
)

// DatabaseYml is used to read a yml file in order to get the cluster name
//noinspection GoStructTag
type DatabaseYml struct {
	Metadata struct {
		Name string `yaml: "name"`
	} `yaml: "metadata"`
}

func (k k8sSetUpImpl) isDatabaseCreated(cluster string) (bool, error) {
	log.Printf("Checking if database cluster %q is already created ...", cluster)
	if _, err := k.kubectl("describe", "postgresql/"+cluster); err != nil {
		return false, err
	}

	return true, nil
}

func (k k8sSetUpImpl) createDatabase(fileName string) error {
	log.Println("Installing database ...")
	if _, err := k.kubectl("create", "-f", fileName); err != nil {
		return err
	}

	return nil
}

func (k k8sSetUpImpl) getClusterName(fileName string) (clusterName string, err error) {
	var yamlFile *os.File
	if yamlFile, err = os.Open(fileName); err == nil {
		//noinspection GoUnhandledErrorResult
		defer yamlFile.Close()
		var yamlBytes []byte
		if yamlBytes, err = ioutil.ReadAll(yamlFile); err == nil {
			data := DatabaseYml{}
			if err = yaml.Unmarshal(yamlBytes, &data); err == nil {
				clusterName = data.Metadata.Name
			}
		}
	}

	return
}

func (k k8sSetUpImpl) isDatabaseRunning(cluster string) (bool, error) {
	log.Printf("Checking if database cluster %q is already running ...", cluster)
	output, err := k.kubectl("get", "postgresql/"+cluster, "-o", "jsonpath={.status}")
	status := ""
	if err != nil {
		return false, err
	}

	var re = regexp.MustCompile(`(?m).*:(.*)]`)
	match := re.FindStringSubmatch(output)
	if len(match) > 1 {
		status = match[1]
	}

	return status == "Running", nil
}

func (k k8sSetUpImpl) waitDatabaseCreation(cluster string) {
	cnt := true
	for cnt {
		running, err := k.isDatabaseRunning(cluster)
		cnt = !(err == nil && running)
	}
	log.Printf("Database cluster %q is running", cluster)
}

func (k *k8sSetUpImpl) CreationDatabase(fileName string) error {
	log.Printf("Creating database from file %q ...", fileName)

	var cluster string
	var err error
	if cluster, err = k.getClusterName(fileName); err != nil {
		return fmt.Errorf("error getting cluster name from yaml file: %v", err)
	}

	if created, err := k.isDatabaseCreated(cluster); err == nil && created {
		return fmt.Errorf("database cluster %q already exists", cluster)
	}

	if err = k.createDatabase(fileName); err == nil {
		log.Printf("Database cluster %q created ...", cluster)
	} else {
		return fmt.Errorf("error creating database cluster %q: %v", cluster, err)
	}

	k.waitDatabaseCreation(cluster)

	if err = k.createDatabaseJob(cluster); err == nil {
		log.Printf("Database job created for cluster %q...", cluster)
	} else {
		return fmt.Errorf("error creating job for cluster %q: %v", cluster, err)
	}
	return nil
}
