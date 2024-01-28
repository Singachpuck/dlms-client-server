import {Component, OnInit} from '@angular/core';
import {Notification} from "../../model/notification";
import {NotificationService} from "../../services/notification.service";
import {Toast} from 'bootstrap'

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationComponent implements OnInit {

  private notificationQueue: Array<Notification> = [];

  private queueMaxLen: number = 5;

  current: Notification = new Notification('');

  toast?: any;

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    let toastElement: any = document.querySelector("#liveToast");
    this.toast = Toast.getOrCreateInstance(toastElement);
    this.notificationService.subscribeToNotification(n => {
      if (!this.toast.isShown()) {
        this.current = n;
        this.toast.show();
      } else {
        if (this.notificationQueue.length < this.queueMaxLen) {
          this.notificationQueue.push(n);
        }
      }
    });
    toastElement.addEventListener('hidden.bs.toast', () => {
      if (this.notificationQueue.length !== 0) {
        this.current = this.notificationQueue.shift() || new Notification('');
        this.toast.show();
      }
    });
  }
}
