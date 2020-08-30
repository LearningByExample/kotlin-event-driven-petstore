package main

import (
	"k8s/k8ssetup"
	"log"
)

// TODO - generalise to any database

// TODO - find out the docker command path
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
	if err := stp.InstallDatabase(); err != nil {
		log.Fatalf("Error installing database, %v", err)
	}
}
