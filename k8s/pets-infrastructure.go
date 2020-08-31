package main

import (
	"k8s/k8ssetup"
	"log"
)

// TODO - check the local registry exists
// TODO - create a dockerfile with the SQLs and postgresql client
// TODO - build docker
// TODO - push docker to a local registry
// TODO - create and run a k8s job with the docker

// TODO - makefile

func main() {
	stp := k8ssetup.NewK8sSetUp()
	if err := stp.Initialize(); err != nil {
		log.Fatalf("Error in initialize, %v", err)
	}
	if err := stp.InstallPostgresqlOperator(); err != nil {
		log.Fatalf("Error installing PostgreSQL operator, %v", err)
	}
	if err := stp.CreationDatabase("pets-db.yml"); err != nil {
		log.Fatalf("Error installing database, %v", err)
	}
}
