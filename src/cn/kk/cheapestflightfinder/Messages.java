package cn.kk.cheapestflightfinder;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "cn.kk.cheapestflightfinder.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Messages.BUNDLE_NAME,
            new UTF8Control());

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return Messages.RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
