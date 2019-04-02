import { TestBed } from '@angular/core/testing';

import { EndpointService } from './endpoint.service';
import { HttpClientModule } from '@angular/common/http';

describe('EndpointService', () => {
  
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => {
    const service: EndpointService = TestBed.get(EndpointService);
    expect(service).toBeTruthy();
  });
});
