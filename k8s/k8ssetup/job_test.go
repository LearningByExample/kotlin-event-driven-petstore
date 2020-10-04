package k8ssetup

import (
	"errors"
	"io/ioutil"
	"os"
	"strings"
	"testing"
)

func Test_dockerBuild(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return no error if docker build works", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}

		var expect error = nil
		got := k8sImpl.dockerBuild(getFilePath("Dockerfile-cluster-job"), "1")

		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("must return error if dockerfile does not exist", func(t *testing.T) {
		expect := "does not exist"
		got := k8sImpl.dockerBuild(getFilePath("dockerfile-not-existing"), "1")

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("must return no error if docker build fails", func(t *testing.T) {
		var invalidErr = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", invalidErr
		}

		expect := "error creating docker image"
		got := k8sImpl.dockerBuild(getFilePath("Dockerfile-cluster-job"), "1")

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})
}

func Test_dockerPush(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return no error if docker push works", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}
		var expect error = nil
		got := k8sImpl.dockerPush("1")

		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("must return no error if docker push fails", func(t *testing.T) {
		var invalidErr = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", invalidErr
		}
		expect := "error pushing docker image"
		got := k8sImpl.dockerPush("1")

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})
}

func Test_createK8sJob(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	k8sImpl.dockerRegistryK8s = "http://localhost:8081"

	t.Run("must create job when no errors", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if content, err := ioutil.ReadFile(params[2]); err != nil {
				t.Fatalf("Error reading file %q", params[2])
			} else {
				expect := "variable: localhost:8081/job"
				got := string(content)
				if got != expect {
					t.Fatalf("Got %q, expect %q", got, expect)
				} else {
					return "", nil
				}
			}

			return "", errors.New("error")
		}

		var expect error = nil
		got := k8sImpl.createK8sJob(getFilePath("cluster-job.yml"))

		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return error when file does not exist", func(t *testing.T) {
		expect := "error reading file"
		got := k8sImpl.createK8sJob("not-existing.yml")

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("must eturn error when job creation fails", func(t *testing.T) {
		invalidErr := errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", invalidErr
		}

		expect := "error creating job in kubectl"
		got := k8sImpl.createK8sJob(getFilePath("cluster-job.yml"))

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_createDatabaseJob(t *testing.T) {
	// setup
	wd, _ := os.Getwd()
	os.Chdir("_test")
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("we could create the database without errors", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}

		var expect error = nil
		got := k8sImpl.createDatabaseJob("cluster")
		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("we should error when docker build error", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "build" {
				return "", errors.New("error on docker build")
			}
			return "", nil
		}

		got := k8sImpl.createDatabaseJob("cluster")
		if got == nil {
			t.Fatal("Got nil, expect error")
		}
	})

	t.Run("we should error when docker push error", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "push" {
				return "", errors.New("error on docker push")
			}
			return "", nil
		}

		got := k8sImpl.createDatabaseJob("cluster")
		if got == nil {
			t.Fatal("Got nil, expect error")
		}
	})

	t.Run("we should error when kubectl create error", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "create" {
				return "", errors.New("error on kubeclt create")
			}
			return "", nil
		}

		got := k8sImpl.createDatabaseJob("cluster")
		if got == nil {
			t.Fatal("Got nil, expect error")
		}
	})

	//tear down
	os.Chdir(wd)
}
