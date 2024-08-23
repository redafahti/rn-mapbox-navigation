import MapboxNavigation from 'mapboxnavigation';
import { Image, StyleSheet, Text, View } from 'react-native';

export default function App() {

  const updateRouteProgressChange = (event: any) => {
        console.log(event)
  }

  return (
    <View style={{
      flex: 1
    }}>
        <View style={{
          height: '80%'
        }}> 
        <MapboxNavigation
            startOrigin={{ latitude: 33.593391, longitude: -7.603147 }}
            destination={{ latitude:  33.678848, longitude:  -7.420064 }} 
            style={styles.container}
            shouldSimulateRoute={false}
            showCancelButton={false}
            waypoints={[
              {latitude: 33.593451, longitude: -7.600996},
              {  latitude: 33.598267, longitude: -7.575928 },
              { latitude: 33.603562, longitude: -7.564305 }
            ]}
            language="fr"
            onLocationChange={event => {
              console.log('onLocationChange', event);
            }}
            onRouteProgressChange={event => {
              updateRouteProgressChange(event);
            }}
          />
        </View>
    
        <View
            style={{
                backgroundColor: '#fff',
                borderTopLeftRadius: 30,
                borderTopRightRadius: 30,
                shadowColor: '#000',
                shadowOffset: {
                    width: 0,
                    height: 2,
                },
                shadowOpacity: 0.25,
                shadowRadius: 20,
                elevation: 30,
                position: 'absolute',
                bottom: 0,
                width: '100%',
                padding: 5,
                zIndex: 1000,
                paddingBottom: 30,
            }}
        >

                <View>
              
                                    <View
                              
                                        style={{
                                            flexDirection:
                                           'row',
                                            alignContent: 'center',
                                            alignItems: 'center',
                                            justifyContent: 'space-between',
                                            paddingHorizontal: 5,
                                            paddingVertical: 4,
                                            borderRadius: 18,
                                        }}
                                    >
                                        <View
                                            style={{
                                                flexDirection:
                                                    'row',
                                                alignContent: 'center',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                gap: 8,
                                            }}
                                        >
                                            <Image
                                                source={{uri: 'https://static.vecteezy.com/system/resources/thumbnails/027/951/137/small_2x/stylish-spectacles-guy-3d-avatar-character-illustrations-png.png' }}
                                                style={{
                                                    width: 35,
                                                    height: 35,
                                                    borderRadius: 100,
                                                }}
                                                resizeMode="contain"
                                            />

                                            <View>
                                                <Text
                                                    style={{
                                                        fontFamily: 'Poppinsmedium',
                                                        color: '#000',
                                                        fontSize: 14,
                                                    }}
                                                >
                                                    Passenger name
                                                </Text>
                                            </View>
                                        </View>
                                    </View>


                                    <View
                                style={{
                                    flexDirection:  'row',
                                    alignContent: 'center',
                                    alignItems: 'center',
                                    gap: 8,
                                    marginHorizontal: 10,
                                }}
                            >
                                <Text
                                    style={{
                                        fontFamily: 'PoppinssemiBold',
                                        color:  '#000',
                                        fontSize: 24,
                                    }}
                                >
                                    {(399 / 60).toFixed(2)} min
                                </Text>
                                <Text
                                    style={{
                                        fontFamily: 'Poppinsmedium',
                                        color: '#000',
                                        fontSize: 15,
                                    }}
                                >
                                    {(1000 / 1000).toFixed(2)} Km
                                </Text>
                            </View>
                            <View
                                style={{
                                    marginHorizontal: 2,
                                }}
                            >
                                <View
                                    style={{
                                        flexDirection: 'row',
                                        alignItems: 'center',
                                        justifyContent: 'space-between',
                                        marginTop: 2,
                                    }}
                                >
                                    <View
                                        style={{
                                            backgroundColor: 'rgba(249, 249, 249, 1)',
                                            height: 30,
                                            width: 30,
                                            borderRadius: 100,
                                            justifyContent: 'center',
                                            alignContent: 'center',
                                            alignItems: 'center',
                                            zIndex: 1000,
                                        }}
                                    >
                                        <View
                                            style={{
                                                backgroundColor:  '#3FA5F0',
                                                height: 13,
                                                width: 13,
                                                borderRadius: 100,
                                                justifyContent: 'center',
                                                alignContent: 'center',
                                                alignItems: 'center',
                                            }}
                                        >
                                            <View
                                                style={{
                                                    backgroundColor:  '#fff',
                                                    height: 4,
                                                    width: 4,
                                                    borderRadius: 100,
                                                }}
                                            ></View>
                                        </View>
                                    </View>
                                    <View
                                        style={{
                                            backgroundColor:  '#3FA5F0',
                                            height: 2,
                                            width: '100%',
                                            position: 'absolute',
                                        }}
                                    ></View>
                                    <View
                                        style={{
                                            backgroundColor:  '#3FA5F0',
                                            height: 30,
                                            width: 30,
                                            borderRadius: 100,
                                            justifyContent: 'center',
                                            alignContent: 'center',
                                            alignItems: 'center',
                                            zIndex: 1000,
                                            position: 'absolute',
                                            left: '20%',
                                        }}
                                    ></View>
                                    <View
                                        style={{
                                            backgroundColor: 'rgba(249, 249, 249, 1)',
                                            height: 30,
                                            width: 30,
                                            borderRadius: 100,
                                            justifyContent: 'center',
                                            alignContent: 'center',
                                            alignItems: 'center',
                                            zIndex: 1000,
                                        }}
                                    >
                                        
                                    </View>
                                </View>
                            </View>

                    </View>
            

        </View>
     
    </View>
  );
}


const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
