import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {TokenStorageService} from "../../services/token-storage.service";
import {Router} from "@angular/router";
import {NotificationService} from "../../services/notification.service";
import {Notification, NotificationType} from "../../model/notification";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  submitted: boolean = false;

  userForm: FormGroup = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required])
  });

  constructor(private router: Router, private authService: AuthService,
              private tokenService: TokenStorageService, private notification: NotificationService) { }

  ngOnInit(): void {
  }

  onUserLogin() {
    this.submitted = true;

    if (this.userForm.invalid) {
      return;
    }

    this.authService.login(this.userForm.get('username')?.value, this.userForm.get('password')?.value)
      .subscribe({
        next: (token: any) => {
          this.tokenService.saveToken(token);
          this.router.navigate(['/dashboard']);
        },
        error: err => {
          let error = new Notification(err.status === 401 ? '401 Unauthorized' : err.error, NotificationType.ERROR);
          this.notification.publish(error);
        }
      });
  }

  get username() {
    return this.userForm.get('username');
  }

  get password() {
    return this.userForm.get('password');
  }
}
