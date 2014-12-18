package com.moandjiezana.autoprefixer;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map.Entry;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class CanIUse {
  
  static enum SupportLevel {
    YES("y"), PREFIXED("x"), PARTIAL("a"), NO("n");
    
    private final String code;

    private SupportLevel(String code) {
      this.code = code;
    }
    
    public static SupportLevel fromCode(String code) {
      if ("y x".equals(code)) {
        return PREFIXED;
      }
      for (SupportLevel level : values()) {
        if (level.code.equals(code)) {
          return level;
        }
      }
      
      throw new IllegalArgumentException("Unknown support level code: " + code);
    }
  }
  
  private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  private JsonObject json;

  CanIUse() {
    json = gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/META-INF/resources/webjars/caniuse-db/1.0.30000034-SNAPSHOT/data.json"), StandardCharsets.UTF_8), JsonObject.class);
  }
  
  public Browser getBrowser(String name, String version) {
    Entry<String, JsonElement> agent = json.getAsJsonObject("agents")
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getAsJsonObject().get("browser").getAsString().equalsIgnoreCase(name))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("No browser found for: " + name));
    
    Browser browser = gson.fromJson(agent.getValue(), Browser.class);
    browser.code = agent.getKey();
    browser.version = version;
    int[] versionArray = new int[3];
    Arrays.fill(versionArray, 0);
    String[] split2 = version.split("\\.");
    for (int i = 0; i < split2.length; i++) {
      versionArray[i] = Integer.parseInt(split2[i]);
    }
    Version browserVersion = Version.forIntegers(versionArray[0], versionArray[1], versionArray[2]);
    if (browser.prefixExceptions != null) {
      browser.prefixExceptions.entrySet().stream().filter(e -> {
        String[] split = e.getKey().split("-");
        if (split.length == 2) {
          return browserVersion.satisfies(">=" + split[0] + " & <=" + split[1]);
        } else {
          int[] versionArray2 = new int[3];
          Arrays.fill(versionArray, 0);
          String[] split3 = version.split("\\.");
          for (int i = 0; i < split3.length; i++) {
            versionArray2[i] = Integer.parseInt(split3[i]);
          }
          Version browserVersion2 = Version.forIntegers(versionArray2[0], versionArray2[1], versionArray2[2]);
          return browserVersion.equals(browserVersion2);
        }
      }).findAny().ifPresent(entry -> browser.prefix = entry.getValue());
    }
    
    return browser;
  }
  
//  public SupportLevel getSupport(String feature, List<Browser> browsers) {
//    return SupportLevel.fromCode(json.getAsJsonObject("data").getAsJsonObject(feature).getAsJsonObject("stats").getAsJsonObject(browser.code).get(browser.version).getAsString());
//  }
  
  public SupportLevel getSupport(String feature, Browser browser) {
    return SupportLevel.fromCode(json.getAsJsonObject("data").getAsJsonObject(feature).getAsJsonObject("stats").getAsJsonObject(browser.code).get(browser.version).getAsString());
  }
  
  public static void main(String[] args) {
    new Version.Builder().setNormalVersion("25.0.0").build();
  }
}
