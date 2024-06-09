import { StompFactory } from './stomp-factory';

describe('StompFactory', () => {
  it('should create an instance', () => {
    expect(new StompFactory()).toBeTruthy();
  });
});
