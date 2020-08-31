package k8ssetup

import (
	"errors"
	"fmt"
	"gopkg.in/yaml.v3"
	"io/ioutil"
	"log"
	"os"
	"regexp"
)

//noinspection GoStructTag
type DatabaseYml struct {
	Metadata struct {
		Name string `yaml: "name"`
	} `yaml: "metadata"`
}

func (k k8sSetUpImpl) isDatabaseCreated(cluster string) (bool, error) {
	log.Println("Checking if pet database is already created ...")
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
	log.Println("Check is database running ...")
	output, err := k.kubectl("get", "postgresql/"+cluster, "-o", "jsonpath={.status}")
	status := ""
	if err != nil {
		return false, err
	} else {
		var re = regexp.MustCompile(`(?m).*:(.*)]`)
		match := re.FindStringSubmatch(output)
		if len(match) > 1 {
			status = match[1]
		}
	}

	return status == "Running", nil
}

func (k k8sSetUpImpl) waitDatabaseCreation(cluster string) {
	cnt := true
	for cnt {
		running, err := k.isDatabaseRunning(cluster)
		cnt = !(err == nil && running)
	}
	log.Println("Database is running ...")
}

func (k *k8sSetUpImpl) CreationDatabase(fileName string) error {
	log.Println("Installing database ...")

	var cluster string
	var err error
	if cluster, err = k.getClusterName(fileName); err != nil {
		return fmt.Errorf("error getting cluster name from yaml file: %v", err)
	}

	if created, err := k.isDatabaseCreated(cluster); err == nil && created {
		return errors.New("database already exists")
	}

	if err = k.createDatabase(fileName); err == nil {
		log.Println("Pet database created ...")
	} else {
		return fmt.Errorf("error creating pet database: %v", err)
	}

	k.waitDatabaseCreation(cluster)

	return nil
}
