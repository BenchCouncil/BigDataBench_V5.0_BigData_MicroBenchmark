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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.math.function.LongCharProcedure;
import org.apache.mahout.math.function.LongProcedure;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.list.CharArrayList;
import org.apache.mahout.math.set.AbstractSet;

import org.junit.Assert;
import org.junit.Test;

public class OpenLongCharHashMapTest extends Assert {

  
  @Test
  public void testConstructors() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenLongCharHashMap(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenLongCharHashMap(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
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
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, map.get((long) 11), 0.0000001);
  }
  
  @Test
  public void testClone() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    OpenLongCharHashMap map2 = (OpenLongCharHashMap) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    assertTrue(map.containsKey((long) 11));
    assertFalse(map.containsKey((long) 12));
  }
  
  @Test
  public void testContainValue() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    assertTrue(map.containsValue((char) 22));
    assertFalse(map.containsValue((char) 23));
  }
  
  @Test
  public void testForEachKey() {
    final LongArrayList keys = new LongArrayList();
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    map.forEachKey(new LongProcedure() {
      
      @Override
      public boolean apply(long element) {
        keys.add(element);
        return true;
      }
    });
    
    long[] keysArray = keys.toArray(new long[keys.size()]);
    Arrays.sort(keysArray);
    
    assertArrayEquals(new long[] {11, 12, 14}, keysArray );
  }
  
  private static class Pair implements Comparable<Pair> {
    long k;
    char v;
    
    Pair(long k, char v) {
      this.k = k;
      this.v = v;
    }
    
    @Override
    public int compareTo(Pair o) {
      if (k < o.k) {
        return -1;
      } else if (k == o.k) {
        return 0;
      } else {
        return 1;
      }
    }
  }
  
  @Test
  public void testForEachPair() {
    final List<Pair> pairs = new ArrayList<Pair>();
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    map.forEachPair(new LongCharProcedure() {
      
      @Override
      public boolean apply(long first, char second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((long) 11, pairs.get(0).k );
    assertEquals((char) 22, pairs.get(0).v );
    assertEquals((long) 12, pairs.get(1).k );
    assertEquals((char) 23, pairs.get(1).v );
    assertEquals((long) 14, pairs.get(2).k );
    assertEquals((char) 25, pairs.get(2).v );
    
    pairs.clear();
    map.forEachPair(new LongCharProcedure() {
      int count = 0;
      
      @Override
      public boolean apply(long first, char second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    assertEquals(22, map.get((long)11) );
    assertEquals(0, map.get((long)0) );
  }
  
  @Test
  public void testAdjustOrPutValue() {
   OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.adjustOrPutValue((long)11, (char)1, (char)3);
    assertEquals(25, map.get((long)11) );
    map.adjustOrPutValue((long)15, (char)1, (char)3);
    assertEquals(1, map.get((long)15) );
  }
  
  @Test
  public void testKeys() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 22);
    LongArrayList keys = new LongArrayList();
    map.keys(keys);
    keys.sort();
    assertEquals(11, keys.get(0) );
    assertEquals(12, keys.get(1) );
    LongArrayList k2 = map.keys();
    k2.sort();
    assertEquals(keys, k2);
  }
  
  @Test
  public void testPairsMatching() {
    LongArrayList keyList = new LongArrayList();
    CharArrayList valueList = new CharArrayList();
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    map.pairsMatching(new LongCharProcedure() {

      @Override
      public boolean apply(long first, char second) {
        return (first % 2) == 0;
      }},
        keyList, valueList);
    keyList.sort();
    valueList.sort();
    assertEquals(2, keyList.size());
    assertEquals(2, valueList.size());
    assertEquals(12, keyList.get(0) );
    assertEquals(14, keyList.get(1) );
    assertEquals(23, valueList.get(0) );
    assertEquals(25, valueList.get(1) );
  }
  
  @Test
  public void testValues() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    CharArrayList values = new CharArrayList(100);
    map.values(values);
    assertEquals(3, values.size());
    values.sort();
    assertEquals(22, values.get(0) );
    assertEquals(23, values.get(1) );
    assertEquals(25, values.get(2) );
  }
  
  // tests of the code in the abstract class
  
  @Test
  public void testCopy() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    OpenLongCharHashMap map2 = (OpenLongCharHashMap) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    OpenLongCharHashMap map2 = (OpenLongCharHashMap) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey((long) 11);
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 22);
    map.put((long) 12, (char) 23);
    map.put((long) 13, (char) 24);
    map.put((long) 14, (char) 25);
    map.removeKey((long) 13);
    LongArrayList keys = new LongArrayList();
    map.keysSortedByValue(keys);
    long[] keysArray = keys.toArray(new long[keys.size()]);
    assertArrayEquals(new long[] {11, 12, 14},
        keysArray );
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenLongCharHashMap map = new OpenLongCharHashMap();
    map.put((long) 11, (char) 100);
    map.put((long) 12, (char) 70);
    map.put((long) 13, (char) 30);
    map.put((long) 14, (char) 3);
    
    LongArrayList keys = new LongArrayList();
    CharArrayList values = new CharArrayList();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((long) 11, keys.get(0) );
    assertEquals((char) 100, values.get(0) );
    assertEquals((long) 12, keys.get(1) );
    assertEquals((char) 70, values.get(1) );
    assertEquals((long) 13, keys.get(2) );
    assertEquals((char) 30, values.get(2) );
    assertEquals((long) 14, keys.get(3) );
    assertEquals((char) 3, values.get(3) );
    keys.clear();
    values.clear();
    map.pairsSortedByValue(keys, values);
    assertEquals((long) 11, keys.get(3) );
    assertEquals((char) 100, values.get(3) );
    assertEquals((long) 12, keys.get(2) );
    assertEquals((char) 70, values.get(2) );
    assertEquals((long) 13, keys.get(1) );
    assertEquals((char) 30, values.get(1) );
    assertEquals((long) 14, keys.get(0) );
    assertEquals((char) 3, values.get(0) );
  }
 
 }
