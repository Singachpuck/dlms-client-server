
export class Notification {

  type: NotificationType = NotificationType.DEFAULT;

  message: string;

  timestamp: number;

  constructor(message: string, type?: NotificationType) {
    this.type = type || NotificationType.DEFAULT;
    this.message = message;
    this.timestamp = Date.now();
  }
}

export enum NotificationType {
  DEFAULT, INFO, SUCCESS, ERROR
}
