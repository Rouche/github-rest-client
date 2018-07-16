package com.utilities.restclient.app.controllers;

import com.utilities.restclient.app.api.v3.Invitation;
import com.utilities.restclient.app.jgit.CloneProgress;
import com.utilities.restclient.app.model.RepositoryForm;
import com.utilities.restclient.app.model.RepositoryItem;
import com.utilities.restclient.app.model.UserPayload;
import com.utilities.restclient.app.services.*;
import com.utilities.restclient.app.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class ReposController {

    private final GitApiService gitApiService;
    private final String api_token;
    private final String api_user;

    private final GitHubClientAugment client;

    public ReposController(GitApiService gitApiService, @Value("${master.api.token}") String api_token,
                           @Value("${master.api.user}") String api_user) {

        client = new GitHubClientAugment();
        client.setOAuth2Token(api_token);

        this.gitApiService = gitApiService;
        this.api_token = api_token;
        this.api_user = api_user;
    }

    /**
     * Index
     * @param model model
     */
    @GetMapping({"", "/", "/index"})
    public String index(Model model) {

        model.addAttribute("user", api_user);

        return "index";
    }

    /**
     * List repositories owned by {@link GitHubClient} see master.api.user
     * @param model model
     * @return view
     */
    @GetMapping("/repositories")
    public String repositories(Model model) {

        RepositoryService service = new RepositoryService(client);
        try {
            List<Repository> repositories = service.getRepositories();
            RepositoryForm repositoryForm = RepositoryForm.builder().build();
            repositories.forEach( repository -> {
                repositoryForm.getRepositoryItems().add(
                        RepositoryItem.builder()
                                .repository(repository)
                                .url(repository.getUrl())
                                .selected(gitApiService.exists(repository))
                                .build());
            });

            model.addAttribute("repositoryForm", repositoryForm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("user", api_user);

        return "github/repositories";
    }

    /**
     * Add a collaborator to every selected repositories
     * @param newLogin Payload body
     * @param model model
     * @return view
     */
    @PostMapping("/repositories/collaborators")
    public String addCollaborator(UserPayload newLogin, Model model) {

        RepositoryService service = new RepositoryService(client);
        CollaboratorService collaboratorService = new CollaboratorService(client);
        List<Repository> repositories = null;
        try {
            repositories = service.getRepositories();
            repositories.forEach(repository -> {
                try {
                    List<User> users = collaboratorService.getCollaborators(repository);
                    String login = newLogin.getLogin();
                    if(users.stream().noneMatch(user -> user.getLogin().equals(login))) {
                        collaboratorService.addCollaborator(repository, login);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("repositories", repositories);

        return "redirect:/repositories";
    }

    /**
     * Transfer a repository to a new Owner
     * @param newLogin Payload body
     * @param model model
     * @return view
     */
    @PostMapping("/repositories/transfer")
    public String transfer(UserPayload newLogin, Model model) {

        RepositoryService service = new RepositoryService(client);
        TransferService transferService = new TransferServiceImpl(client);

        List<Repository> repositories = null;
        try {
            repositories = service.getRepositories();
            repositories.forEach(repository -> {
                try {
                    if(repository.getOwner().getLogin().equals(newLogin.getLogin())) {
                        return;
                    }
                    transferService.transfer(repository, newLogin.getLogin());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("repositories", repositories);

        return "redirect:/repositories";
    }

    /**
     * List collabvorators for a repository
     * @param owner Repo owner
     * @param name name of repo
     * @param model model
     * @return view
     */
    @GetMapping("/repositories/{owner}/{name}/collaborators")
    public String collaborators(@PathVariable("owner") String owner, @PathVariable("name") String name,
                                Model model) {

        CollaboratorService collaboratorService = new CollaboratorService(client);
        try {
            model.addAttribute("collaborators", collaboratorService.getCollaborators(new Repository()
                    .setName(name).setOwner(new User().setLogin(owner))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("owner", owner);
        model.addAttribute("name", name);

        return "github/collaborators";
    }

    /**
     * Add a collaborator to one repository
     * @param owner Repo owner
     * @param name name of repo
     * @param newLogin login payload
     * @param model model
     * @return view
     */
    @PostMapping("/repositories/{owner}/{name}/collaborators")
    public String addCollaborator(@PathVariable("owner") String owner,
                                  @PathVariable("name") String name,
                                  UserPayload newLogin, Model model) {

        CollaboratorService collaboratorService = new CollaboratorService(client);
        try {
            String login = newLogin.getLogin();
            Repository repository = new Repository().setName(name).setOwner(new User().setLogin(owner));
            collaboratorService.addCollaborator(repository, login);
            model.addAttribute("collaborators", collaboratorService.getCollaborators(repository));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("repository", name);

        return "redirect:/repositories";
    }

    @GetMapping("/invitations")
    public String getInvitations(@Value("${rouche.api.token}") String token, Model model) {

        GitHubClientAugment roucheClient = new GitHubClientAugment();
        roucheClient.setOAuth2Token(token);

        try {
            InvitationService service = new InvitationServiceImpl(roucheClient);
            List<Invitation> invitations = service.getInvitations();

            model.addAttribute("invitations", invitations);
            model.addAttribute("user", "rouche");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "github/invitations";

    }

    @GetMapping("/invitations/acceptall")
    public String acceptAllInvitations(@Value("${rouche.api.token}") String token) {

        GitHubClientAugment roucheClient = new GitHubClientAugment();
        roucheClient.setOAuth2Token(token);

        try {
            InvitationService service = new InvitationServiceImpl(roucheClient);
            List<Invitation> invitations = service.getInvitations();

            invitations.forEach( invitation -> {
                try {
                    service.acceptInvitation(invitation);
                } catch (IOException e) {
                    log.error("Error accepting invitation", e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/index";

    }

    /**
     * Clone a repository.
     * Uses jGit directly.
     * @param response http response
     * @param repositoryForm form
     * @param model model
     */
    @PostMapping("/clone")
    public void cloneRepositories(HttpServletResponse response,
                                  @ModelAttribute("repositoryForm") RepositoryForm repositoryForm, Model model) {

        RepositoryService service = new RepositoryService(client);
        List<Repository> resultList = new ArrayList<>();

        try (Writer w = new PrintWriter(response.getOutputStream())) {
            List<Repository> repos = service.getRepositories();
            List<RepositoryItem> items = repositoryForm.getRepositoryItems();

            writeHead(w);
            IOUtils.write(w, "<div>Receiving repos</div>");
            for (MutableInt i = new MutableInt(); i.intValue() < repos.size(); i.add(1)) {

                Repository repository = repos.get(i.intValue());

                int index = items.indexOf(RepositoryItem.builder().url(repository.getUrl()).build());
                if(items.get(index).isSelected()) {
                    IOUtils.write(w, "<div>Starting repository [" + repository.getUrl() + "]</div>");

                    gitApiService.clone(repository, new CloneProgress(w, i.intValue()));
                    resultList.add(repository);
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
        model.addAttribute("repositories", resultList);
    }

    private void writeHead(Writer w) throws Exception {
        w.write("<html lang=\"en\">");

        w.write("<head>");
        w.write("<title>Repository</title>");
        w.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
        w.write("<meta name=\"description\" content=\"\"/>");
        w.write("<meta name=\"keywords\" content=\"\"/>");
        w.write("<meta name=\"robots\" content=\"index,nofollow\"/>");
        w.write("<meta charset=\"UTF-8\"/>");

        w.write("<title>Show Recipe</title>");

        w.write("<link rel=\"stylesheet\" href=\"/css/custom.css\">");

        w.write("<link rel=\"stylesheet\" href=\"/webjars/bootstrap/4.0.0-2/css/bootstrap.min.css\"");
        w.write("crossorigin=\"anonymous\">");

        w.write("        <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css\"");
        w.write("crossorigin=\"anonymous\">");

        w.write("<script src=\"/webjars/jquery/3.3.1-1/jquery.min.js\"");
        w.write("crossorigin=\"anonymous\" integrity=\"sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=\"></script>");
        w.write("<script src=\"/webjars/popper.js/1.14.1/umd/popper.js\"");
        w.write("crossorigin=\"anonymous\" integrity=\"sha256-1bI2hYpoXO8605goiOg+TiWksCVw2RBxWRtnvzPRWN0=\"></script>");
        w.write("<script src=\"/webjars/bootstrap/4.0.0-2/js/bootstrap.min.js\"");
        w.write("crossorigin=\"anonymous\" integrity=\"\"></script>    </head>");

        w.write("<body>");
        w.flush();
    }
}