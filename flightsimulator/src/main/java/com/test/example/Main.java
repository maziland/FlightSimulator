package com.test.example;

import java.util.ArrayList;
import java.util.List;

import com.test.TestRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        List<TestRunner> tests = new ArrayList<>();
        tests.add(new TestMainModel());
        tests.add(new TestMainViewModel());

        for (TestRunner t : tests) {
            t.Run();
        }
    }
}
