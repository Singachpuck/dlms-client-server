import { Component, OnInit } from '@angular/core';
import {MandjetService} from "../../services/mandjet.service";
import {Mandjet} from "../../model/mandjet";
import {Notification, NotificationType} from "../../model/notification";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  private mandjetUpdateInterval = 15000;

  currentMandjet: Mandjet = new Mandjet();

  constructor(private mandjetService: MandjetService, private notification: NotificationService) { }

  ngOnInit(): void {
    this.updateMandjet();
    setInterval(() => {
      this.updateMandjet();
    }, this.mandjetUpdateInterval);
  }

  private updateMandjet() {
    this.mandjetService.getMandjetData().subscribe({
      next: (mandjetData: any) => {
        console.log(mandjetData)
        this.currentMandjet = mandjetData;
      },
      error: err => {
        let error = new Notification(err.status === 401 ? '401 Unauthorized' : err.error, NotificationType.ERROR);
        this.notification.publish(error);
      }
    });
  }
}
