package pro.sky.telegrambot.exception;

public class WrongDataException extends RuntimeException {
    public WrongDataException() {
    }

    public WrongDataException(String message) {
        super(message);
    }

    public WrongDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongDataException(Throwable cause) {
        super(cause);
    }

    public WrongDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
