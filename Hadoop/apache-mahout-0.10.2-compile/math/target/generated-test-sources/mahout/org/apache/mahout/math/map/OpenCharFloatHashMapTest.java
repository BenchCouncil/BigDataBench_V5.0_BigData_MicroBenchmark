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

import org.apache.mahout.math.function.CharFloatProcedure;
import org.apache.mahout.math.function.CharProcedure;
import org.apache.mahout.math.list.CharArrayList;
import org.apache.mahout.math.list.FloatArrayList;
import org.apache.mahout.math.set.AbstractSet;

import org.junit.Assert;
import org.junit.Test;

public class OpenCharFloatHashMapTest extends Assert {

  
  @Test
  public void testConstructors() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenCharFloatHashMap(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenCharFloatHashMap(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
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
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, map.get((char) 11), 0.0000001);
  }
  
  @Test
  public void testClone() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    OpenCharFloatHashMap map2 = (OpenCharFloatHashMap) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    assertTrue(map.containsKey((char) 11));
    assertFalse(map.containsKey((char) 12));
  }
  
  @Test
  public void testContainValue() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    assertTrue(map.containsValue((float) 22));
    assertFalse(map.containsValue((float) 23));
  }
  
  @Test
  public void testForEachKey() {
    final CharArrayList keys = new CharArrayList();
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    map.forEachKey(new CharProcedure() {
      
      @Override
      public boolean apply(char element) {
        keys.add(element);
        return true;
      }
    });
    
    char[] keysArray = keys.toArray(new char[keys.size()]);
    Arrays.sort(keysArray);
    
    assertArrayEquals(new char[] {11, 12, 14}, keysArray );
  }
  
  private static class Pair implements Comparable<Pair> {
    char k;
    float v;
    
    Pair(char k, float v) {
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
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    map.forEachPair(new CharFloatProcedure() {
      
      @Override
      public boolean apply(char first, float second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((char) 11, pairs.get(0).k );
    assertEquals((float) 22, pairs.get(0).v , (float)0.000001);
    assertEquals((char) 12, pairs.get(1).k );
    assertEquals((float) 23, pairs.get(1).v , (float)0.000001);
    assertEquals((char) 14, pairs.get(2).k );
    assertEquals((float) 25, pairs.get(2).v , (float)0.000001);
    
    pairs.clear();
    map.forEachPair(new CharFloatProcedure() {
      int count = 0;
      
      @Override
      public boolean apply(char first, float second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    assertEquals(22, map.get((char)11) , (float)0.000001);
    assertEquals(0, map.get((char)0) , (float)0.000001);
  }
  
  @Test
  public void testAdjustOrPutValue() {
   OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.adjustOrPutValue((char)11, (float)1, (float)3);
    assertEquals(25, map.get((char)11) , (float)0.000001);
    map.adjustOrPutValue((char)15, (float)1, (float)3);
    assertEquals(1, map.get((char)15) , (float)0.000001);
  }
  
  @Test
  public void testKeys() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 22);
    CharArrayList keys = new CharArrayList();
    map.keys(keys);
    keys.sort();
    assertEquals(11, keys.get(0) );
    assertEquals(12, keys.get(1) );
    CharArrayList k2 = map.keys();
    k2.sort();
    assertEquals(keys, k2);
  }
  
  @Test
  public void testPairsMatching() {
    CharArrayList keyList = new CharArrayList();
    FloatArrayList valueList = new FloatArrayList();
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    map.pairsMatching(new CharFloatProcedure() {

      @Override
      public boolean apply(char first, float second) {
        return (first % 2) == 0;
      }},
        keyList, valueList);
    keyList.sort();
    valueList.sort();
    assertEquals(2, keyList.size());
    assertEquals(2, valueList.size());
    assertEquals(12, keyList.get(0) );
    assertEquals(14, keyList.get(1) );
    assertEquals(23, valueList.get(0) , (float)0.000001);
    assertEquals(25, valueList.get(1) , (float)0.000001);
  }
  
  @Test
  public void testValues() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    FloatArrayList values = new FloatArrayList(100);
    map.values(values);
    assertEquals(3, values.size());
    values.sort();
    assertEquals(22, values.get(0) , (float)0.000001);
    assertEquals(23, values.get(1) , (float)0.000001);
    assertEquals(25, values.get(2) , (float)0.000001);
  }
  
  // tests of the code in the abstract class
  
  @Test
  public void testCopy() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    OpenCharFloatHashMap map2 = (OpenCharFloatHashMap) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    OpenCharFloatHashMap map2 = (OpenCharFloatHashMap) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey((char) 11);
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 22);
    map.put((char) 12, (float) 23);
    map.put((char) 13, (float) 24);
    map.put((char) 14, (float) 25);
    map.removeKey((char) 13);
    CharArrayList keys = new CharArrayList();
    map.keysSortedByValue(keys);
    char[] keysArray = keys.toArray(new char[keys.size()]);
    assertArrayEquals(new char[] {11, 12, 14},
        keysArray );
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenCharFloatHashMap map = new OpenCharFloatHashMap();
    map.put((char) 11, (float) 100);
    map.put((char) 12, (float) 70);
    map.put((char) 13, (float) 30);
    map.put((char) 14, (float) 3);
    
    CharArrayList keys = new CharArrayList();
    FloatArrayList values = new FloatArrayList();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((char) 11, keys.get(0) );
    assertEquals((float) 100, values.get(0) , (float)0.000001);
    assertEquals((char) 12, keys.get(1) );
    assertEquals((float) 70, values.get(1) , (float)0.000001);
    assertEquals((char) 13, keys.get(2) );
    assertEquals((float) 30, values.get(2) , (float)0.000001);
    assertEquals((char) 14, keys.get(3) );
    assertEquals((float) 3, values.get(3) , (float)0.000001);
    keys.clear();
    values.clear();
    map.pairsSortedByValue(keys, values);
    assertEquals((char) 11, keys.get(3) );
    assertEquals((float) 100, values.get(3) , (float)0.000001);
    assertEquals((char) 12, keys.get(2) );
    assertEquals((float) 70, values.get(2) , (float)0.000001);
    assertEquals((char) 13, keys.get(1) );
    assertEquals((float) 30, values.get(1) , (float)0.000001);
    assertEquals((char) 14, keys.get(0) );
    assertEquals((float) 3, values.get(0) , (float)0.000001);
  }
 
 }
