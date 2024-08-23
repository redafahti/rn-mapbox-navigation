#import <React/RCTConvert.h>
#import <React/RCTConvert+CoreLocation.h>

#import "MapboxCoordinate.h"

@interface RCTConvert (MapboxNavigation)

+ (MapboxCoordinate *)MapboxCoordinate:(id)json;

typedef NSArray MapboxCoordinateArray;
+ (MapboxCoordinateArray *)MapboxCoordinateArray:(id)json;

@end
