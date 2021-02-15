import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('AuthService', () => {
  let authService: AuthService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    authService = TestBed.get(AuthService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
    spyOn(http, 'post');
  });

  it('should be created', () => {
    expect(authService).toBeTruthy();
  });

  it('should login', () => {
    authService.login({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should get auth sources', () => {
    authService.getAuthSources();
    expect(http.get).toHaveBeenCalled();
  });

});
