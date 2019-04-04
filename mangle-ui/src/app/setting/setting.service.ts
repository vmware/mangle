import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class SettingService {

    constructor(private http: HttpClient) { }

    public getIdentities(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/auth-provider-management/ad-auth-providers');
    }

    public addIdentitySource(identitySource) {
        return this.http.post('/mangle-services/rest/api/v1/auth-provider-management/ad-auth-providers', identitySource);
    }

    public updateIdentitySource(identitySource) {
        return this.http.put('/mangle-services/rest/api/v1/auth-provider-management/ad-auth-providers', identitySource);
    }

    public deleteIdentity(domainName) {
        return this.http.delete('/mangle-services/rest/api/v1/auth-provider-management/ad-auth-providers?domainNames=' + domainName);
    }

    public getLocalUserList(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/authentication-management/users');
    }

    public addLocalUser(user) {
        return this.http.post('/mangle-services/rest/api/v1/authentication-management/users', user);
    }

    public updateLocalUser(user) {
        return this.http.put('/mangle-services/rest/api/v1/authentication-management/users', user);
    }

    public updateLocalUserConfig(user, oldPassword): Observable<any> {
        return this.http.put('/mangle-services/rest/api/v1/authentication-management/users', user, { observe: 'response', headers: new HttpHeaders().set('Content-Type', 'application/json').set('Authorization', 'Basic ' + btoa(user.username + ':' + oldPassword)) });
    }

    public deleteLocalUser(name) {
        return this.http.delete('/mangle-services/rest/api/v1/authentication-management/users?usernames=' + name);
    }

    public getUserList(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/user-management/users');
    }

    public addUser(user) {
        return this.http.post('/mangle-services/rest/api/v1/user-management/users', user);
    }

    public updateUser(user) {
        return this.http.put('/mangle-services/rest/api/v1/user-management/users', user);
    }

    public deleteUser(name) {
        return this.http.delete('/mangle-services/rest/api/v1/user-management/users?users=' + name);
    }

    public getPrivilegeList(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/role-management/privileges');
    }

    public getRoleList(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/role-management/roles');
    }

    public addRole(role) {
        return this.http.post('/mangle-services/rest/api/v1/role-management/roles', role);
    }

    public updateRole(role) {
        return this.http.put('/mangle-services/rest/api/v1/role-management/roles', role);
    }

    public deleteRole(name) {
        return this.http.delete('/mangle-services/rest/api/v1/role-management/roles?roles=' + name);
    }

    public getLoggers(): Observable<any> {
        return this.http.get('/mangle-services/application/loggers');
    }

    public updateLogger(logger) {
        return this.http.post('/mangle-services/application/loggers/' + logger.name, logger.configProp);
    }

}
