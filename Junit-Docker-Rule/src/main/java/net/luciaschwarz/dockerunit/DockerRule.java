package net.luciaschwarz.dockerunit;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.PortBinding;
import org.junit.rules.ExternalResource;

import java.util.*;

public class DockerRule extends ExternalResource {


    private final DockerClient docker;
    private final ContainerCreation container;

    public DockerRule(String imageName) {
        try {
            docker = DefaultDockerClient.fromEnv().build();
        } catch (DockerCertificateException e) {
            throw new IllegalStateException(e);
        }

        final String[] ports = {"80", "8080", "9990"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(imageName)
                .networkDisabled(false)
                .exposedPorts(ports).build();

        try {
            //docker.pull(imageName);
            container = docker.createContainer(containerConfig);
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void before() throws Throwable {
        System.out.println("Start container");
        super.before();
        docker.startContainer(container.id());
        ContainerInfo info = docker.inspectContainer(container.id());
        Map<String, List<PortBinding>> ports = info.networkSettings().ports();

        for(String port : ports.keySet()) {
            System.out.println("Port " + port + " : ");
            if(ports.get(port)!=null){
                System.out.println(ports.get(port).toString());
            } else {
                System.out.println("null");
            }
        }

//        if (params.portToWaitOn != null) {
//            waitForPort(getHostPort(params.portToWaitOn), params.waitTimeout);
//        }
//
//        if (params.logToWait != null) {
//            waitForLog(params.logToWait);
//        }

    }

    @Override
    protected void after() {
        System.out.println("Kill container");
        super.after();
        try {
            docker.killContainer(container.id());
            docker.removeContainer(container.id(), true);
            docker.close();
        } catch (DockerException | InterruptedException e) {
            throw new RuntimeException("Unable to stop/remove docker container " + container.id(), e);
        }
    }
}