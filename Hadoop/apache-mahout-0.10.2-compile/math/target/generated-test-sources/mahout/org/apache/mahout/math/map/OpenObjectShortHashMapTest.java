/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
  
package org.apache.mahout.math.map;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.math.function.ObjectShortProcedure;
import org.apache.mahout.math.function.ObjectProcedure;
import org.apache.mahout.math.list.ShortArrayList;
import org.apache.mahout.math.set.AbstractSet;
import org.junit.Assert;
import org.junit.Test;

public class OpenObjectShortHashMapTest extends Assert {

    private static class NotComparableKey {
    protected int x;
    
    public NotComparableKey(int x) {
      this.x = x;
    }
      
    @Override
    public String toString() {
      return "[k " + x + " ]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + x;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      NotComparableKey other = (NotComparableKey) obj;
      return x == other.x;
    }
  }
  
  private final NotComparableKey[] ncKeys = {
    new NotComparableKey(101),
    new NotComparableKey(99),
    new NotComparableKey(2),
    new NotComparableKey(3),
    new NotComparableKey(4),
    new NotComparableKey(5)
    };
  
  @Test
  public void testConstructors() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenObjectShortHashMap<String>(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenObjectShortHashMap<String>(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    int prime = PrimeFinder.nextPrime(907);
    
    map.ensureCapacity(prime);
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
  }
  
  @Test
  public void testClear() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short)11); 
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testClone() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short)11);
    OpenObjectShortHashMap<String> map2 = (OpenObjectShortHashMap<String>) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short)11);
    assertTrue(map.containsKey("Eleven"));
    assertTrue(map.containsKey("Eleven"));
    assertFalse(map.containsKey("Twelve"));
  }
  
  @Test
  public void testContainValue() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short)11);
    assertTrue(map.containsValue((short)11));
    assertFalse(map.containsValue((short)12));
  }
  
  @Test
  public void testForEachKey() {
    final List<String> keys = new ArrayList<String>();
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    map.forEachKey(new ObjectProcedure<String>() {
      
      @Override
      public boolean apply(String element) {
        keys.add(element);
        return true;
      }
    });
    
    assertEquals(3, keys.size());
    Collections.sort(keys);
    assertSame("Fourteen", keys.get(1));
    assertSame("Twelve", keys.get(2));
    assertSame("Eleven", keys.get(0));
  }
  
  private static class Pair implements Comparable<Pair> {
    short v;
    String k;
    
    Pair(String k, short v) {
      this.k = k;
      this.v = v;
    }
    
    @Override
    public int compareTo(Pair o) {
      return k.compareTo(o.k);
    }
  }
  
  @Test
  public void testForEachPair() {
    final List<Pair> pairs = new ArrayList<Pair>();
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    map.forEachPair(new ObjectShortProcedure<String>() {
      
      @Override
      public boolean apply(String first, short second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((short)14, pairs.get(1).v );
    assertSame("Fourteen", pairs.get(1).k);
    assertEquals((short) 12, pairs.get(2).v );
    assertSame("Twelve", pairs.get(2).k);
    assertEquals((short) 11, pairs.get(0).v );
    assertSame("Eleven", pairs.get(0).k);
    
    pairs.clear();
    map.forEachPair(new ObjectShortProcedure<String>() {
      int count = 0;
      
      @Override
      public boolean apply(String first, short second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    assertEquals((short)11, map.get("Eleven") );
  }

  @Test
  public void testKeys() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    List<String> keys = new ArrayList<String>();
    map.keys(keys);
    Collections.sort(keys);
    assertSame("Twelve", keys.get(1));
    assertSame("Eleven", keys.get(0));
    List<String> k2 = map.keys();
    Collections.sort(k2);
    assertEquals(keys, k2);
  }
  
  @Test
  public void testAdjustOrPutValue() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.adjustOrPutValue("Eleven", (short)1, (short)3);
    assertEquals(14, map.get("Eleven") );
    map.adjustOrPutValue("Fifteen", (short)1, (short)3);
    assertEquals(1, map.get("Fifteen") );
  }
  
  @Test
  public void testPairsMatching() {
    List<String> keyList = new ArrayList<String>();
    ShortArrayList valueList = new ShortArrayList();
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    map.pairsMatching(new ObjectShortProcedure<String>() {

      @Override
      public boolean apply(String first, short second) {
        return (second % 2) == 0;
      }},
        keyList, valueList);
    Collections.sort(keyList);
    valueList.sort();
    assertEquals(2, keyList.size());
    assertEquals(2, valueList.size());
    assertSame("Fourteen", keyList.get(0));
    assertSame("Twelve", keyList.get(1));
    assertEquals((short)14, valueList.get(1) );
    assertEquals((short)12, valueList.get(0) );
  }
  
  @Test
  public void testValues() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    ShortArrayList values = new ShortArrayList(100);
    map.values(values);
    assertEquals(3, values.size());
    values.sort();
    assertEquals(11, values.get(0) );
    assertEquals(12, values.get(1) );
    assertEquals(14, values.get(2) );
  }
  
  // tests of the code in the abstract class
  
  @Test
  public void testCopy() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short)11);
    OpenObjectShortHashMap<String> map2 = (OpenObjectShortHashMap<String>) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    OpenObjectShortHashMap<String> map2 = (OpenObjectShortHashMap<String>) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey("Eleven");
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    map.removeKey("Thirteen");
    List<String> keys = new ArrayList<String>();
    map.keysSortedByValue(keys);
    String[] keysArray = keys.toArray(new String[keys.size()]);
    assertArrayEquals(new String[] {"Eleven", "Twelve", "Fourteen"},
        keysArray);
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    
    ShortArrayList values = new ShortArrayList();
    List<String> keys = new ArrayList<String>();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((short) 11, values.get(0) );
    assertSame("Eleven", keys.get(0));
    assertEquals((short) 14, values.get(1) );
    assertSame("Fourteen", keys.get(1));
    assertEquals((short) 13, values.get(2) );
    assertSame("Thirteen", keys.get(2));
    assertEquals((short) 12, values.get(3) );
    assertSame("Twelve", keys.get(3));
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testPairsSortedByKeyNotComparable() {
    OpenObjectShortHashMap<NotComparableKey> map = new OpenObjectShortHashMap<NotComparableKey>();
    map.put(ncKeys[0], (short) 11);
    map.put(ncKeys[1], (short) 12);
    map.put(ncKeys[2], (short) 13);
    map.put(ncKeys[3], (short) 14);
    ShortArrayList values = new ShortArrayList();
    List<NotComparableKey> keys = new ArrayList<NotComparableKey>();
    map.pairsSortedByKey(keys, values);
  }
  
  @Test
  public void testPairsSortedByValue() {
    OpenObjectShortHashMap<String> map = new OpenObjectShortHashMap<String>();
    map.put("Eleven", (short) 11);
    map.put("Twelve", (short) 12);
    map.put("Thirteen", (short) 13);
    map.put("Fourteen", (short) 14);
    
    List<String> keys = new ArrayList<String>();
    ShortArrayList values = new ShortArrayList();
    map.pairsSortedByValue(keys, values);
    assertEquals((short) 11, values.get(0) );
    assertEquals("Eleven", keys.get(0));
    assertEquals((short) 12, values.get(1) );
    assertEquals("Twelve", keys.get(1));
    assertEquals((short) 13, values.get(2) );
    assertEquals("Thirteen", keys.get(2));
    assertEquals((short) 14, values.get(3) );
    assertEquals("Fourteen", keys.get(3));
  }
 
 }
