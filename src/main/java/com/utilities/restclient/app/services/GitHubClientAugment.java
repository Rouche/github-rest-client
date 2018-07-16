package com.utilities.restclient.app.services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Created by jealar2 on 2018-06-05
 */
public class GitHubClientAugment extends GitHubClient {

    private String credentials;
    private String userAgent = "GitHubJava/2.1.5";
    private String accept = "application/vnd.github.beta+json";
    private String acceptNightshade = null;

    public void enableNightshade() {
        this.acceptNightshade = "application/vnd.github.nightshade-preview+json";
    }

    @Override
    public GitHubClient setOAuth2Token(String token) {
        if (token != null && token.length() > 0) {
            this.credentials = "token " + token;
        } else {
            this.credentials = null;
        }

        return super.setOAuth2Token(token);
    }

    public GitHubResponse patch(GitHubRequest request) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPatch connection = new HttpPatch(new URI(this.createUri(request.generateUri())));
            connection.setHeader("Authorization", this.credentials);
            String accept = request.getResponseContentType();
            if (accept != null) {
                connection.setHeader("Accept", accept);
            }

            CloseableHttpResponse response = httpClient.execute(connection);
            if (this.isEmpty(response.getStatusLine().getStatusCode())) {
                return new GitHubResponse(null, null);
            } else {
                throw new RuntimeException("Error: " + response.getEntity().toString());
            }
        } catch(Exception e) {
            throw new RuntimeException("Error: ", e);
        }
    }

    @Override
    protected HttpURLConnection configureRequest(HttpURLConnection request) {
        if (this.credentials != null) {
            request.setRequestProperty("Authorization", this.credentials);
        }

        request.setRequestProperty("User-Agent", this.userAgent);
        request.setRequestProperty("Accept", acceptNightshade == null ? accept : acceptNightshade);
        acceptNightshade = null;
        return request;
    }
}
