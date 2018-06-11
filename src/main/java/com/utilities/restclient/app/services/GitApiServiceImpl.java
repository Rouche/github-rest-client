package com.utilities.restclient.app.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class GitApiServiceImpl implements GitApiService {

    private static final String REPOS_URI = "user/repos";

    private final String api_token;
    private final String project_git_root;

    public GitApiServiceImpl(@Value("${api.token}") String api_token,
                             @Value("${project.git.root}") String project_git_root) {

        this.api_token = api_token;
        this.project_git_root = project_git_root;
    }

    @Override
    public void clone(Repository repository, ProgressMonitor monitor) {
        try {
            Git git = Git.cloneRepository()
                    .setURI( repository.getCloneUrl() )
                    .setDirectory( new File(project_git_root + repository.getName()) )
                    .setCloneAllBranches( true )
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider( api_token, "" ) )
                    .setProgressMonitor(monitor)
                    .call();

            git.close();

        } catch (GitAPIException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean exists(Repository repository) {

        return (new File(project_git_root + repository.getName())).exists();
    }
}