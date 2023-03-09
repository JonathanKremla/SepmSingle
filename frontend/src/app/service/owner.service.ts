import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Owner} from '../dto/owner';

const baseUri = environment.backendUrl + '/owners';

@Injectable({
  providedIn: 'root'
})
export class OwnerService {

  constructor(
    private http: HttpClient,
  ) {
  }

  /**
   * Searches for Name suggestions for owners
   *
   * @param name name or substring of Owner(s) to be searched for
   * @param limitTo limits the result list to the specified number
   * @return an Observable of the List of all matched Owners
   */
  public searchByName(name: string, limitTo: number): Observable<Owner[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('maxAmount', limitTo);
    return this.http.get<Owner[]>(baseUri, {params});
  }

  /**
   * Get All Owners stored in the system
   *
   * @return an Observable of the List of all Owners
   */
  public getAll(): Observable<Owner[]> {
    return this.http.get<Owner[]>(baseUri);
  }

  /**
   * Create a new Owner in the system.
   *
   * @param owner the data for the owner that should be created
   * @return an Observable for the created owner
   */
  public create(owner: Owner): Observable<Owner> {
    return this.http.post<Owner>(
      baseUri,
      owner
    );
  }
}
