import { TestBed } from '@angular/core/testing';

import { RequestsService } from './requests.service';
import { HttpClientModule } from '@angular/common/http';

describe('RequestsService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => { 
    const service: RequestsService = TestBed.get(RequestsService);
    expect(service).toBeTruthy();
  });
});
