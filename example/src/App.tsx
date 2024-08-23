import MapboxNavigation from 'mapboxnavigation';
import { StyleSheet, View } from 'react-native';

export default function App() {
    const updateRouteProgressChange = (event: any) => {
        //console.log(event);
    };

    return (
        <View
            style={{
                flex: 1,
            }}
        >
            <View
                style={{
                    height: '100%',
                }}
            >
                <MapboxNavigation
                    startOrigin={{ latitude: 33.593391, longitude: -7.603147 }}
                    destination={{ latitude: 33.603562, longitude: -7.564305 }}
                    style={styles.container}
                    shouldSimulateRoute={false}
                    showCancelButton={false}
                    waypoints={[
                        { latitude: 33.593451, longitude: -7.600996 },
                        { latitude: 33.598267, longitude: -7.575928 },
                    ]}
                    language="fr"
                    onLocationChange={(event) => {
                        //console.log('onLocationChange', event);
                    }}
                    onRouteProgressChange={(event) => {
                        updateRouteProgressChange(event);
                    }}
                    onRoutesReady={(event: any) => {
                        console.log('onRoutesReady', event.route);
                    }}
                />
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
});
