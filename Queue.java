package com.example.enigma_breaker;

public class Queue<T>
{
	 private Node<T> first;
     private Node<T> last;

     public Queue()
     {
         this.first = null; //1 2 3 -> 1
         this.last = null; // 1 2 3 -> 3
     }

     public boolean isEmpty()
     {
         return this.first == null;

     }

     public void insert(T x)
     {
         Node<T> k = new Node<T>(x);
        
         if (this.first == null)
         {
             this.first = k;
             this.last = this.first;
         }
         else
         {
             this.last.setNext(k);
             this.last = this.last.getNext();

         }
     }

     public T remove()
     {
         T x = this.first.getValue();
         this.first = this.first.getNext();
         return x;
     }

     public T head()
     {
         return this.first.getValue();
     }
     public String toString()
     {
    	 if(this.isEmpty()) return "[]";
    	 this.insert(null);
    	 String s="[";
    	 T temp=this.remove();
    	 while(temp!=null){
    		 s=s+temp+",";
    		 this.insert(temp);
    		 temp=this.remove();}
    	 s=s.substring(0, s.length()-1)+ "]";
    	 return s;
     }
 }

