import { TestBed } from '@angular/core/testing';

import { FaultService } from './fault.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('FaultService', () => {
  let faultService: FaultService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
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
  it('should execute K8S drain node fault', () => {
    faultService.executeCpuFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute K8S resource not ready fault', () => {
    faultService.executeK8SResourceNotReadyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter disk fault', () => {
    faultService.executeVCenterVMDiskFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter nic fault', () => {
    faultService.executeVCenterVMNicFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute vcenter state fault', () => {
    faultService.executeVCenterVMStateFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute thread-leak fault', () => {
    faultService.executeThreadLeakFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute filehandler-leak fault', () => {
    faultService.executeFilehandlerLeakFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute java-method-latency fault', () => {
    faultService.executeJavaMethodLatencyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute spring-service-latency fault', () => {
    faultService.executeSpringServiceLatencyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute spring-service-exception fault', () => {
    faultService.executeSpringServiceExceptionFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute kill-jvm fault', () => {
    faultService.executeKillJVMFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute simulate-java-exception fault', () => {
    faultService.executeSimulateJavaExceptionFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute network fault', () => {
    faultService.executeNetworkFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute disk space fault', () => {
    faultService.executeDiskSpaceFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute kernel panic fault', () => {
    faultService.executeKernelPanicFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute clock skew fault', () => {
    faultService.executeClockSkewFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute db connection leak fault', () => {
    faultService.executeConnectionLeakFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute transaction error fault', () => {
    faultService.executeTransactionErrorFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should get transaction error codes', () => {
    spyOn(http, 'get');
    faultService.getTransactionErrorCode('test');
    expect(http.get).toHaveBeenCalled();
  });

  it('should execute transaction latency fault', () => {
    faultService.executeTransactionLatencyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute redis db delay fault', () => {
    faultService.executeRedisDelayFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute redis db return error fault', () => {
    faultService.executeRedisReturnErrorFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute redis db return empty fault', () => {
    faultService.executeRedisReturnEmptyFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute redis db drop connection fault', () => {
    faultService.executeRedisDropConnectionFault({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should execute Network Partition fault', () => {
    faultService.executeNetworkPartitionFault({});
    expect(http.post).toHaveBeenCalled();
  });

});
