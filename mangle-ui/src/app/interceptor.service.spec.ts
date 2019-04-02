import { TestBed } from '@angular/core/testing';

import { InterceptorService } from './interceptor.service';
import { RouterTestingModule } from '@angular/router/testing';

describe('InterceptorService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      RouterTestingModule
    ]
  }));

  it('should be created', () => {
    const service: InterceptorService = TestBed.get(InterceptorService);
    expect(service).toBeTruthy();
  });
});
