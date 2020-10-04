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
}

var (
	errorInit            = errors.New("error on initialize")
	errorInstallOperator = errors.New("error on installing postgresql operator")
	errorDBCreation      = errors.New("error on database creation")
)

func (k k8sSetUpFake) Initialize() error {
	if k.failOnInitialize {
		return errorInit
	}
	return nil
}

func (k k8sSetUpFake) InstallPostgresqlOperator() error {
	if k.failOnInstallPostgresqlOperator {
		return errorInstallOperator
	}
	return nil
}

func (k k8sSetUpFake) DatabaseCreation(fileName string) error {
	if k.failOnDatabaseCreation {
		return errorDBCreation
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
			expect: fmt.Errorf("error installing PostgreSQL operator, %v", errorInstallOperator),
		},
		{
			name: "should run error when creation database fails",
			stp: k8sSetUpFake{
				failOnDatabaseCreation: true,
			},
			expect: fmt.Errorf("error installing database, %v", errorDBCreation),
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
