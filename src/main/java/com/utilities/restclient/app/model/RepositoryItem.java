package com.utilities.restclient.app.model;

import lombok.*;
import org.eclipse.egit.github.core.Repository;

import java.util.Objects;

/**
 * Created by jealar2 on 2018-06-11
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryItem {
    private boolean selected;
    private Repository repository;
    private String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepositoryItem that = (RepositoryItem) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(url);
    }
}
