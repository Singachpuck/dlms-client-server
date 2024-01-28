import {Component, OnInit} from '@angular/core';
import {User} from "../../model/user";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../services/user.service";
import {TokenStorageService} from "../../services/token-storage.service";
import {noWhitespaceValidator} from "../../helpers/validators";
import {Router} from "@angular/router";
import {UtilService} from "../../services/util.service";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  userSubmitted: boolean = false;

  emailSubmitted: boolean = false;

  passwordSubmitted: boolean = false;

  user?: User;

  userUpdate = new FormGroup({
    username: new FormControl('', [Validators.required,
      noWhitespaceValidator,
      Validators.maxLength(50)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required])
  })

  constructor(private router: Router, private userService: UserService, private util: UtilService,
              private tokenService: TokenStorageService, private notification: NotificationService) { }

  ngOnInit(): void {
  }

  populateUser(user: User) {
    this.user = user;
  }

  onUsernameUpdate() {
    this.userSubmitted = true;

    if (this.username?.invalid) {
      return;
    }

    let user: User = new User();
    user.username = this.userUpdate.get('username')?.value || '';
    if (this.user?.username) {
      this.userService.updateUser(this.user?.username, user).subscribe({
        next: result => {
          this.tokenService.setUsername(result.username);
          window.location.reload();
        },
        error: this.notification.defaultErrorHandler
      });
    }
  }

  onEmailUpdate() {
    this.emailSubmitted = true;

    if (this.email?.invalid) {
      return;
    }

    let user: User = new User();
    user.email = this.userUpdate.get('email')?.value || '';
    if (this.user?.username) {
      this.userService.updateUser(this.user?.username, user).subscribe({
        next: result => {
          window.location.reload();
        }, error: this.notification.defaultErrorHandler
      });
    }
  }

  onPasswordUpdate() {
    this.passwordSubmitted = true;

    if (this.password?.invalid) {
      return;
    }

    let user: User = new User();
    user.password = this.userUpdate.get('password')?.value || '';
    if (this.user?.username) {
      this.userService.updateUser(this.user?.username, user).subscribe({
        next: result => {
          window.location.reload();
        }, error: this.notification.defaultErrorHandler
      });
    }
  }

  onDeleteAccount(e: Event) {
    e.preventDefault();
    if (this.user?.username) {
      this.userService.deleteUser(this.user.username).subscribe({
        next: result => {
          this.router.navigate(['/login']);
        },
        error: this.notification.defaultErrorHandler
      })
    }
  }

  get username() {
    return this.userUpdate.get('username');
  }

  get email() {
    return this.userUpdate.get('email');
  }

  get password() {
    return this.userUpdate.get('password');
  }
}
