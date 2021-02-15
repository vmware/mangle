import { TestBed } from '@angular/core/testing';

import { SharedService } from './shared.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SharedService', () => {
  let sharedService: SharedService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    sharedService = TestBed.get(SharedService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
  });

  it('should be created', () => {
    expect(SharedService).toBeTruthy();
  });

  it('should get app events', () => {
    sharedService.getAppEvents();
    expect(http.get).toHaveBeenCalled();
  });

});
