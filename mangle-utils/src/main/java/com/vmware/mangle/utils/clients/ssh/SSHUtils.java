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

package com.vmware.mangle.utils.clients.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.RemoteBase;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.constants.NumberConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */
@Log4j2
public class SSHUtils extends RemoteBase implements ICommandExecutor, EndpointClient {
    public static final int DEFAULT_PORT = 22;
    public static final int EXIT_STATUS_CODE = 0;
    public static final String FORMAT_LINE_MSG = "--------------------------------------------";

    public SSHUtils(String host, String userName, String password, int port) {
        super(host, userName, password, port);
    }

    public SSHUtils(String host, String userName, String password, int port, int timeout) {
        super(host, userName, password, port, timeout);
    }

    public SSHUtils(String host, String userName, int port, String privateKey) {
        super(host, userName, port, privateKey);
    }

    public SSHUtils(String host, String userName, int port, String privateKey, int timeout) {
        super(host, userName, port, privateKey, timeout);
    }

    public SSHUtils(String host, String userName, String password, int port, String privateKey, int timeout) {
        super(host, userName, password, port, privateKey, timeout);
    }

    public boolean login() {
        try {
            Session session = getSession();
            session.connect();
            log.debug(FORMAT_LINE_MSG);
            log.debug("successfully Connected");
            log.debug(FORMAT_LINE_MSG);
            session.disconnect();
        } catch (JSchException e) {
            log.error(e);
            return false;
        }
        return true;
    }

    public CommandExecutionResult runCommand(String command) {
        return runCommandReturningResult(command, 0);
    }

    public boolean putFile(String sourceFilePath, String workDir) throws MangleException {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        log.info("preparing the host information for sftp.");

        try {
            session = this.getSession();
            session.connect();
            log.info("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            log.info("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            if (workDir != null) {
                channelSftp.cd(workDir);
            }
            File f = new File(sourceFilePath);
            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                channelSftp.put(fileInputStream, f.getName());
            }
        } catch (SftpException se) {
            log.error("Exception found while changing the directory", se.getMessage());
            throw new MangleException(se.getMessage(), ErrorCode.DIRECTORY_NOT_FOUND, workDir);
        } catch (Exception e) {
            log.error("Exception found while tranfer the response.", e);
            throw new MangleException(e.getMessage(), ErrorCode.FILE_TRANSFER_ERROR, sourceFilePath,workDir);
        } finally {
            cleanupSession(session, channelSftp, channel);
        }

        return true;
    }

    public boolean getFile(String sourceFileName, String destinationFileName, String workDir) {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            session = this.getSession();
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            if (workDir != null) {
                channelSftp.cd(workDir);
            }

            File newFile = new File(destinationFileName);
            FileUtils.copyInputStreamToFile(channelSftp.get(sourceFileName), newFile);
        } catch (Exception arg14) {
            log.error("Exception found while tranfer the response.", arg14);
        } finally {
            cleanupSession(session, channelSftp, channel);
        }

        return true;
    }

    // channelTimeout in Seconds
    public CommandExecutionResult runCommandReturningResult(String command, int channelTimeout) {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            log.info("Running Command ...");
            Session session = getSession();
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            InputStream ext = channel.getExtInputStream();
            channel.connect();
            log.debug("SSH Connected Successfully");
            commandExecutionResult.setCommandOutput(getCommandExecutionOutput(channel, in, channelTimeout)
                    + getCommandExecutionOutput(channel, ext, channelTimeout));
            commandExecutionResult.setExitCode(channel.getExitStatus());
            log.info("Command-output: " + commandExecutionResult.getCommandOutput());
            log.info("exit-status: " + commandExecutionResult.getExitCode());
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            log.error(e);
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(500);
            return commandExecutionResult;
        }
        return commandExecutionResult;
    }

    private String getCommandExecutionOutput(Channel channel, InputStream in, int channelTimeout) {
        byte[] tmp = new byte[NumberConstants.MEGA_VALUE];
        StringBuilder output = new StringBuilder();
        long end = System.currentTimeMillis() + (channelTimeout) * (NumberConstants.THOUSAND);
        try {
            while (true) {
                int i;
                while (in.available() > NumberConstants.ZERO) {
                    i = in.read(tmp, NumberConstants.ZERO, NumberConstants.MEGA_VALUE);
                    if (i < NumberConstants.ZERO) {
                        break;
                    }
                    output.append(new String(tmp, NumberConstants.ZERO, i));

                }
                if ((channel.isClosed()) || (channelTimeout > 0 && System.currentTimeMillis() >= end)) {
                    break;
                }

                Thread.sleep(NumberConstants.THOUSAND);
            }
        } catch (InterruptedException | IOException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
        return output.toString();
    }

    private void cleanupSession(Session session, ChannelSftp channelSftp, Channel channel) {
        if (channelSftp != null) {
            channelSftp.exit();
            log.info("sftp Channel exited.");
        }

        if (channel != null) {
            channel.disconnect();
            log.info("Channel disconnected.");
        }

        if (session != null) {
            session.disconnect();
        }

        log.info("Host Session disconnected.");
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult result = runCommand(command);
        log.trace("Executed command: " + command + "Command execution Result: " + result);
        return result;
    }

    @Override
    public boolean testConnection() {
        try {
            Session session = getSession();
            session.connect();
            session.disconnect();
            return true;
        } catch (JSchException exception) {
            return false;
        }
    }
}
