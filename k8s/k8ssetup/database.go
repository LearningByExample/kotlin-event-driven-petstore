package k8ssetup

import (
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"strings"

	"gopkg.in/yaml.v2"
)

// DatabaseYml is used to read a yml file in order to get the cluster name
//noinspection GoStructTag
type DatabaseYml struct {
	Metadata struct {
		Name string `yaml: "name"`
	} `yaml: "metadata"`
}

func (k k8sSetUpImpl) isDatabaseCreated(cluster string) (bool, error) {
	return k.isResourceCreated("postgresql", cluster, "default")
}

func (k k8sSetUpImpl) createDatabase(fileName string) error {
	log.Println("Installing database ...")
	_, err := k.kubectl("create", "-f", fileName)
	return err
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
				if data.Metadata.Name == "" {
					err = errors.New("no cluster name found")
				}
				clusterName = data.Metadata.Name
			}
		}
	}

	return
}

func (k k8sSetUpImpl) isDatabaseRunning(cluster string) (bool, error) {
	log.Printf("Checking if database cluster %q is already running ...", cluster)
	output, err := k.kubectl("get", "postgresql/"+cluster, "-o", "jsonpath={.status}")
	if err != nil {
		return false, err
	}

	return strings.Contains(output, "Running"), nil
}

func (k k8sSetUpImpl) waitDatabaseCreation(cluster string) {
	cnt := true
	for cnt {
		running, err := k.isDatabaseRunning(cluster)
		cnt = !(err == nil && running)
	}
	log.Printf("Database cluster %q is running", cluster)
}

func (k *k8sSetUpImpl) DatabaseCreation(fileName string) error {
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
