package com.dumbphone.powertoggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class RootShell {
    public static final class Result {
        public final int exitCode;
        public final String output;

        Result(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public boolean succeeded() {
            return exitCode == 0 && output.contains("POWER_TOGGLE_OK");
        }
    }

    private RootShell() {}

    public static Result run(String command) throws Exception {
        Process process = new ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append('\n');
        }
        int exit = process.waitFor();
        return new Result(exit, output.toString());
    }
}
