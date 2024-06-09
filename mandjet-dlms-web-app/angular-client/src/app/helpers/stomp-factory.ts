import { StompService } from '../services/stomp.service';
import { stopmConfig } from './stopm-config';

export function rxStompServiceFactory() {
  const rxStomp = new StompService();
  rxStomp.configure(stopmConfig);
  rxStomp.activate();
  return rxStomp;
}
