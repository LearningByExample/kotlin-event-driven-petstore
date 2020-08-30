package main

import (
	"k8s/k8ssetup"
	"log"
)

func main() {
	stp := k8ssetup.NewK8sSetUp()
	if err := stp.Initialize(); err != nil {
		log.Fatalf("Error in initialize %v", err)
	}
	if err := stp.InstallDatabase(); err != nil {
		log.Fatalf("Error in install %v", err)
	}
}
