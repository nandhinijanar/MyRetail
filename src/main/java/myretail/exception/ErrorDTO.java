package myretail.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorDTO {

    @JsonProperty("error_message")
    private String errorMessage;

    public ErrorDTO() {
    }

    public ErrorDTO(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
