package com.moandjiezana.autoprefixer;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import com.helger.css.decl.CSSExpressionMemberTermSimple;
import com.helger.css.decl.CSSSelector;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSExpressionMember;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;
import com.moandjiezana.autoprefixer.CanIUse.SupportLevel;

public class AutoPrefixer {

  private final CanIUse canIUse = new CanIUse();
  private final Settings settings;
  private final List<Browser> browsers = new ArrayList<>();

  public static class Settings {
    private final List<String> browsers = new ArrayList<>();
    private boolean cascade;
    
    public Settings browsers(String... browsers) {
      for (String browser : browsers) {
        this.browsers.add(browser);
      }
      
      return this;
    }
  }
  
  public AutoPrefixer(AutoPrefixer.Settings settings) {
    this.settings = settings;
    settings.browsers.forEach(b -> {
      String[] split = b.split(" ");
      this.browsers.add(canIUse.getBrowser(split[0], split[1]));
    });
  }

  public AutoPrefixerResult process(String unprefixedCss) {
    CascadingStyleSheet css = CSSReader.readFromString(unprefixedCss, ECSSVersion.CSS30);
    CascadingStyleSheet out = new CascadingStyleSheet();
    
    for (ICSSTopLevelRule rule : css.getAllRules()) {
      if (rule instanceof CSSStyleRule) {
        styleRule(out, (CSSStyleRule) rule);
      } else {
        out.addRule(rule);
      }
    }
    
    StringWriter writer = new StringWriter();
    try {
      CSSWriterSettings writerSettings = new CSSWriterSettings(ECSSVersion.CSS30, false);
      CSSWriter cssWriter = new CSSWriter(writerSettings);
      cssWriter.setWriteHeaderText(false);
      cssWriter.writeCSS(out, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new AutoPrefixerResult(writer.toString());
  }

  private void styleRule(CascadingStyleSheet css, CSSStyleRule rule) {
    CSSStyleRule newRule = new CSSStyleRule();
    for (int i = 0; i < rule.getSelectorCount(); i++) {
      CSSSelector selector = rule.getSelectorAtIndex(i);
      newRule.addSelector(selector);
    }
    
    for (int i = 0; i < rule.getDeclarationCount(); i++) {
      CSSDeclaration declaration = rule.getDeclarationAtIndex(i);
      String property = declaration.getProperty();
      
      if (property.equals("transition")) {
        List<Browser> prefixedBrowsers = browsers.stream().filter(b -> canIUse.getSupport("css-transitions", b) != SupportLevel.YES).collect(toList());
        List<CSSDeclaration> prefixedDeclarations = prefixedBrowsers.stream().map(b -> new CSSDeclaration("-" + b.prefix + "-" + property, new CSSExpression())).collect(Collectors.toList());
        for (ICSSExpressionMember member : declaration.getExpression().getAllMembers()) {
          if (member instanceof CSSExpressionMemberTermSimple && ((CSSExpressionMemberTermSimple) member).getValue().equals("transform")) {
            for (int j = 0; j < prefixedBrowsers.size(); j++) {
              prefixedDeclarations.get(j).getExpression().addMember(new CSSExpressionMemberTermSimple("-" + prefixedBrowsers.get(j).prefix + "-transform"));
            }
          } else {
            prefixedDeclarations.forEach(decl -> decl.getExpression().addMember(member));
          }
        }
        
        prefixedDeclarations.forEach(decl -> newRule.addDeclaration(decl));
        newRule.addDeclaration(declaration);
      } else if (property.equals("transition-property")) {
          List<Browser> prefixedBrowsers = browsers.stream().filter(b -> canIUse.getSupport("css-transitions", b) != SupportLevel.YES).collect(toList());
          List<CSSDeclaration> prefixedDeclarations = prefixedBrowsers.stream().map(b -> new CSSDeclaration("-" + b.prefix + "-" + property, new CSSExpression())).collect(Collectors.toList());
          for (ICSSExpressionMember member : declaration.getExpression().getAllMembers()) {
            if (member instanceof CSSExpressionMemberTermSimple && ((CSSExpressionMemberTermSimple) member).getValue().equals("filter")) {
              for (int j = 0; j < prefixedBrowsers.size(); j++) {
                prefixedDeclarations.get(j).getExpression().addMember(new CSSExpressionMemberTermSimple("-" + prefixedBrowsers.get(j).prefix + "-filter"));
              }
            } else {
              prefixedDeclarations.forEach(decl -> decl.getExpression().addMember(member));
            }
          }
          
          prefixedDeclarations.forEach(decl -> newRule.addDeclaration(decl));
          newRule.addDeclaration(declaration);
      } else if (property.equals("transform")) {
        List<Browser> prefixedBrowsers = browsers.stream().filter(b -> canIUse.getSupport("transforms2d", b) != SupportLevel.YES).collect(toList());
        List<CSSDeclaration> prefixedDeclarations = prefixedBrowsers.stream().map(b -> new CSSDeclaration("-" + b.prefix + "-" + property, new CSSExpression())).collect(Collectors.toList());
        for (ICSSExpressionMember member : declaration.getExpression().getAllMembers()) {
          prefixedDeclarations.forEach(decl -> decl.getExpression().addMember(member));
        }
        
        prefixedDeclarations.forEach(decl -> newRule.addDeclaration(decl));
        newRule.addDeclaration(declaration);
      } else if (property.equals("animation-name")) {
        List<Browser> prefixedBrowsers = browsers.stream().filter(b -> canIUse.getSupport("css-animation", b) != SupportLevel.YES).collect(toList());
        List<CSSDeclaration> prefixedDeclarations = prefixedBrowsers.stream().map(b -> new CSSDeclaration("-" + b.prefix + "-" + property, new CSSExpression())).collect(Collectors.toList());
        for (ICSSExpressionMember member : declaration.getExpression().getAllMembers()) {
          prefixedDeclarations.forEach(decl -> decl.getExpression().addMember(member));
        }
        
        prefixedDeclarations.forEach(decl -> newRule.addDeclaration(decl));
        newRule.addDeclaration(declaration);
      } else {
        newRule.addDeclaration(declaration);
      }
    }
    
    css.addRule(newRule);
  }
}
