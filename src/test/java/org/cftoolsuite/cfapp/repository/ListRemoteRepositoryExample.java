package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.client.GitClient;
import org.cftoolsuite.cfapp.config.GitSettings;
import org.eclipse.jgit.lib.Repository;

public class ListRemoteRepositoryExample {

    private static final String REMOTE_URL_1 = "https://github.com/github/testrepo.git";
    private static final String REMOTE_URL_2 = "https://github.com/cf-toolsuite/test-repo.git";
    public static void main(String[] args) throws Exception {
        GitClient helper = new GitClient();
        Repository repo = helper.getRepository(GitSettings.builder().uri(REMOTE_URL_1).build());
        String advice = helper.readFile(repo, "26fc70913efc66a93fe84f8ce1bba09954624490", "test/advice.c");
        System.out.println(advice);

        // This will intentionally fail b/c I'm not sharing credentials for a private repository
        // but it serves to demonstrate how one can retrieve file contests
        repo = helper.getRepository(GitSettings.builder().uri(REMOTE_URL_2).username("change_me").build());
        String readme = helper.readFile(repo, "0f7aac949b32d7636f11918dc34ae8bb251fa610", "README.md");
        System.out.println(readme);
    }

}
