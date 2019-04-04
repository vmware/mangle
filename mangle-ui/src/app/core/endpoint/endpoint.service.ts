import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class EndpointService {

    constructor(private http: HttpClient) { }

    public getAllEndpoints(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/endpoints');
    }

    public getEndpoints(endPointType): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/endpoints/type/' + endPointType);
    }

    public addEndpoint(endpoint) {
        return this.http.post('/mangle-services/rest/api/v1/endpoints', endpoint);
    }

    public updateEndpoint(endpoint) {
        return this.http.put('/mangle-services/rest/api/v1/endpoints', endpoint);
    }

    public deleteEndpoint(name) {
        return this.http.delete('/mangle-services/rest/api/v1/endpoints?endpointNames=' + name);
    }

    public getCredentials(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/endpoints/credentials');
    }

    public addk8sCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append("name", credential.name);
        body.append("kubeConfig", fileToUpload);
        return this.http.post('/mangle-services/rest/api/v1/endpoints/credentials/k8s', body);
    }

    public addRemoteMachineCredential(credential, fileToUpload) {
        let body = new FormData();
        body.append("name", credential.name);
        body.append("username", credential.username);
        body.append("password", credential.password);
        body.append("privateKey", fileToUpload);
        return this.http.post('/mangle-services/rest/api/v1/endpoints/credentials/remotemachine', body);
    }

    public addVcenterCredential(credential) {
        return this.http.post('/mangle-services/rest/api/v1/endpoints/credentials/vcenter', credential);
    }

    public deleteCredential(name) {
        return this.http.delete('/mangle-services/rest/api/v1/endpoints/credentials?credentialNames=' + name);
    }

    public testEndpointConnection(endpoint): Observable<any> {
        return this.http.post('/mangle-services/rest/api/v1/endpoints/testEndpoint', endpoint);
    }

}
