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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.NumberConstants;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 *
 */
@Log4j2
public class RemoteHost {

    /**
     * Function Name ExecuteCommand
     *
     * @param ip
     *            : IP address of remote server
     * @param userName
     *            : UserName to login
     * @param password
     *            : Password to login
     * @param command
     *            : What command to execute
     * @return String : Output of the executed command
     *         <p>
     *         This function executes a command on the remote host and returns the output. If
     *         verbose is set, prints the output to stdout
     */
    public String executeCommand(String ip, String userName, String password, String command) {
        StringBuilder output = new StringBuilder();
        try {
            Connection connection = makeConnection(ip, userName, password);
            Session session = connection.openSession();
            session.execCommand(command);
            InputStream stdout = new StreamGobbler(session.getStdout());
            InputStream stderr = new StreamGobbler(session.getStderr());
            BufferedReader stdOutBufferedReader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader stdErrorBufferedReader = new BufferedReader(new InputStreamReader(stderr));
            while (true) {
                String outStream = stdOutBufferedReader.readLine();
                String errStream = stdErrorBufferedReader.readLine();
                if (StringUtils.isEmpty(outStream) && StringUtils.isEmpty(errStream)) {
                    break;
                }
                if (!StringUtils.isEmpty(outStream)) {
                    output.append(outStream + "\n");
                }
                if (StringUtils.isEmpty(outStream) && !StringUtils.isEmpty(errStream)) {
                    output.append(errStream + "\n");
                }
                if (stdErrorBufferedReader.ready()) {
                    errStream = errStream + stdErrorBufferedReader.readLine();
                    if (StringUtils.isEmpty(outStream) && !StringUtils.isEmpty(errStream)) {
                        output.append(errStream);
                    }
                    log.error(errStream);
                }
            }
            stdOutBufferedReader.close();
            stdErrorBufferedReader.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            output.append("Exception: " + e + ", Could not get Status...");
            log.error(e);
        }
        return output.toString();
    }

    /**
     * <p>
     * FromRemoteToNewFile Copies a Remote File To Local File
     *
     * @param ip
     *            IP address of remote server
     * @param userName
     *            UserName to login
     * @param password
     *            Password to login
     * @param src
     *            The file to scp on the remote host
     * @param dst
     *            The local File where it will be copied
     * @return int : Return code of the scp. RC_FAIL for Fail and RC_OK for Pass
     ****************************************************************************
     */
    public int fromRemoteToNewFile(String ip, String userName, String password, String src, String dst) {
        File dstFile = new File(dst);
        Connection conn = null;
        try {
            if (!dstFile.exists() && !dstFile.createNewFile()) {
                throw new MangleRuntimeException(ErrorCode.CREATE_FILE_OPERATION_FAILURE, dstFile.getPath());
            }
            conn = makeConnection(ip, userName, password);
        } catch (Exception e) {
            log.info("Exception occured in FromRemoteToNewFile: " + e.getMessage());
            log.debug("Exception occured in FromRemoteToNewFile: ", e);
            return -1;
        }
        try (FileOutputStream outS = new FileOutputStream(dst)) {
            SCPClient scp = new SCPClient(conn);
            scp.get(src, outS);
            conn.close();
        } catch (Exception e) {
            log.info("Exception occured in FromRemoteToNewFile: " + e.getMessage());
            log.debug("Exception occured in FromRemoteToNewFile: ", e);
            return -1;
        }
        return 1;
    }

    /**
     * The logic to support "keyboard-interactive" authentication method. For MN, "password" mode
     * authentacion for of SSH is not supported. As such, another mode of authentication:
     * "keyboard-interactive" has to be used. With this method, a class of type: InteractiveCallBack
     * has to be created so that when try to connect via SSH to a host/VM, the
     * connection.authenticate function can call the function: replyToChallenge in
     * InteractiveCallBack class get responses to the challenges presented.
     */
    class InteractiveLogic implements InteractiveCallback {
        String mPassword;

        public InteractiveLogic(String newPassword) {
            this.mPassword = newPassword;
        }

        // callback may be invoked several times
        @Override
        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
                boolean[] echo) throws IOException {
            String[] result = new String[numPrompts];
            int i = 0;
            for (String temp : prompt) {
                // temp may contain many things, for now, we only know how to
                // deal with
                // "Password". This will have to be updates as challenges change
                // or
                // are added to the SSH authentcation process.
                if ("Password:".contains(temp)) {
                    result[i] = mPassword;
                }
                i++;
            }
            return result;
        }

    }

    /**
     * Utility class to make Connection
     *
     * @param ip
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    private Connection makeConnection(String ip, String userName, String password) throws Exception {
        Connection conn = new Connection(ip);
        boolean isAuthenticated = false;
        boolean passwordAuthentication = false;
        boolean keyboardInteractive = false;
        conn.connect(null, Constants.SSH_CONNECT_TIMEOUT * NumberConstants.THOUSAND,
                Constants.SSH_KEX_TIMEOUT * NumberConstants.THOUSAND);
        for (String temp : conn.getRemainingAuthMethods(userName)) {
            if ("password".equalsIgnoreCase(temp)) {
                passwordAuthentication = true;
            } else if ("keyboard-interactive".equalsIgnoreCase(temp)) {
                keyboardInteractive = true;
            }
        }
        if (!passwordAuthentication && !keyboardInteractive) {
            throw new Exception("Authentication failed. Neither password, nor "
                    + "keyboard-interactive authentication methods are provided.");
        }
        if (passwordAuthentication) {
            isAuthenticated = conn.authenticateWithPassword(userName, password);
        } else if (keyboardInteractive) {
            InteractiveLogic il = new InteractiveLogic(password);
            isAuthenticated = conn.authenticateWithKeyboardInteractive(userName, il);
        }
        if (!isAuthenticated) {
            throw new IOException("Authentication failed.");
        }
        return conn;
    }
}
