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

package com.vmware.mangle.java.agent.faults.helpers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.vmware.mangle.java.agent.faults.exception.FiaascoException;


/**
 * This is a Utils class for Command related Operations
 *
 * @author hkilari
 * @author bkaranam
 */

public class CommandUtils implements ICommandExecutor {
    private static final Logger LOG = Logger.getLogger(CommandUtils.class.getName());
    private static final long WAIT_TIME_FOR_PROCESS_DESTROY = 60L;
    private static String LogMessage = "Executing Commmand:";

    private CommandUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Method to run system command
     *
     * @param command
     * @return String : output as an string
     */
    public static CommandExecutionResult runCommandReturningResult(String command) {
        return runCommandReturningResult(command, 0);
    }

    /**
     * Method to run system command
     *
     * @param command
     * @return String : output as an string
     */
    public static CommandExecutionResult runCommandReturningResult(String command, long timeout) {
        StringBuilder commandOutput = new StringBuilder();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        LOG.info(LogMessage + command);
        try {
            String[] commandArray = createCommandArray(command);
            Process process = Runtime.getRuntime().exec(commandArray);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            if (timeout != 0) {
                process.waitFor(timeout, TimeUnit.SECONDS);
            } else {
                process.waitFor();
            }
            commandOutput.append(errorGobbler.getOutput());
            commandOutput.append(outputGobbler.getOutput());
            commandExecutionResult.setExitCode(process.exitValue());
            commandExecutionResult.setCommandOutput(commandOutput.toString());
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
        } catch (IOException | InterruptedException e) {
            LOG.severe(e.getMessage());
            commandOutput.append(e.getMessage());
            commandExecutionResult.setExitCode(500);
            commandExecutionResult.setCommandOutput(e.getMessage());
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
        return runCommandReturningResult(command);
    }

    /**
     * Utility Class to execute list of the commands Defined in Fault for Injection, Remediation and
     * Test Machine Preperation
     *
     * @param commandInfos
     */
    public static void runCommands(List<CommandInfo> commandInfos) throws FiaascoException {
        if (commandInfos != null && !commandInfos.isEmpty()) {
            for (CommandInfo commandInfo : commandInfos) {
                CommandExecutionResult commandExecutionResult = runCommandReturningResult(commandInfo.getCommand());
                //Condition to validate if the Command Execution completed with valid exit code
                if (!commandInfo.isIgnoreExitValueCheck() && commandExecutionResult.getExitCode() != 0) {
                    throw new FiaascoException("Execution of Command: " + commandInfo.getCommand() + " exited with "
                            + commandExecutionResult.getExitCode());

                }
                //Condition to validate the command execution by verification of command execution result
                if (commandInfo.getExpectedCommandOutput() != null && !commandExecutionResult.getCommandOutput()
                        .contains(commandInfo.getExpectedCommandOutput())) {
                    throw new FiaascoException("Execution of Command does not Contain expected String: "
                            + commandInfo.getExpectedCommandOutput());
                }
            }
        }
    }
}
