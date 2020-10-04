package k8ssetup

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path"
	"strings"
)

func (k k8sSetUpImpl) dockerBuild(dockerFile string, tag string) error {
	if file, err := os.Open(dockerFile); err == nil {
		file.Close()
		if _, err := k.docker("build", "..", "-f", dockerFile, "-t", tag); err != nil {
			return fmt.Errorf("error creating docker image with tag %q, %v", tag, err)
		}

	} else {
		return fmt.Errorf("the file %q does not exist", dockerFile)
	}
	return nil
}

func (k k8sSetUpImpl) dockerPush(tag string) error {
	if _, err := k.docker("push", tag); err != nil {
		return fmt.Errorf("error pushing docker image with tag %q, %v", tag, err)
	}
	return nil
}

func (k k8sSetUpImpl) createK8sJob(fileName string) error {
	if content, err := ioutil.ReadFile(fileName); err == nil {
		registryK8s := strings.Replace(k.dockerRegistryK8s, "https://", "", 1)
		registryK8s = strings.Replace(registryK8s, "http://", "", 1)

		newContent := strings.Replace(string(content), "$DOCKER_REGISTRY_K8S", registryK8s, 1)
		_, onlyFileName := path.Split(fileName)
		if newFile, err := ioutil.TempFile("", onlyFileName); err == nil {
			defer newFile.Close()
			if _, err := newFile.WriteString(newContent); err != nil {
				return fmt.Errorf("error writting in temp file %q", newFile.Name())
			}
			if _, err := k.kubectl("create", "-f", newFile.Name()); err != nil {
				return fmt.Errorf("error creating job in kubectl, %v", err)
			}

		} else {
			return fmt.Errorf("error creating temp file for %q", fileName)
		}

	} else {
		return fmt.Errorf("error reading file %q", fileName)
	}
	return nil
}

func (k k8sSetUpImpl) createDatabaseJob(cluster string) error {
	label := cluster + "-job"

	registry := strings.Replace(k.dockerRegistry, "https://", "", 1)
	registry = strings.Replace(registry, "http://", "", 1)
	tag := registry + "/" + label

	if err := k.dockerBuild("Dockerfile-"+label, tag); err == nil {
		log.Printf("Database job image created with label %q ...", label)
	} else {
		return err
	}

	if err := k.dockerPush(tag); err == nil {
		log.Printf("Database job image pushed with label %q ...", label)
	} else {
		return err
	}

	fileName := label + ".yml"
	if err := k.createK8sJob(fileName); err == nil {
		log.Printf("K8s database job created from file %q ...", fileName)
	} else {
		return err
	}
	return nil
}
