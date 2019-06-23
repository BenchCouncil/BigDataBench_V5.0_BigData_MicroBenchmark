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

import org.apache.mahout.math.function.DoubleDoubleProcedure;
import org.apache.mahout.math.function.DoubleProcedure;
import org.apache.mahout.math.list.DoubleArrayList;
import org.apache.mahout.math.set.AbstractSet;

import org.junit.Assert;
import org.junit.Test;

public class OpenDoubleDoubleHashMapTest extends Assert {

  
  @Test
  public void testConstructors() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenDoubleDoubleHashMap(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenDoubleDoubleHashMap(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
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
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, map.get((double) 11), 0.0000001);
  }
  
  @Test
  public void testClone() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    OpenDoubleDoubleHashMap map2 = (OpenDoubleDoubleHashMap) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    assertTrue(map.containsKey((double) 11));
    assertFalse(map.containsKey((double) 12));
  }
  
  @Test
  public void testContainValue() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    assertTrue(map.containsValue((double) 22));
    assertFalse(map.containsValue((double) 23));
  }
  
  @Test
  public void testForEachKey() {
    final DoubleArrayList keys = new DoubleArrayList();
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    map.forEachKey(new DoubleProcedure() {
      
      @Override
      public boolean apply(double element) {
        keys.add(element);
        return true;
      }
    });
    
    double[] keysArray = keys.toArray(new double[keys.size()]);
    Arrays.sort(keysArray);
    
    assertArrayEquals(new double[] {11, 12, 14}, keysArray , (double)0.000001);
  }
  
  private static class Pair implements Comparable<Pair> {
    double k;
    double v;
    
    Pair(double k, double v) {
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
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    map.forEachPair(new DoubleDoubleProcedure() {
      
      @Override
      public boolean apply(double first, double second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((double) 11, pairs.get(0).k , (double)0.000001);
    assertEquals((double) 22, pairs.get(0).v , (double)0.000001);
    assertEquals((double) 12, pairs.get(1).k , (double)0.000001);
    assertEquals((double) 23, pairs.get(1).v , (double)0.000001);
    assertEquals((double) 14, pairs.get(2).k , (double)0.000001);
    assertEquals((double) 25, pairs.get(2).v , (double)0.000001);
    
    pairs.clear();
    map.forEachPair(new DoubleDoubleProcedure() {
      int count = 0;
      
      @Override
      public boolean apply(double first, double second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    assertEquals(22, map.get((double)11) , (double)0.000001);
    assertEquals(0, map.get((double)0) , (double)0.000001);
  }
  
  @Test
  public void testAdjustOrPutValue() {
   OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.adjustOrPutValue((double)11, (double)1, (double)3);
    assertEquals(25, map.get((double)11) , (double)0.000001);
    map.adjustOrPutValue((double)15, (double)1, (double)3);
    assertEquals(1, map.get((double)15) , (double)0.000001);
  }
  
  @Test
  public void testKeys() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 22);
    DoubleArrayList keys = new DoubleArrayList();
    map.keys(keys);
    keys.sort();
    assertEquals(11, keys.get(0) , (double)0.000001);
    assertEquals(12, keys.get(1) , (double)0.000001);
    DoubleArrayList k2 = map.keys();
    k2.sort();
    assertEquals(keys, k2);
  }
  
  @Test
  public void testPairsMatching() {
    DoubleArrayList keyList = new DoubleArrayList();
    DoubleArrayList valueList = new DoubleArrayList();
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    map.pairsMatching(new DoubleDoubleProcedure() {

      @Override
      public boolean apply(double first, double second) {
        return (first % 2) == 0;
      }},
        keyList, valueList);
    keyList.sort();
    valueList.sort();
    assertEquals(2, keyList.size());
    assertEquals(2, valueList.size());
    assertEquals(12, keyList.get(0) , (double)0.000001);
    assertEquals(14, keyList.get(1) , (double)0.000001);
    assertEquals(23, valueList.get(0) , (double)0.000001);
    assertEquals(25, valueList.get(1) , (double)0.000001);
  }
  
  @Test
  public void testValues() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    DoubleArrayList values = new DoubleArrayList(100);
    map.values(values);
    assertEquals(3, values.size());
    values.sort();
    assertEquals(22, values.get(0) , (double)0.000001);
    assertEquals(23, values.get(1) , (double)0.000001);
    assertEquals(25, values.get(2) , (double)0.000001);
  }
  
  // tests of the code in the abstract class
  
  @Test
  public void testCopy() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    OpenDoubleDoubleHashMap map2 = (OpenDoubleDoubleHashMap) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    OpenDoubleDoubleHashMap map2 = (OpenDoubleDoubleHashMap) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey((double) 11);
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 22);
    map.put((double) 12, (double) 23);
    map.put((double) 13, (double) 24);
    map.put((double) 14, (double) 25);
    map.removeKey((double) 13);
    DoubleArrayList keys = new DoubleArrayList();
    map.keysSortedByValue(keys);
    double[] keysArray = keys.toArray(new double[keys.size()]);
    assertArrayEquals(new double[] {11, 12, 14},
        keysArray , (double)0.000001);
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenDoubleDoubleHashMap map = new OpenDoubleDoubleHashMap();
    map.put((double) 11, (double) 100);
    map.put((double) 12, (double) 70);
    map.put((double) 13, (double) 30);
    map.put((double) 14, (double) 3);
    
    DoubleArrayList keys = new DoubleArrayList();
    DoubleArrayList values = new DoubleArrayList();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((double) 11, keys.get(0) , (double)0.000001);
    assertEquals((double) 100, values.get(0) , (double)0.000001);
    assertEquals((double) 12, keys.get(1) , (double)0.000001);
    assertEquals((double) 70, values.get(1) , (double)0.000001);
    assertEquals((double) 13, keys.get(2) , (double)0.000001);
    assertEquals((double) 30, values.get(2) , (double)0.000001);
    assertEquals((double) 14, keys.get(3) , (double)0.000001);
    assertEquals((double) 3, values.get(3) , (double)0.000001);
    keys.clear();
    values.clear();
    map.pairsSortedByValue(keys, values);
    assertEquals((double) 11, keys.get(3) , (double)0.000001);
    assertEquals((double) 100, values.get(3) , (double)0.000001);
    assertEquals((double) 12, keys.get(2) , (double)0.000001);
    assertEquals((double) 70, values.get(2) , (double)0.000001);
    assertEquals((double) 13, keys.get(1) , (double)0.000001);
    assertEquals((double) 30, values.get(1) , (double)0.000001);
    assertEquals((double) 14, keys.get(0) , (double)0.000001);
    assertEquals((double) 3, values.get(0) , (double)0.000001);
  }
 
 }
