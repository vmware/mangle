import { TestBed } from '@angular/core/testing';

import { CoreService } from './core.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('CoreService', () => {
  let coreService: CoreService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    coreService = TestBed.get(CoreService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
  });

  it('should be created', () => {
    expect(coreService).toBeTruthy();
  });

  it('should get my details', () => {
    coreService.getMyDetails();
    expect(http.get).toHaveBeenCalled();
  });

  it('should get my roles and privileges', () => {
    coreService.getMyRolesAndPrivileges("");
    expect(http.get).toHaveBeenCalled();
  });

});
