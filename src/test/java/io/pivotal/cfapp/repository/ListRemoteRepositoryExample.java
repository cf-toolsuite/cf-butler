package io.pivotal.cfapp.repository;

import io.pivotal.cfapp.client.GitClient;
import org.eclipse.jgit.lib.Repository;

public class ListRemoteRepositoryExample {

    private static final String REMOTE_URL = "https://github.com/github/testrepo.git";

    public static void main(String[] args) throws Exception {
        GitClient helper = new GitClient();
        Repository repo = helper.getRepository(REMOTE_URL);
        String advice = helper.readFile(repo, "26fc70913efc66a93fe84f8ce1bba09954624490", "test/advice.c");
        System.out.println(advice);
    }

}
