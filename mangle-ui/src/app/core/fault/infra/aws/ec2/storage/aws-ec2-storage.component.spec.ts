import {ComponentFixture, TestBed} from "@angular/core/testing";

import {NO_ERRORS_SCHEMA} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule} from "@angular/common";
import {ClarityModule} from "@clr/angular";
import {of} from "rxjs";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {AwsEC2StorageComponent} from "./aws-ec2-storage.component";
import {FaultService} from "../../../../fault.service";
import {Router} from "@angular/router";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe("AwsEC2StorageComponent", () => {
  let component: AwsEC2StorageComponent;
  let faultService: FaultService;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<AwsEC2StorageComponent>;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{path: "aws-ec2-storage", component: AwsEC2StorageComponent}])
      ],
      declarations: [AwsEC2StorageComponent],
      providers: [
        FaultService,
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(AwsEC2StorageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, "getAllEndpoints").and.returnValue(of([]));
    faultService = TestBed.get(FaultService);
    spyOn(faultService, "executeAwsEC2StorageFault").and.returnValue(of([component.faultFormData]));
    router = TestBed.get(Router);
    spyOn(router, "navigateByUrl");
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should execute aws ec2 instance storage fault", () => {
    component.executeAwsEC2StorageFault(component.faultFormData);
    expect(faultService.executeAwsEC2StorageFault).toHaveBeenCalled();
  });

});
