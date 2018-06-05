package com.utilities.restclient.app.services;

import com.google.gson.reflect.TypeToken;
import com.utilities.restclient.app.api.v3.Invitation;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.GitHubService;

import java.io.IOException;
import java.util.List;

/**
 * Created by jealar2 on 2018-06-04
 */
public class InvitationServiceImpl extends GitHubService implements InvitationService {

    private static final String LIST_INVITATIONS_URI = "/user/repository_invitations";
    private static final String ACCEPT_INVITATION_URI = "/user/repository_invitations/";

    public InvitationServiceImpl(GitHubClientAugment client) {
        super(client);
    }

    @Override
    public List<Invitation> getInvitations() throws IOException {
        PagedRequest<Repository> request = this.createPagedRequest(1, 100);
        request.setUri(LIST_INVITATIONS_URI);
        request.setType((new TypeToken<List<Invitation>>() {
        }).getType());

        PageIterator<Invitation> iterator = new PageIterator(request, this.client);

        return this.getAll(iterator);
    }

    @Override
    public GitHubResponse acceptInvitation(Invitation invitation) throws IOException {
        GitHubRequest request = this.createRequest();
        request.setUri(ACCEPT_INVITATION_URI + invitation.getId());
        return ((GitHubClientAugment)this.client).patch(request);
    }

}
