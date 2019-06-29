package io.pivotal.cfapp.task;

import org.springframework.boot.ApplicationRunner;

public interface PolicyExecutorTask extends ApplicationRunner {
    void execute();
}