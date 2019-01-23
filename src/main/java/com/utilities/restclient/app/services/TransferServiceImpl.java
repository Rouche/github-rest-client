package com.utilities.restclient.app.services;

import java.io.IOException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.GitHubService;
import lombok.Builder;
import lombok.Data;

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
