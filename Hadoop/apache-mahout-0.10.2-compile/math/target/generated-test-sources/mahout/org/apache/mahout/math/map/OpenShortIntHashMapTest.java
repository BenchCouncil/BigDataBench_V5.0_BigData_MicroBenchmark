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

import org.apache.mahout.math.function.ShortIntProcedure;
import org.apache.mahout.math.function.ShortProcedure;
import org.apache.mahout.math.list.ShortArrayList;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.AbstractSet;

import org.junit.Assert;
import org.junit.Test;

public class OpenShortIntHashMapTest extends Assert {

  
  @Test
  public void testConstructors() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenShortIntHashMap(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenShortIntHashMap(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
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
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, map.get((short) 11), 0.0000001);
  }
  
  @Test
  public void testClone() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    OpenShortIntHashMap map2 = (OpenShortIntHashMap) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    assertTrue(map.containsKey((short) 11));
    assertFalse(map.containsKey((short) 12));
  }
  
  @Test
  public void testContainValue() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    assertTrue(map.containsValue((int) 22));
    assertFalse(map.containsValue((int) 23));
  }
  
  @Test
  public void testForEachKey() {
    final ShortArrayList keys = new ShortArrayList();
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    map.forEachKey(new ShortProcedure() {
      
      @Override
      public boolean apply(short element) {
        keys.add(element);
        return true;
      }
    });
    
    short[] keysArray = keys.toArray(new short[keys.size()]);
    Arrays.sort(keysArray);
    
    assertArrayEquals(new short[] {11, 12, 14}, keysArray );
  }
  
  private static class Pair implements Comparable<Pair> {
    short k;
    int v;
    
    Pair(short k, int v) {
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
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    map.forEachPair(new ShortIntProcedure() {
      
      @Override
      public boolean apply(short first, int second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((short) 11, pairs.get(0).k );
    assertEquals((int) 22, pairs.get(0).v );
    assertEquals((short) 12, pairs.get(1).k );
    assertEquals((int) 23, pairs.get(1).v );
    assertEquals((short) 14, pairs.get(2).k );
    assertEquals((int) 25, pairs.get(2).v );
    
    pairs.clear();
    map.forEachPair(new ShortIntProcedure() {
      int count = 0;
      
      @Override
      public boolean apply(short first, int second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    assertEquals(22, map.get((short)11) );
    assertEquals(0, map.get((short)0) );
  }
  
  @Test
  public void testAdjustOrPutValue() {
   OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.adjustOrPutValue((short)11, (int)1, (int)3);
    assertEquals(25, map.get((short)11) );
    map.adjustOrPutValue((short)15, (int)1, (int)3);
    assertEquals(1, map.get((short)15) );
  }
  
  @Test
  public void testKeys() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 22);
    ShortArrayList keys = new ShortArrayList();
    map.keys(keys);
    keys.sort();
    assertEquals(11, keys.get(0) );
    assertEquals(12, keys.get(1) );
    ShortArrayList k2 = map.keys();
    k2.sort();
    assertEquals(keys, k2);
  }
  
  @Test
  public void testPairsMatching() {
    ShortArrayList keyList = new ShortArrayList();
    IntArrayList valueList = new IntArrayList();
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    map.pairsMatching(new ShortIntProcedure() {

      @Override
      public boolean apply(short first, int second) {
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
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    IntArrayList values = new IntArrayList(100);
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
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    OpenShortIntHashMap map2 = (OpenShortIntHashMap) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    OpenShortIntHashMap map2 = (OpenShortIntHashMap) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey((short) 11);
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 22);
    map.put((short) 12, (int) 23);
    map.put((short) 13, (int) 24);
    map.put((short) 14, (int) 25);
    map.removeKey((short) 13);
    ShortArrayList keys = new ShortArrayList();
    map.keysSortedByValue(keys);
    short[] keysArray = keys.toArray(new short[keys.size()]);
    assertArrayEquals(new short[] {11, 12, 14},
        keysArray );
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenShortIntHashMap map = new OpenShortIntHashMap();
    map.put((short) 11, (int) 100);
    map.put((short) 12, (int) 70);
    map.put((short) 13, (int) 30);
    map.put((short) 14, (int) 3);
    
    ShortArrayList keys = new ShortArrayList();
    IntArrayList values = new IntArrayList();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((short) 11, keys.get(0) );
    assertEquals((int) 100, values.get(0) );
    assertEquals((short) 12, keys.get(1) );
    assertEquals((int) 70, values.get(1) );
    assertEquals((short) 13, keys.get(2) );
    assertEquals((int) 30, values.get(2) );
    assertEquals((short) 14, keys.get(3) );
    assertEquals((int) 3, values.get(3) );
    keys.clear();
    values.clear();
    map.pairsSortedByValue(keys, values);
    assertEquals((short) 11, keys.get(3) );
    assertEquals((int) 100, values.get(3) );
    assertEquals((short) 12, keys.get(2) );
    assertEquals((int) 70, values.get(2) );
    assertEquals((short) 13, keys.get(1) );
    assertEquals((int) 30, values.get(1) );
    assertEquals((short) 14, keys.get(0) );
    assertEquals((int) 3, values.get(0) );
  }
 
 }
