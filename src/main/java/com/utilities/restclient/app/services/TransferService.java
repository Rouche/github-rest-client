package com.utilities.restclient.app.services;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.IOException;

public interface TransferService {
    GitHubResponse transfer(Repository repository, String login) throws IOException;
}