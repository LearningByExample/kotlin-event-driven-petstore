package k8ssetup

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/go-git/go-git/v5"
)

// K8sSetUp is an interface that defines our steps
type K8sSetUp interface {
	Initialize() error
	InstallPostgresqlOperator() error
	DatabaseCreation(fileName string) error
	InstallKafkaOperator() error
}

type k8sSetUpImpl struct {
	kubectlPath       string
	dockerPath        string
	dockerRegistry    string
	dockerRegistryK8s string
	psqlOperatorRepo  string
	executeCommand    func(cmdName string, params ...string) (string, error)
}

const (
	zalandoPsqlOperator = "https://github.com/zalando/postgres-operator.git"
)

func (k k8sSetUpImpl) InstallPostgresqlOperator() error {
	log.Println("Installing PostgreSQL operator ...")

	if installed := k.isPostgreSQLOperatorInstalled(); !installed {
		log.Println("PostgreSQL operator not installed ...")
		if err := k.doPsqlOperatorInstallation(); err != nil {
			return fmt.Errorf("error installing PostgreSQL operator: %v", err)
		}
		k.waitPsqlOperatorRunning()

	} else {
		log.Println("PostgreSQL operator is installed ...")
	}

	return nil
}

func (k k8sSetUpImpl) InstallKafkaOperator() error {
	log.Println("Installing Kafka operator ...")

	if installed := k.isKafkaOperatorInstalled(); !installed {
		log.Println("Kafka operator not installed ...")
		if err := k.doKafkaOperatorInstallation(); err != nil {
			return fmt.Errorf("error installing Kafka operator: %v", err)
		}
		k.waiKafkaOperatorRunning()

	} else {
		log.Println("Kafka operator is installed ...")
	}

	return nil
}

func (k k8sSetUpImpl) waitPsqlOperatorRunning() {
	cnt := true
	for cnt {
		running, err := k.isPsqlOperatorRunning()
		cnt = !(err == nil && running)
	}
	log.Print("Psql operator is running")
}

func (k k8sSetUpImpl) waiKafkaOperatorRunning() {
	cnt := true
	for cnt {
		ready, err := k.isKafkaOperatorRunning()
		cnt = !(err == nil && ready)
	}
	log.Print("Kafka operator is running")
}

func (k k8sSetUpImpl) isOperatorRunning(name, namespace string) (running bool, err error) {
	log.Printf("Checking if %s operator is already running ...", name)

	var podNames, output string
	if podNames, err = k.kubectl("get", "pod", "-o", "name", "-n", namespace); err == nil {
		for _, podName := range strings.Split(podNames, "\n") {
			if strings.Contains(podName, name) {
				if output, err = k.kubectl("get", podName, "-o", "jsonpath='{.status.containerStatuses[0].ready}'", "-n", namespace); err != nil {
					return false, err
				}
				running = output == "'true'"
				break
			}
		}
	}
	return
}

func (k k8sSetUpImpl) isPsqlOperatorRunning() (bool, error) {
	return k.isOperatorRunning("postgres-operator", "default")
}

func (k k8sSetUpImpl) isKafkaOperatorRunning() (bool, error) {
	return k.isOperatorRunning("strimzi-cluster-operator", "kafka")
}

func (k k8sSetUpImpl) isPostgreSQLOperatorInstalled() bool {
	log.Println("Checking if postgresql operator is already installed ...")
	if _, err := k.kubectl("describe", "service/postgres-operator"); err != nil {
		return false
	}

	return true
}

func (k k8sSetUpImpl) isKafkaOperatorInstalled() bool {
	log.Println("Checking if kafka operator is already installed ...")
	if _, err := k.kubectl("describe", "deployment.apps/strimzi-cluster-operator", "-n", "kafka"); err != nil {
		return false
	}

	return true
}

func (k *k8sSetUpImpl) doPsqlOperatorInstallation() error {
	log.Println("Installing postgreSQL operator ...")
	dir, err := ioutil.TempDir("", "pets-go-infra")
	if err != nil {
		return fmt.Errorf("error creating temp dir: %v", err)
	}
	//noinspection GoUnhandledErrorResult
	defer os.RemoveAll(dir)
	log.Printf("Created temp dir %s ...", dir)

	if _, err = git.PlainClone(dir, false, &git.CloneOptions{
		URL:      k.psqlOperatorRepo,
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

func (k *k8sSetUpImpl) doKafkaOperatorInstallation() error {
	log.Println("Installing kafka operator ...")
	if _, err := k.kubectl("create", "namespace", "kafka"); err != nil {
		return fmt.Errorf("error in kubectl create: %v", err)
	}
	if _, err := k.kubectl("apply", "-f", "https://strimzi.io/install/latest?namespace=kafka", "-n", "kafka"); err != nil {
		return fmt.Errorf("error in kubectl apply: %v", err)
	}

	return nil
}

func (k k8sSetUpImpl) kubectl(params ...string) (output string, err error) {
	return k.executeCommand(k.kubectlPath, params...)
}

func (k k8sSetUpImpl) docker(params ...string) (output string, err error) {
	return k.executeCommand(k.dockerPath, params...)
}

func (k k8sSetUpImpl) defaultExecuteCommand(cmdName string, params ...string) (output string, err error) {
	cmd := exec.Command(cmdName, params...)

	var stdBuffer bytes.Buffer
	cmd.Stdout = &stdBuffer
	cmd.Stderr = &stdBuffer

	err = cmd.Run()
	output = stdBuffer.String()
	log.Println(output)
	if err != nil {
		err = fmt.Errorf("error '%v' in %q %q", err, cmdName, output)
	}
	return
}

// NewK8sSetUp returns a K8sSetUp interface
func NewK8sSetUp() K8sSetUp {
	impl := &k8sSetUpImpl{
		psqlOperatorRepo: zalandoPsqlOperator,
	}
	impl.executeCommand = impl.defaultExecuteCommand

	return impl
}
