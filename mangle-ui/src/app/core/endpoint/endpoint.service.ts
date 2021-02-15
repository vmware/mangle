import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from 'src/app/common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class EndpointService {

    constructor(private http: HttpClient) { }

    public getAllEndpoints(): Observable<any> {
        return this.http.get(ServiceConstants.ENDPOINTS);
    }

    public getEndpoints(endPointType): Observable<any> {
        return this.http.get(ServiceConstants.ENDPOINTS_TYPE + ServiceConstants.FILE_SEPARATOR + endPointType);
    }

    public addEndpoint(endpoint) {
        this.removeExtraArgs(endpoint);
        return this.http.post(ServiceConstants.ENDPOINTS_V2, endpoint);
    }

    public updateEndpoint(endpoint) {
        this.removeExtraArgs(endpoint);
        return this.http.put(ServiceConstants.ENDPOINTS_V2, endpoint);
    }

    public enableEndpoints(endpointNames, enableFlag: boolean) {
        return this.http.post(ServiceConstants.ENDPOINTS_ENABLE + CommonConstants.QUESTION_MARK + "enable" + CommonConstants.EQUALS_TO + enableFlag + CommonConstants.AND_OP + "names" + CommonConstants.EQUALS_TO + endpointNames, null);
    }

    public deleteEndpoint(name) {
        return this.http.delete(ServiceConstants.ENDPOINTS + CommonConstants.QUESTION_MARK + CommonConstants.endpointNames + CommonConstants.EQUALS_TO + name);
    }

    public getCredentials(): Observable<any> {
        return this.http.get(ServiceConstants.ENDPOINTS_CREDENTIALS);
    }

    public getCertificates(): Observable<any> {
        return this.http.get(ServiceConstants.ENDPOINTS_CERTIFICATES);
    }

    public getDockerContainers(endPointName): Observable<any> {
        return this.http.get(ServiceConstants.ENDPOINTS_DOCKER_CONTAINER + ServiceConstants.FILE_SEPARATOR + endPointName);
    }

    public addk8sCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append(CommonConstants.name_var, credential.name);
        body.append(CommonConstants.kubeConfig, fileToUpload);
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_K8S, body);
    }

    public addRemoteMachineCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append(CommonConstants.name_var, credential.name);
        body.append(CommonConstants.username, credential.username);
        if (typeof credential.password != undefined && credential.password != "" && credential.password != null) {
            body.append(CommonConstants.password, credential.password);
        }
        else {
            body.append(CommonConstants.privateKey, fileToUpload);
        }
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_REMOTE_MACHINE, body);
    }

    public addVcenterCredential(credential) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_VCENTER, credential);
    }

    public addDockerCertificates(certificates, caCert, serverCert, privateKey) {
        let body = new FormData();
        body.append(CommonConstants.name_var, certificates.name);
        body.append(CommonConstants.caCert, caCert);
        body.append(CommonConstants.serverCert, serverCert);
        body.append(CommonConstants.privateKey, privateKey);
        return this.http.post(ServiceConstants.ENDPOINTS_CERTIFICATES_DOCKER, body);
    }

    public updateDockerCertificates(certificates, caCert, serverCert, privateKey) {
        let body = new FormData();
        body.append(CommonConstants.id, certificates.id);
        body.append(CommonConstants.name_var, certificates.name);
        body.append(CommonConstants.caCert, caCert);
        body.append(CommonConstants.serverCert, serverCert);
        body.append(CommonConstants.privateKey, privateKey);
        return this.http.put(ServiceConstants.ENDPOINTS_CERTIFICATES_DOCKER, body);
    }

    public updatek8sCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append(CommonConstants.name_var, credential.name);
        body.append(CommonConstants.kubeConfig, fileToUpload);
        return this.http.put(ServiceConstants.ENDPOINTS_CREDENTIALS_K8S, body);
    }

    public updateRemoteMachineCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append(CommonConstants.name_var, credential.name);
        body.append(CommonConstants.username, credential.username);
        if (typeof credential.password != undefined && credential.password != "" && credential.password != null) {
            body.append(CommonConstants.password, credential.password);
        }
        else {
            body.append(CommonConstants.privateKey, fileToUpload);
        }
        return this.http.put(ServiceConstants.ENDPOINTS_CREDENTIALS_REMOTE_MACHINE, body);
    }

    public updateVcenterCredential(credential) {
        return this.http.put(ServiceConstants.ENDPOINTS_CREDENTIALS_VCENTER, credential);
    }

    public addAwsCredential(credential) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_AWS, credential);
    }

    public updateAwsCredential(credential) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_AWS, credential);
    }

    public addAzureCredential(credential) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_AZURE, credential);
    }

    public updateAzureCredential(credential) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_AZURE, credential);
    }

    public deleteCredential(name) {
        return this.http.delete(ServiceConstants.ENDPOINTS_CREDENTIALS + CommonConstants.QUESTION_MARK + CommonConstants.credentialNames + CommonConstants.EQUALS_TO + name);
    }

    public deleteCertificates(name) {
        return this.http.delete(ServiceConstants.ENDPOINTS_CERTIFICATES + '?' + CommonConstants.certificatesNames + '=' + name);
    }

    public testEndpointConnection(endpoint): Observable<any> {
        this.removeExtraArgs(endpoint);
        return this.http.post(ServiceConstants.TEST_ENDPOINTS, endpoint);
    }

    public removeExtraArgs(endpoint) {
        if (typeof endpoint.dockerConnectionProperties !== "undefined") {
            if (endpoint.dockerConnectionProperties.certificatesName == "" || endpoint.dockerConnectionProperties.certificatesName == null) {
                delete endpoint.dockerConnectionProperties[CommonConstants.certificatesName];
            }
        }
    }

    public getVCenterAdapterDetails(): Observable<any> {
        return this.http.get(ServiceConstants.VCENTER_ADAPTER_DETAILS_URL);
    }

    public updateVCenterAdapterDetails(vCenterAdapterDetail): Observable<any> {
        return this.http.put(ServiceConstants.VCENTER_ADAPTER_DETAILS_URL, vCenterAdapterDetail);
    }

    public addVCenterAdapterDetails(vCenterAdapterDetail): Observable<any> {
        return this.http.post(ServiceConstants.VCENTER_ADAPTER_DETAILS_URL, vCenterAdapterDetail);
    }

    public deleteVCenterAdapterDetails(vCenterAdapterDetailsNames) {
        return this.http.delete(ServiceConstants.VCENTER_ADAPTER_DETAILS_URL + "?adapterDetailsNames=" + vCenterAdapterDetailsNames);
    }

    public testVCenterAdapterConnection(vCenterAdapterDetail) {
        return this.http.post(ServiceConstants.VCENTER_ADAPTER_DETAILS_TEST_CONNECTION_URL, vCenterAdapterDetail);
    }

    public addDatabaseCredential(credential: any) {
        return this.http.post(ServiceConstants.ENDPOINTS_CREDENTIALS_DATABASE, credential);
    }

    public updateDatabaseCredential(credential: any) {
        return this.http.put(ServiceConstants.ENDPOINTS_CREDENTIALS_DATABASE, credential);
    }
}
