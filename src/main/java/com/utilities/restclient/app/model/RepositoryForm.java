package com.utilities.restclient.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jealar2 on 2018-06-11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryForm {

    @Builder.Default
    private List<RepositoryItem> repositoryItems = new ArrayList<>();
}
