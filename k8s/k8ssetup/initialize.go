package k8ssetup

import (
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
)

func (k k8sSetUpImpl) findDockerRegistryK8s() (string, error) {
	log.Print("Checking K8s docker registry ...")
	registry := os.Getenv("DOCKER_REGISTRY_K8S")
	if registry == "" {
		return "", errors.New("error checking K8s docker registry, variable DOCKER_REGISTRY_K8S does not exist")
	}
	return registry, nil
}

func (k k8sSetUpImpl) findDockerRegistry() (string, error) {
	log.Print("Checking docker registry ...")
	registry := os.Getenv("DOCKER_REGISTRY")
	if registry == "" {
		return "", errors.New("error checking docker registry, variable DOCKER_REGISTRY does not exist")
	}
	var resp *http.Response
	var err error
	if resp, err = http.Get(registry + "/v2/"); err != nil {
		return "", fmt.Errorf("error checking docker registry, %v", err)
	}
	//noinspection GoUnhandledErrorResult
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("error checking docker registry, status is %d", resp.StatusCode)
	}

	return registry, nil
}

func (k *k8sSetUpImpl) findKubectlPath() (string, error) {
	return k.findCommandPath("kubectl")
}

func (k *k8sSetUpImpl) findDockerPath() (string, error) {
	return k.findCommandPath("docker")
}

func (k *k8sSetUpImpl) findCommandPath(cmdName string) (string, error) {
	path := os.Getenv("PATH")
	sep := ":"
	if runtime.GOOS == "windows" {
		sep = ";"
	}
	for _, v := range strings.Split(path, sep) {
		cmdPath := filepath.Join(v, cmdName)
		if file, err := os.Open(cmdPath); err == nil {
			_ = file.Close()
			return cmdPath, nil
		}
	}
	return "", fmt.Errorf("not %q path found", cmdName)
}

func (k *k8sSetUpImpl) Initialize() error {
	if kubectlPath, err := k.findKubectlPath(); err == nil {
		k.kubectlPath = kubectlPath
		log.Printf("Kubectl found in %s", kubectlPath)
	} else {
		return fmt.Errorf("error getting kubectl path: %v", err)
	}

	if dockerPath, err := k.findDockerPath(); err == nil {
		k.dockerPath = dockerPath
		log.Printf("docker found in %s", dockerPath)
	} else {
		return fmt.Errorf("error getting docker path: %v", err)
	}

	if dockerRegistry, err := k.findDockerRegistry(); err == nil {
		k.dockerRegistry = dockerRegistry
		log.Printf("Docker registry found at %s", dockerRegistry)
	} else {
		return fmt.Errorf("error checking docker registry: %v", err)
	}

	if dockerRegistryK8s, err := k.findDockerRegistryK8s(); err == nil {
		k.dockerRegistryK8s = dockerRegistryK8s
		log.Printf("K8s docker registry found at %s", dockerRegistryK8s)
	} else {
		return fmt.Errorf("error checking K8s docker registry: %v", err)
	}

	return nil
}
