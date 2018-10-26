/*
 * SLinkedList.h
 *
 *  Created on: 25.10.2018
 *      Author: Eemil
 */

#ifndef SLINKEDLIST_H_
#define SLINKEDLIST_H_
#include "Node.h"
namespace sll {
/*
 * Meant to keep track of the average value in the list when new objects are pushed in.
 */
template<class T> class SLinkedList {
public:
	SLinkedList(int size, T zeroValue): average(zeroValue), size(size) {
		if (size <= 0) {
			first = new Node<T>(zeroValue);
		} else {
			first = new Node<T>(zeroValue);
			Node<T>* cur = first;
			for (int i = 1; i < size; i++)
			{
				cur->setNext(new Node<T>(zeroValue));
				cur = cur->getNext();
			}
		}
	}

	virtual ~SLinkedList() {
		delete(first);
	}

	T average;
	int size;

	void push(T input)
	{
		average = (average*size-getLast() + input)/size;
		Node<T>* tmp = first;
		first = new Node<T>(input);
		first->setNext(tmp);
	}
	void fill(T input)
	{
		Node<T>* cursor = first;
		while (cursor != NULL)
		{
			cursor->value = input;
			cursor = cursor->getNext();
		}
		average = input;
	}

private:
	Node<T>* first;
	T getLast() //Also removes the last one
	{
		Node<T>* res = first;
		while (res->getNext() != NULL)
		{
			if (res->getNext()->getNext() == NULL)
			{
				T tmp = (res->getNext())->value;
				delete(res->getNext());
				res->setNext(NULL);
				return tmp;
			}
			else res = res->getNext();
		}
		T val = res->value;
		delete(res);
		return val;
	}
};

} /* namespace sll */

#endif /* SLINKEDLIST_H_ */
