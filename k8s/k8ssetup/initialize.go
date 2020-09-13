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

func (k k8sSetUpImpl) findDockerRegistry() (string, error) {
	log.Print("Checking docker registry ...")
	registry := os.Getenv("DOCKER_REGISTRY")
	if registry == "" {
		return "", errors.New("error checking docker registry, variable DOCKER_REGISTRY does not exist")
	}
	if resp, err := http.Get(registry + "/v2/"); err != nil {
		return "", fmt.Errorf("error checking docker registry, %v", err)
	} else {
		//noinspection GoUnhandledErrorResult
		defer resp.Body.Close()
		if resp.StatusCode != http.StatusOK {
			return "", fmt.Errorf("error checking docker registry, status is %d", resp.StatusCode)
		}
	}
	log.Printf("Docker registry found at %q", registry)
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
		log.Printf("docker registry found in %s", dockerRegistry)
	} else {
		return fmt.Errorf("error checking docker registry: %v", err)
	}

	return nil
}
