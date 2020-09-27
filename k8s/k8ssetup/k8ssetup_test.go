package k8ssetup

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

const (
	okCommand = "test.sh"
	koCommand = "test_ko.sh"
)

func getCmdPath(cmd string) string {
	path, _ := os.Getwd()
	path = filepath.Dir(path)
	path = filepath.Join(path, testFolder)
	path = filepath.Join(path, cmd)
	return path
}

func Test_executeCommand(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must run the command without errors", func(t *testing.T) {
		expect := "ok params: param-1 param-2 param-3\n"
		var expectErr error = nil
		cmd := getCmdPath(okCommand)
		got, gotErr := k8sImpl.executeCommand(cmd, "param-1", "param-2", "param-3")
		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must run the command with error", func(t *testing.T) {
		expect := "ko params: param-1 param-2 param-3\n"
		expectErr := "error 'exit status 255'"
		cmd := getCmdPath(koCommand)
		got, gotErr := k8sImpl.executeCommand(cmd, "param-1", "param-2", "param-3")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func Test_kubectl(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	expect := "ok params: param-1 param-2 param-3\n"
	var expectErr error = nil
	k8sImpl.kubectlPath = getCmdPath(okCommand)
	got, gotErr := k8sImpl.kubectl("param-1", "param-2", "param-3")
	if gotErr != expectErr {
		t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
	}
	if got != expect {
		t.Fatalf("Got %q, expect %q", got, expect)
	}
}

func Test_docker(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	expect := "ok params: param-1 param-2 param-3\n"
	var expectErr error = nil
	k8sImpl.dockerPath = getCmdPath(okCommand)
	got, gotErr := k8sImpl.docker("param-1", "param-2", "param-3")
	if gotErr != expectErr {
		t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
	}
	if got != expect {
		t.Fatalf("Got %q, expect %q", got, expect)
	}
}
