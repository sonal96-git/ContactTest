package com.pwc.util;
import javax.servlet.ServletRequest;

import com.day.cq.wcm.api.AuthoringUIMode;

public final class AuthoringUtils {

    /**
     * Checks if the components rendered in the passed request will be rendered for the Touch UI editor.
     *
     * @param request the request for which to check the editor type
     * @return {@code true} if the editor for the current request belongs to Touch UI, {@code false} otherwise
     */
    public static boolean isTouch(ServletRequest request) {
        AuthoringUIMode currentMode = AuthoringUIMode.fromRequest(request);
        return AuthoringUIMode.TOUCH == currentMode;
    }

    /**
     * Checks if the components rendered in the passed request will be rendered for the Classic UI editor.
     *
     * @param request the request for which to check the editor type
     * @return {@code true} if the editor for the current request belongs to Classic UI, {@code false} otherwise
     */
    public static boolean isClassic(ServletRequest request) {
        AuthoringUIMode currentMode = AuthoringUIMode.fromRequest(request);
        return AuthoringUIMode.CLASSIC == currentMode;
    }

}