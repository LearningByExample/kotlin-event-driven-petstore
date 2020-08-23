package main

import (
    "errors"
    "fmt"
    "k8s.io/client-go/kubernetes"
    "k8s.io/client-go/tools/clientcmd"
    "log"
    "os"
    "path/filepath"
)

const (
    postgresqlResourceName = "postgresqls"
)

func main() {
    if err := installDatabase(); err != nil {
        log.Fatalf("Error checking postgreSQL database intallation %v", err)
    }
}

func installDatabase() error {
    log.Println("Installing database ...")
    if client, err := getClientSetOnLocal(); err == nil {
        if installed, err := postgresqlOperatorIsInstalled(client); err == nil {
            fmt.Println(installed)
        } else {
            return fmt.Errorf("zalando is not installed: %v", err)
        }
    } else {
        return fmt.Errorf("cannot get the client %v", err)
    }

    return nil
}

func postgresqlOperatorIsInstalled(client *kubernetes.Clientset) (bool, error) {
    log.Println("Checking postgresql operator already installed ...")
    if _, lists, err := client.ServerGroupsAndResources(); err == nil {
        for _, elem := range lists {
            for _, res := range elem.APIResources {
                if res.Name == postgresqlResourceName {
                    return true, nil
                }
            }
        }
    } else {
        return false, err
    }

    return false, nil
}

func getClientSetOnLocal() (*kubernetes.Clientset, error) {
    log.Println("getting k8s client..")

    home := os.Getenv("HOME")
    kubeConfig := filepath.Join(home, ".kube", "config")

    var config, err = clientcmd.BuildConfigFromFlags("", kubeConfig)
    if err != nil {
        return nil, errors.New(fmt.Sprintf("error in kubernetes, err = , %s\n", err.Error()))
    }

    clientSet, err := kubernetes.NewForConfig(config)
    if err != nil {
        return nil, errors.New(fmt.Sprintf("error in kubernetes, err = , %s\n", err.Error()))
    }

    log.Println("got k8s client")

    return clientSet, nil
}
