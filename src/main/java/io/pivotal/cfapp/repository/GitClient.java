package io.pivotal.cfapp.repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@ConditionalOnProperty(prefix = "cf.policies", name = "provider", havingValue = "git")
public class GitClient {
	
	public Repository getRepository(String uri) throws GitAPIException, IOException {
		Assert.hasText(uri, "URI of remote Git repository must be specified");
		Assert.isTrue(uri.endsWith(".git"), "URI must end with .git");
		String path = String.join(File.separator, "tmp", uri.substring(uri.lastIndexOf("/")).replace(".git",""));
		File directory = new File(path);
		Path p = Paths.get(directory.toURI());
		if (Files.exists(p)) {
    		Files
    			.walk(p)
	    	    .sorted(Comparator.reverseOrder())
	    	    .map(Path::toFile)
	    	    .forEach(File::delete);
    		}
		Git
			.cloneRepository()
				.setURI(uri)
				.setDirectory(directory)
				.setCloneAllBranches(true)
				.call();
		return Git.open(directory).getRepository();
	}
	
    public String readFile(Repository repo, String commitId, String filepath) throws IOException {
    	ObjectId oid = repo.resolve(commitId);
        RevCommit commit = repo.parseCommit(oid);
    	try (TreeWalk walk = TreeWalk.forPath(repo, filepath, commit.getTree())) {
            if (walk != null) {
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("No path found.");
            }
        }
    }
}
