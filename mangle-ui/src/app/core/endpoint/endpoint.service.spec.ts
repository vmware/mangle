import { TestBed } from '@angular/core/testing';

import { EndpointService } from './endpoint.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('EndpointService', () => {
  let endpointService: EndpointService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    endpointService = TestBed.get(EndpointService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
    spyOn(http, 'post');
    spyOn(http, 'put');
    spyOn(http, 'delete');
  });

  it('should be created', () => {
    expect(endpointService).toBeTruthy();
  });

  it('should get all endpoints', () => {
    endpointService.getAllEndpoints();
    expect(http.get).toHaveBeenCalled();
  });

  it('should get endpoints', () => {
    endpointService.getEndpoints('');
    expect(http.get).toHaveBeenCalled();
  });

  it('should get dockerContainers', () => {
    endpointService.getDockerContainers('');
    expect(http.get).toHaveBeenCalled();
  });

  it('should add endpoint', () => {
    endpointService.addEndpoint({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should update endpoint', () => {
    endpointService.updateEndpoint({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete endpoint', () => {
    endpointService.deleteEndpoint('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should get credentials', () => {
    endpointService.getCredentials();
    expect(http.get).toHaveBeenCalled();
  });

  it('should get certificates', () => {
    endpointService.getCertificates();
    expect(http.get).toHaveBeenCalled();
  });

  it('should add k8s credential', () => {
    endpointService.addk8sCredential({ "name": "name1" }, null);
    expect(http.post).toHaveBeenCalled();
  });

  it('should add remote machine credential', () => {
    endpointService.addRemoteMachineCredential({ "name": "name1", "username": "user1", "password": "" }, null);
    expect(http.post).toHaveBeenCalled();
  });

  it('should add vcenter credential', () => {
    endpointService.addVcenterCredential({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should add docker certificates', () => {
    endpointService.addDockerCertificates({ "name": "name1" }, null, null, null);
    expect(http.post).toHaveBeenCalled();
  });

  it('should update docker certificates', () => {
    endpointService.updateDockerCertificates({ "id": "id1", "name": "name1" }, null, null, null);
    expect(http.put).toHaveBeenCalled();
  });

  it('should update k8s credential', () => {
    endpointService.updatek8sCredential({ "name": "name1" }, null);
    expect(http.put).toHaveBeenCalled();
  });

  it('should update remote machine credential', () => {
    endpointService.updateRemoteMachineCredential({ "name": "name1", "username": "user1", "password": "" }, null);
    expect(http.put).toHaveBeenCalled();
  });

  it('should update vcenter credential', () => {
    endpointService.updateVcenterCredential({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete credential', () => {
    endpointService.deleteCredential('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should delete certificates', () => {
    endpointService.deleteCertificates('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should test endpoint connection', () => {
    endpointService.testEndpointConnection({});
    expect(http.post).toHaveBeenCalled();
  });

});
