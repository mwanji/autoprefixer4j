package com.moandjiezana.autoprefixer;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CssProcessingTest {

  @Test
  public void should_prefix_transition() throws Exception {
    String input = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "postCss", "autoprefixer-core", "transition.css")));
    String output = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "postCss", "autoprefixer-core", "transition.out.css")));
    
    
    AutoPrefixer.Settings settings = new AutoPrefixer.Settings().browsers("Chrome 25", "Opera 12");
    
    AutoPrefixerResult result = new AutoPrefixer(settings).process(input);
    
    assertEquals(output.replace(" ", ""), result.getCss().replace(" ", ""));
  }
  
  public static void main(String[] args) {
    JsonObject json = new Gson().fromJson(new InputStreamReader(CssProcessingTest.class.getResourceAsStream("/META-INF/resources/webjars/caniuse-db/1.0.30000034-SNAPSHOT/data.json"), StandardCharsets.UTF_8), JsonObject.class);
    
    json.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue().getClass()));
    System.out.println("eras");
    json.getAsJsonObject("eras").entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    
    System.out.println("agents");
    json.getAsJsonObject("agents").entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    
    System.out.println("statuses");
    json.getAsJsonObject("statuses").entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    
    System.out.println("cats");
    json.getAsJsonObject("cats").entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    
    System.out.println("updated: " + new Date(json.get("updated").getAsLong() * 1000));
    
    System.out.println("data");
    List<String> dataKeys = Arrays.asList("status", "stats", "usage_perc_y", "usage_perc_a", "ucprefix", "parent", "ie_id", "chrome_id");
//    json.getAsJsonObject("data").entrySet().forEach(entry -> System.out.println(entry.getKey() + ": \n\t" + entry.getValue().getAsJsonObject().entrySet().stream().filter(data -> dataKeys.contains(data.getKey())).map(data -> data.getKey() + ": " + data.getValue()).collect(Collectors.joining(","))));
//    json.getAsJsonObject("data").entrySet().stream().map(Map.Entry::getKey).sorted().forEach(System.out::println);
    
  }
}
