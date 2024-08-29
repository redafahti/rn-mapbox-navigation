package com.mapboxnavigation

import android.graphics.Color
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.view.Gravity
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintLayout

import com.facebook.react.bridge.Arguments
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.DirectionsCriteria.ProfileCriteria
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.attribution.*
import com.mapbox.maps.plugin.logo.*
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.internal.route.Waypoint
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.shield.model.RouteShieldCallback
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverPrimaryOptions
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverSecondaryOptions
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverSubOptions
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverViewOptions
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume

import com.mapboxnavigation.databinding.NavigationViewBinding
import com.mapboxnavigation.R
import com.google.gson.Gson
import java.util.Locale

@SuppressLint("ViewConstructor")
class MapboxNavigationView(private val context: ThemedReactContext): FrameLayout(context.baseContext) {
  private companion object {
    private const val BUTTON_ANIMATION_DURATION = 1500L
  }

    private var origin: Point? = null
    private var destination: Point? = null
    private var waypoints: List<Point> = listOf()
    private var locale = Locale.getDefault()

    private var followZoomLevel: Double = 18.0
    private var showSpeedLimit: Boolean = true
    private var speedLimitAnchor: Array<Double>? = null
    private var maneuverAnchor: Array<Double>? = null
    private var maneuverRadius: Int = 26
    private var maneuverBackgroundColor: String = "#303030"
    private var userPuckImage: String? = null
    private var userPuckScale: Double = 1.0
    private var destinationImage: String? = null
    private var originImage: String? = null
    private var mapPadding: Array<Double>? = null
    private var routeColor: String = "#FF0000"
    private var routeCasingColor: String = "#2F7AC6"
    private var routeClosureColor: String = "#000000"
    private var alternateRouteColor: String = "#8694A5"
    private var alternateRouteCasingColor: String = "#727E8D"
    private var traversedRouteColor: String? = null
    private var traversedRouteCasingColor: String? = null
    private var trafficUnknownColor: String = "#56A8FB"
    private var trafficLowColor: String = "#56A8FB"
    private var trafficModerateColor: String = "#ff9500"
    private var trafficHeavyColor: String = "#ff4d4d"
    private var trafficSevereColor: String = "#8f2447"
    private var restrictedRoadColor: String = "#000000"
    private var routeArrowColor: String = "#FFFFFF"
    private var routeArrowCasingColor: String = "#2D3F53"
    private var waypointColor: String = "#2F7AC6"
    private var waypointRadius: Int = 8
    private var waypointOpacity: Int = 1
    private var waypointStrokeWidth: Int = 2
    private var waypointStrokeOpacity: Int = 1
    private var waypointStrokeColor: String = "#FFFFFF"
    private var logoVisible: Boolean = true
    private var logoPadding: Array<Double>? = null
    private var attributionVisible: Boolean = true
    private var attributionPadding: Array<Double>? = null
    private var mute: Boolean = false
    private var darkMode: Boolean = false
    private var debug: Boolean = false
    private var fontFamily: String? = null
    private var primaryColour: String = "#303030"
    private var secondaryColour: String = "#707070"
    private var primaryTextColour: String = "#FFFFFF"
    private var secondaryTextColour: String = "#9B9B9B"
    private var textSizeSmall: Double = 14.0
    private var textSizeMedium: Double = 16.0
    private var textSizeLarge: Double = 20.0
    private var textSizeXLarge: Double = 22.0

    private var isMapStyleLoaded: Boolean = false
    private var currentOrigin: Point? = null
    private var currentDestination: Point? = null
    private var currentWaypoints: Array<Point>? = null
    private var currentLegIndex: Int = -1
    private var currentActiveRoutes: List<NavigationRoute>? = null
    private var currentPreviewRoutes: List<NavigationRoute>? = null
  /**
   * Bindings to the example layout.
   */
  private var binding: NavigationViewBinding = NavigationViewBinding.inflate(LayoutInflater.from(context), this, true)

  /**
   * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
   */
  private var viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.mapboxMap)

  /**
   * Used to execute camera transitions based on the data generated by the [viewportDataSource].
   * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
   */
  private var navigationCamera = NavigationCamera(
    binding.mapView.mapboxMap,
    binding.mapView.camera,
    viewportDataSource
  )

  /**
   * Mapbox Navigation entry point. There should only be one instance of this object for the app.
   * You can use [MapboxNavigationProvider] to help create and obtain that instance.
   */
  private lateinit var mapboxNavigation: MapboxNavigation

  /*
   * Below are generated camera padding values to ensure that the route fits well on screen while
   * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
   */
  private val pixelDensity = Resources.getSystem().displayMetrics.density

  private val overviewPadding: EdgeInsets by lazy {
    EdgeInsets(
      140.0 * pixelDensity,
      40.0 * pixelDensity,
      120.0 * pixelDensity,
      40.0 * pixelDensity
    )
  }
  private val landscapeOverviewPadding: EdgeInsets by lazy {
    EdgeInsets(
    140.0 * pixelDensity,
      40.0 * pixelDensity,
      120.0 * pixelDensity,
      40.0 * pixelDensity
    )
  }
  private val followingPadding: EdgeInsets by lazy {
    EdgeInsets(
      180.0 * pixelDensity,
      40.0 * pixelDensity,
      150.0 * pixelDensity,
      40.0 * pixelDensity
    )
  }
  private val landscapeFollowingPadding: EdgeInsets by lazy {
    EdgeInsets(
      30.0 * pixelDensity,
      380.0 * pixelDensity,
      110.0 * pixelDensity,
      40.0 * pixelDensity
    )
  }

  /**
   * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
   * and remaining distance to the maneuver point.
   */
  private lateinit var maneuverApi: MapboxManeuverApi

  /**
   * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
   */
  private lateinit var tripProgressApi: MapboxTripProgressApi

  /**
   * Stores and updates the state of whether the voice instructions should be played as they come or muted.
   */
  private var isVoiceInstructionsMuted = false
    set(value) {
      field = value
      if (value) {
        voiceInstructionsPlayer?.volume(SpeechVolume(0f))
      } else {
        voiceInstructionsPlayer?.volume(SpeechVolume(1f))
      }
    }

  /**
   * Extracts message that should be communicated to the driver about the upcoming maneuver.
   * When possible, downloads a synthesized audio file that can be played back to the driver.
   */
  private lateinit var speechApi: MapboxSpeechApi

  /**
   * Plays the synthesized audio files with upcoming maneuver instructions
   * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
   * NOTE: do not use lazy initialization for this class since it takes some time to initialize
   * the system services required for on-device speech synthesis. With lazy initialization
   * there is a high risk that said services will not be available when the first instruction
   * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
   * `Activity#onCreate`.
   */
  private var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer? = null

  /**
   * Observes when a new voice instruction should be played.
   */
  private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
    speechApi.generate(voiceInstructions, speechCallback)
  }




  /**
   * Based on whether the synthesized audio file is available, the callback plays the file
   * or uses the fall back which is played back using the on-device Text-To-Speech engine.
   */
  private val speechCallback =
    MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
      expected.fold(
        { error ->
          // play the instruction via fallback text-to-speech engine
          voiceInstructionsPlayer?.play(
            error.fallback,
            voiceInstructionsPlayerCallback
          )
        },
        { value ->
          // play the sound file from the external generator
          voiceInstructionsPlayer?.play(
            value.announcement,
            voiceInstructionsPlayerCallback
          )
        }
      )
    }

  /**
   * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
   */
  private val voiceInstructionsPlayerCallback =
    MapboxNavigationConsumer<SpeechAnnouncement> { value ->
      // remove already consumed file to free-up space
      speechApi.clean(value)
    }

  /**
   * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
   * to the Maps SDK in order to update the user location indicator on the map.
   */
  private val navigationLocationProvider = NavigationLocationProvider()

  /**
   * RouteLine: Additional route line options are available through the
   * [MapboxRouteLineViewOptions] and [MapboxRouteLineApiOptions].
   * Notice here the [MapboxRouteLineViewOptions.routeLineBelowLayerId] option. The map is made up of layers. In this
   * case the route line will be placed below the "road-label" layer which is a good default
   * for the most common Mapbox navigation related maps. You should consider if this should be
   * changed for your use case especially if you are using a custom map style.
   */

  val customColorResources = RouteLineColorResources.Builder()
          .routeDefaultColor(Color.parseColor(routeColor))
          .routeCasingColor(Color.parseColor(routeCasingColor))
          .routeClosureColor(Color.parseColor(routeClosureColor))
          .restrictedRoadColor(Color.parseColor(restrictedRoadColor))
          .build()

  private val routeLineViewOptions: MapboxRouteLineViewOptions by lazy {
    
    MapboxRouteLineViewOptions.Builder(context)
      .routeLineColorResources(customColorResources)
      .routeLineBelowLayerId("road-label-navigation")
      .build()
  }

  private val routeLineApiOptions: MapboxRouteLineApiOptions by lazy {
    MapboxRouteLineApiOptions.Builder()
      .vanishingRouteLineEnabled(true)
      .build()
  }

  /**
   * RouteLine: This class is responsible for rendering route line related mutations generated
   * by the [routeLineApi]
   */
  private val routeLineView by lazy {
    MapboxRouteLineView(routeLineViewOptions)
  }


  /**
   * RouteLine: This class is responsible for generating route line related data which must be
   * rendered by the [routeLineView] in order to visualize the route line on the map.
   */
  private val routeLineApi: MapboxRouteLineApi by lazy {
    MapboxRouteLineApi(routeLineApiOptions)
  }

  /**
   * RouteArrow: This class is responsible for generating data related to maneuver arrows. The
   * data generated must be rendered by the [routeArrowView] in order to apply mutations to
   * the map.
   */
  private val routeArrowApi: MapboxRouteArrowApi by lazy {
    MapboxRouteArrowApi()
  }

    /**
    * RouteArrow: Customization of the maneuver arrow(s) can be done using the
    * [RouteArrowOptions]. Here the above layer ID is used to determine where in the map layer
    * stack the arrows appear. Above the layer of the route traffic line is being used here. Your
    * use case may necessitate adjusting this to a different layer position.
    */
    private val routeArrowOptions by lazy {
      RouteArrowOptions.Builder(context)
        .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
        .build()
    }

    /**
    * RouteArrow: This class is responsible for rendering the arrow related mutations generated
    * by the [routeArrowApi]
    */
    private val routeArrowView: MapboxRouteArrowView by lazy {
      MapboxRouteArrowView(routeArrowOptions)
    }

    /**
     * The [RouteShieldCallback] will be invoked with an appropriate result for Api call
     * [MapboxManeuverApi.getRoadShields]
     */
    private val roadShieldCallback = RouteShieldCallback { shields ->
        binding.maneuverContainer.findViewById<MapboxManeuverView>(R.id.maneuverView).renderManeuverWith(shields)
    }

  /**
   * Gets notified with location updates.
   *
   * Exposes raw updates coming directly from the location services
   * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
   */
  private val locationObserver = object : LocationObserver {
    var firstLocationUpdateReceived = false

    override fun onNewRawLocation(rawLocation: Location) {
      // not handled
    }

    override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
      val enhancedLocation = locationMatcherResult.enhancedLocation
      // update location puck's position on the map
      navigationLocationProvider.changePosition(
        location = enhancedLocation,
        keyPoints = locationMatcherResult.keyPoints,
      )

      // update camera position to account for new location
      viewportDataSource.onLocationChanged(enhancedLocation)
      viewportDataSource.evaluate()

      // if this is the first location update the activity has received,
      // it's best to immediately move the camera to the current user location
      if (!firstLocationUpdateReceived) {
        firstLocationUpdateReceived = true
        navigationCamera.requestNavigationCameraToOverview(
          stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
            .maxDuration(0) // instant transition
            .build()
        )
      }

      val event = Arguments.createMap()
      event.putDouble("longitude", enhancedLocation.longitude)
      event.putDouble("latitude", enhancedLocation.latitude)
      event.putDouble("heading", enhancedLocation.bearing ?: 0.0)
      event.putDouble("accuracy", enhancedLocation.horizontalAccuracy ?: 0.0)
      context
        .getJSModule(RCTEventEmitter::class.java)
        .receiveEvent(id, "onLocationChange", event)
    }
  }

  /**
   * Gets notified with progress along the currently active route.
   */
  private val routeProgressObserver = RouteProgressObserver { routeProgress ->
    // update the camera position to account for the progressed fragment of the route
    viewportDataSource.onRouteProgressChanged(routeProgress)
    viewportDataSource.evaluate()

    // draw the upcoming maneuver arrow on the map
    val style = binding.mapView.mapboxMap.style
    if (style != null) {
      val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
      routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
    }

    // update top banner with maneuver instructions
    val maneuvers = maneuverApi.getManeuvers(routeProgress)
    maneuvers.fold(
      { error ->
        Toast.makeText(
          context,
          error.errorMessage,
          Toast.LENGTH_SHORT
        ).show()
      },
      {

        maneuvers.onValue { maneuverList ->
          maneuverApi.getRoadShields(maneuverList, roadShieldCallback)
        }

        val maneuverViewOptions = ManeuverViewOptions.Builder()
          .primaryManeuverOptions(
            ManeuverPrimaryOptions.Builder()
              .textAppearance(R.style.PrimaryManeuverTextAppearance)
              .build()
          )
          .secondaryManeuverOptions(
            ManeuverSecondaryOptions.Builder()
              .textAppearance(R.style.ManeuverTextAppearance)
              .build()
          )
          .subManeuverOptions(
            ManeuverSubOptions.Builder()
              .textAppearance(R.style.ManeuverTextAppearance)
              .build()
          )
          .stepDistanceTextAppearance(R.style.StepDistanceRemainingAppearance)
          .build()

        binding.maneuverContainer.visibility = View.VISIBLE
        binding.maneuverContainer.findViewById<MapboxManeuverView>(R.id.maneuverView).renderManeuvers(maneuvers)
      }
    )

    val event = Arguments.createMap()
    val StopDurationRemaining = routeProgress.currentLegProgress?.durationRemaining?.toDouble() ?: 0.0
    val StopDistanceRemaining = routeProgress.currentLegProgress?.distanceRemaining?.toDouble() ?: 0.0
    val StopIndex = routeProgress.currentLegProgress?.legIndex?.toDouble() ?: 0.0 
    event.putDouble("distanceTraveled", routeProgress.distanceTraveled.toDouble())
    event.putDouble("durationRemaining", routeProgress.durationRemaining.toDouble())
    event.putDouble("fractionTraveled", routeProgress.fractionTraveled.toDouble())
    event.putDouble("distanceRemaining", routeProgress.distanceRemaining.toDouble())
    event.putDouble("StopIndex", StopIndex)
    event.putDouble("StopDurationRemaining", StopDurationRemaining)
    event.putDouble("StopDistanceRemaining", StopDistanceRemaining)
    context
      .getJSModule(RCTEventEmitter::class.java)
      .receiveEvent(id, "onRouteProgressChange", event)
  }

  /**
   * Gets notified whenever the tracked routes change.
   *
   * A change can mean:
   * - routes get changed with [MapboxNavigation.setNavigationRoutes]
   * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
   * - driver got off route and a reroute was executed
   */
  private val routesObserver = RoutesObserver { routeUpdateResult ->
    if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
      // generate route geometries asynchronously and render them
      routeLineApi.setNavigationRoutes(
        routeUpdateResult.navigationRoutes
      ) { value ->
        binding.mapView.mapboxMap.style?.apply {
          routeLineView.renderRouteDrawData(this, value)
        }
      }


      // update the camera position to account for the new route
      viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
      viewportDataSource.evaluate()
    } else {
      // remove the route line and route arrow from the map
      val style = binding.mapView.mapboxMap.style
      if (style != null) {
        routeLineApi.clearRouteLine { value ->
          routeLineView.renderClearRouteLineValue(
            style,
            value
          )
        }
        routeArrowView.render(style, routeArrowApi.clearArrows())
      }

      // remove the route reference from camera position evaluations
      viewportDataSource.clearRouteData()
      viewportDataSource.evaluate()
    }
  }

  @SuppressLint("MissingPermission")
  fun onCreate() {
    if (origin == null || destination == null) {
      sendErrorToReact("origin and destination are required")
      return
    }

    binding.mapView.logo.updateSettings {
      enabled = false
    }
    binding.mapView.attribution.updateSettings {
      enabled = false
    }
    binding.mapView.compass.enabled = false
    binding.mapView.scalebar.enabled = false
    binding.mapView.gestures.pitchEnabled = false
    binding.mapView.gestures.rotateEnabled = false


    // initialize Mapbox Navigation
    mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
      MapboxNavigationProvider.retrieve()
    } else {
      MapboxNavigationProvider.create(
        NavigationOptions.Builder(context)
          .build()
      )
    }

        // initialize location puck
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.Companion.from(
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            puckBearingEnabled = true
            enabled = true
        }


    // set the animations lifecycle listener to ensure the NavigationCamera stops
    // automatically following the user location when the map is interacted with
    binding.mapView.camera.addCameraAnimationsLifecycleListener(
      NavigationBasicGesturesHandler(navigationCamera)
    )
    navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
      // shows/hide the recenter button depending on the camera state
      when (navigationCameraState) {
        NavigationCameraState.TRANSITION_TO_FOLLOWING,
        NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
        NavigationCameraState.TRANSITION_TO_OVERVIEW,
        NavigationCameraState.OVERVIEW,
        NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
      }
    }

    // load map style
    binding.mapView.mapboxMap.loadStyle("mapbox://styles/redafa/clxm5vwgx00h701pd1uvublem") {
      // Ensure that the route line related layers are present before the route arrow
      routeLineView.initializeLayers(it)
    }

    viewportDataSource.options.followingFrameOptions.defaultPitch = 40.0
    viewportDataSource.options.followingFrameOptions.centerUpdatesAllowed= true
    viewportDataSource.options.followingFrameOptions.zoomUpdatesAllowed = true
    viewportDataSource.options.followingFrameOptions.minZoom = followZoomLevel
    viewportDataSource.options.followingFrameOptions.maxZoom = followZoomLevel
    viewportDataSource.options.followingFrameOptions.bearingUpdatesAllowed = true


    // set the padding values depending on screen orientation and visible view layout
    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      viewportDataSource.overviewPadding = landscapeOverviewPadding
    } else {
      viewportDataSource.overviewPadding = overviewPadding
    }
    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      viewportDataSource.followingPadding = landscapeFollowingPadding
    } else {
      viewportDataSource.followingPadding = followingPadding
    }


    // set the padding values depending on screen orientation and visible view layout
    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      viewportDataSource.overviewPadding = landscapeOverviewPadding
    } else {
      viewportDataSource.overviewPadding = overviewPadding
    }
    if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      viewportDataSource.followingPadding = landscapeFollowingPadding
    } else {
      viewportDataSource.followingPadding = followingPadding
    }

    // make sure to use the same DistanceFormatterOptions across different features
    val distanceFormatterOptions = DistanceFormatterOptions.Builder(context).build()

    // initialize maneuver api that feeds the data to the top banner maneuver view
    maneuverApi = MapboxManeuverApi(
      MapboxDistanceFormatter(distanceFormatterOptions)
    )


    // initialize voice instructions api and the voice instruction player
    speechApi = MapboxSpeechApi(
      context,
      locale.language
    )
    voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
      context,
      locale.language
    )

    binding.recenter.setOnClickListener {
      navigationCamera.requestNavigationCameraToFollowing()
      binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
    }
    binding.routeOverview.setOnClickListener {
      navigationCamera.requestNavigationCameraToOverview()
      binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
    }


    // Check initial muted or not
    if (this.isVoiceInstructionsMuted) {
      voiceInstructionsPlayer?.volume(SpeechVolume(0f))
    } else {
      voiceInstructionsPlayer?.volume(SpeechVolume(1f))
    }


    mapboxNavigation.startTripSession(withForegroundService = false)

    startRoute()

  }

  private fun onDestroy() {
    maneuverApi.cancel()
    routeLineApi.cancel()
    routeLineView.cancel()
    speechApi.cancel()
    voiceInstructionsPlayer?.shutdown()
    mapboxNavigation.stopTripSession()
  }

  private val arrivalObserver = object : ArrivalObserver {

    override fun onWaypointArrival(routeProgress: RouteProgress) {
      // do something when the user arrives at a waypoint
    }

    override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
      // do something when the user starts a new leg
    }

    override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
      val event = Arguments.createMap()
      event.putString("onArrive", "")
      context
        .getJSModule(RCTEventEmitter::class.java)
        .receiveEvent(id, "onRouteProgressChange", event)
    }
  }

  override fun requestLayout() {
    super.requestLayout()
    post(measureAndLayout)
  }

  private val measureAndLayout = Runnable {
    measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    )
    layout(left, top, right, bottom)
  }

  private fun findRoute(coordinates: List<Point>) {
    mapboxNavigation.requestRoutes(
      RouteOptions.builder()
        .applyDefaultNavigationOptions()
        .applyLanguageAndVoiceUnitOptions(context)
        .coordinatesList(coordinates)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .language(locale.language)
        .build(),
      object : NavigationRouterCallback {
        override fun onCanceled(routeOptions: RouteOptions, @RouterOrigin routerOrigin: String) {
          // no impl
        }

        override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
          sendErrorToReact("Error finding route $reasons")
        }

        override fun onRoutesReady(
          routes: List<NavigationRoute>,
          @RouterOrigin routerOrigin: String
        ) {
        val gson = Gson()
        val routesJson = gson.toJson(routes)

        val event = Arguments.createMap()
        event.putString("route", routesJson)

        context
        .getJSModule(RCTEventEmitter::class.java)
        .receiveEvent(id, "onRoutesReady", event)

            setRouteAndStartNavigation(routes)
        }
      }
    )
  }

  @SuppressLint("MissingPermission")
  private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
    // set routes, where the first route in the list is the primary route that
    // will be used for active guidance
    mapboxNavigation.setNavigationRoutes(routes)

    // show UI elements
    binding.routeOverview.visibility = View.VISIBLE

    // move the camera to overview when new route is available
    navigationCamera.requestNavigationCameraToOverview()

  }

  private fun startRoute() {
    // register event listeners
    mapboxNavigation.registerRoutesObserver(routesObserver)
    mapboxNavigation.registerArrivalObserver(arrivalObserver)
    mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
    mapboxNavigation.registerLocationObserver(locationObserver)
    mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

    // Create a list of coordinates that includes origin, destination
    val coordinatesList = mutableListOf<Point>()
    this.origin?.let { coordinatesList.add(it) }
    this.waypoints.let { coordinatesList.addAll(waypoints) }
    this.destination?.let { coordinatesList.add(it) }
    findRoute(coordinatesList)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mapboxNavigation.unregisterRoutesObserver(routesObserver)
    mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
    mapboxNavigation.unregisterLocationObserver(locationObserver)
    mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
    mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)

    // Clear routs and end
    mapboxNavigation.setNavigationRoutes(listOf())

    // hide UI elements
    binding.maneuverView.visibility = View.INVISIBLE
    binding.routeOverview.visibility = View.INVISIBLE
  }

  private fun sendErrorToReact(error: String?) {
    val event = Arguments.createMap()
    event.putString("error", error)
    context
      .getJSModule(RCTEventEmitter::class.java)
      .receiveEvent(id, "onError", event)
  }


  private fun updateManeuverAnchor(anchor: Array<Double>?) {
        this.maneuverAnchor = anchor

        if (anchor != null) {
            (binding.maneuverContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginStart = if (anchor!!.size > 0) (anchor!!.get(0) * pixelDensity).toInt() else (20 * pixelDensity).toInt()
                topMargin = if (anchor!!.size > 1) (anchor!!.get(1) * pixelDensity).toInt() else (20 * pixelDensity).toInt()
                marginEnd = if (anchor!!.size > 2) (anchor!!.get(2) * pixelDensity).toInt() else (20 * pixelDensity).toInt()
                bottomMargin = if (anchor!!.size > 3) (anchor!!.get(3) * pixelDensity).toInt() else 0
            }
        } else {
            (binding.maneuverContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginStart = (20 * pixelDensity).toInt()
                topMargin = (20* pixelDensity).toInt()
                marginStart = (20 * pixelDensity).toInt()
                bottomMargin = 0
            }
        }
    }

    private fun updateManeuverRadius(radius: Int) {
        binding.maneuverContainer.setRadius(radius.toFloat())
    }

    private fun updateManeuverBackgroundColor(color: String) {
        binding.maneuverContainer.setCardBackgroundColor(Color.parseColor(color))
    }



  fun onDropViewInstance() {
    this.onDestroy()
  }

  fun setStartOrigin(origin: Point?) {
    this.origin = origin
  }

  fun setDestination(destination: Point?) {
    this.destination = destination
  }

  fun setWaypoints(waypoints: List<Point>) {
    this.waypoints = waypoints
  }

  fun setLocal(language: String) {
    val locals = language.split("-")
    when (locals.size) {
      1 -> locale = Locale(locals.first())
      2 -> locale = Locale(locals.first(), locals.last())
    }
  }

  fun setMute(mute: Boolean) {
    this.isVoiceInstructionsMuted = true
  }

  fun setShowCancelButton(show: Boolean) {
    //binding.stop.visibility = if (show) View.VISIBLE else View.INVISIBLE
  }
}
