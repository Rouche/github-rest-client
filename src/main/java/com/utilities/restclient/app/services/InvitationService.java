package com.utilities.restclient.app.services;

import com.utilities.restclient.app.api.v3.Invitation;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.IOException;
import java.util.List;

public interface InvitationService {
    List<Invitation> getInvitations() throws IOException;

    GitHubResponse acceptInvitation(Invitation invitation) throws IOException;
}