package io.pivotal.cfapp.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.pivotal.cfapp.config.GitSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "cf.policies.git", name = "uri"
        )
public class GitClient {

    // @see https://stackoverflow.com/questions/42820282/get-the-latest-commit-in-a-repository-with-jgit
    public RevCommit getLatestCommit(Repository repo) throws IOException, GitAPIException {
        RevCommit latestCommit = null;
        int inc = 0;
        try(
                Git git = new Git(repo);
                RevWalk walk = new RevWalk(repo);
                ) {
            List<Ref> branches = git.branchList().call();
            for(Ref branch : branches) {
                RevCommit commit = walk.parseCommit(branch.getObjectId());
                if (inc == 0)
                    latestCommit = commit;
                if(commit.getAuthorIdent().getWhen().compareTo(latestCommit.getAuthorIdent().getWhen()) > 0)
                    latestCommit = commit;
                inc++;
            }
        }
        return latestCommit;
    }

    public Repository getRepository(GitSettings settings) {
        Repository result = null;
        String uri = settings.getUri();
        Assert.hasText(uri, "URI of remote Git repository must be specified");
        Assert.isTrue(uri.startsWith("https://"), "URI scheme must be https");
        Assert.isTrue(uri.endsWith(".git"), "URI must end with .git");
        String path = String.join(File.separator, "tmp", uri.substring(uri.lastIndexOf("/") + 1).replace(".git",""));
        try {
            File directory = new File(path);
            Path p = Paths.get(directory.toURI());
            if (Files.exists(p)) {
                Files
                .walk(p)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            }
            p.toFile().delete();
            if (settings.isAuthenticated()) {
                String username = settings.getUsername();
                String password = settings.getPassword();
                Git
                .cloneRepository()
                .setURI(uri)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setDirectory(directory)
                .setCloneAllBranches(true)
                .call()
                .close();
            } else {
                Git
                .cloneRepository()
                .setURI(uri)
                .setDirectory(directory)
                .setCloneAllBranches(true)
                .call()
                .close();
            }
            result = Git.open(directory).getRepository();
        } catch (GitAPIException | IOException e) {
            log.warn(String.format("Cannot clone Git repository at %s", uri), e);
        }
        return result;
    }

    public String orLatestCommit(String commit, Repository repo) {
        String result = null;
        if (StringUtils.isNotBlank(commit)) {
            result = commit;
        } else {
            try {
                result = getLatestCommit(repo).getName();
            } catch (GitAPIException | IOException e) {
                log.error("Trouble fetching latest commit id.", e);
            }
        }
        return result;
    }

    public String readFile(Repository repo, String commitId, String filePath) throws IOException {
        ObjectId oid = repo.resolve(commitId);
        RevCommit commit = repo.parseCommit(oid);
        try (TreeWalk walk = TreeWalk.forPath(repo, filePath, commit.getTree())) {
            if (walk != null) {
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException(String.format("No file found for commitId=%s and filePath=%s", commitId, filePath));
            }
        }
    }

}
