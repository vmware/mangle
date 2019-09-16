import { TestBed } from '@angular/core/testing';

import { SharedService } from './shared.service';
import { HttpClient, HttpClientModule } from '@angular/common/http';

describe('SharedService', () => {
  let sharedService: SharedService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
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
