package com.utilities.restclient.app.api.v3;

import lombok.Data;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import java.util.Date;

/**
 * Created by jealar2 on 2018-06-04
 */
@Data
public class Invitation {

    private String id;
    private Repository repository;
    private User invitee;
    private User inviter;
    private String permissions;
    private Date created_at;
    private String url;
    private String html_url;
}