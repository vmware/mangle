/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;

/**
 * This is a Utils class for Command related Operations
 *
 * @author hkilari
 * @author bkaranam
 */
@Log4j2
public class CommandUtils implements ICommandExecutor {
    private static final long WAIT_TIME_FOR_PROCESS_DESTROY = 60L;
    private static final String LOG_MESSAGE = "Executing Commmand:";

    /**
     * Method to run system command
     *
     * @param command
     * @return String : output as an string
     */
    public static CommandExecutionResult runCommand(String command) {
        return runCommand(command, 0);
    }

    /**
     * Method to run system command
     *
     * @param command
     * @return String : output as an string
     */
    public static CommandExecutionResult runCommand(String command, long timeout) {
        StringBuilder commandOutput = new StringBuilder();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        log.debug(LOG_MESSAGE + command);
        try {
            Process process = Runtime.getRuntime().exec(createCommandArray(command));
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            if (timeout != 0) {
                process.waitFor(timeout, TimeUnit.SECONDS);
            } else {
                process.waitFor();
            }
            errorGobbler.join();
            outputGobbler.join();
            commandOutput.append(errorGobbler.getOutput());
            commandOutput.append(outputGobbler.getOutput());
            commandExecutionResult.setExitCode(process.exitValue());
            commandExecutionResult.setCommandOutput(commandOutput.toString());
            log.debug("Command Output:" + commandOutput.toString());
            return getCommandExecutionResult(process, commandExecutionResult);

        } catch (IOException | InterruptedException e) {
            log.error(e);
            commandOutput.append(e.getMessage());
            commandExecutionResult.setExitCode(500);
            commandExecutionResult.setCommandOutput(e.getMessage());
            Thread.currentThread().interrupt();
        }
        return commandExecutionResult;
    }

    private static CommandExecutionResult getCommandExecutionResult(Process process,
            CommandExecutionResult commandExecutionResult) throws IOException, InterruptedException {
        try {
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                terminateProcess(process);
                return commandExecutionResult;
            }
        } catch (IllegalThreadStateException ex) {
            terminateProcess(process);
            return commandExecutionResult;
        }
        return commandExecutionResult;
    }

    private static boolean terminateProcess(Process process) throws IOException, InterruptedException {
        process.getInputStream().close();
        process.getOutputStream().close();
        process.getErrorStream().close();
        process.destroyForcibly().waitFor(WAIT_TIME_FOR_PROCESS_DESTROY, TimeUnit.SECONDS);
        return process.isAlive();
    }

    /**
     * @return
     */
    private static String[] createCommandArray(String command) {
        String[] commandArray = new String[] { "bash", "-c", command };
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            commandArray = new String[] { "cmd", "/c", command };
        }
        return commandArray;
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult result = runCommand(command);
        log.trace("Executed command: " + command + "Command execution Result: " + result);
        return result;
    }
}
