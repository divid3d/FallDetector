package com.example.divided.falldetector.model;

import java.io.File;

public class TestSignal {
    private File path;
    private String testResult;

    public TestSignal(File path,String testResult){
        this.path = path;
        this.testResult = testResult;
    }

    public File getPath() {
        return path;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }
}
