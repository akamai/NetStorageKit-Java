package com.akamai;

import com.akamai.builders.APIEventDu;
import com.akamai.builders.APIEventMkDir;
import com.akamai.builders.APIEventMtime;

public class APIEventBuilder {
    public static APIEventMkDir mkdir() {
        return new APIEventMkDir();
    }

    public static APIEventDu du() {
        return new APIEventDu();
    }

    public static APIEventMtime mtime() {
        return new APIEventMtime();
    }
}
