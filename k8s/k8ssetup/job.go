package k8ssetup

// docker build . -f Dockerfile-petstore-pets-cluster-job -t 192.168.64.3:32000/petsdb
func (k k8sSetUpImpl) dockerBuild(dockerFile string, tag string) error {

	return nil
}

func (k k8sSetUpImpl) createDatabaseJob(cluster string) error {
	if err := k.dockerBuild("Dockerfile-"+cluster+"-job", cluster+"-job"); err != nil {
		return err
	}
	return nil
}
