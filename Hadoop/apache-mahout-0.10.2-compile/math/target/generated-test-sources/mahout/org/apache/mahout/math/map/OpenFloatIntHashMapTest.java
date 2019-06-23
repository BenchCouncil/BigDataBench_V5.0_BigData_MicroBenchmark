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

import org.apache.mahout.math.function.FloatIntProcedure;
import org.apache.mahout.math.function.FloatProcedure;
import org.apache.mahout.math.list.FloatArrayList;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.AbstractSet;

import org.junit.Assert;
import org.junit.Test;

public class OpenFloatIntHashMapTest extends Assert {

  
  @Test
  public void testConstructors() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    int[] capacity = new int[1];
    double[] minLoadFactor = new double[1];
    double[] maxLoadFactor = new double[1];
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(AbstractSet.DEFAULT_CAPACITY, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    int prime = PrimeFinder.nextPrime(907);
    map = new OpenFloatIntHashMap(prime);
    
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(AbstractSet.DEFAULT_MAX_LOAD_FACTOR, maxLoadFactor[0], 0.001);
    assertEquals(AbstractSet.DEFAULT_MIN_LOAD_FACTOR, minLoadFactor[0], 0.001);
    
    map = new OpenFloatIntHashMap(prime, 0.4, 0.8);
    map.getInternalFactors(capacity, minLoadFactor, maxLoadFactor);
    assertEquals(prime, capacity[0]);
    assertEquals(0.4, minLoadFactor[0], 0.001);
    assertEquals(0.8, maxLoadFactor[0], 0.001);
  }
  
  @Test
  public void testEnsureCapacity() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
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
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    assertEquals(1, map.size());
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, map.get((float) 11), 0.0000001);
  }
  
  @Test
  public void testClone() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    OpenFloatIntHashMap map2 = (OpenFloatIntHashMap) map.clone();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testContainsKey() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    assertTrue(map.containsKey((float) 11));
    assertFalse(map.containsKey((float) 12));
  }
  
  @Test
  public void testContainValue() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    assertTrue(map.containsValue((int) 22));
    assertFalse(map.containsValue((int) 23));
  }
  
  @Test
  public void testForEachKey() {
    final FloatArrayList keys = new FloatArrayList();
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
    map.forEachKey(new FloatProcedure() {
      
      @Override
      public boolean apply(float element) {
        keys.add(element);
        return true;
      }
    });
    
    float[] keysArray = keys.toArray(new float[keys.size()]);
    Arrays.sort(keysArray);
    
    assertArrayEquals(new float[] {11, 12, 14}, keysArray , (float)0.000001);
  }
  
  private static class Pair implements Comparable<Pair> {
    float k;
    int v;
    
    Pair(float k, int v) {
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
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
    map.forEachPair(new FloatIntProcedure() {
      
      @Override
      public boolean apply(float first, int second) {
        pairs.add(new Pair(first, second));
        return true;
      }
    });
    
    Collections.sort(pairs);
    assertEquals(3, pairs.size());
    assertEquals((float) 11, pairs.get(0).k , (float)0.000001);
    assertEquals((int) 22, pairs.get(0).v );
    assertEquals((float) 12, pairs.get(1).k , (float)0.000001);
    assertEquals((int) 23, pairs.get(1).v );
    assertEquals((float) 14, pairs.get(2).k , (float)0.000001);
    assertEquals((int) 25, pairs.get(2).v );
    
    pairs.clear();
    map.forEachPair(new FloatIntProcedure() {
      int count = 0;
      
      @Override
      public boolean apply(float first, int second) {
        pairs.add(new Pair(first, second));
        count++;
        return count < 2;
      }
    });
    
    assertEquals(2, pairs.size());
  }
  
  @Test
  public void testGet() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    assertEquals(22, map.get((float)11) );
    assertEquals(0, map.get((float)0) );
  }
  
  @Test
  public void testAdjustOrPutValue() {
   OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.adjustOrPutValue((float)11, (int)1, (int)3);
    assertEquals(25, map.get((float)11) );
    map.adjustOrPutValue((float)15, (int)1, (int)3);
    assertEquals(1, map.get((float)15) );
  }
  
  @Test
  public void testKeys() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 22);
    FloatArrayList keys = new FloatArrayList();
    map.keys(keys);
    keys.sort();
    assertEquals(11, keys.get(0) , (float)0.000001);
    assertEquals(12, keys.get(1) , (float)0.000001);
    FloatArrayList k2 = map.keys();
    k2.sort();
    assertEquals(keys, k2);
  }
  
  @Test
  public void testPairsMatching() {
    FloatArrayList keyList = new FloatArrayList();
    IntArrayList valueList = new IntArrayList();
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
    map.pairsMatching(new FloatIntProcedure() {

      @Override
      public boolean apply(float first, int second) {
        return (first % 2) == 0;
      }},
        keyList, valueList);
    keyList.sort();
    valueList.sort();
    assertEquals(2, keyList.size());
    assertEquals(2, valueList.size());
    assertEquals(12, keyList.get(0) , (float)0.000001);
    assertEquals(14, keyList.get(1) , (float)0.000001);
    assertEquals(23, valueList.get(0) );
    assertEquals(25, valueList.get(1) );
  }
  
  @Test
  public void testValues() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
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
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    OpenFloatIntHashMap map2 = (OpenFloatIntHashMap) map.copy();
    map.clear();
    assertEquals(1, map2.size());
  }
  
  @Test
  public void testEquals() {
    // since there are no other subclasses of 
    // Abstractxxx available, we have to just test the
    // obvious.
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
    OpenFloatIntHashMap map2 = (OpenFloatIntHashMap) map.copy();
    assertEquals(map, map2);
    assertTrue(map2.equals(map));
    assertFalse("Hello Sailor".equals(map));
    assertFalse(map.equals("hello sailor"));
    map2.removeKey((float) 11);
    assertFalse(map.equals(map2));
    assertFalse(map2.equals(map));
  }
  
  // keys() tested in testKeys
  
  @Test
  public void testKeysSortedByValue() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 22);
    map.put((float) 12, (int) 23);
    map.put((float) 13, (int) 24);
    map.put((float) 14, (int) 25);
    map.removeKey((float) 13);
    FloatArrayList keys = new FloatArrayList();
    map.keysSortedByValue(keys);
    float[] keysArray = keys.toArray(new float[keys.size()]);
    assertArrayEquals(new float[] {11, 12, 14},
        keysArray , (float)0.000001);
  }
  
  @Test
  public void testPairsSortedByKey() {
    OpenFloatIntHashMap map = new OpenFloatIntHashMap();
    map.put((float) 11, (int) 100);
    map.put((float) 12, (int) 70);
    map.put((float) 13, (int) 30);
    map.put((float) 14, (int) 3);
    
    FloatArrayList keys = new FloatArrayList();
    IntArrayList values = new IntArrayList();
    map.pairsSortedByKey(keys, values);
    
    assertEquals(4, keys.size());
    assertEquals(4, values.size());
    assertEquals((float) 11, keys.get(0) , (float)0.000001);
    assertEquals((int) 100, values.get(0) );
    assertEquals((float) 12, keys.get(1) , (float)0.000001);
    assertEquals((int) 70, values.get(1) );
    assertEquals((float) 13, keys.get(2) , (float)0.000001);
    assertEquals((int) 30, values.get(2) );
    assertEquals((float) 14, keys.get(3) , (float)0.000001);
    assertEquals((int) 3, values.get(3) );
    keys.clear();
    values.clear();
    map.pairsSortedByValue(keys, values);
    assertEquals((float) 11, keys.get(3) , (float)0.000001);
    assertEquals((int) 100, values.get(3) );
    assertEquals((float) 12, keys.get(2) , (float)0.000001);
    assertEquals((int) 70, values.get(2) );
    assertEquals((float) 13, keys.get(1) , (float)0.000001);
    assertEquals((int) 30, values.get(1) );
    assertEquals((float) 14, keys.get(0) , (float)0.000001);
    assertEquals((int) 3, values.get(0) );
  }
 
 }
