package org.littil.api.metrics;

public class LittilMetrics {
    private static String metric() {
        return "[LITTIL] Metric:";
    }

    public static class Registration {
        private static String registration() {
            return metric() + "registration-";
        }

        public static String userCreatedInOidc() {
            return registration() + "user-created-oidc";
        }

        public static String userPersisted() {
            return registration() + "user-persisted";
        }

        public static String mailSent() {
            return registration() + "mail-sent";
        }
    }

    public static class Contact {
        private static String contact() {
            return metric() + "contact-";
        }

        public static String mailSent() {
            return contact() + "mail-sent";
        }
    }
}
