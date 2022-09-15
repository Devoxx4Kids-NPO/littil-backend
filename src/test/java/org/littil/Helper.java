package org.littil;

import java.util.ResourceBundle;

public class Helper {
    public static String getErrorMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }
}
