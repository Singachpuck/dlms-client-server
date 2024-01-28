import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {API_ENDPOINT} from "./util.service";
import {Mandjet} from "../model/mandjet";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MandjetService {

  constructor(private http: HttpClient) { }

  getMandjetData(): Observable<Mandjet> {
    return this.http.get<Mandjet>(API_ENDPOINT + 'mandjet/data');
  }
}
