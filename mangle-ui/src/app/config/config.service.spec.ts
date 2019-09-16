import { TestBed } from '@angular/core/testing';

import { ConfigService } from './config.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('ConfigService', () => {
  let configService: ConfigService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    configService = TestBed.get(ConfigService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'put');
  });

  it('should be created', () => {
    expect(configService).toBeTruthy();
  });

  it('should update local user config', () => {
    configService.updateLocalUserConfig({}, "");
    expect(http.put).toHaveBeenCalled();
  });

});
