import { TestBed } from '@angular/core/testing';

import { SettingService } from './setting.service';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SettingService', () => {
  let settingService: SettingService;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    settingService = TestBed.get(SettingService);
    http = TestBed.get(HttpClient);
    spyOn(http, 'get');
    spyOn(http, 'post');
    spyOn(http, 'put');
    spyOn(http, 'delete');
  });

  it('should be created', () => {
    expect(settingService).toBeTruthy();
  });

  it('should get domains', () => {
    settingService.getDomains();
    expect(http.get).toHaveBeenCalled();
  });

  it('should add identity source', () => {
    settingService.addIdentitySource({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should update identity source', () => {
    settingService.updateIdentitySource({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete identity', () => {
    settingService.deleteIdentity("");
    expect(http.delete).toHaveBeenCalled();
  });

  it('should get local user list', () => {
    settingService.getLocalUserList();
    expect(http.get).toHaveBeenCalled();
  });

  it('should add local user', () => {
    settingService.addLocalUser({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should update password', () => {
    settingService.updatePassword({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should update local user', () => {
    settingService.updateLocalUser({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete local user', () => {
    settingService.deleteLocalUser("");
    expect(http.delete).toHaveBeenCalled();
  });

  it('should add user', () => {
    settingService.addUser({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should update user', () => {
    settingService.updateUser({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete user', () => {
    settingService.deleteUser("");
    expect(http.delete).toHaveBeenCalled();
  });

  it('should get privilege list', () => {
    settingService.getPrivilegeList();
    expect(http.get).toHaveBeenCalled();
  });

  it('should get role list', () => {
    settingService.getRoleList();
    expect(http.get).toHaveBeenCalled();
  });

  it('should add role', () => {
    settingService.addRole({});
    expect(http.post).toHaveBeenCalled();
  });

  it('should update role', () => {
    settingService.updateRole({});
    expect(http.put).toHaveBeenCalled();
  });

  it('should delete role', () => {
    settingService.deleteRole("");
    expect(http.delete).toHaveBeenCalled();
  });

  it('should get loggers', () => {
    settingService.getLoggers();
    expect(http.get).toHaveBeenCalled();
  });

  it('should update logger', () => {
    settingService.updateLogger({});
    expect(http.post).toHaveBeenCalled();
  });

});
