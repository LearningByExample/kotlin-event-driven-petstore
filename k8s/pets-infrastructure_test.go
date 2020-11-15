package main

import (
	"errors"
	"fmt"
	"testing"
)

type k8sSetUpFake struct {
	failOnInitialize                bool
	failOnInstallPostgresqlOperator bool
	failOnDatabaseCreation          bool
	failOnKafkaClusterCreation      bool
	failOnCheckKudoInstallation		bool
}

var (
	errorInit                 = errors.New("error on initialize")
	errorInstallPsqlOperator  = errors.New("error on installing postgresql operator")
	errorDBCreation           = errors.New("error on database creation")
	errorKafkaClusterCreation = errors.New("error on kafka cluster creation")
	errorCheckKudoInstallation = errors.New("error on kudo checking kudo installation")
)

func (k k8sSetUpFake) Initialize() error {
	if k.failOnInitialize {
		return errorInit
	}
	return nil
}

func (k k8sSetUpFake) InstallPostgresqlOperator() error {
	if k.failOnInstallPostgresqlOperator {
		return errorInstallPsqlOperator
	}
	return nil
}

func (k k8sSetUpFake) DatabaseCreation(fileName string) error {
	if k.failOnDatabaseCreation {
		return errorDBCreation
	}
	return nil
}

func (k k8sSetUpFake) KafkaClusterCreation(fileName string) error {
	if k.failOnKafkaClusterCreation {
		return errorKafkaClusterCreation
	}
	return nil
}

func (k k8sSetUpFake) CheckKudoInstallation() error {
	if k.failOnCheckKudoInstallation {
		return errorCheckKudoInstallation
	}
	return nil
}

func Test_run(t *testing.T) {
	type TestCase struct {
		name   string
		stp    k8sSetUpFake
		expect error
	}

	cases := []TestCase{
		{
			name:   "should run without errors",
			stp:    k8sSetUpFake{},
			expect: nil,
		},
		{
			name: "should run error when initialize fails",
			stp: k8sSetUpFake{
				failOnInitialize: true,
			},
			expect: fmt.Errorf("error on initialize, %v", errorInit),
		},
		{
			name: "should run error when install postgresql operator fails",
			stp: k8sSetUpFake{
				failOnInstallPostgresqlOperator: true,
			},
			expect: fmt.Errorf("error installing PostgreSQL operator, %v", errorInstallPsqlOperator),
		},
		{
			name: "should run error when creation database fails",
			stp: k8sSetUpFake{
				failOnDatabaseCreation: true,
			},
			expect: fmt.Errorf("error installing database, %v", errorDBCreation),
		},
		{
			name: "should run error when checking kudo installation fails",
			stp: k8sSetUpFake{
				failOnCheckKudoInstallation: true,
			},
			expect: fmt.Errorf("error checking kudo installation, %v", errorCheckKudoInstallation),
		},
		{
			name: "should run error when creation kafka cluster fails",
			stp: k8sSetUpFake{
				failOnKafkaClusterCreation: true,
			},
			expect: fmt.Errorf("error installing Kafka cluster, %v", errorKafkaClusterCreation),
		},
	}

	for _, tt := range cases {
		t.Run(tt.name, func(t *testing.T) {
			got := run(tt.stp)
			if tt.expect == nil {
				if got != nil {
					t.Errorf("Got %v, expect nil", got)
				}
			} else {
				if got.Error() != tt.expect.Error() {
					t.Errorf("Got %v, expect %v", got, tt.expect)
				}
			}
		})
	}
}
