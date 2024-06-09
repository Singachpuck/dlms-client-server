import {Component, OnDestroy, OnInit} from '@angular/core';
import {MandjetService} from "../../services/mandjet.service";
import {Mandjet} from "../../model/mandjet";
import {Notification, NotificationType} from "../../model/notification";
import {NotificationService} from "../../services/notification.service";
import {Subscription} from "rxjs";
import {StompService} from "../../services/stomp.service";
import {Message} from "@stomp/stompjs";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  private mandjetUpdateInterval = 15000;

  private maxSensorValues = 100;

  currentMandjet: Mandjet = new Mandjet();

  lastUpdate: Date = new Date();

  // @ts-ignore
  private managementSubscription: Subscription;

  // @ts-ignore
  private supportingSubscription: Subscription;

  constructor(private mandjetService: MandjetService, private notification: NotificationService, private stompService: StompService) { }

  ngOnInit(): void {
    // this.updateMandjet();
    // setInterval(() => {
    //   this.updateMandjet();
    // }, this.mandjetUpdateInterval);

    this.managementSubscription = this.stompService.watch('/mandjet/management').subscribe((message: Message) => {
      let obj = JSON.parse(message.body);
      this.currentMandjet.battery = obj['battery'];
      this.lastUpdate = new Date();

      let notification = new Notification(message.body, NotificationType.INFO);
      this.notification.publish(notification);
    });

    this.supportingSubscription = this.stompService.watch('/mandjet/supporting').subscribe((message: Message) => {
      let obj = JSON.parse(message.body);
      this.currentMandjet.voltage = obj['voltage'];
      if (this.currentMandjet.sensorValues.length >= this.maxSensorValues) {
        this.currentMandjet.sensorValues.shift();
      }
      this.currentMandjet.sensorValues.push([...obj['sensorValues'], new Date()]);

      let notification = new Notification(message.body, NotificationType.INFO);
      this.notification.publish(notification);
    });
  }

  // private updateMandjet() {
  //   this.mandjetService.getMandjetData().subscribe({
  //     next: (mandjetData: any) => {
  //       console.log(mandjetData)
  //       this.currentMandjet = mandjetData;
  //     },
  //     error: err => {
  //       let error = new Notification(err.status === 401 ? '401 Unauthorized' : err.error, NotificationType.ERROR);
  //       this.notification.publish(error);
  //     }
  //   });
  // }

  ngOnDestroy() {
    this.managementSubscription.unsubscribe();
    this.supportingSubscription.unsubscribe();
  }
}
