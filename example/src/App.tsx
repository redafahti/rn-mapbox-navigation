import { useEffect, useState } from 'react';
import { Alert, StyleSheet, View } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import MapboxNavigation from 'mapboxnavigation';


export default function App() {
    const updateRouteProgressChange = (event: any) => {
        console.log(event);
    };

    const getCurrentPosition = () => {
        Geolocation.getCurrentPosition(
          (pos: any) => {
            setPosition(pos.coords);
          },
          (error: any) => Alert.alert('GetCurrentPosition Error', JSON.stringify(error)),
          { enableHighAccuracy: true }
        );
      };
    
      const [position, setPosition] = useState<any>(null);

      useEffect(()=>{
        getCurrentPosition()
      },[])

    return (
        <View
            style={{
                flex: 1,
            }}
        >
            <View
                style={{
                    height: '70%',
                }}
            >
            {
                position ?                
                <MapboxNavigation
                    startOrigin={{ latitude: position.latitude, longitude: position.longitude }}
                    destination={{ latitude: 33.603562, longitude: -7.564305 }}
                    style={styles.container}
                    shouldSimulateRoute={true}
                    showCancelButton={false}
                    waypoints={[
                        { latitude: 33.593451, longitude: -7.600996 },
                        { latitude: 33.598267, longitude: -7.575928 },
                    ]}
                    language="fr"
                    onLocationChange={(event: any) => {
                        console.log('onLocationChange', event);
                    }}
                    onRouteProgressChange={(event) => {
                        updateRouteProgressChange(event);
                    }}
                    onRoutesReady={(event: any) => {
                        //console.log('onRoutesReady', event.route);
                    }}
                />: null
            }
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
});
