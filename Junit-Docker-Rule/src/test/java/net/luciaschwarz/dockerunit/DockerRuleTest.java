package net.luciaschwarz.dockerunit;

import org.junit.ClassRule;
import org.junit.Test;

public class DockerRuleTest {

    @ClassRule
    public static DockerRule dockerRule = new DockerRule("wildfly");

    @Test
    public void test() throws InterruptedException {
        System.out.println("Started!");
        Thread.sleep(20000);
    }

}
