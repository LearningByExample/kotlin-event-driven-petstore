package k8ssetup

import (
	"errors"
	"strings"
	"testing"
)

const (
	KudoInstancesFound = `[
			{
				"metadata": {
					"name": "zookeeper-pets"
				},
				"spec": {
					"operatorVersion": {
						"name": "zookeeper-3.4.14-0.3.1"
					}
				}
			},
			{
				"metadata": {
					"name": "kafka-pets"
				},
				"spec": {
					"operatorVersion": {
						"name": "kafka-2.5.1-1.3.3"
					}
				}
			}			
		]`

	KudoKafkaInstanceNotFound = `[
			{
				"metadata": {
					"name": "zookeeper-instance"
				},
				"spec": {
					"operatorVersion": {
						"name": "zookeeper-3.4.14-0.3.1"
					}
				}
			}			
		]`

	KudoZookeeperInstanceNotFound = `[
			{
				"metadata": {
					"name": "kafka-instance"
				},
				"spec": {
					"operatorVersion": {
						"name": "kafka-2.5.1-1.3.3"
					}
				}
			}			
		]`
)

func Test_isKafkaClusterCreated(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	t.Run("must return true if kafka and zookeper clusters are created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return KudoInstancesFound, nil
		}

		expect := true
		var expectErr error = nil
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")

		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if kafka cluster is not created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return KudoKafkaInstanceNotFound, nil
		}

		expect := false
		expectErr := "Error not found kafka and zookeeper"
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if zookeeper cluster is not created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return KudoZookeeperInstanceNotFound, nil
		}

		expect := false
		expectErr := "Error not found kafka and zookeeper"
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if zookeeper nor kafka clusters are created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "[]", nil
		}

		expect := false
		expectErr := "Error not found kafka and zookeeper"
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if returned json is invalid", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "error", nil
		}

		expect := false
		expectErr := "Error not found kafka and zookeeper, invalid json"
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})

	t.Run("must return false if getting instances fails", func(t *testing.T) {
		invalidErr := errors.New("invalid")
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" && params[1] == "get" && params[2] == "instances" {
				return "", invalidErr
			}
			return "", nil
		}

		expect := false
		expectErr := "error getting kudo instances"
		got, gotErr := k8sImpl.isKafkaClusterCreated("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
		if got != expect {
			t.Fatalf("Got %v, expect %v", got, expect)
		}
	})
}

func Test_createZookeeperCluster(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	var invalidErr = errors.New("invalid")

	type podStatus struct {
		name  string
		ready bool
	}
	var pods = []podStatus{
		{
			name:  "pod/zookeeper-pets-0",
			ready: true,
		},
		{
			name:  "pod/zookeeper-pets-1",
			ready: true,
		},
		{
			name:  "pod/zookeeper-pets-2",
			ready: true,
		},
	}

	t.Run("must return no error if zookeeper cluster is created", func(t *testing.T) {

		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" {
				return "", nil
			}

			if params[0] == "get" {
				if params[1] == "pod" {
					var result = ""
					for _, v := range pods {
						result = result + v.name + "\n"
					}
					return result, nil
				}
				return "'true'", nil
			}
			return "", nil
		}

		var expectErr error = nil
		gotErr := k8sImpl.createZookeeperCluster("pets")
		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})

	t.Run("must return error if zookeeper installation fails", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "error", invalidErr
		}

		expectErr := "Error creating zookeeper cluster"
		gotErr := k8sImpl.createZookeeperCluster("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})
}

func Test_waitZookeeperRunning(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	type podStatus struct {
		name  string
		ready bool
	}

	var pods = []podStatus{
		{
			name:  "pod/zookeeper-pets-0",
			ready: false,
		},
		{
			name:  "pod/zookeeper-pets-1",
			ready: false,
		},
		{
			name:  "pod/zookeeper-pets-2",
			ready: false,
		},
	}

	k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
		if params[0] == "get" {
			if params[1] == "pod" {
				var result = ""
				for _, v := range pods {
					result = result + v.name + "\n"
				}
				return result, nil
			}
			for _, v := range pods {
				if v.name == params[1] {
					if v.ready {
						return "'true'", nil
					}
				}
			}
			if strings.Contains(params[1], pods[2].name) {
				for k := range pods {
					pods[k].ready = true
				}
			}
			return "'false'", nil
		}
		return "", nil
	}

	k8sImpl.waitZookeeperRunning("pets")
	for _, v := range pods {
		if !v.ready {
			t.Fatalf("Got %v, expect %v", false, true)
		}
	}
}

func Test_createKafkaCluster(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	var invalidErr = errors.New("invalid")

	type podStatus struct {
		name  string
		ready bool
	}
	var pods = []podStatus{
		{
			name:  "pod/kafka-pets-0",
			ready: true,
		},
		{
			name:  "pod/kafka-pets-1",
			ready: true,
		},
		{
			name:  "pod/kafka-pets-2",
			ready: true,
		},
	}

	t.Run("must return no error if kafka cluster is created", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" {
				return "", nil
			}

			if params[0] == "get" {
				if params[1] == "pod" {
					var result = ""
					for _, v := range pods {
						result = result + v.name + "\n"
					}
					return result, nil
				}
				return "'true'", nil
			}
			return "", nil
		}

		var expectErr error = nil
		gotErr := k8sImpl.createKafkaCluster("pets")
		if gotErr != expectErr {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})

	t.Run("must return error if kafka installation fails", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			return "error", invalidErr
		}

		expectErr := "Error creating kafka cluster"
		gotErr := k8sImpl.createKafkaCluster("pets")
		if !strings.Contains(gotErr.Error(), expectErr) {
			t.Fatalf("Got error %v, expect error %v", gotErr, expectErr)
		}
	})
}

func Test_waitKafkaClusterCreation(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)

	type podStatus struct {
		name  string
		ready bool
	}

	var pods = []podStatus{
		{
			name:  "pod/kafka-pets-0",
			ready: false,
		},
		{
			name:  "pod/kafka-pets-1",
			ready: false,
		},
		{
			name:  "pod/kafka-pets-2",
			ready: false,
		},
	}

	k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
		if params[0] == "get" {
			if params[1] == "pod" {
				var result = ""
				for _, v := range pods {
					result = result + v.name + "\n"
				}
				return result, nil
			}
			for _, v := range pods {
				if v.name == params[1] {
					if v.ready {
						return "'true'", nil
					}
				}
			}
			if strings.Contains(params[1], pods[2].name) {
				for k := range pods {
					pods[k].ready = true
				}
			}
			return "'false'", nil
		}
		return "", nil
	}

	k8sImpl.waitKafkaClusterCreation("pets")
	for _, v := range pods {
		if !v.ready {
			t.Fatalf("Got %v, expect %v", false, true)
		}
	}
}

func Test_KafkaClusterCreation(t *testing.T) {
	k8sImpl := NewK8sSetUp().(*k8sSetUpImpl)
	invalidErr := errors.New("invalid") 
	type podStatus struct {
		name  string
		ready bool
	}

	var pods = []podStatus{
		{
			name:  "pod/zookeeper-pets-0",
			ready: false,
		},
		{
			name:  "pod/zookeeper-pets-1",
			ready: false,
		},
		{
			name:  "pod/zookeeper-pets-2",
			ready: false,
		},
		{
			name:  "pod/kafka-pets-0",
			ready: false,
		},
		{
			name:  "pod/kafka-pets-1",
			ready: false,
		},
		{
			name:  "pod/kafka-pets-2",
			ready: false,
		},
	}

	t.Run("we should create the kafka cluster", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" && params[2] == "instances" && params[3] == "-o" && params[4] == "json" {
				return KudoKafkaInstanceNotFound, nil
			}
			if params[0] == "kudo" && params[1] == "install" {
				return "", nil
			}

			if params[0] == "get" {
				if params[1] == "pod" {
					var result = ""
					for _, v := range pods {
						result = result + v.name + "\n"
					}
					return result, nil
				}
				return "'true'", nil
			}
			return "", nil
		}

		var expect error = nil
		got := k8sImpl.KafkaClusterCreation("pets")
		if got != expect {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when kafka and zookeeper already exist", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" && params[2] == "instances" && params[3] == "-o" && params[4] == "json" {
				return KudoInstancesFound, nil
			}
			if params[0] == "kudo" && params[1] == "install" {
				return "", nil
			}

			if params[0] == "get" {
				if params[1] == "pod" {
					var result = ""
					for _, v := range pods {
						result = result + v.name + "\n"
					}
					return result, nil
				}
				return "'true'", nil
			}
			return "", nil
		}

		expect := "already exists"
		got := k8sImpl.KafkaClusterCreation("pets")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when creating zookeeper cluster", func(t *testing.T) {
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" && params[2] == "instances" && params[3] == "-o" && params[4] == "json" {
				return KudoKafkaInstanceNotFound, nil
			}
			if params[0] == "kudo" && params[1] == "install" {
				return "error", invalidErr
			}

			return "", nil
		}

		expect := "error creating zookeeper cluster"
		got := k8sImpl.KafkaClusterCreation("pets")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})

	t.Run("we should return an error when creating kafka cluster", func(t *testing.T) {
		// TODO - pending
		k8sImpl.executeCommand = func(cmdName string, params ...string) (string, error) {
			if params[0] == "kudo" && params[2] == "instances" && params[3] == "-o" && params[4] == "json" {
				return KudoKafkaInstanceNotFound, nil
			}
			if params[0] == "kudo" && params[1] == "install" && params[2] == "zookeeper" {
				return "", nil
			}

			if params[0] == "kudo" && params[1] == "install" && params[2] == "kafka" {
				return "error", invalidErr
			}

			if params[0] == "get" {
				if params[1] == "pod" {
					var result = ""
					for _, v := range pods {
						result = result + v.name + "\n"
					}
					return result, nil
				}
				return "'true'", nil
			}
			return "", nil
		}

		expect := "error creating kafka cluster"
		got := k8sImpl.KafkaClusterCreation("pets")
		if !strings.Contains(got.Error(), expect) {
			t.Fatalf("Got error %v, expect  error %v", got, expect)
		}
	})
}
