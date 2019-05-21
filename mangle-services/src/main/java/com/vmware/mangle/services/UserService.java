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

package com.vmware.mangle.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UsernameDomain;
import com.vmware.mangle.model.enums.DefaultRoles;
import com.vmware.mangle.services.repository.UserRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Service
@Log4j2
public class UserService {

    UserRepository userRepository;

    RoleService roleService;

    PrivilegeService privilegeService;

    ADAuthProviderService authProviderService;

    PasswordEncoder passwordEncoder;

    private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    public UserService(UserRepository userRepository, PrivilegeService privilegeService,
            ADAuthProviderService adAuthProviderService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.privilegeService = privilegeService;
        this.authProviderService = adAuthProviderService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public Set<Role> getRoleForUser(String username) {
        log.info("Retrieving role for the user: " + username);
        Set<Role> roles = null;
        User user = userRepository.findByName(username);
        if (user != null) {
            user.setRoles(new HashSet<>(roleService.getRolesByNames(user.getRoleNames())));
            roles = user.getRoles();
            log.info(String.format("Found %d roles for user %s", roles.size(), username));
            log.debug(String.format("Roles for user %s are: %s", username, roles.toString()));
        } else {
            log.info(String.format("User %s does not have any rules defined", username));
        }
        return roles;
    }

    public User getUserByName(String username) {
        log.info("Retrieving User for the username: " + username);
        User user = userRepository.findByName(username);
        if (user != null) {
            user.setRoles(new HashSet<>(roleService.getRolesByNames(user.getRoleNames())));
        }
        return user;
    }

    public User updateUser(User user) throws MangleException {
        User dbUser = getUserByName(user.getName());

        if (dbUser == null) {
            log.error(String.format("Failed to find the user %s, execution of the updateUser failed", user.getName()));
            throw new MangleException(ErrorConstants.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND, user.getName());
        }
        dbUser.setRoleNames(user.getRoleNames());
        dbUser.setPassword(user.getPassword());
        return createUser(dbUser);
    }

    public void updatePassword(String username, String currentPassword, String newPassword) throws MangleException {
        User dbUser = getUserByName(username);
        if (dbUser == null) {
            log.error("Failed to find the user {}, execution of the updateUser failed", username);
            throw new MangleException(ErrorConstants.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND, username);
        }

        if (!passwordEncoder.matches(currentPassword, dbUser.getPassword())) {
            log.error("Failed to update the password for the user {}, incorrect current password ", username);
            throw new MangleException(ErrorConstants.CURRENT_PWD_MISMATCH, ErrorCode.CURRENT_PASSWORD_MISMATCH);
        }
        dbUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(dbUser);
    }

    public User createUser(User user) throws MangleException {
        log.info("Creating user " + user.getName());
        UsernameDomain usernameDomain = extractUserAndDomain(user.getName());
        if (usernameDomain.getDomain() == null || (!usernameDomain.getDomain().equals(Constants.LOCAL_DOMAIN_NAME)
                && !authProviderService.getAllDomains().contains(usernameDomain.getDomain()))) {
            throw new MangleException(ErrorConstants.INVALID_DOMAIN_NAME, ErrorCode.INVALID_DOMAIN_NAME);
        }

        if (CollectionUtils.isEmpty(user.getRoleNames())) {
            user.setRoleNames(new HashSet<>());
        }

        user.getRoleNames().add(DefaultRoles.ROLE_READONLY.name());

        user.setRoles(getPersitentRoles(user.getRoleNames()));
        if (usernameDomain.getDomain().equals(getDefaultDomainName())
                && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        log.info("Retrieving all the users");
        return userRepository.findAll();
    }

    public String getCurrentUserName() {
        Authentication authentication = getCurrentAuthentication();
        String currentUser = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                currentUser = ((UserDetails) principal).getUsername();
            } else {
                currentUser = principal.toString();
            }
        }
        return currentUser;
    }

    public User getCurrentUser() {
        String currentUser = getCurrentUserName();
        return userRepository.findByName(currentUser);
    }

    public void deleteUsersByNames(List<String> usernames) throws MangleException {
        String currentUser = getCurrentUserName();
        List<User> persistedUsers = userRepository.findByNameIn(usernames);
        Set<String> persistedUserNames = persistedUsers.stream().map(User::getName).collect(Collectors.toSet());

        if (persistedUserNames.contains(currentUser)) {
            throw new MangleException(ErrorCode.CURRENT_USER_DELETION_FAILED);
        }

        if (persistedUserNames.contains(Constants.MANGLE_DEFAULT_USER)) {
            throw new MangleException(ErrorConstants.DEFAULT_MANGLE_USER_DELETE_FAIL, ErrorCode.DEFAULT_MANGLE_USER_DELETE_FAIL);
        }

        usernames.removeAll(persistedUserNames);

        if (!CollectionUtils.isEmpty(usernames)) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ROLE, usernames.toString());
        }

        // Terminating Sessions for the Deleted Users
        for (String username : persistedUserNames) {
            terminateUserSession(username);
        }
        userRepository.deleteByNameIn(persistedUserNames);
    }

    public Set<Role> getPersitentRoles(Set<String> roles) throws MangleException {
        Set<Role> persistentRoles = new HashSet<>();
        for (String role : roles) {
            Role dbRole = roleService.getRoleByName(role);
            if (dbRole != null) {
                persistentRoles.add(dbRole);
            } else {
                log.error(String.format("Failed to find the role %s, execution of the createUser failed", role));
                throw new MangleException(ErrorConstants.ROLE_NOT_FOUND, ErrorCode.ROLE_NOT_FOUND, role);
            }
        }
        return persistentRoles;
    }

    public Role getDefaultUserRole() {
        return roleService.getRoleByName(DefaultRoles.ROLE_READONLY.name());
    }

    public List<Privilege> getPrivilegeForUser(String username) {
        Set<Role> roles = getRoleForUser(username);
        List<Privilege> privileges = new ArrayList<>();
        for (Role role : roles) {
            privileges.addAll(privilegeService.getPrivilegeByNames(role.getPrivilegeNames()));
        }
        return privileges;
    }

    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    /**
     * Decodes the header into a username and password.
     *
     * @throws BadCredentialsException
     *             if the Basic header is not present or is not valid Base64
     */
    private UsernameDomain extractUserAndDomain(String userAndDomain) {
        int delim = userAndDomain.indexOf('@');
        if (delim == -1) {
            return new UsernameDomain(userAndDomain, null);
        }
        String username = userAndDomain.substring(0, delim);
        String domain = userAndDomain.substring(delim + 1);
        return new UsernameDomain(username, domain);
    }

    public List<User> getUsersForRole(String role) {
        return userRepository.findByRole(role);
    }

    public String getDefaultDomainName() {
        return Constants.LOCAL_DOMAIN_NAME;
    }

    public void terminateCurrentSession() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        for (javax.servlet.http.Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
        }
    }

    /**
     * Function to get the Users from Session Registry
     *
     * @return list of the Users
     *
     */
    public List<String> getUsersFromSessionRegistry() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty()).map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Function to get the UserName for the provided session
     *
     * @param session
     *            Provide Session Info
     * @return userName who uses that provided session
     *
     */
    public String getUserFromSession(SessionInformation session) {
        Object principalObj = session.getPrincipal();
        if (principalObj instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user =
                    (org.springframework.security.core.userdetails.User) principalObj;
            return user.getUsername();
        }
        return null;
    }

    /**
     * Function to get the ActiveSessions
     *
     * @return the list of the ActiveSessions
     *
     */
    public List<SessionInformation> getAllActiveSessions() {
        List<SessionInformation> activeSessions = new ArrayList<>();
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            activeSessions.addAll(sessionRegistry.getAllSessions(principal, false));
        }
        return activeSessions;
    }

    /**
     * Funciton to create HashMap where Users as key and List of Sessions Used by that user as
     * values
     *
     * @return hashMap
     *
     */
    public HashMap<String, List<SessionInformation>> allUserToSessionsMapping() {
        HashMap<String, List<SessionInformation>> usersToSessionsMapping =
                new HashMap<String, List<SessionInformation>>();
        List<String> users = getUsersFromSessionRegistry();
        List<SessionInformation> sessions = getAllActiveSessions();
        if (!CollectionUtils.isEmpty(users) && !CollectionUtils.isEmpty(sessions)) {
            for (SessionInformation session : sessions) {
                String user = getUserFromSession(session);
                if (user != null) {
                    List<SessionInformation> sessionList;
                    if (!(usersToSessionsMapping.containsKey(user))) {
                        sessionList = new ArrayList<SessionInformation>();
                    } else {
                        sessionList = usersToSessionsMapping.get(user);
                    }
                    sessionList.add(session);
                    usersToSessionsMapping.put(user, sessionList);
                }
            }
            return usersToSessionsMapping;
        }
        return null;
    }

    /**
     * Function to Terminate the Session for the provided User
     *
     * @param user
     *            provide User
     *
     */
    public void terminateUserSession(String user) {
        HashMap<String, List<SessionInformation>> usersToSessionsMapping = allUserToSessionsMapping();
        if (usersToSessionsMapping.containsKey(user)) {
            ListIterator iterator = usersToSessionsMapping.get(user).listIterator();
            while (iterator.hasNext()) {
                SessionInformation session = (SessionInformation) iterator.next();
                if (!(session.isExpired())) {
                    log.info("Calling Session Expire method for the User " + user);
                    session.expireNow();
                    log.info("Session expired for the User " + user);
                } else {
                    log.info("Session Already Expired for user " + user);
                }
            }
        } else {
            log.info("The User " + user + " not Exists/logged In");
        }
    }

}
