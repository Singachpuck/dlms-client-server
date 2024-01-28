import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {noWhitespaceValidator} from "../../helpers/validators";
import {Router} from "@angular/router";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {

  submitted: boolean = false;
  error: boolean = false;
  errorMessage = '';

  userCreate = new FormGroup({
    username: new FormControl('', [Validators.required,
      noWhitespaceValidator,
      Validators.maxLength(50)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required])
  });

  constructor(private router: Router, private authService: AuthService, private notification: NotificationService) { }

  ngOnInit(): void {
  }

  onUserCreate() {
    this.submitted = true;

    if (this.userCreate.invalid) {
      return;
    }

    this.authService.register(this.userCreate.get('username')?.value,
      this.userCreate.get('email')?.value,
      this.userCreate.get('password')?.value)
      .subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: this.notification.defaultErrorHandler
      });
  }

  get username() {
    return this.userCreate.get('username');
  }

  get email() {
    return this.userCreate.get('email');
  }

  get password() {
    return this.userCreate.get('password');
  }

}
