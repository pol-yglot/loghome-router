package com.example.loghome_project.model;

/**
 * 배치 실행 결과를 담는 Model 클래스
 */
public class BatchResult {
    private String message;
    private String output;
    private int exitCode;
    private boolean success;

    public BatchResult() {
    }

    public BatchResult(String message, String output, int exitCode, boolean success) {
        this.message = message;
        this.output = output;
        this.exitCode = exitCode;
        this.success = success;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "BatchResult{" +
                "message='" + message + '\'' +
                ", output='" + output + '\'' +
                ", exitCode=" + exitCode +
                ", success=" + success +
                '}';
    }
}
