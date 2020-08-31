package k8ssetup

import (
	"bytes"
	"fmt"
	"github.com/go-git/go-git/v5"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
)

type K8sSetUp interface {
	Initialize() error
	InstallPostgresqlOperator() error
	CreationDatabase(fileName string) error
}

type k8sSetUpImpl struct {
	kubectlPath string
	dockerPath  string
}

func (k k8sSetUpImpl) InstallPostgresqlOperator() error {
	log.Println("Installing PostgreSQL operator ...")

	if installed, err := k.isPostgreSqlOperatorInstalled(); err == nil {
		if !installed {
			log.Println("PostgreSQL operator not installed ...")
			if err = k.installPsqlOperator(); err != nil {
				return fmt.Errorf("error installing PostgreSQL operator: %v", err)
			}
		} else {
			log.Println("PostgreSQL operator is installed ...")
		}

	} else {
		return fmt.Errorf("error checking operator installation: %v", err)
	}

	return nil
}

func (k k8sSetUpImpl) isPostgreSqlOperatorInstalled() (bool, error) {
	log.Println("Checking if postgresql operator is already installed ...")
	if _, err := k.kubectl("describe", "service/postgres-operator"); err != nil {
		return false, err
	}

	return true, nil
}

func (k *k8sSetUpImpl) installPsqlOperator() error {
	log.Println("Installing postgreSQL operator ...")
	dir, err := ioutil.TempDir("", "pets-go-infra")
	if err != nil {
		return fmt.Errorf("error creating temp dir: %v", err)
	}
	//noinspection GoUnhandledErrorResult
	defer os.RemoveAll(dir)
	log.Printf("Created temp dir %s ...", dir)

	if _, err = git.PlainClone(dir, false, &git.CloneOptions{
		URL:      "https://github.com/zalando/postgres-operator.git",
		Progress: os.Stdout,
	}); err != nil {
		return fmt.Errorf("error clonning postgres operator: %v", err)
	}
	log.Println("Zalando postgresSQL operator repo cloned ...")

	var files = []string{
		"manifests/configmap.yaml",
		"manifests/operator-service-account-rbac.yaml",
		"manifests/postgres-operator.yaml",
		"manifests/api-service.yaml",
	}
	for _, v := range files {
		log.Printf("Creating %q", v)
		if _, err := k.kubectl("create", "-f", filepath.Join(dir, v)); err != nil {
			return fmt.Errorf("error in kubectl: %v", err)
		}
	}

	return nil
}

func (k k8sSetUpImpl) kubectl(params ...string) (output string, err error) {
	return k.executeCommand(k.kubectlPath, params...)
}

func (k k8sSetUpImpl) docker(params ...string) (output string, err error) {
	return k.executeCommand(k.dockerPath, params...)
}

func (k k8sSetUpImpl) executeCommand(cmdName string, params ...string) (output string, err error) {
	cmd := exec.Command(cmdName, params...)

	var stdBuffer bytes.Buffer
	cmd.Stdout = &stdBuffer
	cmd.Stderr = &stdBuffer

	err = cmd.Run()
	output = stdBuffer.String()
	if err != nil {
		err = fmt.Errorf("error in %q %q", cmdName, output)
	}

	return
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

	return nil
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

func NewK8sSetUp() K8sSetUp {
	return &k8sSetUpImpl{}
}
