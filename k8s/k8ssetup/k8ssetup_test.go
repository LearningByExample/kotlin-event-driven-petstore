package k8ssetup

import (
	"errors"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

const (
	okCommand = "test.sh"
	koCommand = "test_ko.sh"
)

func getFilePath(cmd string) string {
	path, _ := os.Getwd()
	path = filepath.Join(path, testFolder)
	path = filepath.Join(path, cmd)
	return path
}

func Test_executeCommand(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must run the command without errors", func(t *testing.T) {
		expect := "ok params: param-1 param-2 param-3\n"
		var expectErr error = nil
		cmd := getFilePath(okCommand)
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
		cmd := getFilePath(koCommand)
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
	k8sImpl.kubectlPath = getFilePath(okCommand)
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
	k8sImpl.dockerPath = getFilePath(okCommand)
	got, gotErr := k8sImpl.docker("param-1", "param-2", "param-3")
	if gotErr != expectErr {
		t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
	}
	if got != expect {
		t.Fatalf("Got %q, expect %q", got, expect)
	}
}

func Test_isPostgreSQLOperatorInstalled(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("postgresql is installed", func(t *testing.T) {
		expect := true
		k8sImpl.kubectlPath = getFilePath(okCommand)
		got := k8sImpl.isPostgreSQLOperatorInstalled()
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("postgresql is not installed", func(t *testing.T) {
		expect := false
		k8sImpl.kubectlPath = getFilePath(koCommand)
		got := k8sImpl.isPostgreSQLOperatorInstalled()
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_doPsqlOperatorInstallation(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("install psql operator runs ok", func(t *testing.T) {
		k8sImpl.kubectlPath = getFilePath(okCommand)
		var expect error = nil
		got := k8sImpl.doPsqlOperatorInstallation()
		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("install psql operator runs ko when kubectl fails", func(t *testing.T) {
		k8sImpl.kubectlPath = getFilePath(koCommand)
		expect := "error in kubectl"
		got := k8sImpl.doPsqlOperatorInstallation()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("install psql operator runs ko when repo clone fails", func(t *testing.T) {
		k8sImpl.kubectlPath = getFilePath(okCommand)
		k8sImpl.psqlOperatorRepo = "http://no-repo.com"
		expect := "error clonning postgres operator"
		got := k8sImpl.doPsqlOperatorInstallation()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})
}

func Test_InstallPostgresqlOperator(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("install postgresql operator runs ok", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "describe" {
				return "error", errors.New("some error")
			}
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					return "'Running'", nil
				}
			}
			return "", nil
		}
		var expect error = nil
		got := k8sImpl.InstallPostgresqlOperator()
		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("install postgresql operator ok if its installed", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}
		var expect error = nil
		got := k8sImpl.InstallPostgresqlOperator()
		if got != expect {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})

	t.Run("install postgresql operator runs ko when installation fails", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "error", errors.New("some error")
		}
		expect := "error installing PostgreSQL operator"
		got := k8sImpl.InstallPostgresqlOperator()
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect error %v", got, expect)
		}
	})
}

func Test_waitPsqlOperatorRunning(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must run until database is running", func(t *testing.T) {
		count := 0
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					count++
					if count == 3 {
						return "'Running'", nil
					}
					return "'Waiting'", nil
				}
			}
			return "", nil
		}

		k8sImpl.waitPsqlOperatorRunning()
		expect := 3
		got := count
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must run until there is no error and running", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		count := 0
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					count++
					if count == 2 {
						return "'Running'", nil
					}
					return "", errInvalid
				}
			}
			return "", nil
		}

		k8sImpl.waitPsqlOperatorRunning()
		expect := 2
		got := count
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_isPsqlOperatorRunning(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return true if postgres operator is running", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					return "'Running'", nil
				}
			}
			return "", nil
		}

		expect := true
		var expectErr error = nil
		got, gotErr := k8sImpl.isPsqlOperatorRunning()

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if postgres operator does not exist", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/test-1234", nil
				}
			}
			return "", nil
		}

		expect := false
		var expectErr error = nil
		got, gotErr := k8sImpl.isPsqlOperatorRunning()

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if postgres operator is in progress", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					return "'Waiting'", nil
				}
			}
			return "", nil
		}

		expect := false
		var expectErr error = nil
		got, gotErr := k8sImpl.isPsqlOperatorRunning()

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if get pods fails", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "", errInvalid
				}
			}
			return "", nil
		}

		expect := false
		expectErr := errInvalid
		got, gotErr := k8sImpl.isPsqlOperatorRunning()

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if postgres operator status check fails", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "get" {
				if params[1] == "pod" {
					return "pod/postgres-operator-59bb89464c-dx2rs", nil
				} else if params[1] == "pod/postgres-operator-59bb89464c-dx2rs" {
					return "", errInvalid
				}
			}
			return "", nil
		}

		expect := false
		expectErr := errInvalid
		got, gotErr := k8sImpl.isPsqlOperatorRunning()

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}
