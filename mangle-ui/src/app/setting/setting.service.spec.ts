import { TestBed } from '@angular/core/testing';

import { SettingService } from './setting.service';
import { HttpClientModule } from '@angular/common/http';

describe('SettingService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule
    ]
  }));

  it('should be created', () => {
    const service: SettingService = TestBed.get(SettingService);
    expect(service).toBeTruthy();
  });
});
