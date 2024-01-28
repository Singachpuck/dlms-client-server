import { TestBed } from '@angular/core/testing';

import { MandjetService } from './mandjet.service';

describe('MandjetService', () => {
  let service: MandjetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MandjetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
