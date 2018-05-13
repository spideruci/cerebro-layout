package org.spideruci.cerebro.layout.model;

import java.util.ArrayList;

public class StringArrayRegistry {
  
  public final ArrayList<String> records;
  
  public StringArrayRegistry() {
    this.records = new ArrayList<>();
  }
  
  public void register(String entry) {
    int entryCode = records.indexOf(entry);
    if(entryCode == -1) {
      records.add(entry);
    }
  }
  
  public int count() {
    return records.size();
  }
  
  public int entryCode(String entry) {
    return records.indexOf(entry);
  }

}
