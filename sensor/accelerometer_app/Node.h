/*
 * Node.h
 *
 *  Created on: 25.10.2018
 *      Author: Eemil
 */

#ifndef NODE_H_
#define NODE_H_

namespace sll {

template <class T> class Node
{
public:
	Node()
{
		next = NULL;
		value = 0;
}
	Node(T initial): value(initial)
	{
		next = NULL;
	}
	virtual ~Node()
	{
		if (next != NULL)
		{
			delete(next);
		}
	}

	T value;

	void setNext(Node* next)
	{
		this->next = next;
	}
	Node* getNext() {return next;}
private:
	Node* next;
};


}



#endif /* NODE_H_ */
