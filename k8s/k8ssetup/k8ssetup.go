package k8ssetup

import (
	"bytes"
	"errors"
	"fmt"
	"github.com/go-git/go-git/v5"
	"io"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strings"
)

type K8sSetUp interface {
	Initialize() error
	InstallDatabase() error
}

type k8sSetUpImpl struct {
	kubectlPath string
}

func (k k8sSetUpImpl) isPostgreSqlOperatorInstalled() (bool, error) {
	log.Println("Checking if postgresql operator is already installed ...")
	if _, err := k.kubectlCommand("describe", "service/postgres-operator"); err != nil {
		return false, err
	}

	return true, nil
}

func (k k8sSetUpImpl) createDatabase() error {
	log.Println("Installing database ...")
	if _, err := k.kubectlCommand("create", "-f", "pets-db.yml"); err != nil {
		return err
	}

	return nil
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
		if _, err := k.kubectlCommand("create", "-f", filepath.Join(dir, v)); err != nil {
			return fmt.Errorf("error in kubectl: %v", err)
		}
	}

	return nil
}

func (k k8sSetUpImpl) kubectlCommand(params ...string) (output string, err error) {
	cmd := exec.Command(k.kubectlPath, params...)

	var stdBuffer bytes.Buffer
	mw := io.MultiWriter(os.Stdout, &stdBuffer)
	cmd.Stdout = mw
	cmd.Stderr = mw

	err = cmd.Run()
	output = stdBuffer.String()
	if err != nil {
		err = fmt.Errorf("error in kubeclt %q", output)
	}

	return
}

func (k k8sSetUpImpl) isDatabaseRunning() (bool, error) {
	log.Println("Check is database running ...")
	output, err := k.kubectlCommand("get", "postgresql/petstore-pets-cluster", "-o", "jsonpath={.status}")
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

func (k *k8sSetUpImpl) InstallDatabase() error {
	log.Println("Installing database ...")

	if installed, err := k.isPostgreSqlOperatorInstalled(); err == nil {
		if !installed {
			log.Println("PostgreSQL operator not installed ...")
			if err = k.installPsqlOperator(); err != nil {
				return fmt.Errorf("error installing operator: %v", err)
			}
		} else {
			log.Println("PostgreSQL operator is installed ...")
		}

		if err := k.createDatabase(); err == nil {
			log.Println("Pet database created ...")
		} else {
			return fmt.Errorf("error creating pet database: %v", err)
		}

		cnt := true
		for cnt {
			running, err := k.isDatabaseRunning()
			cnt = !(err == nil && running)
		}
		log.Println("Database is running ...")

	} else {
		return fmt.Errorf("error checking zalando installation: %v", err)
	}

	return nil
}

func (k *k8sSetUpImpl) Initialize() error {
	if kubectlPath, err := k.findKubectlPath(); err == nil {
		k.kubectlPath = kubectlPath
		log.Printf("Kubectl found in %s", kubectlPath)
	} else {
		return fmt.Errorf("error getting kubectl path: %v", err)
	}

	return nil
}

func (k *k8sSetUpImpl) findKubectlPath() (string, error) {
	path := os.Getenv("PATH")
	sep := ":"
	if runtime.GOOS == "windows" {
		sep = ";"
	}
	for _, v := range strings.Split(path, sep) {
		kubectlPath := filepath.Join(v, "kubectl")
		if file, err := os.Open(kubectlPath); err == nil {
			_ = file.Close()
			return kubectlPath, nil
		}
	}
	return "", errors.New("not kubectl path found")
}

func NewK8sSetUp() K8sSetUp {
	return &k8sSetUpImpl{}
}
