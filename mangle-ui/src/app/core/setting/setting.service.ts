import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceConstants } from '../../common/service.constants';
import { CommonConstants } from '../../common/common.constants';

@Injectable({
  providedIn: 'root'
})
export class SettingService {

  constructor(private http: HttpClient) {
  }

  public getIdentities(): Observable<any> {
    return this.http.get(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS);
  }

  public getDomains(): Observable<any> {
    return this.http.get(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_DOMAINS);
  }

  public addIdentitySource(identitySource) {
    return this.http.post(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS_V2, identitySource);
  }

  public updateIdentitySource(identitySource) {
    return this.http.put(ServiceConstants.AUTH_PROVIDER_MANAGEMENT_PROVIDERS_V2, identitySource);
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

  public downloadSupportBundle() {
    return this.http.get(ServiceConstants.APPLICATION_SUPPORT_BUNDLE, { responseType: 'arraybuffer' });
  }

  public getCluster(): Observable<any> {
    return this.http.get(ServiceConstants.CLUSTER_CONFIG);
  }

  public getPluginDetails(): Observable<any> {
    return this.http.get(ServiceConstants.PLUGIN_DETAILS);
  }

  public getPluginFiles(): Observable<any> {
    return this.http.get(ServiceConstants.PLUGIN_FILES);
  }

  public detelePluginFile(pluginFileName): Observable<any> {
    return this.http.delete(ServiceConstants.PLUGIN_FILES + CommonConstants.QUESTION_MARK + CommonConstants.fileName + CommonConstants.EQUALS_TO + pluginFileName);
  }

  public uploadPlugin(pluginFileToUpload) {
    const body = new FormData();
    body.append(CommonConstants.file_var, pluginFileToUpload);
    return this.http.post(ServiceConstants.PLUGIN_FILES, body);
  }

  public deletePlugin(pluginId) {
    return this.http.delete(ServiceConstants.PLUGINS + CommonConstants.QUESTION_MARK + CommonConstants.pluginId + CommonConstants.EQUALS_TO + pluginId);
  }

  public performPluginAction(pluginInfo) {
    return this.http.put(ServiceConstants.PLUGINS, pluginInfo);
  }

  public updateClusterQuorumValue(quorumValue) {
    return this.http.post(ServiceConstants.CLUSTER_QUORUM_UPDATE + CommonConstants.QUESTION_MARK + CommonConstants.quorumValue + CommonConstants.EQUALS_TO + quorumValue, {});
  }

  public updateClusterDeploymentMode(deploymentMode) {
    return this.http.post(ServiceConstants.CLUSTER_DEPLOYMENT_MODE_UPDATE + CommonConstants.QUESTION_MARK + CommonConstants.deploymentType + CommonConstants.EQUALS_TO + deploymentMode, {});
  }

  public testADConnection(identityInfo): Observable<any> {
    return this.http.post(ServiceConstants.AUTH_PROVIDER_TEST_CONNECTION, identityInfo);
  }

  public getResiliencyScoreMetricConfig(): Observable<any> {
    return this.http.get(ServiceConstants.RESILIENCY_SCORE_METRIC_CONFIG_URL);
  }

  public addResiliencyScoreMetricConfig(metricConfig: any): Observable<any> {
    return this.http.post(ServiceConstants.RESILIENCY_SCORE_METRIC_CONFIG_URL, metricConfig);
  }

  public updateResiliencyScoreMetricConfig(metricConfig: any): Observable<any> {
    return this.http.put(ServiceConstants.RESILIENCY_SCORE_METRIC_CONFIG_URL, metricConfig);
  }

  public deleteResiliencyScoreMetricConfig(name: any): Observable<any> {
    return this.http.delete(ServiceConstants.RESILIENCY_SCORE_METRIC_CONFIG_URL
      + CommonConstants.QUESTION_MARK + CommonConstants.metricConfigName + CommonConstants.EQUALS_TO + name);
  }

}
