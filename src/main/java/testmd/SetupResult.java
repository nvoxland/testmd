package testmd;

public interface SetupResult {

    public static SetupResult.OkResult OK = new SetupResult.OkResult();

    boolean isValid();

    boolean canVerify();

    String getMessage();

    public static class Invalid implements SetupResult {

        private String message;

        public Invalid(String message) {
            this.message = message;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean canVerify() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public static class CannotVerify implements SetupResult {

        private String message;

        public CannotVerify(String message) {
            this.message = message;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }


    public static class OkResult implements SetupResult {

        public OkResult() {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return true;
        }

        @Override
        public String getMessage() {
            return null;
        }
    }

}
