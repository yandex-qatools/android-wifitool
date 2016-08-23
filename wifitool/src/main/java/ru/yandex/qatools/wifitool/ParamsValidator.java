package ru.yandex.qatools.wifitool;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

class ParamsValidator {
    @Nonnull
    private static final List<String> VALID_SECURITY = new ArrayList<>();

    static {
        VALID_SECURITY.add(null);
        VALID_SECURITY.add(ParamNames.SECURITY_WEP);
        VALID_SECURITY.add(ParamNames.SECURITY_WPA);
    }

    final String message;
    final boolean isValid;

    ParamsValidator(Params params) {
        if (params.retryCount < 0) {
            message = "Retry count must be greater than or equal to 0";
            isValid = false;
            return;
        }

        if (params.retryDelay < 0) {
            message = "Retry delay must be greater than or equal to 0";
            isValid = false;
            return;
        }

        if (params.quotedSsid == null) {
            message = "ssid not specified";
            isValid = false;
            return;
        }

        if (!VALID_SECURITY.contains(params.securityString)) {
            message = "Unknown securityString value '" + params.securityString + "'";
            isValid = false;
            return;
        }

        if (params.pass == null && params.securityString != null) {
            message = "pass not specified while securityString is '" + params.securityString + "'";
            isValid = false;
            return;
        }

        isValid = true;
        message = null;
    }
}
