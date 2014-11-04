package com.sachinshinde.wordlearner;

/**
 * Created by sachin on 21/10/14.
 */

import java.io.IOException;

/**
 * FIXME PIG-80 replace this code when pig will be java 6 compliant with "throw
 * new IOException(e);"
 */
public class WrappedIOException {

    public static IOException wrap(final Throwable e) {
        return wrap(e.getMessage(), e);
    }

    public static IOException wrap(final String message, final Throwable e) {
        final IOException wrappedException = new IOException(message + " ["
                + e.getMessage() + "]");
        wrappedException.setStackTrace(e.getStackTrace());
        wrappedException.initCause(e);
        return wrappedException;
    }
}