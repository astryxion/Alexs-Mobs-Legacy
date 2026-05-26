package com.github.alexthe666.citadel.server.entity.datatracker;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WeakIdentityHashMap<K, V> implements Map<K, V> {
   private final ReferenceQueue<K> queue = new ReferenceQueue();
   private final Map<WeakIdentityHashMap<K, V>.IdentityWeakReference, V> delegate;

   public WeakIdentityHashMap(int expectedMaxSize, float loadFactor) {
      this.delegate = new HashMap(expectedMaxSize, loadFactor);
   }

   public WeakIdentityHashMap(int expectedMaxSize) {
      this.delegate = new HashMap(expectedMaxSize);
   }

   public WeakIdentityHashMap() {
      this.delegate = new HashMap();
   }

   public int size() {
      this.reap();
      return this.delegate.size();
   }

   public boolean isEmpty() {
      this.reap();
      return this.delegate.isEmpty();
   }

   public boolean containsKey(Object key) {
      this.reap();
      return this.delegate.containsKey(new IdentityWeakReference((K)key));
   }

   public boolean containsValue(Object value) {
      this.reap();
      return this.delegate.containsValue(value);
   }

   public V get(Object key) {
      this.reap();
      return (V)this.delegate.get(new IdentityWeakReference((K)key));
   }

   public V put(K key, V value) {
      return (V)this.delegate.put(new IdentityWeakReference(key), value);
   }

   public V remove(Object key) {
      this.reap();
      return (V)this.delegate.remove(new IdentityWeakReference((K)key));
   }

   public void putAll(Map<? extends K, ? extends V> m) {
      for(Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }

   }

   public void clear() {
      this.delegate.clear();
      this.reap();
   }

   public Set<K> keySet() {
      this.reap();
      return new AbstractSet<K>() {
         public int size() {
            WeakIdentityHashMap.this.reap();
            return WeakIdentityHashMap.this.delegate.size();
         }

         public Iterator<K> iterator() {
            WeakIdentityHashMap.this.reap();
            final Iterator<WeakIdentityHashMap<K, V>.IdentityWeakReference> iter = WeakIdentityHashMap.this.delegate.keySet().iterator();
            return new Iterator<K>() {
               public boolean hasNext() {
                  return iter.hasNext();
               }

               public K next() {
                  return (K)((IdentityWeakReference)iter.next()).get();
               }

               public void remove() {
                  iter.remove();
               }
            };
         }

         public void clear() {
            WeakIdentityHashMap.this.delegate.clear();
            WeakIdentityHashMap.this.reap();
         }
      };
   }

   public Collection<V> values() {
      return new AbstractCollection<V>() {
         public int size() {
            WeakIdentityHashMap.this.reap();
            return WeakIdentityHashMap.this.delegate.size();
         }

         public boolean contains(Object o) {
            WeakIdentityHashMap.this.reap();
            return WeakIdentityHashMap.this.containsValue(o);
         }

         public Iterator<V> iterator() {
            WeakIdentityHashMap.this.reap();
            return WeakIdentityHashMap.this.delegate.values().iterator();
         }

         public void clear() {
            WeakIdentityHashMap.this.delegate.clear();
            WeakIdentityHashMap.this.reap();
         }
      };
   }

   public Set<Map.Entry<K, V>> entrySet() {
      this.reap();
      return new AbstractSet<Map.Entry<K, V>>() {
         public int size() {
            WeakIdentityHashMap.this.reap();
            return WeakIdentityHashMap.this.delegate.size();
         }

         public Iterator<Map.Entry<K, V>> iterator() {
            WeakIdentityHashMap.this.reap();
            final Iterator<Map.Entry<WeakIdentityHashMap<K, V>.IdentityWeakReference, V>> iter = WeakIdentityHashMap.this.delegate.entrySet().iterator();
            return new Iterator<Map.Entry<K, V>>() {
               public boolean hasNext() {
                  return iter.hasNext();
               }

               public Map.Entry<K, V> next() {
                  final Map.Entry<WeakIdentityHashMap<K, V>.IdentityWeakReference, V> entry = (Map.Entry)iter.next();
                  return new Map.Entry<K, V>() {
                     public K getKey() {
                        return (K)((IdentityWeakReference)entry.getKey()).get();
                     }

                     public V getValue() {
                        return (V)entry.getValue();
                     }

                     public V setValue(V value) {
                        return (V)WeakIdentityHashMap.this.delegate.put(entry.getKey(), value);
                     }
                  };
               }

               public void remove() {
                  iter.remove();
               }
            };
         }

         public void clear() {
            WeakIdentityHashMap.this.delegate.clear();
            WeakIdentityHashMap.this.reap();
         }
      };
   }

   public String toString() {
      this.reap();
      return this.delegate.toString();
   }

   private synchronized void reap() {
      for(Reference<? extends K> zombie = this.queue.poll(); zombie != null; zombie = this.queue.poll()) {
         WeakIdentityHashMap<K, V>.IdentityWeakReference victim = (IdentityWeakReference)zombie;
         this.delegate.remove(victim);
      }

   }

   private class IdentityWeakReference extends WeakReference<K> {
      private final int hash;

      public IdentityWeakReference(K obj) {
         super(obj, WeakIdentityHashMap.this.queue);
         this.hash = System.identityHashCode(obj);
      }

      public int hashCode() {
         return this.hash;
      }

      public boolean equals(Object obj) {
         return this == obj || ((IdentityWeakReference)obj).get() == this.get();
      }

      public String toString() {
         return String.valueOf(this.get());
      }
   }
}
