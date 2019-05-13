import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from '../common/service.constants';
import { CommonConstants } from '../common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class SettingService {

    constructor(private http: HttpClient) { }

    public getIdentities(): Observable<any> {
        return this.http.get(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS);
    }

    public getDomains(): Observable<any> {
        return this.http.get(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_DOMAINS);
    }

    public addIdentitySource(identitySource) {
        return this.http.post(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS, identitySource);
    }

    public updateIdentitySource(identitySource) {
        return this.http.put(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS, identitySource);
    }

    public deleteIdentity(domainName) {
        return this.http.delete(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS + CommonConstants.QUESTION_MARK + CommonConstants.domainNames + CommonConstants.EQUALS_TO + domainName);
    }

    public getLocalUserList(): Observable<any> {
        return this.http.get(ServiceConstants.USER_MANAGEMENT_USERS);
    }

    public addLocalUser(user) {
        return this.http.post(ServiceConstants.USER_MANAGEMENT_USERS, user);
    }

    public updatePassword(passwordBody) {
        return this.http.put(ServiceConstants.USER_MANAGEMENT_PASSWORD, passwordBody);
    }

    public updateLocalUser(user) {
        return this.http.put(ServiceConstants.USER_MANAGEMENT_USERS, user);
    }

    public deleteLocalUser(name) {
        return this.http.delete(ServiceConstants.USER_MANAGEMENT_USERS + CommonConstants.QUESTION_MARK + CommonConstants.usernames + CommonConstants.EQUALS_TO + name);
    }

    public getUserList(): Observable<any> {
        return this.http.get(ServiceConstants.USER_MANAGEMENT_USERS);
    }

    public addUser(user) {
        return this.http.post(ServiceConstants.USER_MANAGEMENT_USERS, user);
    }

    public updateUser(user) {
        return this.http.put(ServiceConstants.USER_MANAGEMENT_USERS, user);
    }

    public deleteUser(name) {
        return this.http.delete(ServiceConstants.USER_MANAGEMENT_USERS + CommonConstants.QUESTION_MARK + CommonConstants.usernames + CommonConstants.EQUALS_TO + name);
    }

    public getPrivilegeList(): Observable<any> {
        return this.http.get(ServiceConstants.ROLE_MANAGEMENT_PRIVILEGES);
    }

    public getRoleList(): Observable<any> {
        return this.http.get(ServiceConstants.ROLE_MANAGEMENT_ROLES);
    }

    public addRole(role) {
        return this.http.post(ServiceConstants.ROLE_MANAGEMENT_ROLES, role);
    }

    public updateRole(role) {
        return this.http.put(ServiceConstants.ROLE_MANAGEMENT_ROLES, role);
    }

    public deleteRole(name) {
        return this.http.delete(ServiceConstants.ROLE_MANAGEMENT_ROLES + CommonConstants.QUESTION_MARK + CommonConstants.roles + CommonConstants.EQUALS_TO + name);
    }

    public getLoggers(): Observable<any> {
        return this.http.get(ServiceConstants.LOGGERS);
    }

    public updateLogger(logger) {
        return this.http.post(ServiceConstants.LOGGERS + ServiceConstants.FILE_SEPARATOR + logger.name, logger.configProp);
    }

}
