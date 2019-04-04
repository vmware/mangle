import { TestBed } from '@angular/core/testing';

import { ConfigService } from './config.service';
import { HttpClientModule } from '@angular/common/http';

describe('ConfigService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => {
    const service: ConfigService = TestBed.get(ConfigService);
    expect(service).toBeTruthy();
  });
});
