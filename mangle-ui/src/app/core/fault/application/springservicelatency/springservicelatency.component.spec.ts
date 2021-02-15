import {ComponentFixture, TestBed} from "@angular/core/testing";

import {NO_ERRORS_SCHEMA} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule} from "@angular/common";
import {ClarityModule} from "@clr/angular";
import {of} from "rxjs";
import {SpringServiceLatencyComponent} from "./springservicelatency.component";
import {FaultService} from "../../fault.service";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {Router} from "@angular/router";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe("SpringServiceLatencyComponent", () => {
  let component: SpringServiceLatencyComponent;
  let faultService: FaultService;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<SpringServiceLatencyComponent>;
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
        RouterTestingModule.withRoutes([{path: "spring-service-latency", component: SpringServiceLatencyComponent}])
      ],
      declarations: [SpringServiceLatencyComponent],
      providers: [
        FaultService,
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(SpringServiceLatencyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, "getAllEndpoints").and.returnValue(of([]));
    spyOn(endpointService, "getDockerContainers").and.returnValue(of([]));
    faultService = TestBed.get(FaultService);
    spyOn(faultService, "executeSpringServiceLatencyFault").and.returnValue(of({"taskData": {"schedule": null}}));
    router = TestBed.get(Router);
    spyOn(router, "navigateByUrl");
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should execute spring-service-latency fault", () => {
    component.executeSpringServiceLatencyFault(component.faultFormData);
    expect(faultService.executeSpringServiceLatencyFault).toHaveBeenCalled();
  });

  it("should execute getDockerContainers", () => {
    component.getDockerContainers("DOCKER", "");
    expect(endpointService.getDockerContainers).toHaveBeenCalled();
  });

});
