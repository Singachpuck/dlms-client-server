import { Injectable } from '@angular/core';
import {Observable, Subject, Subscription} from "rxjs";
import {Notification, NotificationType} from "../model/notification";

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private emitter: Subject<Notification> = new Subject<Notification>();

  constructor() { }

  publish(notification: Notification) {
    this.emitter.next(notification);
  }

  subscribeToNotification(callback: (n: Notification) => void) {
    this.emitter.subscribe(callback);
  }

  defaultErrorHandler = (err: any) => {
    console.log(err.error);
    let error = new Notification(err.error.description, NotificationType.ERROR);
    this.publish(error);
  }
}
