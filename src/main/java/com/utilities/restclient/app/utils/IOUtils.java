package com.utilities.restclient.app.utils;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by jealar2 on 2018-06-04
 */
public class IOUtils {

    public static void write(Writer w, String s) throws IOException {
        w.write(s);
        w.flush();
    }

}
