
package de.callosumSw.RNGoogleAdManager;

import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNGoogleAdManagerModule extends ReactContextBaseJavaModule {
  private final String LOG_TAG = "RNGoogleAdManager";

  private final String FEMALE = "FEMALE";
  private final String MALE = "MALE";

  private final ReactApplicationContext reactContext;

  public RNGoogleAdManagerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNGoogleAdManager";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("simulatorTestId", PublisherAdRequest.DEVICE_ID_EMULATOR);

    return constants;
  }
}
