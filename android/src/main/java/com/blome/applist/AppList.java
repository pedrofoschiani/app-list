package com.blome.applist;

import com.getcapacitor.Logger;

public class AppList {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
