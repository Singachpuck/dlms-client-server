import {Injectable} from '@angular/core';
import {HttpBackend, HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_ENDPOINT, UtilService} from "./util.service";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http: HttpClient;
  constructor(handler: HttpBackend, private util: UtilService) {
    this.http = new HttpClient(handler);
  }

  login(username: any, password: any): Observable<any> {
    let encodedCreds = 'Basic ' + this.util.encodeBase64(username + ':' + password);
    return this.http.post(API_ENDPOINT + 'auth/token', null, {
      headers: {
        'Authorization': encodedCreds,
        'X-Requested-With': 'XMLHttpRequest'
      },
      withCredentials: true
    });
  }

  register(username: any, email: any, password: any): Observable<any> {
    return this.http.post(API_ENDPOINT + 'signup', {
      username,
      email,
      password
    }, {
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      withCredentials: true
    });
  }
}
