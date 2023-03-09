import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date | undefined;
  sex: Sex | undefined;
  owner?: Owner;
  mother?: Horse;
  father?: Horse;
}

export interface HorseFamilyTree {
  id?: number;
  name: string;
  dateOfBirth: Date | undefined;
  mother?: HorseFamilyTree;
  father?: HorseFamilyTree;
}

export interface HorseSearch {
  name?: string;
  description?: string;
  bornBefore?: Date;
  sex?: Sex;
  owner?: string;
}
