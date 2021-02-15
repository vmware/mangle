import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import { RouterTestingModule } from '@angular/router/testing';
import { CoreComponent } from '../core.component';
import { CoreService } from '../core.service';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let coreService: CoreService;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([{ path: 'home', component: HomeComponent }])
      ],
      declarations: [HomeComponent, CoreComponent],
      providers: [
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
