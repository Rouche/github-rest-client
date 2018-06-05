package com.utilities.restclient.app.services;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.lib.ProgressMonitor;

public interface GitApiService {
    void clone(Repository repository, ProgressMonitor monitor);
}