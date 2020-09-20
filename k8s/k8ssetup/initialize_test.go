package k8ssetup

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"
)

const (
	testPathVar        = "TESTPATH"
	testFolder         = "_test"
	existingCommand    = "test.sh"
	notExistingCommand = "test_does_not_exist.sh"
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
