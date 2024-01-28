import {Injectable} from '@angular/core';
import {UtilService} from "./util.service";

const TOKEN_KEY = 'auth-token';
const EXPIRES_KEY = 'expires_in';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  constructor(private util: UtilService) { }

  public signOut(): void {
    window.sessionStorage.clear();
  }

  public saveToken(data: { username: string, token: string, expiresIn: number }): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.removeItem(EXPIRES_KEY);
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, data.token);
    window.sessionStorage.setItem(USER_KEY, this.util.encodeBase64(data.username));
    let expires_in = new Date();
    expires_in.setSeconds(expires_in.getSeconds() + data.expiresIn);
    window.sessionStorage.setItem(EXPIRES_KEY, this.util.encodeBase64(expires_in.toISOString()));
  }

  public getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }

  public getUsername(): string | null {
    let usernameEncoded = window.sessionStorage.getItem(USER_KEY);
    return usernameEncoded === null ? null : this.util.decodeBase64(usernameEncoded);
  }

  public setUsername(username: string) {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, this.util.encodeBase64(username));
  }

  public isValid(): boolean {
    let isExpired = true;
    let expires: string | null;
    if ((expires = window.sessionStorage.getItem(EXPIRES_KEY))) {
      expires = this.util.decodeBase64(expires);
      isExpired = Date.now() - Date.parse(expires) >= 0;
    }
    return !isExpired;
  }
}
