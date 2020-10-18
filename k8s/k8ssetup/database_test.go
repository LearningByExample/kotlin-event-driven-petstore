package k8ssetup

import (
	"errors"
	"os"
	"strings"
	"testing"
)

func Test_getClusterName(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return the cluster name", func(t *testing.T) {
		expect := "cluster"
		var expectErr error = nil
		got, gotErr := k8sImpl.getClusterName(getFilePath("cluster.yml"))

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must return an error when file does not exist", func(t *testing.T) {
		expect := ""
		got, gotErr := k8sImpl.getClusterName(getFilePath("not-exist.yml"))

		if gotErr == nil {
			t.Fatal("Got nil, expect error")
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must return an error when yml file is not valid", func(t *testing.T) {
		expect := ""
		got, gotErr := k8sImpl.getClusterName(getFilePath("invalid.yml"))

		if gotErr == nil {
			t.Fatal("Got nil, expect error")
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})

	t.Run("must return an error when yml file has no cluster name", func(t *testing.T) {
		expect := ""
		got, gotErr := k8sImpl.getClusterName(getFilePath("valid.yml"))

		if gotErr == nil {
			t.Fatal("Got nil, expect error")
		}
		if got != expect {
			t.Fatalf("Got %q, expect %q", got, expect)
		}
	})
}

func Test_isDatabaseCreated(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return true if database exists", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}

		expect := true
		var expectErr error = nil
		got, gotErr := k8sImpl.isDatabaseCreated("cluster")

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if database does not exist", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", errInvalid
		}

		expect := false
		expectErr := errInvalid
		got, gotErr := k8sImpl.isDatabaseCreated("cluster")

		if !errors.Is(gotErr, expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_createDatabase(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return true if database is created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", nil
		}

		var expectErr error = nil
		gotErr := k8sImpl.createDatabase("cluster")

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})

	t.Run("must return false if database is not created", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", errInvalid
		}

		expectErr := errInvalid
		gotErr := k8sImpl.createDatabase("cluster")

		if !errors.Is(gotErr, expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})
}

// map[PostgresClusterStatus:Running]
func Test_isDatabaseRunning(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return true if database is running", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "map[PostgresClusterStatus:Running]", nil
		}

		expect := true
		var expectErr error = nil
		got, gotErr := k8sImpl.isDatabaseRunning("cluster")

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if database is not running", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "map[PostgresClusterStatus:Waiting]", nil
		}

		expect := false
		var expectErr error = nil
		got, gotErr := k8sImpl.isDatabaseRunning("cluster")

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if database check fails", func(t *testing.T) {
		var errInvalid = errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "", errInvalid
		}

		expect := false
		expectErr := errInvalid
		got, gotErr := k8sImpl.isDatabaseRunning("cluster")

		if !errors.Is(gotErr, expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_waitDatabaseCreation(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must run until database is running", func(t *testing.T) {
		count := 0
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			count++
			if count == 3 {
				return "map[PostgresClusterStatus:Running]", nil
			}
			return "map[PostgresClusterStatus:Waiting]", nil
		}

		k8sImpl.waitDatabaseCreation("cluster")
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
			count++
			if count == 2 {
				return "map[PostgresClusterStatus:Running]", nil
			}
			return "", errInvalid
		}

		k8sImpl.waitDatabaseCreation("cluster")
		expect := 2
		got := count
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_DatabaseCreation(t *testing.T) {
	// setup
	wd, _ := os.Getwd()
	os.Chdir("_test")
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("we should create the database", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "describe" && params[1] == "postgresql/cluster" {
				return "error", errors.New("error kubectl describe")
			}
			return "map[PostgresClusterStatus:Running]", nil
		}

		var expect error = nil
		got := k8sImpl.DatabaseCreation("cluster.yml")
		if got != expect {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when getting cluster name fails", func(t *testing.T) {
		expect := "error getting cluster name"
		got := k8sImpl.DatabaseCreation("cluster1.yml")

		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when database already exists", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "describe" && params[1] == "postgresql/cluster" {
				return "", nil
			}
			return "map[PostgresClusterStatus:Running]", nil
		}

		expect := "already exists"
		got := k8sImpl.DatabaseCreation("cluster.yml")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when database creation fails", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "describe" && params[1] == "postgresql/cluster" {
				return "error", errors.New("error kubectl describe")
			}
			if params[0] == "create" && params[1] == "-f" && params[2] == "cluster.yml" {
				return "error", errors.New("error kubectl create")
			}
			return "map[PostgresClusterStatus:Running]", nil
		}

		expect := "error creating database cluster"
		got := k8sImpl.DatabaseCreation("cluster.yml")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when job creation fails", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "describe" && params[1] == "postgresql/cluster" {
				return "error", errors.New("error kubectl describe")
			}
			if params[0] == "build" && params[3] == "Dockerfile-cluster-job" {
				return "error", errors.New("error docker build")
			}
			return "map[PostgresClusterStatus:Running]", nil
		}

		expect := "error creating job for cluster"
		got := k8sImpl.DatabaseCreation("cluster.yml")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	//tear down
	os.Chdir(wd)
}
