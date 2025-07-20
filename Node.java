package com.example.enigma_breaker;


public class Node<T>
{
        private T value;
        private Node<T> next;

        public Node(T value)
        {
            this.value = value;
        }
        public Node(T value, Node<T> next)
        {
            this.value = value;
            this.next = next;
        }
        public T getValue()
        {
            return this.value;
        }
        public Node<T> getNext()
        {
            return this.next;
        }
        public boolean hasNext()
        {
            return (this.next != null);
        }
        public void setValue(T value)
        {
            this.value = value;
        }
        public void setNext(Node<T> next)
        {
            this.next = next;
        }
        public String ToString()
        {
            return this.value + "-->" + this.next;
        }
    }



