package com.utilities.restclient.app.services;

import com.google.gson.reflect.TypeToken;
import com.utilities.restclient.app.api.v3.Invitation;
import lombok.Builder;
import lombok.Data;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.GitHubService;

import java.io.IOException;
import java.util.List;

@Data
@Builder
class TransferPayload {
    String new_owner;
}

/**
 * Created by jealar2 on 2018-06-04
 */
public class TransferServiceImpl extends GitHubService implements TransferService {

    private static final String TRANSFER_URI = "/repos/${owner}/${repo}/transfer";

    public TransferServiceImpl(GitHubClientAugment client) {
        super(client);
    }

    @Override
    public GitHubResponse transfer(Repository repository, String login) throws IOException {

        String uri = TRANSFER_URI.replace("${owner}", repository.getOwner().getLogin()).replace("${repo}", repository.getName());
        ((GitHubClientAugment)this.client).enableNightshade();
        return client.post(uri, TransferPayload.builder().new_owner(login), null);
    }

}
