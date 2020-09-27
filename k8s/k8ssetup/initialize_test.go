package k8ssetup

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

const (
	testPathVar        = "TESTPATH"
	testRegistryVar    = "TESTREGISTRY"
	testK8SRegistryVar = "TESTREGISTRYK8S"
	testFolder         = "_test"
	existingCommand    = "test.sh"
	notExistingCommand = "test_does_not_exist.sh"
	existingHost       = "https://google.com"
	notExistingHost    = "https://not-existing-host.com"
	existingValue      = "value"
)

func setEnvVar() (path string) {
	pathVar = testPathVar
	path, _ = os.Getwd()
	path = filepath.Dir(path)
	path = filepath.Join(path, testFolder)
	os.Setenv(pathVar, path)
	return
}

func Test_findCommandPath(t *testing.T) {
	path := setEnvVar()
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the command", func(t *testing.T) {
		expect := filepath.Join(path, existingCommand)
		got, gotErr := k8sImpl.findCommandPath(existingCommand)
		os.Unsetenv(pathVar)
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the command", func(t *testing.T) {
		expect := ""
		expectErr := fmt.Errorf("not %q path found", notExistingCommand)
		got, gotErr := k8sImpl.findCommandPath(notExistingCommand)
		os.Unsetenv(pathVar)
		if gotErr.Error() != expectErr.Error() {
			t.Fatalf("Got error %v, expect %v error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func setUpTestFindKubectlPath(existing bool) (path string) {
	if existing {
		kubectlCommand = existingCommand
	} else {
		kubectlCommand = notExistingCommand
	}
	path = setEnvVar()
	return
}

func tearDown() {
	os.Unsetenv(pathVar)
	os.Unsetenv(dockerRegistryVar)
	os.Unsetenv(dockerRegistryK8sVar)
}

func Test_findKubectlPath(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the kubectl command", func(t *testing.T) {
		path := setUpTestFindKubectlPath(true)
		expect := filepath.Join(path, existingCommand)
		got, gotErr := k8sImpl.findKubectlPath()
		tearDown()
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the kubectl command", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(false)
		expect := ""
		expectErr := fmt.Errorf("not %q path found", notExistingCommand)
		got, gotErr := k8sImpl.findKubectlPath()
		tearDown()
		if gotErr.Error() != expectErr.Error() {
			t.Fatalf("Got error %v, expect %v error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func setUpTestFindDockerPath(existing bool) (path string) {
	if existing {
		dockerCommand = existingCommand
	} else {
		dockerCommand = notExistingCommand
	}
	path = setEnvVar()
	return
}

func Test_findDockerPath(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the kubectl command", func(t *testing.T) {
		path := setUpTestFindDockerPath(true)
		expect := filepath.Join(path, existingCommand)
		got, gotErr := k8sImpl.findDockerPath()
		tearDown()
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the kubectl command", func(t *testing.T) {
		_ = setUpTestFindDockerPath(false)
		expect := ""
		expectErr := fmt.Errorf("not %q path found", notExistingCommand)
		got, gotErr := k8sImpl.findDockerPath()
		tearDown()
		if gotErr.Error() != expectErr.Error() {
			t.Fatalf("Got error %v, expect %v error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

type setupDockerRegistryType int

const (
	dockerRegistryFound           = setupDockerRegistryType(iota) // the docker registr is found
	dockerRegistryHostNotExists                                   // the docker registry is found and with a valid path
	dockerRegistryPathNotFound                                    // the docker registry is found but not the path
	dockerRegistryEnvVarNotExists                                 // the docker registry environment variable does not exist
)

func setUpTestFindDockerRegistry(setup setupDockerRegistryType) {
	dockerRegistryVar = testRegistryVar
	switch setup {
	case dockerRegistryFound:
		os.Setenv(dockerRegistryVar, existingHost)
		dockerRegistryPath = "/"
		break
	case dockerRegistryHostNotExists:
		os.Setenv(dockerRegistryVar, notExistingHost)
		dockerRegistryPath = "/"
		break
	case dockerRegistryPathNotFound:
		os.Setenv(dockerRegistryVar, existingHost)
		dockerRegistryPath = "/not-found"
		break
	case dockerRegistryEnvVarNotExists:
		dockerRegistryPath = "/not-found"
		break
	}
}

func Test_findDockerRegistry(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the docker registry", func(t *testing.T) {
		setUpTestFindDockerRegistry(dockerRegistryFound)
		expect := existingHost
		got, gotErr := k8sImpl.findDockerRegistry()
		tearDown()
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect) //Got "http://192.168.64.3:32000", expect "https://google.com"
		}
	})

	t.Run("must not find the docker registry when host does not exist", func(t *testing.T) {
		setUpTestFindDockerRegistry(dockerRegistryHostNotExists)
		expect := ""
		expectErr := "error checking docker registry, "
		got, gotErr := k8sImpl.findDockerRegistry()
		tearDown()
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the docker registry when host does not return ok", func(t *testing.T) {
		setUpTestFindDockerRegistry(dockerRegistryPathNotFound)
		expect := ""
		expectErr := "error checking docker registry, status is 404"
		got, gotErr := k8sImpl.findDockerRegistry()
		tearDown()
		if gotErr.Error() != expectErr {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the docker registry when env var does not exist", func(t *testing.T) {
		setUpTestFindDockerRegistry(dockerRegistryEnvVarNotExists)
		expect := ""
		expectErr := fmt.Sprintf("error checking docker registry, variable %s does not exist", testRegistryVar)
		got, gotErr := k8sImpl.findDockerRegistry()
		if gotErr == nil {
			t.Fatalf("Got error nil, expect %q error", expectErr)
		}
		if gotErr.Error() != expectErr {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func setUpTestFindDockerRegistryK8s(existing bool) {
	dockerRegistryK8sVar = testK8SRegistryVar
	if existing {
		os.Setenv(dockerRegistryK8sVar, existingValue)
	} else {
		os.Unsetenv(dockerRegistryK8sVar)
	}
}
func Test_findDockerRegistryK8s(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the k8s docker registry", func(t *testing.T) {
		setUpTestFindDockerRegistryK8s(true)
		expect := existingValue
		got, gotErr := k8sImpl.findDockerRegistryK8s()
		tearDown()
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the k8s docker registry when env var does not exist", func(t *testing.T) {
		setUpTestFindDockerRegistryK8s(false)
		expect := ""
		expectErr := fmt.Sprintf("error checking K8s docker registry, variable %s does not exist", testK8SRegistryVar)
		got, gotErr := k8sImpl.findDockerRegistryK8s()
		if gotErr == nil {
			t.Fatalf("Got error nil, expect %q error", expectErr)
		}
		if gotErr.Error() != expectErr {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func Test_Initialize(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must initialize without errors", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(true)
		_ = setUpTestFindDockerPath(true)
		setUpTestFindDockerRegistry(dockerRegistryFound)
		setUpTestFindDockerRegistryK8s(true)
		got := k8sImpl.Initialize()
		if got != nil {
			t.Fatalf("Got error %q, expect nil", got)
		}
	})

	t.Run("must not initialize find kubectl command fails", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(false)
		_ = setUpTestFindDockerPath(true)
		setUpTestFindDockerRegistry(dockerRegistryFound)
		setUpTestFindDockerRegistryK8s(true)
		expect := "error getting kubectl path"
		got := k8sImpl.Initialize()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %q, expect %q", got, expect)
		}
	})

	t.Run("must not initialize find docker command fails", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(true)
		_ = setUpTestFindDockerPath(false)
		setUpTestFindDockerRegistry(dockerRegistryFound)
		setUpTestFindDockerRegistryK8s(true)
		expect := "error getting docker path"
		got := k8sImpl.Initialize()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %q, expect %q", got, expect)
		}
	})

	t.Run("must not initialize find docker registry fails", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(true)
		_ = setUpTestFindDockerPath(true)
		setUpTestFindDockerRegistry(dockerRegistryHostNotExists)
		setUpTestFindDockerRegistryK8s(true)
		expect := "error checking docker registry"
		got := k8sImpl.Initialize()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %q, expect %q", got, expect)
		}
	})

	t.Run("must not initialize find k8s docker registry fails", func(t *testing.T) {
		_ = setUpTestFindKubectlPath(true)
		_ = setUpTestFindDockerPath(true)
		setUpTestFindDockerRegistry(dockerRegistryFound)
		setUpTestFindDockerRegistryK8s(false)
		expect := "error checking K8s docker registry"
		got := k8sImpl.Initialize()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %q, expect %q", got, expect)
		}
	})
}
