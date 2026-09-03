export interface Temple {
  id: number;
  name: string;
  moolavar: string | null;
  urchavar: string | null;
  ammanThayar: string | null;
  thalaVirutcham: string | null;
  theertham: string | null;
  agamamPooja: string | null;
  oldYear: string | null;
  historicalName: string | null;
  city: string | null;
  district: string | null;
  state: string | null;
  singers: string | null;
  festival: string | null;
  generalInformation: string | null;
  address: string | null;
  phone: string | null;
  openingTime: string | null;
  speciality: string | null;
  prayers: string | null;
  thanksGiving: string | null;
  greatness: string | null;
  history: string | null;
  features: string | null;
  hfLat: number | null;
  hfLan: number | null;
  location: string | null;
  nearByAirport: string | null;
  nearByRailwayStation: string | null;
  accommodation: string | null;
}

export interface StreamQueryResponse {
  chunk: string;
  isDone: boolean;
}

export interface TempleImage {
  url: string;
  title: string;
  description: string;
  source: string;
}
