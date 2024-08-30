import type { StyleProp, ViewStyle } from 'react-native';

import type { Language } from './locals';

export type Coordinate = {
  latitude: number;
  longitude: number;
};

export type Location = {
  latitude: number;
  longitude: number;
  heading: number;
  accuracy: number;
};

export type NativeEvent<T> = {
  nativeEvent: T;
};

export type RouteProgress = {
  distanceTraveled: number;
  durationRemaining: number;
  fractionTraveled: number;
  distanceRemaining: number;
};

export type NativeEventsProps = {
  onLocationChange?: (event: NativeEvent<Location>) => void;
  onRouteProgressChange?: (event: NativeEvent<any>) => void;
  onRoutesReady?: (event: NativeEvent<any>) => void;
  onRouteChange?: (event: NativeEvent<any>) => void;
  onError?: (event: any) => void;
  onCancelNavigation?: () => void;
  onArrive?: () => void;
};

export interface MapboxNavigationProps {
  style?: StyleProp<ViewStyle>;
  mute?: boolean;
  showCancelButton?: boolean;
  startOrigin: Coordinate;
  waypoints?: Coordinate[];
  destination: Coordinate;
  language?: Language;
  showsEndOfRouteFeedback?: boolean;
  hideStatusView?: boolean;
  shouldSimulateRoute?: boolean;
  onLocationChange?: (location: Location) => void;
  onRouteProgressChange?: (progress: RouteProgress) => void;
  onRouteLegProgressChange?: (progress: RouteProgress) => void;
  onRouteChange?: (route: any) => void;
  onRoutesReady?: (route: any) => void;
  onError?: (error: any) => void;
  onCancelNavigation?: () => void;
  onArrive?: () => void;
}
