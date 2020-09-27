package k8ssetup

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"

	"github.com/go-git/go-git/v5"
)

// K8sSetUp is an interface that defines our steps
type K8sSetUp interface {
	Initialize() error
	InstallPostgresqlOperator() error
	CreationDatabase(fileName string) error
}

type k8sSetUpImpl struct {
	kubectlPath       string
	dockerPath        string
	dockerRegistry    string
	dockerRegistryK8s string
}

func (k k8sSetUpImpl) InstallPostgresqlOperator() error {
	log.Println("Installing PostgreSQL operator ...")

	if installed, err := k.isPostgreSQLOperatorInstalled(); err == nil {
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

func (k k8sSetUpImpl) isPostgreSQLOperatorInstalled() (bool, error) {
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
		err = fmt.Errorf("error '%v' in %q %q", err, cmdName, output)
	}

	return
}

// NewK8sSetUp returns a K8sSetUp interface
func NewK8sSetUp() K8sSetUp {
	return &k8sSetUpImpl{}
}
