import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {API_ENDPOINT} from "./util.service";
import {Observable} from "rxjs";
import {User} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  getUsers(): Observable<any> {
    return this.http.get(API_ENDPOINT + 'users');
  }

  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(API_ENDPOINT + 'users/' + username);
  }

  updateUser(username: string, user: User): Observable<any> {
    return this.http.patch(API_ENDPOINT + 'users/' + username, user, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  deleteUser(username: string): Observable<any> {
    return this.http.delete(API_ENDPOINT + 'users/' + username);
  }
}
