package de.callosumSw.RNGoogleAdManager;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;
import java.util.Map;

class BannerView extends ReactViewGroup {
    public static final String LOG_TAG = "RNGoogleAdManager";

    public static final String AD_CLICKED = "AD_CLICKED";
    public static final String AD_CLOSED = "AD_CLOSED";
    public static final String AD_FAILED = "AD_FAILED";
    public static final String AD_LOADED = "AD_LOADED";
    public static final String AD_REQUEST = "AD_REQUEST";
    public static final String NATIVE_ERROR = "NATIVE_ERROR";
    public static final String PROPS_SET = "PROPS_SET";

    public static final String BANNER = "BANNER";
    public static final String FULL_BANNER = "FULL_BANNER";
    public static final String LARGE_BANNER = "LARGE_BANNER";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String MEDIUM_RECTANGLE = "MEDIUM_RECTANGLE";

    protected Integer adHeight = null;
    protected Integer adHeightInPixel = null;
    protected String adId = null;
    protected ArrayList<AdSize> adSizes = null;
    protected String adType = null;
    protected PublisherAdView adView;
    protected Integer adWidth = null;
    protected Integer adWidthInPixel = null;
    protected Context context = null;
    protected ArrayList<String> testDeviceIds = null;
    protected Map<String, Object> targeting = null;

    public BannerView (final Context context) {
        super(context);
    }

    public void sendEvent(String type, @Nullable WritableMap event) {
        try {
            ReactContext reactContext = (ReactContext)getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    getId(),
                    type,
                    event
            );
        } catch (Exception err) {
            Log.d(LOG_TAG, Log.getStackTraceString(err));
        }
    }

    public void logAndSendError(Exception e){
        try {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
            String errorMessage = e.getMessage();

            WritableMap event = Arguments.createMap();
            event.putString("errorMessage", errorMessage);

            sendEvent(NATIVE_ERROR, event);
        } catch (Exception err) {
            Log.d(LOG_TAG, Log.getStackTraceString(err));
        }
    }

    private void sendIfPropsSet(){
        if(adId != null && (adSizes != null || adType != null)){
            sendEvent(PROPS_SET, null);
        }
    }

    private void addAdView(){
        try {
            this.addView(this.adView);
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    private void createAdView(){
        try {
            context = getContext();
            this.adView = new PublisherAdView(context);
            this.adView.setAdUnitId(adId);

            if (adType != null) {
                switch (adType) {
                    case BANNER:
                        this.adView.setAdSizes(new AdSize[]{AdSize.BANNER});
                        break;
                    case FULL_BANNER:
                        this.adView.setAdSizes(new AdSize[]{AdSize.FULL_BANNER});
                        break;
                    case LARGE_BANNER:
                        this.adView.setAdSizes(new AdSize[]{AdSize.LARGE_BANNER});
                        break;
                    case LEADERBOARD:
                        this.adView.setAdSizes(new AdSize[]{AdSize.LEADERBOARD});
                        break;
                    case MEDIUM_RECTANGLE:
                    default:
                        this.adView.setAdSizes(new AdSize[]{AdSize.MEDIUM_RECTANGLE});
                        break;
                }

                AdSize adSize = this.adView.getAdSize();
                adWidth = adSize.getWidth();
                adHeight = adSize.getHeight();
                adWidthInPixel = adSize.getWidthInPixels(context);
                adHeightInPixel = adSize.getHeightInPixels(context);
            } else if (adSizes != null) {
                AdSize []sizes = adSizes.toArray(new AdSize[0]);
                this.adView.setAdSizes(sizes);
            }
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    private void destroyAdView(){
        try {
            if (this.adView != null) {
                this.adView.destroy();
            }
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    private void removeAdView(){
        try {
            this.removeView(this.adView);
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    private String getFailedToLoadReason(int code){
        switch (code){
            case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                return "Internal Error";
            case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                return "Invalid Request";
            case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                return "Network error";
            case PublisherAdRequest.ERROR_CODE_NO_FILL:
                return "No Fill";
            default:
                return "Could not get message. Unknown code: " + code;
        }
    }

    public class BannerAppEventListener extends Activity implements AppEventListener {
        @Override
        public void onAppEvent(String name, String info) {
            switch (name) {
                case AD_CLICKED: {
                    Log.d(LOG_TAG, "Ad clicked");

                    WritableMap event = Arguments.createMap();
                    event.putString("url", info);

                    BannerView.this.sendEvent(AD_CLICKED, event);

                    break;
                }

                case AD_CLOSED: {
                    Log.d(LOG_TAG, "Ad closed");

                    destroyAdView();

                    BannerView.this.sendEvent(AD_CLOSED, null);
                    break;
                }
            }
        }
    }

    private void sendLoadEvent(int width, int height) {
        WritableMap event = Arguments.createMap();
        event.putInt("width", width);
        event.putInt("height", height);

        sendEvent(AD_LOADED, event);
    }

    private void handleLoad(String adServer) {
        try {
            Log.d(LOG_TAG, "Ad loaded. Server: " + adServer);

            if (adType == null) {
                AdSize adSize = adView.getAdSize();
                adWidth = adSize.getWidth();
                adHeight = adSize.getHeight();
                adWidthInPixel = adSize.getWidthInPixels(context);
                adHeightInPixel = adSize.getHeightInPixels(context);
            }

            adView.measure(adWidth, adHeight);
            adView.layout(0, 0, adWidthInPixel, adHeightInPixel);

            sendLoadEvent(adWidth, adHeight);
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    private void setListeners(){
        this.adView.setAppEventListener(new BannerAppEventListener());
        this.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                try {
                    handleLoad("GAM");
                } catch (Exception e) {
                    BannerView.this.logAndSendError(e);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                try {
                    String errorMessage = getFailedToLoadReason(errorCode);

                    Log.d(LOG_TAG, "Ad failed to load. Reason: " + errorMessage);

                    WritableMap event = Arguments.createMap();
                    event.putString("errorMessage", errorMessage);

                    BannerView.this.sendEvent(AD_FAILED, event);
                } catch (Exception e) {
                    BannerView.this.logAndSendError(e);
                }
            }
        });
    }

    private void loadAd(){
        try {
            PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

            for(String testId : testDeviceIds){
                adRequestBuilder.addTestDevice(testId);
            }

            for (Map.Entry<String, Object> entry : targeting.entrySet()) {
                String key = entry.getKey();
                ArrayList value =  (ArrayList) entry.getValue();

                adRequestBuilder.addCustomTargeting(key, value);
            }

            final PublisherAdRequest adRequest = adRequestBuilder.build();
            final String adUnitId = this.adView.getAdUnitId();
            final AdSize adSize = this.adView.getAdSize();

            WritableMap event = Arguments.createMap();

            Log.d(LOG_TAG, "GAM Banner request with adunit id " + adUnitId + " with size " + adSize);
            this.adView.loadAd(adRequest);
            sendEvent(AD_REQUEST, event);
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void addBannerView() {
        try {
            if(this.adView == null ){
                this.createAdView();
                this.setListeners();
            }
            this.addAdView();
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void destroyBanner() {
        try {
            if(this.adView != null) {
                this.destroyAdView();
            }
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void loadBanner() {
        try {
            if(this.adView != null) {
                final String adUnitId = this.adView.getAdUnitId();

                if (!adId.equals(adUnitId) && adUnitId != null) {
                    this.destroyAdView();
                }
            }

            if(this.adView == null) {
                this.createAdView();
                this.setListeners();
            }

            this.loadAd();
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void removeBannerView() {
        try {
            if(this.adView != null) {
                this.removeAdView();
            }
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void openDebugMenu() {
        try {
            ReactContext reactContext = (ReactContext)getContext();
            MobileAds.openDebugMenu(reactContext.getCurrentActivity(), adId);
        } catch (Exception e) {
            logAndSendError(e);
        }
    }

    protected void setAdUnitId() {
        if(this.adView == null){
            sendIfPropsSet();
        }
    }

    protected void setAdSizes() {
        try {
            if(this.adView != null) {
                AdSize[] arr = adSizes.toArray(new AdSize[0]);
                this.adView.setAdSizes(arr);
            }
            sendIfPropsSet();
        } catch (Exception e) {
            logAndSendError(e);
        }
    }
}

public class BannerViewManager extends ViewGroupManager<BannerView> {
    private static final String REACT_CLASS = "RNGAMBannerView";
    public static final String LOG_TAG = "RNGoogleAdManager";

    public static final int COMMAND_ADD_BANNER_VIEW = 1;
    public static final int COMMAND_DESTROY_BANNER = 2;
    public static final int COMMAND_LOAD_BANNER = 3;
    public static final int COMMAND_REMOVE_BANNER_VIEW = 4;
    public static final int COMMAND_PAUSE_BANNER_VIEW = 5;
    public static final int COMMAND_RESUME_BANNER_VIEW = 6;
    public static final int COMMAND_OPEN_DEBUG_MENU = 7;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected BannerView createViewInstance(ThemedReactContext context) {
        return new BannerView(context);
    }

    @Override
    public void addView(BannerView parent, View child, int index) {
        throw new RuntimeException("RNGAMBannerView cannot have subviews");
    }

    @Override
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put(BannerView.AD_CLICKED,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onAdClicked")))
                .put(BannerView.AD_CLOSED,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onAdClosed")))
                .put(BannerView.AD_FAILED,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onAdFailedToLoad")))
                .put(BannerView.AD_LOADED,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onAdLoaded")))
                .put(BannerView.AD_REQUEST,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onAdRequest")))
                .put(BannerView.NATIVE_ERROR,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onNativeError")))
                .put(BannerView.PROPS_SET,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onPropsSet")))
                .build();
    }

    @ReactProp(name = "adId")
    public void setAdId(BannerView view, @Nullable String adId) {
        try {
            view.adId = adId;
            view.setAdUnitId();
        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    @ReactProp(name = "adSizes")
    public void setSize(BannerView view, @Nullable ReadableArray adSizes) {
        try {
            ArrayList<AdSize> list = new ArrayList<>();

            for(int i = 0; i < adSizes.size(); i++){
                ReadableArray sizes = adSizes.getArray(i);
                Integer width = sizes.getInt(0);
                Integer height = sizes.getInt(1);
                AdSize adSize = new AdSize(width, height);
                list.add(adSize);
            }

            view.adSizes = list;
            view.setAdSizes();
        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    @ReactProp(name = "adType")
    public void setAdSize(BannerView view, @Nullable String adType) {
        Log.d(LOG_TAG, String.valueOf(adType));
        view.adType = adType;
    }

    @ReactProp(name = "testDeviceIds")
    public void setTestDeviceIds(BannerView view, ReadableArray testDeviceIds) {
        try {
            ArrayList<String> list = new ArrayList<>();

            for(int i = 0; i < testDeviceIds.size(); i++){
                String item = testDeviceIds.getString(i);
                list.add(item);
            }

            view.testDeviceIds = list;
        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    @ReactProp(name = "targeting")
    public void setTargeting(BannerView view, ReadableMap targeting) {
        view.targeting = targeting.toHashMap();
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "addBannerView", COMMAND_ADD_BANNER_VIEW,
                "destroyBanner", COMMAND_DESTROY_BANNER,
                "loadBanner", COMMAND_LOAD_BANNER,
                "removeBannerView", COMMAND_REMOVE_BANNER_VIEW,
                "openDebugMenu", COMMAND_OPEN_DEBUG_MENU
        );
    }

    @Override
    public void receiveCommand(BannerView view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_ADD_BANNER_VIEW:
                view.addBannerView();
                break;

            case COMMAND_DESTROY_BANNER:
                view.destroyBanner();
                break;

            case COMMAND_LOAD_BANNER:
                view.loadBanner();
                break;

            case COMMAND_REMOVE_BANNER_VIEW:
                view.removeBannerView();
                break;

            case COMMAND_OPEN_DEBUG_MENU:
                view.openDebugMenu();
                break;
        }
    }
}
