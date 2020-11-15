package k8ssetup

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"strings"
)

type KudoInstances []struct {
	Metadata struct {
		Name string `json:"name"`
	} `json:"metadata"`
	Spec struct {
		OperatorVersion struct {
			Name string `json:"name"`
		} `json:"operatorVersion"`
	} `json:"spec"`
}

func (k k8sSetUpImpl) isKafkaClusterCreated(cluster string) (bool, error) {
	var output string
	var err error
	if output, err = k.kubectl("kudo", "get", "instances", "-o", "json"); err != nil {
		return false, fmt.Errorf("error getting kudo instances: %v", err)
	}
	var jsonBytes []byte = []byte(output)
	data := KudoInstances{}
	if err = json.Unmarshal(jsonBytes, &data); err == nil {
		var kf, zf bool
		for _, v := range data {
			if v.Metadata.Name == "kafka-"+cluster &&
				strings.Contains(v.Spec.OperatorVersion.Name, "kafka") {
				kf = true
			} else if v.Metadata.Name == "zookeeper-"+cluster &&
				strings.Contains(v.Spec.OperatorVersion.Name, "zookeeper") {
				zf = true
			}
			if kf && zf {
				break
			}
		}
		if kf && zf {
			return true, nil
		}
	}

	if err != nil {
		return false, fmt.Errorf("Error not found kafka and zookeeper, invalid json: %v", err)
	}

	return false, errors.New("Error not found kafka and zookeeper")
}

func (k k8sSetUpImpl) createZookeeperCluster(name string) error {
	log.Println("Installing zookeper cluster ...")
	if _, err := k.kubectl("kudo", "install", "zookeeper", "--instance", fmt.Sprintf("\"zookeeper-%s\"", name)); err != nil {
		return fmt.Errorf("Error creating zookeeper cluster: %v", err)
	}
	k.waitZookeeperRunning(name)
	return nil
}

func (k k8sSetUpImpl) waitZookeeperRunning(name string) {
	cnt := true
	for cnt {
		allReady := true
		for i := 0; i < 3; i++ {
			ready, err := k.isPodRunning(fmt.Sprintf("zookeeper-%s-%d", name, i), "default")
			if err == nil {
				allReady = allReady && ready
			}
		}
		cnt = !allReady
	}
	log.Print("Zookeeper operator is running")
}

func (k k8sSetUpImpl) createKafkaCluster(name string) error {
	log.Println("Installing kafka cluster ...")
	if _, err := k.kubectl("kudo", "install", "kafka", "--instance", fmt.Sprintf("\"kafka-%s\"", name), "-p", "ZOOKEEPER_URI=\"zookeeper-pets-zookeeper-0.zookeeper-pets-hs:2181,zookeeper-pets-zookeeper-1.zookeeper-pets-hs:2181,zookeeper-pets-zookeeper-2.zookeeper-pets-hs:2181\""); err != nil {
		return fmt.Errorf("Error creating kafka cluster: %v", err)
	}
	
	k.waitKafkaClusterCreation(name)
	return nil
}

func (k k8sSetUpImpl) waitKafkaClusterCreation(name string) {
	cnt := true
	for cnt {
		allReady := true
		for i := 0; i < 3; i++ {
			ready, err := k.isPodRunning(fmt.Sprintf("kafka-%s-%d", name, i), "default")
			if err == nil {
				allReady = allReady && ready
			}
		}
		cnt = !allReady
	}
	log.Print("Kafka operator is running")
}

func (k *k8sSetUpImpl) KafkaClusterCreation(clusterName string) error {
	log.Printf("Creating kafka with name %q ...", clusterName)
	var err error
	
	if created, err := k.isKafkaClusterCreated(clusterName); err == nil && created {
		return fmt.Errorf("kafka cluster %q already exists", clusterName)
	}

	if err = k.createZookeeperCluster(clusterName); err == nil {
		log.Printf("Zookeeper cluster %q created ...", clusterName)
	} else {
		return fmt.Errorf("error creating zookeeper cluster %q: %v", clusterName, err)
	}

	if err = k.createKafkaCluster(clusterName); err == nil {
		log.Printf("Kafka cluster %q created ...", clusterName)
	} else {
		return fmt.Errorf("error creating kafka cluster %q: %v", clusterName, err)
	}

	return nil
}
