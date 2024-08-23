#import "RCTConvert+MapboxNavigation.h"

@implementation RCTConvert (MapboxNavigation)

+ (MapboxCoordinate *)MapboxCoordinate:(id)json {
    MapboxCoordinate *coord = [MapboxCoordinate new];
    json = [self NSDictionary:json];
    coord.coordinate = (CLLocationCoordinate2D){
        [self CLLocationDegrees:json[@"latitude"]],
        [self CLLocationDegrees:json[@"longitude"]]
    };
    return coord;
}

RCT_ARRAY_CONVERTER(MapboxCoordinate)

@end
