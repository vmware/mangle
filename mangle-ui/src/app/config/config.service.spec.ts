import { TestBed } from '@angular/core/testing';

import { ConfigService } from './config.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ConfigService', () => {
  let configService: ConfigService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
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
    configService.updateLocalUserConfig({});
    expect(http.put).toHaveBeenCalled();
  });

});