package com.utilities.restclient.app.jgit;

import com.utilities.restclient.app.utils.IOUtils;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by jealar2 on 2018-06-04
 */
public class CloneProgress implements ProgressMonitor {

    private final int current;
    private final Writer w;

    private String taskTitle;
    private int taskWork;
    private int currentWork;

    public CloneProgress(Writer w, int current) {
        this.current = current;
        this.w = w;
    }

    @Override
    public void start(int subtasks) {
        try {
            IOUtils.write(w, "[" + subtasks + "] subtasks</div>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beginTask(String title, int total) {
        try {
            this.taskTitle = title.replace(':', '_').replace(' ', '-');
            this.taskWork = total;
            this.currentWork = 0;
            IOUtils.write(w, "<div>" + title + "</div>");
            IOUtils.write(w, "<div id=\"progress" + current + taskTitle + "\"></div>");
            IOUtils.write(w, "<script>$('html, body').animate({ scrollTop: $(document).height() }, 'slow');</script>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(int i) {
        if (taskWork == UNKNOWN) {
            return;
        }
        try {
            currentWork += i;
            IOUtils.write(w, "<script>$('#progress" + current + taskTitle + "').html('" + (currentWork * 100) / taskWork + "%')</script>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTask() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
