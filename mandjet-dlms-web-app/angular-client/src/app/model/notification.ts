
export class Notification {

  type: NotificationType = NotificationType.DEFAULT;

  title: string;

  message: string;

  timestamp: number;

  constructor(message: string, type?: NotificationType, title?: string) {
    this.type = type || NotificationType.DEFAULT;
    this.title = title || NotificationDescriptor[this.type].description;
    this.message = message;
    this.timestamp = Date.now();
  }
}

export enum NotificationType {
  DEFAULT, INFO, SUCCESS, ERROR
}

export const NotificationDescriptor: {[key in NotificationType]: { description: string, colorSchema: string }} = {
  [NotificationType.DEFAULT]: {description: "Notification received!", colorSchema: "bg-light-subtle"},
  [NotificationType.INFO]: {description: "Info!", colorSchema: "bg-info-subtle"},
  [NotificationType.SUCCESS]: {description: "Operation successful!", colorSchema: "bg-success-subtle"},
  [NotificationType.ERROR]: {description: "Error occured!", colorSchema: "bg-danger-subtle"},
}
