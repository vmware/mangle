import { TestBed } from '@angular/core/testing';

import { CoreService } from './core.service';
import { HttpClientModule } from '@angular/common/http';

describe('CoreService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => {
    const service: CoreService = TestBed.get(CoreService);
    expect(service).toBeTruthy();
  });
});
