import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';

describe('AuthService', () => {
  let authService: AuthService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    authService = TestBed.get(AuthService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
  });

  it('should be created', () => {
    expect(authService).toBeTruthy();
  });

  it('should login', () => {
    authService.login({});
    expect(http.get).toHaveBeenCalled();
  });

  it('should get auth sources', () => {
    authService.getAuthSources();
    expect(http.get).toHaveBeenCalled();
  });

});
