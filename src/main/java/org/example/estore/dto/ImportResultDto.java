package org.example.estore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/** Итог импорта данных из zip-архива с CSV-файлами */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResultDto {

    private boolean success;
    private String message;
    private List<FileReport> files = new ArrayList<>();

    public static FileReport fileReport(String fileName) {
        return new FileReport(fileName);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FileReport> getFiles() {
        return files;
    }

    public void setFiles(List<FileReport> files) {
        this.files = files;
    }

    public static class FileReport {
        private final String file;
        private int added;
        private int updated;
        private int skippedErrors;
        private List<String> errors = new ArrayList<>();

        public FileReport(String file) {
            this.file = file;
        }

        public void addError(int rowNumber, String error) {
            errors.add("строка " + rowNumber + ": " + error);
            skippedErrors++;
        }

        public String getFile() {
            return file;
        }

        public int getAdded() {
            return added;
        }

        public void setAdded(int added) {
            this.added = added;
        }

        public int getUpdated() {
            return updated;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }

        public int getSkippedErrors() {
            return skippedErrors;
        }

        public void setSkippedErrors(int skippedErrors) {
            this.skippedErrors = skippedErrors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
