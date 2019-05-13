import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdentityComponent } from './identity.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { SettingService } from '../setting.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreService } from 'src/app/core/core.service';
import { CoreComponent } from 'src/app/core/core.component';

describe('IdentityComponent', () => {
  let component: IdentityComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<IdentityComponent>;

  let ad_data = { "id": null, "adDomain": "eso.local", "adUrl": "ldap://0.0.0.0" };
  let ad_data_id = { "id": "with_id", "adDomain": "eso.local", "adUrl": "ldap://0.0.0.0" };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'identity', component: IdentityComponent }])
      ],
      declarations: [IdentityComponent, CoreComponent],
      providers: [
        SettingService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(IdentityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getIdentities').and.returnValue(of({ "_embedded": { "aDAuthProviderDtoList": [ad_data] } }));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate identity form', () => {
    component.populateIdentityForm(ad_data);
    expect(component.identityFormData.adDomain).toBe("eso.local");
  });

  it('should get identities', () => {
    component.getIdentities();
    expect(component.identities[0].adDomain).toBe("eso.local");
    expect(settingService.getIdentities).toHaveBeenCalled();
  });

  it('should add or update identity source', () => {
    //add identity
    spyOn(settingService, 'addIdentitySource').and.returnValue(of({ "_embedded": { "aDAuthProviderDtoList": [ad_data_id] } }));
    component.addOrUpdateIdentitySource(ad_data);
    expect(component.successFlag).toBe(true);
    expect(settingService.addIdentitySource).toHaveBeenCalled();
    expect(settingService.getIdentities).toHaveBeenCalled();
    //update identity
    spyOn(settingService, 'updateIdentitySource').and.returnValue(of({ "_embedded": { "aDAuthProviderDtoList": [ad_data_id] } }));
    component.addOrUpdateIdentitySource(ad_data_id);
    expect(component.successFlag).toBe(true);
    expect(settingService.updateIdentitySource).toHaveBeenCalled();
    expect(settingService.getIdentities).toHaveBeenCalled();
  });

  it('should delete identity', () => {
    spyOn(settingService, 'deleteIdentity').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteIdentity(ad_data_id);
    expect(component.successFlag).toBe(true);
    expect(settingService.deleteIdentity).toHaveBeenCalled();
    expect(settingService.getIdentities).toHaveBeenCalled();
  });

});
