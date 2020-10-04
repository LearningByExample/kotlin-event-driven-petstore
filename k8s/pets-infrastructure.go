package main

import (
	"fmt"
	"k8s/k8ssetup"
	"log"
)

func run(stp k8ssetup.K8sSetUp) error {
	if err := stp.Initialize(); err != nil {
		return fmt.Errorf("error on initialize, %v", err)
	}
	if err := stp.InstallPostgresqlOperator(); err != nil {
		return fmt.Errorf("error installing PostgreSQL operator, %v", err)
	}
	if err := stp.DatabaseCreation("pets-db.yml"); err != nil {
		return fmt.Errorf("error installing database, %v", err)
	}
	return nil
}

func main() {
	stp := k8ssetup.NewK8sSetUp()
	if err := run(stp); err != nil {
		log.Fatalf("Error running the set up, %v", err)
	}
}
