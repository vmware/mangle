import { TestBed } from '@angular/core/testing';

import { RequestsService } from './requests.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('RequestsService', () => {
  let requestsService: RequestsService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    requestsService = TestBed.get(RequestsService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
    spyOn(http, 'post');
    spyOn(http, 'delete');
  });

  it('should be created', () => {
    expect(requestsService).toBeTruthy();
  });

  it('should get all tasks', () => {
    requestsService.getAllTasks();
    expect(http.get).toHaveBeenCalled();
  });

  it('should get task by Id', () => {
    requestsService.getTaskById('');
    expect(http.get).toHaveBeenCalled();
  });

  it('should get all schedule jobs', () => {
    requestsService.getAllScheduleJobs();
    expect(http.get).toHaveBeenCalled();
  });

  it('should delete schedule only', () => {
    requestsService.deleteScheduleOnly('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should delete schedule', () => {
    requestsService.deleteSchedule('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should cancel schedule', () => {
    requestsService.cancelSchedule('');
    expect(http.post).toHaveBeenCalled();
  });

  it('should pause schedule', () => {
    requestsService.pauseSchedule('');
    expect(http.post).toHaveBeenCalled();
  });

  it('should resume schedule', () => {
    requestsService.resumeSchedule('');
    expect(http.post).toHaveBeenCalled();
  });

  it('should delete task', () => {
    requestsService.deleteTask('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should remediate fault', () => {
    requestsService.remediateFault('');
    expect(http.delete).toHaveBeenCalled();
  });

  it('should get log file', () => {
    requestsService.getLogFile();
    expect(http.get).toHaveBeenCalled();
  });

});
