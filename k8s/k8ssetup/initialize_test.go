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
	testFolder         = "_test"
	existingCommand    = "test.sh"
	notExistingCommand = "test_does_not_exist.sh"
	existingHost       = "https://google.com"
	notExistingHost    = "https://not-existing-host.com"
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

func Test_findKubectlPath(t *testing.T) {
	path := setEnvVar()
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the kubectl command", func(t *testing.T) {
		kubectlCommand = existingCommand
		expect := filepath.Join(path, existingCommand)
		got, gotErr := k8sImpl.findKubectlPath()
		os.Unsetenv(pathVar)
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the kubectl command", func(t *testing.T) {
		kubectlCommand = notExistingCommand
		expect := ""
		expectErr := fmt.Errorf("not %q path found", notExistingCommand)
		got, gotErr := k8sImpl.findKubectlPath()
		os.Unsetenv(pathVar)
		if gotErr.Error() != expectErr.Error() {
			t.Fatalf("Got error %v, expect %v error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func Test_findDockerPath(t *testing.T) {
	path := setEnvVar()
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the kubectl command", func(t *testing.T) {
		dockerCommand = existingCommand
		expect := filepath.Join(path, existingCommand)
		got, gotErr := k8sImpl.findDockerPath()
		os.Unsetenv(pathVar)
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the kubectl command", func(t *testing.T) {
		dockerCommand = notExistingCommand
		expect := ""
		expectErr := fmt.Errorf("not %q path found", notExistingCommand)
		got, gotErr := k8sImpl.findDockerPath()
		os.Unsetenv(pathVar)
		if gotErr.Error() != expectErr.Error() {
			t.Fatalf("Got error %v, expect %v error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func Test_findDockerRegistry(t *testing.T) {
	dockerRegistryVar = testPathVar
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must find the docker registry", func(t *testing.T) {
		os.Setenv(testPathVar, existingHost)
		dockerRegistryPath = "/"
		expect := existingHost
		got, gotErr := k8sImpl.findDockerRegistry()
		os.Unsetenv(dockerRegistryVar)
		if gotErr != nil {
			t.Fatalf("Got error %v, expect nil error", gotErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the docker registry when host does not exist", func(t *testing.T) {
		os.Setenv(testPathVar, notExistingHost)
		dockerRegistryPath = "/"
		expect := ""
		expectErr := "error checking docker registry, "
		got, gotErr := k8sImpl.findDockerRegistry()
		os.Unsetenv(dockerRegistryVar)
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the docker registry when host does not return ok", func(t *testing.T) {
		os.Setenv(testPathVar, existingHost)
		dockerRegistryPath = "/not-found"
		expect := ""
		expectErr := "error checking docker registry, status is 404"
		got, gotErr := k8sImpl.findDockerRegistry()
		os.Unsetenv(dockerRegistryVar)
		if gotErr.Error() != expectErr {
			t.Fatalf("Got error %q, expect %q error", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must not find the docker registry when env var does not exist", func(t *testing.T) {
		dockerRegistryPath = "/"
		expect := ""
		expectErr := fmt.Sprintf("error checking docker registry, variable %s does not exist", testPathVar)
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
