import { TestBed } from '@angular/core/testing';

import { FaultService } from './fault.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('FaultService', () => {
  let faultService: FaultService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    faultService = TestBed.get(FaultService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'post');
  });

  it('should be created', () => {
    expect(faultService).toBeTruthy();
  });

  it('should execute cpu fault', () => {
    faultService.executeCpuFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute memory fault', () => {
    faultService.executeMemoryFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute disk IO fault', () => {
    faultService.executeDiskIOFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute kill process fault', () => {
    faultService.executeKillProcessFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute docker state change fault', () => {
    faultService.executeDockerStateChangeFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute K8S delete resource fault', () => {
    faultService.executeCpuFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute K8S resource not ready fault', () => {
    faultService.executeK8SResourceNotReadyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter disk fault', () => {
    faultService.executeVcenterDiskFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter nic fault', () => {
    faultService.executeVcenterNicFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter state fault', () => {
    faultService.executeVcenterStateFault({});
    expect(http.post).toHaveBeenCalled();
  });

});
