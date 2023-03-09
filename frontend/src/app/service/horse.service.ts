import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseSearch} from '../dto/horse';
import {Sex} from '../dto/sex';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) {
  }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getHorsesMatchingParams(horse: HorseSearch): Observable<Horse[]> {
    let params = new HttpParams();
    if (horse.name !== null && horse.name !== undefined) {
      params = params.append('name', horse.name);
    }
    if (horse.description !== null && horse.description !== undefined && horse.description !== '') {
      params = params.append('description', horse.description);
    }
    if (horse.sex !== null && horse.sex !== undefined) {
      params = params.append('sex', horse.sex);
    }
    if (horse.bornBefore !== null && horse.bornBefore !== undefined) {
      params = params.append('bornBefore', horse.bornBefore.toString());
    }
    if (horse.owner !== null && horse.owner !== undefined && horse.owner !== '') {
      params = params.append('ownerName', horse.owner);
    }
    return this.http.get<Horse[]>(baseUri, {params});
  }


  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  /**
   * Get Horse by ID
   *
   * @param id of the horse to retrieve
   * @return an Observable of the horse with the given id
   */
  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(baseUri + '/' + id);
  }

  /**
   * Edit an existing Horse
   *
   * @param horse the new data for the horse to update
   * @return an Observable of the edited horse
   */
  edit(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(baseUri + '/' + horse.id, horse);
  }

  /**
   * Delete an existing Horse
   *
   * @param id of the horse to be deleted
   * @return an Observable of the deleted horse
   */
  delete(id: number): Observable<Horse> {
    return this.http.delete<Horse>(baseUri + '/' + id);
  }

  /**
   * Searches for Name suggestions for possible Mothers for a horse
   *
   * @param dateOfBirth birthdate of the horse for which possible mothers should be searched for
   * @param name name or substring of the name of possible mothers
   * @param limitTo limits the result list to the specified number
   * @return an Observable of the list of the matching Mothers
   */
  public searchByNameMother(dateOfBirth: Date | undefined, name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('sex', Sex.female)
      .set('bornBefore', dateOfBirth !== undefined ? dateOfBirth.toString() : '')
      .set('limit', limitTo);
    return this.http.get<Horse[]>(baseUri, {params});
  }

  /**
   * Searches for Name suggestions for possible Fathers for a horse
   *
   * @param dateOfBirth birthdate of the horse for which possible fathers should be searched for
   * @param name name or substring of the name of possible fathers
   * @param limitTo limits the result list to the specified number
   * @return an Observable of the list of the matching Fathers
   */
  public searchByNameFather(dateOfBirth: Date | undefined, name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('bornBefore', dateOfBirth !== undefined ? dateOfBirth.toString() : '')
      .set('sex', Sex.male)
      .set('limit', limitTo);
    return this.http.get<Horse[]>(baseUri, {params});
  }

  /**
   * Retrieves the familyTree for a given Horse as the root horse, meaning all ancestors of the root horse
   * up to a given generation
   *
   * @param id of the root horse
   * @param generations number of generations which should be returned
   * @return an Observable of the FamilyTree with the horse with the given id beeing the root of the tree
   */
  public getFamilyTree(id: number, generations: number): Observable<Horse> {
    const params = new HttpParams().set('generations', generations);
    return this.http.get<Horse>(baseUri + '/' + id + '/familytree', {params});
  }
}
