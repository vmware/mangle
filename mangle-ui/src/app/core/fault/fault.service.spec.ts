import { TestBed } from '@angular/core/testing';

import { FaultService } from './fault.service';
import { HttpClientModule } from '@angular/common/http';

describe('FaultService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => {
    const service: FaultService = TestBed.get(FaultService);
    expect(service).toBeTruthy();
  });
});
